package com.elearning.platform.services;

import com.elearning.platform.config.VlearningProperties;
import com.elearning.platform.entity.Inscripcion;
import com.elearning.platform.entity.Usuario;
import com.elearning.platform.enums.EstadoInscripcion;
import com.elearning.platform.enums.EstadoUsuario;
import com.elearning.platform.enums.RolUsuario;
import com.elearning.platform.enums.EstadoIntento;
import com.elearning.platform.repository.EntregaTareaRepository;
import com.elearning.platform.repository.EvaluacionRepository;
import com.elearning.platform.repository.InscripcionRepository;
import com.elearning.platform.repository.IntentoEvaluacionRepository;
import com.elearning.platform.repository.TareaRepository;
import com.elearning.platform.repository.NotificacionRepository;
import com.elearning.platform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF-011: recordatorios diarios. Avisa de evaluaciones y tareas con fecha límite próxima que el estudiante aún
 * no ha presentado, de cursos sin completar y de inactividad prolongada. Respeta las preferencias de cada
 * estudiante (si deshabilitó las notificaciones, el envío se omite y solo se registra en el log del servidor)
 * y no repite el mismo aviso el mismo día.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioJob {

    static final String TITULO_PENDIENTES = "Tienes actividades pendientes";
    static final String TITULO_INACTIVIDAD = "Te esperamos para seguir aprendiendo";
    static final String PREFIJO_FECHA_LIMITE = "Fecha límite próxima: ";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final UsuarioRepository usuarios;
    private final InscripcionRepository inscripciones;
    private final NotificacionRepository notificaciones;
    private final NotificacionService notificador;
    private final VlearningProperties props;
    private final EvaluacionRepository evaluaciones;
    private final TareaRepository tareas;
    private final IntentoEvaluacionRepository intentos;
    private final EntregaTareaRepository entregas;

    /** Días de anticipación con que se avisa una fecha límite (RF-011). */
    @Value("${vlearning.notificaciones.dias-anticipacion-fecha-limite:2}")
    private int diasAnticipacion = 2;

    /** Actividad con fecha límite: evaluación o tarea. */
    record Vencimiento(boolean esEvaluacion, Long id, String titulo, LocalDateTime fechaLimite, Long cursoId, String curso) {}

    @Scheduled(cron = "${vlearning.notificaciones.cron}")
    public void ejecutar() {
        int enviados = 0;
        int omitidos = 0;
        Map<Long, List<Vencimiento>> vencimientos = vencimientosPorCurso();
        for (Usuario u : usuarios.findByRolAndEstado(RolUsuario.ESTUDIANTE, EstadoUsuario.ACTIVO)) {
            if (!u.isNotificacionesHabilitadas()) {
                omitidos++;
                log.info("Recordatorio omitido: el usuario {} deshabilitó las notificaciones.", u.getId());
                continue;
            }
            try {
                enviados += recordar(u, vencimientos);
            } catch (RuntimeException e) {
                log.warn("No se pudo generar el recordatorio para el usuario {}: {}", u.getId(), e.getMessage());
            }
        }
        log.info("Recordatorios diarios generados: {} · omitidos por preferencia: {}", enviados, omitidos);
    }

    /** Evaluaciones y tareas cuya fecha límite está entre ahora y los próximos días de anticipación. */
    Map<Long, List<Vencimiento>> vencimientosPorCurso() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hasta = ahora.plusDays(Math.max(1, diasAnticipacion));
        Map<Long, List<Vencimiento>> res = new HashMap<>();
        evaluaciones.conFechaLimiteEntre(ahora, hasta).forEach(e -> res
                .computeIfAbsent(e.getModulo().getCurso().getId(), k -> new ArrayList<>())
                .add(new Vencimiento(true, e.getId(), e.getTitulo(), e.getFechaLimite(),
                        e.getModulo().getCurso().getId(), e.getModulo().getCurso().getTitulo())));
        tareas.conFechaLimiteEntre(ahora, hasta).forEach(t -> res
                .computeIfAbsent(t.getModulo().getCurso().getId(), k -> new ArrayList<>())
                .add(new Vencimiento(false, t.getId(), t.getTitulo(), t.getFechaLimite(),
                        t.getModulo().getCurso().getId(), t.getModulo().getCurso().getTitulo())));
        return res;
    }

    int recordar(Usuario u) {
        return recordar(u, vencimientosPorCurso());
    }

    int recordar(Usuario u, Map<Long, List<Vencimiento>> vencimientos) {
        int n = 0;
        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        List<Inscripcion> activas = inscripciones.delEstudiante(u.getId()).stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .toList();

        // 1) Fechas límite próximas de actividades que el estudiante aún no presentó.
        for (Inscripcion i : activas) {
            for (Vencimiento v : vencimientos.getOrDefault(i.getCurso().getId(), List.of())) {
                boolean presentada = v.esEvaluacion()
                        ? intentos.existsByEvaluacionIdAndEstudianteIdAndEstado(v.id(), u.getId(), EstadoIntento.FINALIZADO)
                        : entregas.existsByTareaIdAndEstudianteId(v.id(), u.getId());
                if (presentada) continue;
                String titulo = PREFIJO_FECHA_LIMITE + recortar(v.titulo(), 120);
                if (notificaciones.contarPorTituloDesde(u.getId(), titulo, hoy) > 0) continue;
                notificador.notificar(u, titulo,
                        (v.esEvaluacion() ? "La evaluación «" : "La tarea «") + v.titulo() + "» del curso «" + v.curso()
                                + "» vence el " + v.fechaLimite().format(FECHA) + ". Aún no la has presentado.",
                        false, v.esEvaluacion() ? "/evaluaciones" : "/cursos/" + v.cursoId());
                n++;
            }
        }

        // 2) Cursos sin completar.
        List<Inscripcion> pendientes = activas.stream()
                .filter(i -> i.getPorcentajeCompletado().compareTo(new BigDecimal("100")) < 0)
                .toList();
        if (!pendientes.isEmpty() && notificaciones.contarPorTituloDesde(u.getId(), TITULO_PENDIENTES, hoy) == 0) {
            String cursos = String.join(", ", pendientes.stream().limit(3).map(i -> "«" + i.getCurso().getTitulo() + "»").toList());
            notificador.notificar(u, TITULO_PENDIENTES,
                    "Tienes " + pendientes.size() + " curso(s) por completar: " + cursos + ".", false, "/cursos");
            n++;
        }

        // 3) Inactividad prolongada.
        LocalDateTime limite = LocalDateTime.now().minusDays(props.notificaciones().diasInactividad());
        boolean inactivo = u.getUltimoAcceso() != null && u.getUltimoAcceso().isBefore(limite);
        if (inactivo && !pendientes.isEmpty()
                && notificaciones.contarPorTituloDesde(u.getId(), TITULO_INACTIVIDAD, hoy) == 0) {
            notificador.notificar(u, TITULO_INACTIVIDAD,
                    "Hace más de " + props.notificaciones().diasInactividad() + " días que no entras. Retoma donde lo dejaste.", false, "/cursos");
            n++;
        }
        return n;
    }

    private static String recortar(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
