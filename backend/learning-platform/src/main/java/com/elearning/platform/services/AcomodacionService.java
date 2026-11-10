package com.elearning.platform.services;

import com.elearning.platform.dto.AcomodacionDtos.AcomodacionRequest;
import com.elearning.platform.dto.AcomodacionDtos.AcomodacionVista;
import com.elearning.platform.entity.Acomodacion;
import com.elearning.platform.entity.Curso;
import com.elearning.platform.entity.Inscripcion;
import com.elearning.platform.enums.EstadoInscripcion;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.RolUsuario;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.AcomodacionRepository;
import com.elearning.platform.repository.CursoRepository;
import com.elearning.platform.repository.InscripcionRepository;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.util.TiempoEvaluacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * RF-041: acomodaciones de evaluación por estudiante. Las asigna el instructor del curso o un
 * administrador, quedan en auditoría y se aplican en el servidor a todas las evaluaciones del curso.
 */
@Service
@RequiredArgsConstructor
public class AcomodacionService {

    /** RF-041: multiplicadores permitidos (1,5x y 2x). */
    static final Set<BigDecimal> MULTIPLICADORES = Set.of(new BigDecimal("1.50"), new BigDecimal("2.00"));

    private final AcomodacionRepository acomodaciones;
    private final InscripcionRepository inscripciones;
    private final CursoRepository cursos;
    private final AccesibilidadService accesibilidad;
    private final NotificacionService notificaciones;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public List<AcomodacionVista> listar(Long cursoId, UsuarioPrincipal actor) {
        cursoAccesible(cursoId, actor);
        LocalDate hoy = LocalDate.now();
        return acomodaciones.activasDelCurso(cursoId).stream().map(a -> vista(a, hoy)).toList();
    }

    /** Crea la acomodación y desactiva la anterior del mismo estudiante (queda como historial). */
    @Transactional
    public AcomodacionVista asignar(Long cursoId, AcomodacionRequest req, UsuarioPrincipal actor, String ip) {
        cursoAccesible(cursoId, actor);
        Inscripcion insc = inscripciones.findByEstudianteIdAndCursoId(req.estudianteId(), cursoId)
                .filter(i -> i.getEstado() != EstadoInscripcion.CANCELADA)
                .orElseThrow(() -> ApiException.solicitudInvalida("ESTUDIANTE_NO_INSCRITO",
                        "El estudiante no está inscrito en este curso."));
        BigDecimal multiplicador = normalizarMultiplicador(req.multiplicadorTiempo());
        boolean ilimitado = Boolean.TRUE.equals(req.tiempoIlimitado());
        int intentos = req.intentosExtra() == null ? 0 : req.intentosExtra();
        int ampliacion = req.minutosAmpliacion() == null ? 0 : req.minutosAmpliacion();
        if (multiplicador == null && !ilimitado && intentos == 0 && ampliacion == 0 && req.fechaLimiteEspecial() == null) {
            throw ApiException.solicitudInvalida("ACOMODACION_VACIA",
                    "Elige al menos una acomodación: más tiempo, tiempo ilimitado, intentos extra, ampliación o una fecha especial.");
        }
        if (req.vigenteHasta() != null && req.vigenteHasta().isBefore(LocalDate.now())) {
            throw ApiException.solicitudInvalida("VIGENCIA_PASADA", "La fecha de vigencia no puede estar en el pasado.");
        }

        acomodaciones.findFirstByInscripcionIdAndActivaTrue(insc.getId()).ifPresent(previa -> {
            previa.setActiva(false);
            acomodaciones.saveAndFlush(previa); // libera el índice único antes de insertar la nueva
        });
        Acomodacion a = new Acomodacion();
        a.setInscripcion(insc);
        a.setMultiplicadorTiempo(ilimitado ? null : multiplicador);
        a.setTiempoIlimitado(ilimitado);
        a.setIntentosExtra(intentos);
        a.setFechaLimiteEspecial(req.fechaLimiteEspecial());
        a.setMinutosAmpliacion(ampliacion);
        a.setVigenteHasta(req.vigenteHasta());
        a.setCreadaPor(actor.id());
        a = acomodaciones.save(a);
        auditoria.registrar(actor.id(), "ACOMODACION_ASIGNADA",
                "cursos/" + cursoId + "/estudiantes/" + req.estudianteId(), ResultadoAuditoria.PERMITIDO, ip);
        return vista(a, LocalDate.now());
    }

