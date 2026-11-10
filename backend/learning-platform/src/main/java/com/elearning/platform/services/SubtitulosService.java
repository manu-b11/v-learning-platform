package com.elearning.platform.services;

import com.elearning.platform.dto.SubtitulosDtos.BorradorVista;
import com.elearning.platform.dto.SubtitulosDtos.LineaDto;
import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.RecursoAccesible;
import com.elearning.platform.entity.SubtituloBorrador;
import com.elearning.platform.entity.SubtituloBorrador.Estado;
import com.elearning.platform.entity.SubtituloBorrador.Origen;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.TipoRecursoAccesible;
import com.elearning.platform.events.SubtitulosSolicitadosEvent;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.RecursoAccesibleRepository;
import com.elearning.platform.repository.SubtituloBorradorRepository;
import com.elearning.platform.util.Vtt;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RF-037: borrador de subtítulos para revisión del instructor. Se genera en segundo plano
 * (SubtitulosGeneracion), se corrige línea por línea o se reemplaza con un .srt/.vtt subido, y
 * solo al aprobarlo se convierte en el recurso de subtítulos (video) y de transcripción (video y
 * podcast). Antes de la aprobación el contenido sigue sin poder publicarse.
 */
@Service
@RequiredArgsConstructor
public class SubtitulosService {

    /** RF-037: tamaño máximo del archivo de subtítulos subido. */
    static final long TAMANO_MAXIMO = 1_000_000;
    /** Ruta pública del VTT aprobado (el navegador la carga en el &lt;track&gt; sin cabecera de sesión). */
    static final String RUTA_VTT = "/api/archivos/subtitulos/%d.vtt";

    private final SubtituloBorradorRepository borradores;
    private final RecursoAccesibleRepository recursos;
    private final AccesoService acceso;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;

