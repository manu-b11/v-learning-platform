package com.elearning.platform.util;

import com.elearning.platform.dto.DashboardDtos.Calificacion;
import com.elearning.platform.dto.DashboardDtos.CursoProgreso;
import com.elearning.platform.enums.Clasificacion;
import com.elearning.platform.enums.EstadoInscripcion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RF-033 (HU-033): resumen en texto de los gráficos y PDF etiquetado con la tabla. */
class ResumenProgresoTest {

    private static CursoProgreso curso(String titulo, int pct) {
        return new CursoProgreso(1L, titulo, null, BigDecimal.valueOf(pct), EstadoInscripcion.ACTIVA, 1, 3, 2, 600);
    }

    private static Calificacion nota(String titulo, int pct) {
        return new Calificacion(1L, titulo, "Curso", BigDecimal.valueOf(pct), 1, Clasificacion.ACEPTABLE, "EVALUACION");
    }

    @Test
    void sinDatosSeDiceEnTextoEnVezDeUnGraficoVacio() {
        assertTrue(ResumenProgreso.avance(List.of()).startsWith("Todavía no tienes cursos"));
        assertTrue(ResumenProgreso.calificaciones(List.of()).startsWith("Todavía no tienes"));
    }

    @Test
    void conUnCursoDaElValorActual() {
        assertEquals("Tienes 1 curso: «Scrum» con 40 % de avance.", ResumenProgreso.avance(List.of(curso("Scrum", 40))));
    }

    @Test
    void conVariosCursosDaPromedioYExtremos() {
        String r = ResumenProgreso.avance(List.of(curso("Scrum", 100), curso("UX", 20), curso("React", 60)));
        assertTrue(r.contains("3 cursos"));
        assertTrue(r.contains("promedio de 60 %"));
        assertTrue(r.contains("1 terminado(s)"));
        assertTrue(r.contains("«Scrum» (100 %)"));
        assertTrue(r.contains("«UX» (20 %)"));
    }

    @Test
    void lasCalificacionesDanPromedioYMejorResultado() {
        String r = ResumenProgreso.calificaciones(List.of(nota("Quiz 1", 80), nota("Quiz 2", 90)));
        assertTrue(r.contains("2 actividad(es)"));
        assertTrue(r.contains("promedio de 85 %"));
        assertTrue(r.contains("«Quiz 2» con 90 %"));
    }

    @Test
    void elPdfExportadoEstaEtiquetadoEIncluyeLaTabla() {
        TablaInforme t = new TablaInforme("Mi progreso", List.of("Resumen del avance: 40 %"),
                List.of("Curso", "Avance"), List.of(List.of("Scrum", "40 %")), List.of());
        String pdf = new String(ExportadorInforme.aPdf(t), StandardCharsets.ISO_8859_1);

        assertTrue(pdf.contains("/StructTreeRoot"), "tiene árbol de estructura");
        assertTrue(pdf.contains("/Marked true"), "está marcado como etiquetado");
        assertTrue(pdf.contains("/Lang(es-CO)") || pdf.contains("/Lang (es-CO)"), "declara el idioma");
        assertTrue(pdf.contains("/DisplayDocTitle true"), "muestra el título del documento");
    }
}
