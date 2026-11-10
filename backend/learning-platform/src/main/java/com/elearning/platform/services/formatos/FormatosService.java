package com.elearning.platform.services.formatos;

import com.elearning.platform.dto.FormatoDtos.FormatoVista;
import com.elearning.platform.dto.FormatoDtos.FormatosRespuesta;
import com.elearning.platform.dto.InformeDtos.ArchivoGenerado;
import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.FormatoAlternativo;
import com.elearning.platform.entity.FormatoAlternativo.Estado;
import com.elearning.platform.entity.FormatoAlternativo.Tipo;
import com.elearning.platform.entity.RecursoAccesible;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.TipoRecursoAccesible;
import com.elearning.platform.events.FormatoSolicitadoEvent;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.FormatoAlternativoRepository;
import com.elearning.platform.repository.RecursoAccesibleRepository;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.AuditoriaService;
import com.elearning.platform.services.ContenidoService;
import com.elearning.platform.util.Hashes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RF-032: formatos alternativos de los contenidos de lectura (MP3, EPUB, HTML accesible y BRF).
 * Lo ya generado se reutiliza mientras el contenido no cambie; lo nuevo se genera en segundo plano
 * (FormatosGeneracion) y se avisa al estudiante. Cada descarga queda en auditoría.
 */
@Service
public class FormatosService {

    static final Map<Tipo, String> NOMBRES = Map.of(Tipo.MP3, "Audio MP3", Tipo.EPUB, "Libro electrónico EPUB",
            Tipo.HTML, "HTML accesible", Tipo.BRF, "Braille electrónico BRF");
    static final Map<Tipo, String> EXTENSIONES = Map.of(Tipo.MP3, "mp3", Tipo.EPUB, "epub", Tipo.HTML, "html", Tipo.BRF, "brf");
    static final Map<Tipo, String> MIME = Map.of(Tipo.MP3, "audio/mpeg", Tipo.EPUB, "application/epub+zip",
            Tipo.HTML, "text/html;charset=UTF-8", Tipo.BRF, "text/plain;charset=US-ASCII");

    private final ContenidoService contenidos;
    private final RecursoAccesibleRepository recursos;
    private final FormatoAlternativoRepository formatos;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;
    private final Path carpeta;

    public FormatosService(ContenidoService contenidos, RecursoAccesibleRepository recursos,
                           FormatoAlternativoRepository formatos, AuditoriaService auditoria,
                           ApplicationEventPublisher eventos, @Value("${vlearning.uploads.dir:uploads}") String uploads) {
        this.contenidos = contenidos;
        this.recursos = recursos;
        this.formatos = formatos;
        this.auditoria = auditoria;
        this.eventos = eventos;
        this.carpeta = Paths.get(uploads, "formatos").toAbsolutePath().normalize();
    }

    /** RF-032: los videos y simulaciones solo ofrecen la transcripción (excepción 2 del CU-032). */
    static boolean soloTranscripcion(Contenido c) {
        return c.getFormato() == FormatoContenido.VIDEO || c.getFormato() == FormatoContenido.SIMULACION;
    }

    /** Huella del texto: si cambia, lo generado ya no se reutiliza. */
    static String huella(Contenido c, String transcripcion) {
        return Hashes.sha256Hex(c.getTitulo() + "\n" + (c.getCuerpo() == null ? "" : c.getCuerpo()) + "\n"
                + (transcripcion == null ? "" : transcripcion));
    }

    @Transactional(readOnly = true)
    public FormatosRespuesta listar(UsuarioPrincipal u, Long contenidoId) {
        Contenido c = contenidos.accesible(u, contenidoId);
        if (soloTranscripcion(c)) {
            return new FormatosRespuesta(contenidoId, c.getTitulo(), true,
                    "Este contenido es un video o una simulación: usa su transcripción, que está en la pestaña de texto alternativo.",
                    List.of());
        }
        String h = huella(c, transcripcion(contenidoId));
        List<FormatoVista> lista = new ArrayList<>();
        for (Tipo t : Tipo.values()) {
            Optional<FormatoAlternativo> f = formatos.findByContenidoIdAndTipo(contenidoId, t);
            lista.add(vista(t, f.orElse(null), h));
        }
        return new FormatosRespuesta(contenidoId, c.getTitulo(), false, null, lista);
    }

