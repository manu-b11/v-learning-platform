package com.elearning.platform.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** RF-041 (HU-041): el tiempo límite se calcula en el servidor y aplica el mayor, sin sumar. */
class TiempoEvaluacionTest {

    private static final BigDecimal X15 = new BigDecimal("1.50");
    private static final BigDecimal X2 = new BigDecimal("2.00");

    @Test
    void sinAcomodacionSoloCuentaElPerfil() {
        assertEquals(30, TiempoEvaluacion.soloPerfil(0).minutosLimite(30));
        assertEquals(45, TiempoEvaluacion.soloPerfil(50).minutosLimite(30));
    }

    @Test
    void seAplicaElMayorEntrePerfilYAcomodacionSinSumarlos() {
        // Perfil +25 % y acomodación 1,5x: 1,5x (no 1,75x).
        assertEquals(45, new TiempoEvaluacion(25, X15, false, null, 0).minutosLimite(30));
        // Perfil +100 % (2x) y acomodación 1,5x: gana el perfil.
        assertEquals(60, new TiempoEvaluacion(100, X15, false, null, 0).minutosLimite(30));
        // Perfil +50 % y acomodación 2x: gana la acomodación.
        assertEquals(60, new TiempoEvaluacion(50, X2, false, null, 0).minutosLimite(30));
    }

    @Test
    void elResultadoSeRedondeaHaciaArriba() {
        assertEquals(17, new TiempoEvaluacion(0, X15, false, null, 0).minutosLimite(11)); // 16,5 → 17
    }

    @Test
    void elTiempoIlimitadoQuitaElLimite() {
        assertNull(new TiempoEvaluacion(50, null, true, null, 0).minutosLimite(30));
    }

    @Test
    void unaEvaluacionSinLimiteSigueSinLimite() {
        assertNull(new TiempoEvaluacion(0, X2, false, null, 0).minutosLimite(null));
    }

    @Test
    void laFechaEspecialSoloAmpliaElPlazo() {
        LocalDateTime evaluacion = LocalDateTime.of(2026, 11, 10, 23, 59);
        LocalDateTime posterior = evaluacion.plusDays(3);
        LocalDateTime anterior = evaluacion.minusDays(1);

        assertEquals(posterior, new TiempoEvaluacion(0, null, false, posterior, 0).fechaLimite(evaluacion));
        assertEquals(evaluacion, new TiempoEvaluacion(0, null, false, anterior, 0).fechaLimite(evaluacion));
        assertNull(new TiempoEvaluacion(0, null, false, posterior, 0).fechaLimite(null), "sin plazo no se crea uno");
    }
}