    @Transactional
    public void quitar(Long cursoId, Long acomodacionId, UsuarioPrincipal actor, String ip) {
        cursoAccesible(cursoId, actor);
        Acomodacion a = acomodaciones.findById(acomodacionId)
                .filter(x -> x.getInscripcion().getCurso().getId().equals(cursoId) && x.isActiva())
                .orElseThrow(() -> ApiException.noEncontrado("No encontramos esa acomodación en el curso."));
        a.setActiva(false);
        acomodaciones.save(a);
        auditoria.registrar(actor.id(), "ACOMODACION_RETIRADA", "acomodaciones/" + acomodacionId,
                ResultadoAuditoria.PERMITIDO, ip);
    }

    /**
     * RF-041: condiciones de tiempo del estudiante en el curso (perfil + acomodación vigente).
     * Si la acomodación está vencida no se aplica y se avisa al instructor una vez al día.
     */
    @Transactional
    public TiempoEvaluacion tiempoPara(Long estudianteId, Curso curso) {
        int extraPerfil = accesibilidad.configuracionDe(estudianteId).tiempoAdicional();
        Optional<Acomodacion> activa = acomodaciones.activaDe(estudianteId, curso.getId());
        if (activa.isEmpty()) return TiempoEvaluacion.soloPerfil(extraPerfil);
        Acomodacion a = activa.get();
        if (a.vencida(LocalDate.now())) {
            notificaciones.notificarUnaVezAlDia(curso.getInstructor().getId(),
                    "Acomodación vencida en «" + curso.getTitulo() + "»",
                    "Una acomodación de evaluación venció y ya no se aplica. Revísala y renuévala si el estudiante la sigue necesitando.",
                    "/cursos/" + curso.getId() + "/notas");
            return TiempoEvaluacion.soloPerfil(extraPerfil);
        }
        return new TiempoEvaluacion(extraPerfil, a.getMultiplicadorTiempo(), a.isTiempoIlimitado(),
                a.getFechaLimiteEspecial(), a.getMinutosAmpliacion() == null ? 0 : a.getMinutosAmpliacion());
    }

    /** RF-041: solo 1,5x o 2x; null si no se pidió multiplicador. */
    static BigDecimal normalizarMultiplicador(BigDecimal m) {
        if (m == null) return null;
        BigDecimal n = m.setScale(2, java.math.RoundingMode.HALF_UP);
        if (!MULTIPLICADORES.contains(n)) {
            throw ApiException.solicitudInvalida("MULTIPLICADOR_NO_PERMITIDO", "El multiplicador de tiempo debe ser 1,5x o 2x.");
        }
        return n;
    }

    private Curso cursoAccesible(Long cursoId, UsuarioPrincipal actor) {
        Curso curso = cursos.findById(cursoId).orElseThrow(() -> ApiException.noEncontrado("No encontramos ese curso."));
        if (actor.rol() == RolUsuario.ADMINISTRADOR) return curso;
        if (actor.rol() == RolUsuario.INSTRUCTOR && curso.getInstructor().getId().equals(actor.id())) return curso;
        throw ApiException.prohibido("ACCESO_DENEGADO", "Solo el instructor del curso o un administrador gestionan sus acomodaciones.");
    }

    private static AcomodacionVista vista(Acomodacion a, LocalDate hoy) {
        Inscripcion i = a.getInscripcion();
        return new AcomodacionVista(a.getId(), i.getEstudiante().getId(), i.getEstudiante().getNombre(),
                a.getMultiplicadorTiempo(), a.isTiempoIlimitado(), a.getIntentosExtra() == null ? 0 : a.getIntentosExtra(),
                a.getFechaLimiteEspecial(), a.getMinutosAmpliacion() == null ? 0 : a.getMinutosAmpliacion(),
                a.getVigenteHasta(), a.vencida(hoy), a.getCreadaPor(), a.getFechaCreacion());
    }
}
