package com.elearning.platform.services.formatos;

import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * RF-032: genera el HTML accesible y el EPUB de un contenido de lectura. Ambos parten del
 * Markdown del contenido y conservan encabezados, listas, tablas (con th) y texto alternativo.
 */
public final class GeneradorFormatos {

    private static final List<org.commonmark.Extension> EXT = List.of(TablesExtension.create());
    private static final Parser MARKDOWN = Parser.builder().extensions(EXT).build();
    /** escapeHtml: el HTML crudo dentro del Markdown no se copia tal cual al archivo. */
    private static final HtmlRenderer HTML = HtmlRenderer.builder().extensions(EXT).escapeHtml(true).build();

    private GeneradorFormatos() {}

    /** Cuerpo HTML seguro (sin scripts ni atributos peligrosos), con encabezados desde h2. */
    static String cuerpo(String markdown, String transcripcion) {
        StringBuilder md = new StringBuilder(markdown == null ? "" : markdown);
        if (transcripcion != null && !transcripcion.isBlank()) {
            md.append("\n\n## Transcripción\n\n").append(transcripcion.trim());
        }
        String html = HTML.render(MARKDOWN.parse(md.toString()));
        Safelist permitidos = Safelist.relaxed().removeTags("h1").addAttributes("th", "scope");
        Document d = Jsoup.parseBodyFragment(Jsoup.clean(html, permitidos));
        d.select("thead th").attr("scope", "col");
        d.select("img:not([alt])").attr("alt", "");
        return d.body().html();
    }

    /** RF-032: documento HTML accesible (idioma, título, h1, main) listo para descargar. */
    public static String htmlAccesible(String titulo, String markdown, String transcripcion) {
        String t = Entities.escape(titulo == null ? "Contenido" : titulo);
        return "<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n<meta charset=\"utf-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<title>" + t + "</title>\n"
                + "<style>body{font-family:system-ui,sans-serif;line-height:1.6;max-width:42rem;margin:2rem auto;padding:0 1rem;color:#1e293b}"
                + "table{border-collapse:collapse}th,td{border:1px solid #64748b;padding:.4rem}a{color:#5b21b6;text-decoration:underline}"
                + "img{max-width:100%}</style>\n</head>\n<body>\n<main>\n<h1>" + t + "</h1>\n"
                + cuerpo(markdown, transcripcion) + "\n</main>\n</body>\n</html>\n";
    }

    /** RF-032: EPUB 3 de un capítulo, con navegación y metadatos de accesibilidad. */
    public static byte[] epub(String titulo, String markdown, String transcripcion) {
        String t = Entities.escape(titulo == null ? "Contenido" : titulo);
        Document cap = Jsoup.parse("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" "
                + "lang=\"es\" xml:lang=\"es\"><head><meta charset=\"utf-8\"/><title>" + t + "</title></head><body><section>"
                + "<h1>" + t + "</h1>" + cuerpo(markdown, transcripcion) + "</section></body></html>");
        cap.outputSettings().syntax(Document.OutputSettings.Syntax.xml).escapeMode(Entities.EscapeMode.xhtml).prettyPrint(false);
        String xhtml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE html>\n" + cap.html();
        String id = "urn:uuid:" + UUID.nameUUIDFromBytes((titulo + markdown).getBytes(StandardCharsets.UTF_8));
        String ahora = Instant.now().toString().replaceAll("\\.\\d+Z$", "Z");
        String opf = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"3.0\" unique-identifier=\"id\" xml:lang=\"es\">\n"
                + "<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n"
                + "<dc:identifier id=\"id\">" + id + "</dc:identifier>\n<dc:title>" + t + "</dc:title>\n<dc:language>es</dc:language>\n"
                + "<meta property=\"dcterms:modified\">" + ahora + "</meta>\n"
                + "<meta property=\"schema:accessMode\">textual</meta>\n"
                + "<meta property=\"schema:accessibilityFeature\">structuralNavigation</meta>\n"
                + "<meta property=\"schema:accessibilityFeature\">alternativeText</meta>\n"
                + "<meta property=\"schema:accessibilityHazard\">none</meta>\n"
                + "<meta property=\"schema:accessibilitySummary\">Contenido de V-Learning con encabezados, listas, tablas y texto alternativo.</meta>\n"
                + "</metadata>\n<manifest>\n"
                + "<item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\"/>\n"
                + "<item id=\"cap\" href=\"capitulo.xhtml\" media-type=\"application/xhtml+xml\"/>\n"
                + "</manifest>\n<spine><itemref idref=\"cap\"/></spine>\n</package>\n";
        String nav = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE html>\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" lang=\"es\" xml:lang=\"es\">"
                + "<head><meta charset=\"utf-8\"/><title>Índice</title></head><body><nav epub:type=\"toc\"><h1>Índice</h1>"
                + "<ol><li><a href=\"capitulo.xhtml\">" + t + "</a></li></ol></nav></body></html>";
        String container = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">"
                + "<rootfiles><rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/></rootfiles></container>";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
            // El primer archivo debe ser «mimetype», sin comprimir (requisito de EPUB).
            byte[] mime = "application/epub+zip".getBytes(StandardCharsets.US_ASCII);
            ZipEntry m = new ZipEntry("mimetype");
            m.setMethod(ZipEntry.STORED);
            m.setSize(mime.length);
            CRC32 crc = new CRC32();
            crc.update(mime);
            m.setCrc(crc.getValue());
            zip.putNextEntry(m);
            zip.write(mime);
            zip.closeEntry();
            agregar(zip, "META-INF/container.xml", container);
            agregar(zip, "OEBPS/content.opf", opf);
            agregar(zip, "OEBPS/nav.xhtml", nav);
            agregar(zip, "OEBPS/capitulo.xhtml", xhtml);
            zip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
