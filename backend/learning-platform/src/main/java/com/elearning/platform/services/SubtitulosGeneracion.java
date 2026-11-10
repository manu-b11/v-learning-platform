package com.elearning.platform.services;

import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.SubtituloBorrador;
import com.elearning.platform.entity.SubtituloBorrador.Estado;
import com.elearning.platform.events.SubtitulosSolicitadosEvent;
import com.elearning.platform.repository.ContenidoRepository;
import com.elearning.platform.repository.SubtituloBorradorRepository;
import com.elearning.platform.services.transcripcion.ServicioTranscripcion;
import com.elearning.platform.util.Vtt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-037: genera el borrador de subtítulos en segundo plano (@Async, después de confirmar la
 * transacción que lo pidió) y avisa al instructor cuando está listo o si falló.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubtitulosGeneracion {

    private final SubtituloBorradorRepository borradores;
    private final ContenidoRepository contenidos;
    private final ServicioTranscripcion transcripcion;
    private final NotificacionService notificaciones;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alSolicitar(SubtitulosSolicitadosEvent e) {
        generar(e.contenidoId(), e.instructorId());
    }

    void generar(Long contenidoId, Long instructorId) {
        SubtituloBorrador b = borradores.findByContenidoId(contenidoId).orElse(null);
        Contenido c = contenidos.findById(contenidoId).orElse(null);
        if (b == null || c == null || b.getEstado() == Estado.GENERANDO) return;
        b.setEstado(Estado.GENERANDO);
        borradores.saveAndFlush(b);
        String titulo = c.getTitulo();
        String enlace = "/cursos/" + c.getModulo().getCurso().getId() + "/editar";
        try {
            List<Vtt.Linea> lineas = transcripcion.transcribir(c.getUrlRecurso());
            if (lineas.isEmpty()) throw new IllegalStateException("El reconocimiento no devolvió líneas.");
            b.setVtt(Vtt.aVtt(lineas));
            b.setEstado(Estado.LISTO);
            b.setError(null);
            b.setFechaGeneracion(LocalDateTime.now());
            borradores.save(b);
            notificaciones.notificar(instructorId, "Borrador de subtítulos listo: «" + titulo + "»",
                    "Revisa y corrige el borrador de subtítulos de «" + titulo + "». Se publicará solo cuando lo apruebes.",
                    true, enlace);
        } catch (RuntimeException ex) {
            log.warn("No se pudo generar el borrador de subtítulos del contenido {}: {}", contenidoId, ex.getMessage());
            b.setEstado(Estado.FALLIDO);
            b.setError("El reconocimiento de voz falló. Sube un archivo .srt o .vtt con los subtítulos.");
            borradores.save(b);
            notificaciones.notificar(instructorId, "No pudimos generar los subtítulos de «" + titulo + "»",
                    "El reconocimiento de voz falló. Puedes subir un archivo .srt o .vtt desde el editor del contenido.",
                    true, enlace);
        }
    }
}
