package com.elearning.platform.services;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RF-044 (HU-044): vigencia mínima del código y política de reenvío. */
class OtpServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 9, 10, 0);

    @Test
    void elCodigoValeAlMenosDiezMinutosAunqueSeConfigureMenos() {
        assertEquals(10, OtpService.minutosVigencia(5));
        assertEquals(10, OtpService.minutosVigencia(10));
        assertEquals(15, OtpService.minutosVigencia(15));
    }

    @Test
    void sinCodigoPendienteNoSeReenvia() {
        assertEquals(OtpService.Reenvio.SIN_DESAFIO, OtpService.politicaReenvio(null, 0, AHORA));
    }

    @Test
    void unCodigoDeHaceMasDeTreintaMinutosNoHabilitaElReenvio() {
        assertEquals(OtpService.Reenvio.SIN_DESAFIO,
                OtpService.politicaReenvio(AHORA.minusMinutes(31), 0, AHORA));
    }

    @Test
    void hayQueEsperarUnMinutoEntreCodigos() {
        assertEquals(OtpService.Reenvio.ESPERAR, OtpService.politicaReenvio(AHORA.minusSeconds(30), 0, AHORA));
        assertEquals(OtpService.Reenvio.PERMITIDO, OtpService.politicaReenvio(AHORA.minusSeconds(61), 0, AHORA));
    }

    @Test
    void sePermitenCincoReenviosPorVentana() {
        assertEquals(OtpService.Reenvio.PERMITIDO, OtpService.politicaReenvio(AHORA.minusMinutes(2), 4, AHORA));
        assertEquals(OtpService.Reenvio.LIMITE, OtpService.politicaReenvio(AHORA.minusMinutes(2), 5, AHORA));
    }

    @Test
    void unCodigoVencidoDentroDeLaVentanaSePuedeReemplazar() {
        // Venció a los 10 minutos, pero el inicio de sesión fue hace 12: se permite pedir otro.
        assertEquals(OtpService.Reenvio.PERMITIDO, OtpService.politicaReenvio(AHORA.minusMinutes(12), 1, AHORA));
    }
}