    /** RF-037: estado actual del borrador del contenido (SIN_BORRADOR si no hay). */
    @Transactional(readOnly = true)
    public BorradorVista ver(Long contenidoId, Long instructorId) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        return borradores.findByContenidoId(contenidoId).map(b -> vista(b, c))
                .orElse(new BorradorVista(contenidoId, "SIN_BORRADOR", null, null, List.of(), false, null, null));
    }

    /** RF-037: pide un borrador automático (al subir el medio o a mano). Se genera en segundo plano. */
    @Transactional
    public BorradorVista solicitar(Long contenidoId, Long instructorId, String ip) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        exigirMedio(c);
        SubtituloBorrador b = borradores.findByContenidoId(contenidoId).orElseGet(SubtituloBorrador::new);
        if (b.getEstado() == Estado.GENERANDO) return vista(b, c);
        b.setContenidoId(contenidoId);
        b.setEstado(Estado.PENDIENTE);
        b.setOrigen(Origen.AUTOMATICO);
        b.setUrlMedio(c.getUrlRecurso());
        b.setError(null);
        b.setFechaSolicitud(LocalDateTime.now());
        borradores.save(b);
        auditoria.registrar(instructorId, "SUBTITULOS_SOLICITADOS", "contenidos/" + contenidoId, ResultadoAuditoria.PERMITIDO, ip);
        eventos.publishEvent(new SubtitulosSolicitadosEvent(contenidoId, instructorId));
        return vista(b, c);
    }

    /**
     * RF-037: al guardar un video o podcast con un enlace nuevo se pide el borrador automático,
     * salvo que ya tenga subtítulos (video) o transcripción (podcast) o un borrador para ese enlace.
     */
    @Transactional
    public void alGuardarContenido(Contenido c, Long instructorId) {
        if (!esMedio(c) || c.getUrlRecurso() == null || c.getUrlRecurso().isBlank()) return;
        TipoRecursoAccesible necesario = c.getFormato() == FormatoContenido.VIDEO
                ? TipoRecursoAccesible.SUBTITULO : TipoRecursoAccesible.TRANSCRIPCION;
        boolean yaTiene = recursos.findByContenidoIdOrderByIdAsc(c.getId()).stream()
                .anyMatch(r -> r.isDisponible() && r.getTipo() == necesario);
        Optional<SubtituloBorrador> b = borradores.findByContenidoId(c.getId());
        boolean mismoMedio = b.isPresent() && c.getUrlRecurso().equals(b.get().getUrlMedio());
        if (yaTiene || mismoMedio) return;
        SubtituloBorrador nuevo = b.orElseGet(SubtituloBorrador::new);
        nuevo.setContenidoId(c.getId());
        nuevo.setEstado(Estado.PENDIENTE);
        nuevo.setOrigen(Origen.AUTOMATICO);
        nuevo.setUrlMedio(c.getUrlRecurso());
        nuevo.setFechaSolicitud(LocalDateTime.now());
        borradores.save(nuevo);
        eventos.publishEvent(new SubtitulosSolicitadosEvent(c.getId(), instructorId));
    }

    /** RF-037: guarda las correcciones del instructor (texto y tiempos). */
    @Transactional
    public BorradorVista editar(Long contenidoId, Long instructorId, List<LineaDto> lineas, String ip) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        List<Vtt.Linea> limpias = lineas.stream()
                .map(l -> new Vtt.Linea(l.inicioMs(), l.finMs(), l.texto() == null ? "" : l.texto().trim())).toList();
        exigirValidas(limpias);
        SubtituloBorrador b = borradores.findByContenidoId(contenidoId).orElseGet(() -> {
            SubtituloBorrador n = new SubtituloBorrador();
            n.setContenidoId(contenidoId);
            n.setOrigen(Origen.MANUAL);
            n.setUrlMedio(c.getUrlRecurso());
            return n;
        });
        if (b.getEstado() == Estado.GENERANDO) {
            throw ApiException.conflicto("SUBTITULOS_GENERANDO", "Espera a que termine el borrador automático para editarlo.");
        }
        b.setVtt(Vtt.aVtt(limpias));
        b.setEstado(Estado.LISTO); // editar un borrador aprobado obliga a aprobarlo de nuevo
        b.setError(null);
        borradores.save(b);
        auditoria.registrar(instructorId, "SUBTITULOS_EDITADOS", "contenidos/" + contenidoId, ResultadoAuditoria.PERMITIDO, ip);
        return vista(b, c);
    }

    /** RF-037: si el reconocimiento falla, el instructor sube un .srt o .vtt (queda como borrador por aprobar). */
    @Transactional
    public BorradorVista subirArchivo(Long contenidoId, Long instructorId, MultipartFile archivo, String ip) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        exigirMedio(c);
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.solicitudInvalida("ARCHIVO_VACIO", "Selecciona un archivo .srt o .vtt.");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        if (!(nombre.endsWith(".srt") || nombre.endsWith(".vtt"))) {
            throw ApiException.solicitudInvalida("ARCHIVO_FORMATO", "El archivo debe ser .srt o .vtt.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw ApiException.solicitudInvalida("ARCHIVO_MUY_GRANDE", "El archivo de subtítulos no puede superar 1 MB.");
        }
        List<Vtt.Linea> lineas;
        try {
            lineas = Vtt.leer(new String(archivo.getBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw ApiException.solicitudInvalida("ARCHIVO_ILEGIBLE", "No pudimos leer el archivo.");
        }
        if (lineas.isEmpty()) {
            throw ApiException.solicitudInvalida("ARCHIVO_SIN_LINEAS", "El archivo no tiene líneas de subtítulos con tiempo.");
        }
        SubtituloBorrador b = borradores.findByContenidoId(contenidoId).orElseGet(SubtituloBorrador::new);
        b.setContenidoId(contenidoId);
        b.setOrigen(Origen.MANUAL);
        b.setUrlMedio(c.getUrlRecurso());
        b.setVtt(Vtt.aVtt(lineas));
        b.setEstado(Estado.LISTO);
        b.setError(null);
        b.setFechaGeneracion(LocalDateTime.now());
        borradores.save(b);
        auditoria.registrar(instructorId, "SUBTITULOS_SUBIDOS", "contenidos/" + contenidoId, ResultadoAuditoria.PERMITIDO, ip);
        return vista(b, c);
    }

    /**
     * RF-037: el instructor aprueba. En un video, el VTT pasa a ser el recurso de subtítulos; en
     * video y podcast, su texto completa la transcripción si aún no hay. Solo entonces se habilita
     * la publicación (RF-019, RF-039).
     */
    @Transactional
    public BorradorVista aprobar(Long contenidoId, Long instructorId, String ip) {
        Contenido c = acceso.contenidoPropio(contenidoId, instructorId);
        SubtituloBorrador b = borradores.findByContenidoId(contenidoId)
                .orElseThrow(() -> ApiException.noEncontrado("Este contenido no tiene borrador de subtítulos."));
        if (b.getEstado() != Estado.LISTO) {
            throw ApiException.conflicto("BORRADOR_NO_LISTO", "Solo se aprueba un borrador listo para revisar.");
        }
        List<Vtt.Linea> lineas = Vtt.leer(b.getVtt());
        exigirValidas(lineas);
        List<RecursoAccesible> existentes = recursos.findByContenidoIdOrderByIdAsc(contenidoId);
        if (c.getFormato() == FormatoContenido.VIDEO) {
            String url = String.format(RUTA_VTT, contenidoId);
            RecursoAccesible sub = existentes.stream().filter(r -> r.getTipo() == TipoRecursoAccesible.SUBTITULO
                    && url.equals(r.getUrl())).findFirst().orElseGet(() -> nuevoRecurso(c, TipoRecursoAccesible.SUBTITULO));
            sub.setUrl(url);
            sub.setDescripcion("Subtítulos revisados y aprobados por el instructor.");
            sub.setDisponible(true);
            recursos.save(sub);
        }
        boolean tieneTranscripcion = existentes.stream().anyMatch(r -> r.isDisponible()
                && r.getTipo() == TipoRecursoAccesible.TRANSCRIPCION);
        if (!tieneTranscripcion) {
            RecursoAccesible t = nuevoRecurso(c, TipoRecursoAccesible.TRANSCRIPCION);
            t.setUrl("interno:texto");
            t.setDescripcion(Vtt.aTexto(lineas));
            recursos.save(t);
        }
        b.setEstado(Estado.APROBADO);
        b.setAprobadoPor(instructorId);
        b.setFechaAprobacion(LocalDateTime.now());
        borradores.save(b);
        auditoria.registrar(instructorId, "SUBTITULOS_APROBADOS", "contenidos/" + contenidoId, ResultadoAuditoria.PERMITIDO, ip);
        return vista(b, c);
    }

    /** RF-037/038: VTT aprobado para el &lt;track&gt; y la transcripción sincronizada. Vacío si no está aprobado. */
    @Transactional(readOnly = true)
    public Optional<String> vttAprobado(Long contenidoId) {
        return borradores.findByContenidoId(contenidoId).filter(b -> b.getEstado() == Estado.APROBADO)
                .map(SubtituloBorrador::getVtt);
    }

    static boolean esMedio(Contenido c) {
        return c.getFormato() == FormatoContenido.VIDEO || c.getFormato() == FormatoContenido.PODCAST;
    }

    private static void exigirMedio(Contenido c) {
        if (!esMedio(c)) {
            throw ApiException.solicitudInvalida("SIN_MEDIO", "Los subtítulos se generan para videos y podcasts.");
        }
        if (c.getUrlRecurso() == null || c.getUrlRecurso().isBlank()) {
            throw ApiException.solicitudInvalida("SIN_ENLACE", "Guarda primero el enlace del video o del podcast.");
        }
    }

    private static void exigirValidas(List<Vtt.Linea> lineas) {
        List<String> errores = Vtt.validar(lineas);
        if (!errores.isEmpty()) throw ApiException.solicitudInvalida("SUBTITULOS_INVALIDOS", String.join(" ", errores));
    }

    private static RecursoAccesible nuevoRecurso(Contenido c, TipoRecursoAccesible tipo) {
        RecursoAccesible r = new RecursoAccesible();
        r.setContenido(c);
        r.setTipo(tipo);
        r.setDisponible(true);
        return r;
    }

    static BorradorVista vista(SubtituloBorrador b, Contenido c) {
        List<LineaDto> lineas = b.getVtt() == null ? List.of()
                : Vtt.leer(b.getVtt()).stream().map(l -> new LineaDto(l.inicioMs(), l.finMs(), l.texto())).toList();
        boolean cambio = b.getUrlMedio() != null && c.getUrlRecurso() != null && !b.getUrlMedio().equals(c.getUrlRecurso());
        return new BorradorVista(b.getContenidoId(), b.getEstado().name(), b.getOrigen().name(), b.getError(), lineas,
                cambio, b.getFechaGeneracion(), b.getFechaAprobacion());
    }
}
