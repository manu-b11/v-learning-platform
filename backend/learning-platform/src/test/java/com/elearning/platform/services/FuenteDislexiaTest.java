package com.elearning.platform.services;

import com.elearning.platform.dto.AccesibilidadDtos.ConfiguracionDto;
import com.elearning.platform.enums.CategoriaAccesibilidad;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/** RF-035 (HU-035): OpenDyslexic como opción adicional y subrayado de enlaces. */
class FuenteDislexiaTest {

    private static ConfiguracionDto con(String tipografia, Boolean subrayar) {
        return new ConfiguracionDto(false, 16, tipografia, new BigDecimal("1.50"),
                false, false, false, false, false, 0, subrayar, false, true);
    }

    @Test
    void openDyslexicEsUnaTipografiaDisponibleSinQuitarLexend() {
        assertTrue(ReglasAccesibilidad.TIPOGRAFIAS.contains("OpenDyslexic"));
        assertTrue(ReglasAccesibilidad.TIPOGRAFIAS.contains("Lexend"));
    }

    @Test
    void laSugerenciaNoReemplazaOpenDyslexicPorOtraFuente() {
        ConfiguracionDto s = ReglasAccesibilidad.sugerir(con("OpenDyslexic", false),
                EnumSet.of(CategoriaAccesibilidad.COGNITIVA));
        assertEquals("OpenDyslexic", s.tipografia());
    }

    @Test
    void conOpenDyslexicNoSeAvisaQueFaltaUnaFuenteDeLectura() {
        assertTrue(ReglasAccesibilidad.advertencias(con("OpenDyslexic", false),
                EnumSet.of(CategoriaAccesibilidad.COGNITIVA)).stream().noneMatch(a -> a.contains("tipografías")));
    }

    @Test
    void elSubrayadoDeEnlacesSeConservaAlSugerir() {
        assertTrue(ReglasAccesibilidad.sugerir(con("Lexend", true), EnumSet.noneOf(CategoriaAccesibilidad.class))
                .subrayarEnlaces());
    }

    @Test
    void laConfiguracionAnteriorNoSubrayaEnlaces() {
        assertFalse(ReglasAccesibilidad.base().subrayarEnlaces());
    }
}
