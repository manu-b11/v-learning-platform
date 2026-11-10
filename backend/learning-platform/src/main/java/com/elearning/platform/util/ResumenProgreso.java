package com.elearning.platform.util;

import com.elearning.platform.dto.DashboardDtos.Calificacion;
import com.elearning.platform.dto.DashboardDtos.CursoProgreso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * RF-033: alternativa textual de los gráficos del dashboard de progreso. Una o dos frases con la
 * tendencia y el valor actual, para quien no puede ver el gráfico. Si no hay datos, lo dice en texto.
 */
public final class ResumenProgreso {

    private ResumenProgreso() {}

    /** Resumen del gráfico «Avance por curso». */
    public static String avance(List<CursoProgreso> cursos) {
        if (cursos == null || cursos.isEmpty()) {
            return "Todavía no tienes cursos con avance para mostrar en el gráfico.";
        }
        if (cursos.size() == 1) {
            CursoProgreso c = cursos.get(0);
            return "Tienes 1 curso: «" + c.titulo() + "» con " + pct(c.porcentaje()) + " de avance.";
        }
        BigDecimal suma = cursos.stream().map(c -> nulo(c.porcentaje())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = suma.divide(BigDecimal.valueOf(cursos.size()), 0, RoundingMode.HALF_UP);
        CursoProgreso mas = cursos.stream().max(Comparator.comparing(c -> nulo(c.porcentaje()))).orElseThrow();
        CursoProgreso menos = cursos.stream().min(Comparator.comparing(c -> nulo(c.porcentaje()))).orElseThrow();
        long terminados = cursos.stream().filter(c -> nulo(c.porcentaje()).compareTo(BigDecimal.valueOf(100)) >= 0).count();
        return "Tienes " + cursos.size() + " cursos con un avance promedio de " + pct(promedio) + "; "
                + terminados + " terminado(s). El más avanzado es «" + mas.titulo() + "» (" + pct(mas.porcentaje())
                + ") y el que menos, «" + menos.titulo() + "» (" + pct(menos.porcentaje()) + ").";
    }

    /** Resumen de las calificaciones. */
    public static String calificaciones(List<Calificacion> lista) {
        if (lista == null || lista.isEmpty()) {
            return "Todavía no tienes evaluaciones ni tareas calificadas.";
        }
        BigDecimal suma = lista.stream().map(c -> nulo(c.mejorPorcentaje())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = suma.divide(BigDecimal.valueOf(lista.size()), 0, RoundingMode.HALF_UP);
        Calificacion mejor = lista.stream().max(Comparator.comparing(c -> nulo(c.mejorPorcentaje()))).orElseThrow();
        return "Tienes " + lista.size() + " actividad(es) calificada(s) con un promedio de " + pct(promedio)
                + ". Tu mejor resultado es «" + mejor.evaluacion() + "» con " + pct(mejor.mejorPorcentaje()) + ".";
    }

    private static BigDecimal nulo(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String pct(BigDecimal v) {
        return nulo(v).setScale(0, RoundingMode.HALF_UP).toPlainString() + " %";
    }
}
