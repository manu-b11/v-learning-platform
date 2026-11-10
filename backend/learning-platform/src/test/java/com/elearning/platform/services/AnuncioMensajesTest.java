package com.elearning.platform.services;

import com.elearning.platform.dto.ChatDtos.MensajeDto;
import com.elearning.platform.enums.CategoriaAccesibilidad;
import com.elearning.platform.enums.EstadoMensaje;
import com.elearning.platform.enums.RolUsuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/** RF-034 (HU-034): texto del anuncio de mensajes y preferencia «anunciar mensajes». */
class AnuncioMensajesTest {

    @Test
    void cadaMensajeTraeElTextoParaAnunciarConAutor() {
        MensajeDto m = new MensajeDto(1L, 5L, 10L, "Inés Gómez", RolUsuario.INSTRUCTOR,
                "Hola a todos,\n  mañana hay quiz.", LocalDateTime.now(), EstadoMensaje.ENVIADO);
        assertEquals("Inés Gómez: Hola a todos, mañana hay quiz.", m.textoPlano());
    }

    @Test
    void sinAutorSeAnunciaDeFormaGenerica() {
        assertEquals("Alguien: hola", MensajeDto.textoPlano(null, " hola "));
    }

    @Test
    void losAnunciosEstanActivadosPorDefecto() {
        assertTrue(ReglasAccesibilidad.base().anunciarMensajes());
        assertTrue(ReglasAccesibilidad.porCategorias(EnumSet.of(CategoriaAccesibilidad.VISUAL)).anunciarMensajes());
    }

    @Test
    void laSugerenciaRespetaQueSeDesactiven() {
        var sinAnuncios = new com.elearning.platform.dto.AccesibilidadDtos.ConfiguracionDto(false, 16, null,
                new java.math.BigDecimal("1.50"), false, false, false, false, false, 0, false, false, false);
        assertFalse(ReglasAccesibilidad.sugerir(sinAnuncios, EnumSet.of(CategoriaAccesibilidad.VISUAL)).anunciarMensajes());
    }
}
