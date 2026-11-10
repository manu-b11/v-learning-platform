package com.elearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RF-032: versión alternativa generada de un contenido (MP3, EPUB, HTML accesible o BRF). Se
 * reutiliza mientras el contenido no cambie (huella del texto); si cambia, se genera de nuevo.
 */
@Entity
@Table(name = "formatos_alternativos")
@Getter
@Setter
@NoArgsConstructor
public class FormatoAlternativo {

    public enum Tipo { MP3, EPUB, HTML, BRF }

    public enum Estado { PENDIENTE, GENERANDO, LISTO, FALLIDO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contenido_id", nullable = false)
    private Long contenidoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Estado estado = Estado.PENDIENTE;

    /** Ruta del archivo generado, relativa a la carpeta de formatos. */
    @Column(name = "ruta_archivo", length = 300)
    private String rutaArchivo;

    /** Huella SHA-256 del texto con el que se generó. */
    @Column(name = "huella_contenido", length = 64)
    private String huellaContenido;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(length = 500)
    private String error;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "generado_en")
    private LocalDateTime generadoEn;

    @PrePersist
    void antesDeGuardar() {
        if (fechaSolicitud == null) fechaSolicitud = LocalDateTime.now();
    }
}
