package com.elearning.platform.services.formatos;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

/** RF-032 (HU-032): HTML accesible, EPUB y braille conservan la estructura del original. */
class GeneradorFormatosTest {

    private static final String MD = """
            ## Roles de Scrum

            Lista de roles:

            - Product Owner
            - Scrum Master

            | Rol | Responsabilidad |
            | --- | --- |
            | PO | Prioriza |

            ![Diagrama del ciclo de Scrum](https://x/ciclo.png)

            <script>alert('x')</script>
            """;

    @Test
    void elHtmlAccesibleConservaEncabezadosListasTablasYTextoAlternativo() {
        Document d = Jsoup.parse(GeneradorFormatos.htmlAccesible("Scrum & equipos", MD, "Hola, bienvenidos."));
        assertEquals("es", d.selectFirst("html").attr("lang"));
        assertEquals("Scrum & equipos", d.title());
        assertEquals("Scrum & equipos", d.selectFirst("main > h1").text());
        assertEquals("Roles de Scrum", d.selectFirst("h2").text());
        assertEquals(2, d.select("ul > li").size());
        assertEquals("col", d.selectFirst("th").attr("scope"));
        assertEquals("Diagrama del ciclo de Scrum", d.selectFirst("img").attr("alt"));
        assertTrue(d.select("h2").eachText().contains("Transcripción"));
        assertTrue(d.select("script").isEmpty(), "no copia scripts del contenido");
    }

    @Test
    void elEpubEsValidoYDeclaraSuAccesibilidad() throws IOException {
        byte[] epub = GeneradorFormatos.epub("Scrum", MD, null);
        Map<String, String> archivos = new HashMap<>();
        String primero = null;
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(epub))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
                if (primero == null) {
                    primero = e.getName();
                    assertEquals(ZipEntry.STORED, e.getMethod(), "mimetype sin comprimir");
                }
                archivos.put(e.getName(), new String(z.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        assertEquals("mimetype", primero);
        assertEquals("application/epub+zip", archivos.get("mimetype"));
        assertTrue(archivos.get("OEBPS/content.opf").contains("schema:accessibilityFeature"));
        assertTrue(archivos.get("OEBPS/content.opf").contains("<dc:language>es</dc:language>"));
        // El capítulo es XHTML bien formado con la estructura del original.
        Document cap = Jsoup.parse(archivos.get("OEBPS/capitulo.xhtml"), "", Parser.xmlParser());
        assertEquals("Roles de Scrum", cap.selectFirst("h2").text());
        assertEquals("Diagrama del ciclo de Scrum", cap.selectFirst("img").attr("alt"));
        assertNotNull(archivos.get("OEBPS/nav.xhtml"));
    }

    @Test
    void elBrailleUsaSignosDeMayusculaYNumero() {
        assertEquals(",hola", BrailleBasico.palabra("Hola"));
        assertEquals("#bjbf", BrailleBasico.palabra("2026"));
        assertEquals("a]o", BrailleBasico.palabra("año"));
        assertEquals("cancion4", BrailleBasico.palabra("canción."));
    }

    @Test
    void elBrfRespetaCuarentaCeldasYVeinticincoLineasPorPagina() {
        String texto = "palabra ".repeat(600);
        String brf = new BrailleBasico().aBrf(texto);
        for (String linea : brf.replace("\f", "").split("\r\n")) assertTrue(linea.length() <= 40);
        String[] paginas = brf.split("\f");
        assertTrue(paginas.length > 1);
        assertEquals(25, paginas[0].split("\r\n").length);
    }

    @Test
    void liblouisRecibeLaTablaConfigurada() {
        assertEquals(java.util.List.of("lou_translate", "--forward", "unicode.dis,es-g1.ctb"),
                BrailleLiblouis.comando("lou_translate", "es-g1.ctb"));
    }
}
