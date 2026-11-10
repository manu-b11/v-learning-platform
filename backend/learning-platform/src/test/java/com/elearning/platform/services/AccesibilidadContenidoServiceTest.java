package com.elearning.platform.services;

import com.elearning.platform.dto.VerificacionDtos.Hallazgo;
import com.elearning.platform.dto.VerificacionDtos.InformeAccesibilidad;
import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.HallazgoAccesibilidad;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.SeveridadHallazgo;
import com.elearning.platform.enums.TipoRecursoAccesible;
import com.elearning.platform.repository.ContenidoRepository;
import com.elearning.platform.repository.HallazgoAccesibilidadRepository;
import com.elearning.platform.repository.ModuloRepository;
import com.elearning.platform.repository.RecursoAccesibleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** RF-049 (HU-049): reglas del verificador, severidad, criterio WCAG, informe y rendimiento. */
class AccesibilidadContenidoServiceTest {

    private static final Set<TipoRecursoAccesible> NINGUNO = EnumSet.noneOf(TipoRecursoAccesible.class);

    private static List<Hallazgo> texto(String markdown) {
        return AccesibilidadContenidoService.analizar(FormatoContenido.LECTURA, null, markdown, NINGUNO);
    }

    private static Optional<Hallazgo> regla(List<Hallazgo> lista, String regla) {
        return lista.stream().filter(h -> h.regla().equals(regla)).findFirst();
    }

    @Test
    void imagenSinTextoAlternativoEsBloqueante() {
        Hallazgo h = regla(texto("Mira esto:\n\n![](https://x.co/img/diagrama.png)"), "IMAGEN_SIN_ALT").orElseThrow();
        assertEquals(SeveridadHallazgo.BLOQUEANTE, h.severidad());
        assertEquals("1.1.1", h.criterioWcag());
        assertTrue(h.mensaje().contains("diagrama.png"));
        assertFalse(h.comoCorregir().isBlank());
    }

    @Test
    void imagenConTextoAlternativoNoGeneraHallazgo() {
        assertTrue(regla(texto("![Ciclo de Scrum con tres roles](https://x.co/s.png)"), "IMAGEN_SIN_ALT").isEmpty());
    }

    @Test
    void imagenHtmlSinAltTambienSeDetecta() {
        assertTrue(regla(texto("<img src=\"a.png\">"), "IMAGEN_SIN_ALT").isPresent());
    }

    @Test
    void saltoDeNivelDeEncabezadoEsAdvertencia() {
        Hallazgo h = regla(texto("## Tema\n\n#### Detalle"), "SALTO_ENCABEZADO").orElseThrow();
        assertEquals(SeveridadHallazgo.ADVERTENCIA, h.severidad());
        assertEquals("1.3.1", h.criterioWcag());
        assertTrue(regla(texto("## Tema\n\n### Detalle\n\n## Otro"), "SALTO_ENCABEZADO").isEmpty());
    }

    @Test
    void tablaSinEncabezadosEsAdvertencia() {
        Hallazgo h = regla(texto("<table><tr><td>1</td><td>2</td></tr></table>"), "TABLA_SIN_ENCABEZADOS").orElseThrow();
        assertEquals(SeveridadHallazgo.ADVERTENCIA, h.severidad());
        assertTrue(regla(texto("| Año | Total |\n| --- | --- |\n| 2026 | 10 |"), "TABLA_SIN_ENCABEZADOS").isEmpty());
    }

    @Test
    void enlaceSinTextoDescriptivoEsAdvertencia() {
        assertTrue(regla(texto("Lee [aquí](https://x.co/guia)."), "ENLACE_NO_DESCRIPTIVO").isPresent());
        assertTrue(regla(texto("[Haz clic aquí.](https://x.co)"), "ENLACE_NO_DESCRIPTIVO").isPresent());
        assertTrue(regla(texto("[https://x.co/guia](https://x.co/guia)"), "ENLACE_NO_DESCRIPTIVO").isPresent());
        Hallazgo h = regla(texto("[ver más](https://x.co)"), "ENLACE_NO_DESCRIPTIVO").orElseThrow();
        assertEquals("2.4.4", h.criterioWcag());
        assertTrue(regla(texto("[Guía de normas APA (PDF)](https://x.co/apa.pdf)"), "ENLACE_NO_DESCRIPTIVO").isEmpty());
    }

