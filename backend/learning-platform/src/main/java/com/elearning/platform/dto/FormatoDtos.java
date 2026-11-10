package com.elearning.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

/** RF-032: DTOs de formatos alternativos. */
public final class FormatoDtos {

    private FormatoDtos() {}

    /** estado: SIN_GENERAR, PENDIENTE, GENERANDO, LISTO o FALLIDO. */
    public record FormatoVista(String tipo, String nombre, String estado, String mensaje, Long tamanoBytes,
                               LocalDateTime generadoEn) {}

    /** soloTranscripcion: en videos y simulaciones solo se ofrece la transcripción (RF-019). */
    public record FormatosRespuesta(Long contenidoId, String titulo, boolean soloTranscripcion, String mensaje,
                                    List<FormatoVista> formatos) {}
}
