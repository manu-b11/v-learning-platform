package com.elearning.platform.dto;

import com.elearning.platform.enums.EstadoMensaje;
import com.elearning.platform.enums.RolUsuario;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/** DTOs de la comunidad del curso (RF-010). */
public final class ChatDtos {

    private ChatDtos() {}

    /**
     * idCliente (opcional): identificador que genera el navegador para cada mensaje. Si el mensaje se reenvía
     * desde la cola (RF-010) con el mismo idCliente, el servidor no lo duplica.
     */
    public record MensajeRequest(@NotBlank(message = "Escribe un mensaje.") String contenido,
                                 @jakarta.validation.constraints.Size(max = 64) String idCliente) {}

    /** Configuración de la comunidad del curso (RF-010). */
    public record ComunidadConfig(Long cursoId, boolean colaReenvio, boolean puedeConfigurar) {}

    public record ComunidadConfigRequest(@jakarta.validation.constraints.NotNull(message = "Indica si la cola de reenvío queda activa.")
                                         Boolean colaReenvio) {}

    /** Mensaje reportado pendiente de la decisión del instructor. */
    public record MensajeReportado(Long id, Long cursoId, String curso, Long autorId, String autor, RolUsuario rolAutor,
                                   String contenido, LocalDateTime fechaEnvio, LocalDateTime fechaReporte,
                                   java.util.List<String> reportadoPor) {}

    public record MensajeDto(Long id, Long cursoId, Long usuarioId, String autor, RolUsuario rol, String contenido,
                             LocalDateTime fechaEnvio, EstadoMensaje estado) {}
}
