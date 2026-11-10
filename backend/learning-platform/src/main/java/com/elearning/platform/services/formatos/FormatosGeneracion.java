package com.elearning.platform.services.formatos;

import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.FormatoAlternativo;
import com.elearning.platform.entity.FormatoAlternativo.Estado;
import com.elearning.platform.events.FormatoSolicitadoEvent;
import com.elearning.platform.repository.ContenidoRepository;
import com.elearning.platform.repository.FormatoAlternativoRepository;
import com.elearning.platform.services.AudioContenidoService;
import com.elearning.platform.services.NotificacionService;
import com.elearning.platform.services.voz.ServicioTextoAVoz;
import com.elearning.platform.util.TextoApoyo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * RF-032: genera en segundo plano el formato pedido y avisa al estudiante cuando está listo. Si
 * falla, informa el error (los demás formatos siguen disponibles) y avisa al instructor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FormatosGeneracion {

    private final FormatoAlternativoRepository formatos;
    private final ContenidoRepository contenidos;
    private final FormatosService servicio;
    private final ServicioTextoAVoz voz;
    private final ServicioBraille braille;
    private final NotificacionService notificaciones;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alSolicitar(FormatoSolicitadoEvent e) {
        generar(e.formatoId(), e.usuarioId());
    }

    void generar(Long formatoId, Long usuarioId) {
        FormatoAlternativo f = formatos.findById(formatoId).orElse(null);
        if (f == null || f.getEstado() == Estado.GENERANDO || f.getEstado() == Estado.LISTO) return;
        Contenido c = contenidos.findById(f.getContenidoId()).orElse(null);
        if (c == null) return;
        f.setEstado(Estado.GENERANDO);
        formatos.saveAndFlush(f);
        String nombre = FormatosService.NOMBRES.get(f.getTipo());
        try {
            byte[] bytes = producir(f.getTipo(), c, servicio.transcripcion(c.getId()));
            f.setRutaArchivo(servicio.guardarArchivo(f, bytes));
            f.setTamanoBytes((long) bytes.length);
            f.setEstado(Estado.LISTO);
            f.setGeneradoEn(LocalDateTime.now());
            formatos.save(f);
            notificaciones.notificar(usuarioId, nombre + " listo: «" + c.getTitulo() + "»",
                    "Ya puedes descargar «" + c.getTitulo() + "» como " + nombre.toLowerCase() + ".", false,
                    "/contenidos/" + c.getId());
        } catch (RuntimeException ex) {
            log.warn("No se pudo generar {} del contenido {}: {}", f.getTipo(), c.getId(), ex.getMessage());
            f.setEstado(Estado.FALLIDO);
            f.setError(ex.getMessage() == null ? "Error al convertir." : ex.getMessage().substring(0, Math.min(400, ex.getMessage().length())));
            formatos.save(f);
            notificaciones.notificar(usuarioId, "No pudimos generar " + nombre.toLowerCase(),
                    "Hubo un error al convertir «" + c.getTitulo() + "». Prueba con otro formato; ya avisamos al instructor.",
                    false, "/contenidos/" + c.getId());
            notificaciones.notificarUnaVezAlDia(c.getModulo().getCurso().getInstructor().getId(),
                    "Falló un formato alternativo de «" + c.getTitulo() + "»",
                    "No se pudo generar " + nombre.toLowerCase() + ". Revisa que el texto del contenido esté completo.",
                    "/cursos/" + c.getModulo().getCurso().getId() + "/editar");
        }
    }

    byte[] producir(FormatoAlternativo.Tipo tipo, Contenido c, String transcripcion) {
        return switch (tipo) {
            case HTML -> GeneradorFormatos.htmlAccesible(c.getTitulo(), c.getCuerpo(), transcripcion).getBytes(StandardCharsets.UTF_8);
            case EPUB -> GeneradorFormatos.epub(c.getTitulo(), c.getCuerpo(), transcripcion);
            case BRF -> braille.aBrf(textoPlano(c, transcripcion)).getBytes(StandardCharsets.US_ASCII);
            case MP3 -> mp3(textoPlano(c, transcripcion));
        };
    }

    /** Un solo MP3: las tramas MPEG de cada parte se pueden concatenar. */
    private byte[] mp3(String texto) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String parte : AudioContenidoService.dividir(texto, AudioContenidoService.PALABRAS_POR_PISTA)) {
            out.writeBytes(voz.sintetizar(parte, 1.0));
        }
        return out.toByteArray();
    }

    static String textoPlano(Contenido c, String transcripcion) {
        String cuerpo = TextoApoyo.limpiar(c.getCuerpo());
        return c.getTitulo() + ".\n" + cuerpo + (transcripcion == null || transcripcion.isBlank() ? "" : "\n" + transcripcion);
    }
}
