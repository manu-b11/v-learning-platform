package com.elearning.platform.services;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/** RF-042 (HU-042): tiempo restante de la sesión y ampliación del intento según la acomodación. */
class AvisoTiempoTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:00:00Z");

    private static LocalDateTime local(Instant i) {
        return LocalDateTime.ofInstant(i, ZoneId.systemDefault());
    }

    @Test
    void laSesionTerminaPorInactividadSiLlegaAntesQueElToken() {
        long s = SesionService.segundosRestantes(local(AHORA.plusSeconds(1800)), AHORA.plusSeconds(8 * 3600), AHORA);
        assertEquals(1800, s);
    }

    @Test
    void laSesionTerminaPorElTopeDelTokenSiLlegaAntes() {
        long s = SesionService.segundosRestantes(local(AHORA.plusSeconds(1800)), AHORA.plusSeconds(90), AHORA);
        assertEquals(90, s);
    }

    @Test
    void elTiempoRestanteNuncaEsNegativo() {
        assertEquals(0, SesionService.segundosRestantes(local(AHORA.minusSeconds(5)), null, AHORA));
    }

    @Test
    void elAvisoDeEvaluacionApareceADosMinutos() {
        assertEquals(120, EvaluacionService.SEGUNDOS_AVISO);
    }

    @Test
    void soloSeAmpliaSiLaAcomodacionLoPermite() {
        assertFalse(EvaluacionService.puedeAmpliar(100L, 0, 0), "sin minutos de ampliación no se amplía");
        assertTrue(EvaluacionService.puedeAmpliar(100L, 10, 0));
    }

    @Test
    void soloSeAmpliaUnaVezPorIntento() {
        assertFalse(EvaluacionService.puedeAmpliar(100L, 10, 10));
    }

    @Test
    void noSeAmpliaSinLimiteNiConMuchoTiempoPorDelante() {
        assertFalse(EvaluacionService.puedeAmpliar(null, 10, 0), "sin límite no hay nada que ampliar");
        assertFalse(EvaluacionService.puedeAmpliar(600L, 10, 0), "faltan 10 minutos: aún no hay aviso");
        assertTrue(EvaluacionService.puedeAmpliar(300L, 10, 0), "margen de 5 minutos alrededor del aviso");
    }
}
