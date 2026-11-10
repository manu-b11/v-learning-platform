package com.elearning.platform.services;

import com.elearning.platform.dto.VerificacionDtos.Hallazgo;
import com.elearning.platform.dto.VerificacionDtos.HallazgoVista;
import com.elearning.platform.dto.VerificacionDtos.InformeAccesibilidad;
import com.elearning.platform.dto.VerificacionDtos.VerificacionBorradorRequest;
import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.HallazgoAccesibilidad;
import com.elearning.platform.entity.Modulo;
import com.elearning.platform.entity.RecursoAccesible;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.SeveridadHallazgo;
import com.elearning.platform.enums.TipoRecursoAccesible;
import com.elearning.platform.repository.ContenidoRepository;
import com.elearning.platform.repository.HallazgoAccesibilidadRepository;
import com.elearning.platform.repository.ModuloRepository;
import com.elearning.platform.repository.RecursoAccesibleRepository;
import com.elearning.platform.util.ContrasteWcag;
import lombok.RequiredArgsConstructor;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * RF-049: verificador de accesibilidad del contenido. El cuerpo está en Markdown (decisión C1 de la
 * auditoría): se convierte a HTML con commonmark (tablas GFM) y se analiza con jsoup. Se ejecuta en
 * vivo desde el editor y de forma completa al publicar un contenido o un curso. Los hallazgos
 * BLOQUEANTES impiden publicar; las ADVERTENCIAS no. Se conserva un informe por contenido.
 */
@Service
@RequiredArgsConstructor
public class AccesibilidadContenidoService {

    /** Textos de enlace que no dicen a dónde llevan (WCAG 2.4.4). */
    static final Set<String> TEXTOS_GENERICOS = Set.of("aquí", "aqui", "clic aquí", "clic aqui", "haz clic aquí",
            "haz clic aqui", "click aquí", "click aqui", "click", "clic", "más", "mas", "leer más", "leer mas",
            "ver más", "ver mas", "enlace", "link", "este enlace", "esto", "here", "click here", "more");

    private static final Parser MARKDOWN = Parser.builder().extensions(List.of(TablesExtension.create())).build();
    private static final HtmlRenderer HTML = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();
    private static final Pattern COLOR = Pattern.compile("(?i)(?:^|;)\\s*color\\s*:\\s*(#[0-9a-f]{3}(?:[0-9a-f]{3})?)\\b");
    private static final Pattern FONDO = Pattern.compile("(?i)background(?:-color)?\\s*:\\s*(#[0-9a-f]{3}(?:[0-9a-f]{3})?)\\b");

    private final ContenidoRepository contenidos;
    private final ModuloRepository modulos;
    private final RecursoAccesibleRepository recursos;
    private final HallazgoAccesibilidadRepository hallazgos;
    private final AccesoService acceso;

    // ------------------------------------------------------------------ reglas

    /** RF-049: analiza un contenido y devuelve sus hallazgos (sin tocar la base). */
    public static List<Hallazgo> analizar(FormatoContenido formato, String urlRecurso, String cuerpo,
                                          Set<TipoRecursoAccesible> recursosDisponibles) {
        List<Hallazgo> lista = new ArrayList<>(reglasDeMedios(formato, recursosDisponibles));
        if (cuerpo != null && !cuerpo.isBlank()) lista.addAll(reglasDelTexto(cuerpo));
        return lista;
    }

    /**
     * Medios: video sin subtítulos o sin transcripción (bloqueantes, WCAG 1.2.2 y 1.2.3) y, por
     * RF-039, podcast o audio grabado sin transcripción (bloqueante, WCAG 1.2.1).
     */
    static List<Hallazgo> reglasDeMedios(FormatoContenido formato, Set<TipoRecursoAccesible> tipos) {
        List<Hallazgo> lista = new ArrayList<>();
        // RF-039: ningún audio publicado queda sin transcripción.
        if (formato == FormatoContenido.PODCAST && !tipos.contains(TipoRecursoAccesible.TRANSCRIPCION)) {
            lista.add(new Hallazgo("AUDIO_SIN_TRANSCRIPCION", "1.2.1", SeveridadHallazgo.BLOQUEANTE,
                    "El podcast no tiene transcripción.",
                    "En «Recursos accesibles» agrega una Transcripción con todo lo que se dice (quién habla y qué dice). "
                            + "También puedes generar el borrador automático (RF-037), revisarlo y aprobarlo."));
        }
        if (formato == FormatoContenido.VIDEO) {
            if (!tipos.contains(TipoRecursoAccesible.SUBTITULO)) {
                lista.add(new Hallazgo("VIDEO_SIN_SUBTITULOS", "1.2.2", SeveridadHallazgo.BLOQUEANTE,
                        "El video no tiene subtítulos.",
                        "Genera el borrador automático de subtítulos (RF-037), revísalo y apruébalo, o agrega en "
                                + "«Recursos accesibles» un recurso de tipo Subtítulos con el enlace al archivo .vtt."));
            }
            if (!tipos.contains(TipoRecursoAccesible.TRANSCRIPCION)) {
                lista.add(new Hallazgo("VIDEO_SIN_TRANSCRIPCION", "1.2.3", SeveridadHallazgo.BLOQUEANTE,
                        "El video no tiene transcripción.",
                        "En «Recursos accesibles» agrega una Transcripción con el texto completo de lo que se dice y se muestra."));
            }
        }
        return lista;
    }

