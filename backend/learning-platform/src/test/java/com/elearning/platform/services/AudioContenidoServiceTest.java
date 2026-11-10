package com.elearning.platform.services;

import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.RolUsuario;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.voz.ServicioTextoAVoz;
import com.elearning.platform.services.voz.TextoAVozPrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-036 (HU-036): MP3 de la lectura en voz alta, velocidad, pistas numeradas y rendimiento. */
class AudioContenidoServiceTest {

    private final UsuarioPrincipal ana = new UsuarioPrincipal(20L, "ana@tdea.edu.co", "Ana", RolUsuario.ESTUDIANTE, 1L);
    private ContenidoService contenidos;
    private ServicioTextoAVoz voz;
    private AuditoriaService auditoria;
    private AudioContenidoService servicio;

    @BeforeEach
    void preparar() {
        contenidos = mock(ContenidoService.class);
        voz = spy(new TextoAVozPrueba());
        auditoria = mock(AuditoriaService.class);
        servicio = new AudioContenidoService(contenidos, voz, auditoria);
    }

    private static String palabras(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) sb.append("palabra").append(i % 10 == 0 ? ". " : " ");
        return sb.toString().trim();
    }

    @Test
    void unContenidoCortoSeEntregaComoUnMp3ConElTituloComoNombre() {
        when(contenidos.textoLeible(ana, 1L)).thenReturn(new ContenidoService.TextoLeible(1L, "Roles de Scrum", "Hola. Esto es Scrum."));

        var a = servicio.descargar(ana, 1L, 1.25, "127.0.0.1");

        assertEquals("Roles de Scrum.mp3", a.nombre());
        assertEquals("audio/mpeg", a.tipoContenido());
        verify(voz).sintetizar(anyString(), eq(1.25));
        verify(auditoria).registrar(eq(20L), eq("AUDIO_DESCARGADO"), contains("velocidad=1.25"),
                eq(ResultadoAuditoria.PERMITIDO), anyString());
    }

    @Test
    void unContenidoLargoSeDivideEnPistasNumeradas() throws IOException {
        when(contenidos.textoLeible(ana, 1L)).thenReturn(new ContenidoService.TextoLeible(1L, "Guía larga", palabras(5000)));

        var a = servicio.descargar(ana, 1L, 1, "127.0.0.1");

        assertEquals("Guía larga.zip".replace("í", "i"), a.nombre());
        List<String> nombres = new ArrayList<>();
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(a.contenido()))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null; ) nombres.add(e.getName());
        }
        assertEquals(List.of("01 - Guia larga.mp3", "02 - Guia larga.mp3", "03 - Guia larga.mp3", "04 - Guia larga.mp3"), nombres);
    }

    @Test
    void lasPistasNoCortanFrasesNiSuperanElMaximo() {
        List<String> pistas = AudioContenidoService.dividir(palabras(3000), 1000);
        assertEquals(3, pistas.size());
        for (String p : pistas) {
            assertTrue(p.split("\\s+").length <= 1000);
            assertTrue(p.endsWith("."), "cada pista termina en una frase completa");
        }
    }

    @Test
    void laVelocidadFueraDeRangoSeRechaza() {
        assertThrows(ApiException.class, () -> servicio.descargar(ana, 1L, 3, "127.0.0.1"));
        assertThrows(ApiException.class, () -> servicio.descargar(ana, 1L, 0.25, "127.0.0.1"));
    }

    @Test
    void sinTextoNoHayAudio() {
        when(contenidos.textoLeible(ana, 1L)).thenReturn(new ContenidoService.TextoLeible(1L, "Video", " "));
        ApiException e = assertThrows(ApiException.class, () -> servicio.descargar(ana, 1L, 1, "127.0.0.1"));
        assertEquals("SIN_TEXTO", e.getCodigo());
    }

    @Test
    void cincoMilPalabrasSeGeneranEnMenosDe30Segundos() {
        when(contenidos.textoLeible(ana, 1L)).thenReturn(new ContenidoService.TextoLeible(1L, "x", palabras(5000)));
        long inicio = System.nanoTime();
        servicio.descargar(ana, 1L, 1, "127.0.0.1");
        assertTrue((System.nanoTime() - inicio) / 1_000_000_000L < 30);
    }

    @Test
    void elNombreDelArchivoNoLlevaSimbolosPeligrosos() {
        assertEquals("Que es Scrum", AudioContenidoService.nombreArchivo("¿Qué es Scrum?"));
        assertEquals("contenido", AudioContenidoService.nombreArchivo("../.."
                .replace(".", "").replace("/", "")));
    }
}
