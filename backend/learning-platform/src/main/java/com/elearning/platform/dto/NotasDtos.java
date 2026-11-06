package com.elearning.platform.dto;

import java.math.BigDecimal;
import java.util.List;

/** DTOs de la sección «Notas del curso» del instructor (RF-029). */
public final class NotasDtos {

    private NotasDtos() {}

    /** Actividad calificable del curso: evaluación o tarea. clave = "E-{id}" o "T-{id}". */
    public record ActividadNota(String clave, String tipo, Long id, String titulo, String modulo) {}

    /**
     * Nota de un estudiante en una actividad. estado: CALIFICADA, PENDIENTE (no presentada, cuenta 0 %),
     * POR_CALIFICAR (tarea entregada sin calificar, no cuenta) o EXCLUIDA (sin alternativa accesible, RF-021).
     */
    public record CeldaNota(String clave, BigDecimal porcentaje, String estado) {}

    /** estado: APRUEBA, NO_APRUEBA o SIN_NOTAS. */
    public record NotaEstudiante(Long estudianteId, String nombre, String codigo, BigDecimal avance,
                                 List<CeldaNota> notas, BigDecimal notaFinal, String estado,
                                 int pendientes, int porCalificar) {}

    public record NotasCurso(Long cursoId, String curso, BigDecimal umbralAprobacion,
                             List<ActividadNota> actividades, List<NotaEstudiante> estudiantes,
                             BigDecimal promedioGeneral, int aprueban, int noAprueban) {}
}
