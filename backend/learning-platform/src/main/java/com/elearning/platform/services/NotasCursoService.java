package com.elearning.platform.services;

import com.elearning.platform.dto.NotasDtos.*;
import com.elearning.platform.entity.*;
import com.elearning.platform.enums.*;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.*;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.util.CorreccionEvaluacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * RF-029 Consultar notas del curso (instructor o administrador).
 * Reglas:
 * - Nota de una evaluación: el mejor intento finalizado, en porcentaje del puntaje máximo.
 * - Nota de una tarea: la calificación del instructor, en porcentaje del puntaje máximo.
 * - Actividad no presentada: cuenta 0 %. Tarea entregada sin calificar: no cuenta hasta que se califique.
 * - Evaluación sin alternativa accesible para un estudiante con perfil motor: excluida (RF-021).
 * - Nota final: promedio de las actividades que cuentan. Aprueba con nota final ≥ 60 % (umbral «aceptable»).
 */
@Service
@RequiredArgsConstructor
public class NotasCursoService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final CursoRepository cursos;
    private final ModuloRepository modulos;
    private final EvaluacionRepository evaluaciones;
    private final TareaRepository tareas;
    private final InscripcionRepository inscripciones;
    private final IntentoEvaluacionRepository intentos;
    private final EntregaTareaRepository entregas;
    private final AccesibilidadService accesibilidad;

    @Transactional(readOnly = true)
    public NotasCurso notas(UsuarioPrincipal u, Long cursoId) {
        Curso curso = cursos.findById(cursoId).orElseThrow(() -> ApiException.noEncontrado("No encontramos ese curso."));
        if (u.rol() == RolUsuario.INSTRUCTOR && !curso.getInstructor().getId().equals(u.id())) {
            throw ApiException.prohibido("ACCESO_DENEGADO", "Solo puedes ver las notas de tus cursos.");
        }
        if (u.rol() == RolUsuario.ESTUDIANTE) {
            throw ApiException.prohibido("ACCESO_DENEGADO", "No tienes permiso para ver las notas del curso.");
        }

        List<Modulo> mods = modulos.findByCursoIdOrderByOrden(cursoId);
        Map<Long, String> tituloModulo = new HashMap<>();
        mods.forEach(m -> tituloModulo.put(m.getId(), m.getTitulo()));
        List<Long> moduloIds = mods.stream().map(Modulo::getId).toList();

        List<Evaluacion> evals = moduloIds.isEmpty() ? List.of() : evaluaciones.findByModuloIdInOrderByIdAsc(moduloIds);
        List<Tarea> tars = moduloIds.isEmpty() ? List.of() : tareas.deLosModulos(moduloIds);

        List<ActividadNota> actividades = new ArrayList<>();
        evals.forEach(e -> actividades.add(new ActividadNota("E-" + e.getId(), "EVALUACION", e.getId(), e.getTitulo(),
                tituloModulo.get(e.getModulo().getId()))));
        tars.forEach(t -> actividades.add(new ActividadNota("T-" + t.getId(), "TAREA", t.getId(), t.getTitulo(),
                tituloModulo.get(t.getModulo().getId()))));

        // Mejor intento por (estudiante, evaluación), en porcentaje.
        Map<String, BigDecimal> notaEval = new HashMap<>();
        for (Object[] f : intentos.mejoresPorCurso(cursoId)) {
            BigDecimal mejor = (BigDecimal) f[2];
            BigDecimal max = (BigDecimal) f[3];
            if (mejor == null || max == null || max.signum() == 0) continue;
            notaEval.put(f[0] + ":" + f[1], porcentaje(mejor, max));
        }
        // Entregas por (estudiante, tarea).
        Map<String, EntregaTarea> entregaDe = new HashMap<>();
        if (!tars.isEmpty()) {
            for (EntregaTarea e : entregas.findByTareaIdIn(tars.stream().map(Tarea::getId).toList())) {
                entregaDe.put(e.getEstudiante().getId() + ":" + e.getTarea().getId(), e);
            }
        }

        BigDecimal umbral = CorreccionEvaluacion.UMBRAL_ACEPTABLE;
        List<NotaEstudiante> filas = new ArrayList<>();
        BigDecimal sumaFinales = BigDecimal.ZERO;
        int conNota = 0, aprueban = 0, noAprueban = 0;

        List<Inscripcion> inscritos = new ArrayList<>(inscripciones.findByCursoIdAndEstadoIn(cursoId,
                List.of(EstadoInscripcion.ACTIVA, EstadoInscripcion.FINALIZADA)));
        inscritos.sort(Comparator.comparing((Inscripcion i) -> i.getEstudiante().getNombre(), String.CASE_INSENSITIVE_ORDER));

        for (Inscripcion i : inscritos) {
            Estudiante est = i.getEstudiante();
            boolean motor = accesibilidad.categoriasDe(est.getId()).contains(CategoriaAccesibilidad.MOTORA);
            List<CeldaNota> celdas = new ArrayList<>();
            BigDecimal suma = BigDecimal.ZERO;
            int cuentan = 0, pendientes = 0, porCalificar = 0;

            for (Evaluacion e : evals) {
                String clave = "E-" + e.getId();
                if (motor && !e.isAlternativaAccesible()) {
                    celdas.add(new CeldaNota(clave, null, "EXCLUIDA"));
                    continue;
                }
                BigDecimal p = notaEval.get(est.getId() + ":" + e.getId());
                if (p == null) {
                    celdas.add(new CeldaNota(clave, BigDecimal.ZERO.setScale(1), "PENDIENTE"));
                    pendientes++;
                    cuentan++;
                } else {
                    celdas.add(new CeldaNota(clave, p, "CALIFICADA"));
                    suma = suma.add(p);
                    cuentan++;
                }
            }
            for (Tarea t : tars) {
                String clave = "T-" + t.getId();
                EntregaTarea ent = entregaDe.get(est.getId() + ":" + t.getId());
                if (ent == null) {
                    celdas.add(new CeldaNota(clave, BigDecimal.ZERO.setScale(1), "PENDIENTE"));
                    pendientes++;
                    cuentan++;
                } else if (ent.getEstado() != EstadoEntrega.CALIFICADA || ent.getPuntaje() == null
                        || t.getPuntajeMaximo() == null || t.getPuntajeMaximo().signum() == 0) {
                    celdas.add(new CeldaNota(clave, null, "POR_CALIFICAR"));
                    porCalificar++;
                } else {
                    BigDecimal p = porcentaje(ent.getPuntaje(), t.getPuntajeMaximo());
                    celdas.add(new CeldaNota(clave, p, "CALIFICADA"));
                    suma = suma.add(p);
                    cuentan++;
                }
            }

            BigDecimal notaFinal = null;
            String estado = "SIN_NOTAS";
            if (cuentan > 0) {
                notaFinal = suma.divide(BigDecimal.valueOf(cuentan), 1, RoundingMode.HALF_UP);
                estado = notaFinal.compareTo(umbral) >= 0 ? "APRUEBA" : "NO_APRUEBA";
                sumaFinales = sumaFinales.add(notaFinal);
                conNota++;
                if ("APRUEBA".equals(estado)) aprueban++; else noAprueban++;
            }
            filas.add(new NotaEstudiante(est.getId(), est.getNombre(), est.getCodigoEstudiante(),
                    i.getPorcentajeCompletado().setScale(1, RoundingMode.HALF_UP), celdas, notaFinal, estado,
                    pendientes, porCalificar));
        }

        BigDecimal promedio = conNota == 0 ? null : sumaFinales.divide(BigDecimal.valueOf(conNota), 1, RoundingMode.HALF_UP);
        return new NotasCurso(cursoId, curso.getTitulo(), umbral, actividades, filas, promedio, aprueban, noAprueban);
    }

    private static BigDecimal porcentaje(BigDecimal obtenido, BigDecimal maximo) {
        return obtenido.multiply(CIEN).divide(maximo, 1, RoundingMode.HALF_UP);
    }
}
