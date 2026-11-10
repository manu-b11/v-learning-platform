package com.elearning.platform.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** RF-037: lectura y escritura de VTT y SRT, y validación de las líneas editadas. */
class VttTest {

    @Test
    void leeVttConEncabezadoEtiquetasYHoras() {
        List<Vtt.Linea> l = Vtt.leer("WEBVTT\n\nNOTE prueba\n\n1\n00:00:01.500 --> 00:00:04.000 align:start\n<v Inés>Hola</v> a todos\n\n"
                + "01:00:00.000 --> 01:00:02.000\nFin\n");
        assertEquals(2, l.size());
        assertEquals(new Vtt.Linea(1500, 4000, "Hola a todos"), l.get(0));
        assertEquals(3_600_000, l.get(1).inicioMs());
    }

    @Test
    void leeSrtConComaEnLosMilisegundos() {
        List<Vtt.Linea> l = Vtt.leer("1\r\n00:00:02,250 --> 00:00:05,000\r\nPrimera línea\r\nsegunda\r\n\r\n");
        assertEquals(List.of(new Vtt.Linea(2250, 5000, "Primera línea segunda")), l);
    }

    @Test
    void escribeVttQueSeVuelveALeerIgual() {
        List<Vtt.Linea> l = List.of(new Vtt.Linea(0, 4000, "Uno"), new Vtt.Linea(4000, 61_250, "Dos"));
        String vtt = Vtt.aVtt(l);
        assertTrue(vtt.startsWith("WEBVTT"));
        assertTrue(vtt.contains("00:00:04.000 --> 00:01:01.250"));
        assertEquals(l, Vtt.leer(vtt));
        assertEquals("Uno\nDos", Vtt.aTexto(l));
    }

    @Test
    void validaTiemposOrdenYTexto() {
        assertTrue(Vtt.validar(List.of(new Vtt.Linea(0, 1000, "ok"))).isEmpty());
        assertFalse(Vtt.validar(List.of()).isEmpty());
        assertTrue(Vtt.validar(List.of(new Vtt.Linea(2000, 1000, "x"))).get(0).contains("posterior"));
        assertTrue(Vtt.validar(List.of(new Vtt.Linea(5000, 6000, "a"), new Vtt.Linea(1000, 2000, "b")))
                .stream().anyMatch(e -> e.contains("antes")));
        assertTrue(Vtt.validar(List.of(new Vtt.Linea(0, 1000, " "))).get(0).contains("texto"));
    }

    @Test
    void rechazaMarcasDeTiempoImposibles() {
        assertNull(Vtt.aMs("00:75:00.000"));
        assertNull(Vtt.aMs("abc"));
    }
}