    /** RF-032: pide un formato; si ya está generado y al día se reutiliza sin volver a procesarlo. */
    @Transactional
    public FormatoVista solicitar(UsuarioPrincipal u, Long contenidoId, Tipo tipo, String ip) {
        Contenido c = contenidos.accesible(u, contenidoId);
        if (soloTranscripcion(c)) {
            throw ApiException.conflicto("SOLO_TRANSCRIPCION", "Para videos y simulaciones usa la transcripción.");
        }
        if (c.getCuerpo() == null || c.getCuerpo().isBlank()) {
            throw ApiException.conflicto("SIN_TEXTO", "Este contenido no tiene texto para convertir.");
        }
        String h = huella(c, transcripcion(contenidoId));
        FormatoAlternativo f = formatos.findByContenidoIdAndTipo(contenidoId, tipo).orElseGet(() -> {
            FormatoAlternativo n = new FormatoAlternativo();
            n.setContenidoId(contenidoId);
            n.setTipo(tipo);
            return n;
        });
        if (alDia(f, h) || f.getEstado() == Estado.GENERANDO
                || (f.getEstado() == Estado.PENDIENTE && h.equals(f.getHuellaContenido()))) {
            return vista(tipo, f, h);
        }
        f.setEstado(Estado.PENDIENTE);
        f.setHuellaContenido(h);
        f.setError(null);
        f.setFechaSolicitud(LocalDateTime.now());
        f = formatos.save(f);
        auditoria.registrar(u.id(), "FORMATO_SOLICITADO", "contenidos/" + contenidoId + " " + tipo, ResultadoAuditoria.PERMITIDO, ip);
        eventos.publishEvent(new FormatoSolicitadoEvent(f.getId(), tipo, u.id()));
        return vista(tipo, f, h);
    }

    /** RF-032: entrega el archivo ya generado (sin procesar nada) y registra la descarga. */
    @Transactional(readOnly = true)
    public ArchivoGenerado descargar(UsuarioPrincipal u, Long contenidoId, Tipo tipo, String ip) {
        Contenido c = contenidos.accesible(u, contenidoId);
        String h = huella(c, transcripcion(contenidoId));
        FormatoAlternativo f = formatos.findByContenidoIdAndTipo(contenidoId, tipo)
                .filter(x -> alDia(x, h))
                .orElseThrow(() -> ApiException.conflicto("FORMATO_NO_LISTO", "Ese formato todavía no está listo. Pídelo primero."));
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(archivo(f));
        } catch (IOException e) {
            throw ApiException.conflicto("FORMATO_NO_LISTO", "No encontramos el archivo. Pídelo de nuevo.");
        }
        auditoria.registrar(u.id(), "FORMATO_DESCARGADO", "contenidos/" + contenidoId + " " + tipo, ResultadoAuditoria.PERMITIDO, ip);
        String nombre = com.elearning.platform.services.AudioContenidoService.nombreArchivo(c.getTitulo()) + "." + EXTENSIONES.get(tipo);
        return new ArchivoGenerado(nombre, MIME.get(tipo), bytes);
    }

    String transcripcion(Long contenidoId) {
        return recursos.findByContenidoIdOrderByIdAsc(contenidoId).stream()
                .filter(r -> r.isDisponible() && r.getTipo() == TipoRecursoAccesible.TRANSCRIPCION)
                .map(RecursoAccesible::getDescripcion).filter(d -> d != null && !d.isBlank())
                .findFirst().orElse(null);
    }

    boolean alDia(FormatoAlternativo f, String huella) {
        return f.getEstado() == Estado.LISTO && huella.equals(f.getHuellaContenido()) && f.getRutaArchivo() != null
                && Files.exists(archivo(f));
    }

    Path archivo(FormatoAlternativo f) {
        Path p = carpeta.resolve(f.getRutaArchivo()).normalize();
        if (!p.startsWith(carpeta)) throw new IllegalStateException("Ruta de formato fuera de la carpeta.");
        return p;
    }

    /** Guarda el archivo generado: formatos/{contenido}/{tipo}.{ext}. */
    String guardarArchivo(FormatoAlternativo f, byte[] bytes) {
        String relativa = f.getContenidoId() + "/" + f.getTipo().name().toLowerCase() + "." + EXTENSIONES.get(f.getTipo());
        Path destino = carpeta.resolve(relativa).normalize();
        try {
            Files.createDirectories(destino.getParent());
            Path tmp = Files.createTempFile(destino.getParent(), "gen", ".tmp");
            Files.write(tmp, bytes);
            Files.move(tmp, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el formato generado.", e);
        }
        return relativa;
    }

    private FormatoVista vista(Tipo t, FormatoAlternativo f, String huella) {
        String nombre = NOMBRES.get(t);
        if (f == null) return new FormatoVista(t.name(), nombre, "SIN_GENERAR", null, null, null);
        if (f.getEstado() == Estado.LISTO && !alDia(f, huella)) {
            return new FormatoVista(t.name(), nombre, "SIN_GENERAR", "El contenido cambió: pide el formato de nuevo.", null, null);
        }
        String mensaje = f.getEstado() == Estado.FALLIDO
                ? "No pudimos generar este formato. Prueba con otro; ya avisamos al instructor." : null;
        return new FormatoVista(t.name(), nombre, f.getEstado().name(), mensaje, f.getTamanoBytes(), f.getGeneradoEn());
    }
}
