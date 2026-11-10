package com.elearning.platform.services;

import com.elearning.platform.dto.AccesibilidadDtos.ConfiguracionDto;
import com.elearning.platform.enums.CategoriaAccesibilidad;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/** RF-046 (HU-046): modo lectura recomendado por defecto para el perfil cognitivo. */
class ModoLecturaTest {

    @Test
    void elPerfilCognitivoActivaElModoLecturaPorDefecto() {
        assertTrue(ReglasAccesibilidad.porCategorias(EnumSet.of(CategoriaAccesibilidad.COGNITIVA)).modoLectura());
    }

    @Test
    void losDemasPerfilesNoLoActivan() {
        assertFalse(ReglasAccesibilidad.porCategorias(EnumSet.of(CategoriaAccesibilidad.VISUAL,
                CategoriaAccesibilidad.AUDITIVA, CategoriaAccesibilidad.MOTORA)).modoLectura());
        assertFalse(ReglasAccesibilidad.base().modoLectura());
    }

    @Test
    void laSugerenciaRespetaElModoLecturaElegido() {
        ConfiguracionDto elegido = new ConfiguracionDto(false, 16, null, new java.math.BigDecimal("1.50"),
                false, false, false, false, false, 0, false, true, true);
        assertTrue(ReglasAccesibilidad.sugerir(elegido, EnumSet.noneOf(CategoriaAccesibilidad.class)).modoLectura());
    }
}