    /** Texto: imágenes, encabezados, tablas, enlaces y contraste de los estilos en línea. */
    static List<Hallazgo> reglasDelTexto(String markdown) {
        Document doc = Jsoup.parseBodyFragment(HTML.render(MARKDOWN.parse(markdown)));
        List<Hallazgo> lista = new ArrayList<>();

        for (Element img : doc.select("img")) {
            if (img.attr("alt").isBlank()) {
                lista.add(new Hallazgo("IMAGEN_SIN_ALT", "1.1.1", SeveridadHallazgo.BLOQUEANTE,
                        "La imagen «" + nombreArchivo(img.attr("src")) + "» no tiene texto alternativo.",
                        "Describe la imagen entre los corchetes: ![qué muestra y para qué sirve](enlace). "
                                + "Si es solo decorativa, quítala del texto."));
            }
        }

        int anterior = 1; // el título de la lección ya es el h1 de la página
        for (Element h : doc.select("h1, h2, h3, h4, h5, h6")) {
            int nivel = h.tagName().charAt(1) - '0';
            if (nivel > anterior + 1) {
                lista.add(new Hallazgo("SALTO_ENCABEZADO", "1.3.1", SeveridadHallazgo.ADVERTENCIA,
                        "El encabezado «" + recortar(h.text()) + "» salta del nivel " + anterior + " al " + nivel + ".",
                        "Usa niveles seguidos: después de ## va ###, no ####. Así el lector de pantalla entiende la estructura."));
            }
            anterior = nivel;
        }

        int numeroTabla = 0;
        for (Element tabla : doc.select("table")) {
            numeroTabla++;
            if (tabla.select("th").isEmpty()) {
                lista.add(new Hallazgo("TABLA_SIN_ENCABEZADOS", "1.3.1", SeveridadHallazgo.ADVERTENCIA,
                        "La tabla " + numeroTabla + " no tiene fila de encabezados.",
                        "Escribe la tabla en Markdown con su primera fila de títulos seguida de | --- | --- |."));
            }
        }

        for (Element a : doc.select("a[href]")) {
            String texto = a.text().trim();
            String normal = texto.toLowerCase(Locale.ROOT).replaceAll("[.:!¡?¿…]+$", "").trim();
            boolean vacio = texto.isEmpty() && a.select("img[alt]").stream().allMatch(i -> i.attr("alt").isBlank());
            boolean esUrl = !texto.isEmpty() && (texto.equals(a.attr("href")) || normal.startsWith("http"));
            if (vacio || TEXTOS_GENERICOS.contains(normal) || esUrl) {
                lista.add(new Hallazgo("ENLACE_NO_DESCRIPTIVO", "2.4.4", SeveridadHallazgo.ADVERTENCIA,
                        vacio ? "Hay un enlace sin texto hacia " + recortar(a.attr("href")) + "."
                                : "El enlace «" + recortar(texto) + "» no dice a dónde lleva.",
                        "Escribe en el texto del enlace qué se va a abrir, por ejemplo: [Guía de normas APA (PDF)](enlace)."));
            }
        }

        for (Element el : doc.select("[style]")) {
            String estilo = el.attr("style");
            Matcher c = COLOR.matcher(estilo);
            if (!c.find()) continue;
            Matcher f = FONDO.matcher(estilo);
            String fondo = f.find() ? f.group(1) : "#FFFFFF";
            double ratio = ContrasteWcag.ratio(hex6(c.group(1)), hex6(fondo));
            if (ratio < ContrasteWcag.MINIMO_AA) {
                lista.add(new Hallazgo("CONTRASTE_INSUFICIENTE", "1.4.3", SeveridadHallazgo.ADVERTENCIA,
                        String.format(Locale.ROOT, "El texto «%s» tiene un contraste de %.1f:1 (mínimo 4.5:1).",
                                recortar(el.text()), ratio),
                        "Quita el color en línea o usa colores más oscuros sobre fondo claro (o al revés)."));
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------ casos de uso

    /** RF-049: verificación completa de un contenido; actualiza su informe. */
    @Transactional
    public InformeAccesibilidad verificar(Long contenidoId, Long instructorId) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        return verificarYGuardar(c, recursos.findByContenidoIdOrderByIdAsc(contenidoId));
    }

    /** RF-049: verificación en vivo desde el editor con los datos sin guardar. No se persiste. */
    @Transactional(readOnly = true)
    public InformeAccesibilidad verificarBorrador(Long contenidoId, Long instructorId, VerificacionBorradorRequest req) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        List<Hallazgo> lista = analizar(req.formato(), req.urlRecurso(), req.cuerpo(),
                ConformidadAccesibilidad.disponibles(recursos.findByContenidoIdOrderByIdAsc(contenidoId)));
        return informe(c, lista, LocalDateTime.now());
    }

    /**
     * RF-049: verificación completa de los contenidos publicados del curso al publicarlo. Guarda el
     * informe de cada contenido y devuelve todos los hallazgos.
     */
    @Transactional
    public List<InformeAccesibilidad> verificarCurso(Long cursoId) {
        List<Long> ids = modulos.findByCursoIdOrderByOrden(cursoId).stream().map(Modulo::getId).toList();
        if (ids.isEmpty()) return List.of();
        List<Contenido> publicados = contenidos.findByModuloIdInAndPublicadoTrueOrderByIdAsc(ids);
        Map<Long, List<RecursoAccesible>> porContenido = publicados.isEmpty() ? Map.of()
                : recursos.findByContenidoIdIn(publicados.stream().map(Contenido::getId).toList()).stream()
                .collect(Collectors.groupingBy(r -> r.getContenido().getId()));
        List<InformeAccesibilidad> informes = new ArrayList<>();
        for (Contenido c : publicados) {
            informes.add(verificarYGuardar(c, porContenido.getOrDefault(c.getId(), List.of())));
        }
        return informes;
    }

    /** RF-039 (criterio 4) y RF-037: reglas que se resuelven generando el borrador automático. */
    public static final Set<String> REGLAS_CON_BORRADOR = Set.of("AUDIO_SIN_TRANSCRIPCION", "VIDEO_SIN_SUBTITULOS",
            "VIDEO_SIN_TRANSCRIPCION");

    public static boolean ofreceBorradorAutomatico(String regla) {
        return REGLAS_CON_BORRADOR.contains(regla);
    }

    /** RF-049: solo los hallazgos bloqueantes de varios informes. */
    public static List<HallazgoVista> bloqueantes(Collection<InformeAccesibilidad> informes) {
        return informes.stream().flatMap(i -> i.hallazgos().stream())
                .filter(h -> h.severidad() == SeveridadHallazgo.BLOQUEANTE).toList();
    }

    InformeAccesibilidad verificarYGuardar(Contenido c, List<RecursoAccesible> recs) {
        List<Hallazgo> lista = analizar(c.getFormato(), c.getUrlRecurso(), c.getCuerpo(),
                ConformidadAccesibilidad.disponibles(recs));
        LocalDateTime ahora = LocalDateTime.now();
        Map<String, HallazgoAccesibilidad> abiertos = new LinkedHashMap<>();
        for (HallazgoAccesibilidad h : hallazgos.findByContenidoIdAndResueltoFalseOrderByIdAsc(c.getId())) {
            abiertos.put(h.getRegla() + "|" + h.getMensaje(), h);
        }
        Set<String> actuales = new HashSet<>();
        for (Hallazgo h : lista) {
            String clave = h.regla() + "|" + h.mensaje();
            actuales.add(clave);
            HallazgoAccesibilidad previo = abiertos.get(clave);
            if (previo != null) {
                previo.setFechaVerificacion(ahora);
                hallazgos.save(previo);
                continue;
            }
            HallazgoAccesibilidad nuevo = new HallazgoAccesibilidad();
            nuevo.setContenidoId(c.getId());
            nuevo.setRegla(h.regla());
            nuevo.setCriterioWcag(h.criterioWcag());
            nuevo.setSeveridad(h.severidad());
            nuevo.setMensaje(recortar(h.mensaje(), 500));
            nuevo.setComoCorregir(recortar(h.comoCorregir(), 500));
            nuevo.setFechaVerificacion(ahora);
            hallazgos.save(nuevo);
        }
        abiertos.forEach((clave, h) -> {
            if (!actuales.contains(clave)) {
                h.setResuelto(true);
                h.setFechaResolucion(ahora);
                hallazgos.save(h);
            }
        });
        return informe(c, lista, ahora);
    }

    private static InformeAccesibilidad informe(Contenido c, List<Hallazgo> lista, LocalDateTime fecha) {
        List<HallazgoVista> vistas = lista.stream().map(h -> new HallazgoVista(c.getId(), c.getTitulo(), h.regla(),
                h.criterioWcag(), h.severidad(), h.mensaje(), h.comoCorregir())).toList();
        int bloqueantes = (int) lista.stream().filter(h -> h.severidad() == SeveridadHallazgo.BLOQUEANTE).count();
        return new InformeAccesibilidad(c.getId(), c.getTitulo(), bloqueantes, lista.size() - bloqueantes, vistas, fecha);
    }

    private static String hex6(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() == 3) h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
        return "#" + h;
    }

    private static String nombreArchivo(String src) {
        if (src == null || src.isBlank()) return "sin enlace";
        String limpio = src.replaceAll("[?#].*$", "");
        int i = limpio.lastIndexOf('/');
        return recortar(i >= 0 && i < limpio.length() - 1 ? limpio.substring(i + 1) : limpio);
    }

    private static String recortar(String s) {
        return recortar(s, 60);
    }

    private static String recortar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