    @Test
    void contrasteInsuficienteEnEstilosEnLinea() {
        Hallazgo h = regla(texto("<span style=\"color:#ffff00\">Importante</span>"), "CONTRASTE_INSUFICIENTE").orElseThrow();
        assertEquals("1.4.3", h.criterioWcag());
        assertEquals(SeveridadHallazgo.ADVERTENCIA, h.severidad());
        assertTrue(regla(texto("<span style=\"color:#1e293b; background-color:#fff\">Bien</span>"),
                "CONTRASTE_INSUFICIENTE").isEmpty());
    }

    @Test
    void videoSinSubtitulosNiTranscripcionEsBloqueante() {
        List<Hallazgo> lista = AccesibilidadContenidoService.analizar(FormatoContenido.VIDEO, "https://v", null, NINGUNO);
        assertEquals(SeveridadHallazgo.BLOQUEANTE, regla(lista, "VIDEO_SIN_SUBTITULOS").orElseThrow().severidad());
        assertEquals("1.2.2", regla(lista, "VIDEO_SIN_SUBTITULOS").orElseThrow().criterioWcag());
        assertTrue(regla(lista, "VIDEO_SIN_TRANSCRIPCION").isPresent());

        List<Hallazgo> completo = AccesibilidadContenidoService.analizar(FormatoContenido.VIDEO, "https://v", null,
                EnumSet.of(TipoRecursoAccesible.SUBTITULO, TipoRecursoAccesible.TRANSCRIPCION));
        assertTrue(completo.isEmpty());
    }

    @Test
    void unContenidoLimpioNoTieneHallazgos() {
        assertTrue(texto("## Introducción\n\nTexto claro con un [enlace a la guía](https://x.co).").isEmpty());
    }

    @Test
    void verificarCienContenidosTardaMenosDeCincoSegundos() {
        String md = "## Tema\n\n![Diagrama](a.png)\n\n| A | B |\n| --- | --- |\n| 1 | 2 |\n\n[Guía de estudio](https://x.co)\n\n"
                .repeat(20);
        long inicio = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            AccesibilidadContenidoService.analizar(FormatoContenido.LECTURA, null, md, NINGUNO);
        }
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(ms < 5000, "tardó " + ms + " ms");
    }

    @Test
    void elInformeMarcaComoResueltoLoQueYaSeCorrigio() {
        HallazgoAccesibilidadRepository repo = mock(HallazgoAccesibilidadRepository.class);
        AccesibilidadContenidoService servicio = new AccesibilidadContenidoService(mock(ContenidoRepository.class),
                mock(ModuloRepository.class), mock(RecursoAccesibleRepository.class), repo, mock(AccesoService.class));
        HallazgoAccesibilidad viejo = new HallazgoAccesibilidad();
        viejo.setContenidoId(1L);
        viejo.setRegla("IMAGEN_SIN_ALT");
        viejo.setMensaje("La imagen «a.png» no tiene texto alternativo.");
        when(repo.findByContenidoIdAndResueltoFalseOrderByIdAsc(1L)).thenReturn(List.of(viejo));

        Contenido c = new Contenido();
        c.setId(1L);
        c.setTitulo("Lectura");
        c.setFormato(FormatoContenido.LECTURA);
        c.setCuerpo("![Mapa conceptual](a.png)\n\nLee [aquí](https://x.co).");

        InformeAccesibilidad informe = servicio.verificarYGuardar(c, List.of());

        assertTrue(viejo.isResuelto());
        assertNotNull(viejo.getFechaResolucion());
        assertEquals(0, informe.bloqueantes());
        assertEquals(1, informe.advertencias());
        ArgumentCaptor<HallazgoAccesibilidad> guardados = ArgumentCaptor.forClass(HallazgoAccesibilidad.class);
        verify(repo, atLeastOnce()).save(guardados.capture());
        assertTrue(guardados.getAllValues().stream().anyMatch(h -> h.getRegla().equals("ENLACE_NO_DESCRIPTIVO")));
    }

    @Test
    void soloLosBloqueantesImpidenPublicar() {
        var informe = new InformeAccesibilidad(1L, "x", 1, 1, List.of(
                new com.elearning.platform.dto.VerificacionDtos.HallazgoVista(1L, "x", "IMAGEN_SIN_ALT", "1.1.1",
                        SeveridadHallazgo.BLOQUEANTE, "m", "c"),
                new com.elearning.platform.dto.VerificacionDtos.HallazgoVista(1L, "x", "SALTO_ENCABEZADO", "1.3.1",
                        SeveridadHallazgo.ADVERTENCIA, "m", "c")), null);
        assertEquals(1, AccesibilidadContenidoService.bloqueantes(List.of(informe)).size());
        assertTrue(AccesibilidadContenidoService.bloqueantes(List.of()).isEmpty());
    }
}
