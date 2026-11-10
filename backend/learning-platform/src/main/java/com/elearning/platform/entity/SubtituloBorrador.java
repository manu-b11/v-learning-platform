package com.elearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RF-037: borrador de subtítulos de un video o podcast (uno por contenido). Solo pasa a ser el
 * recurso de subtítulos/transcripción del contenido cuando el instructor lo aprueba.
 */
@Entity
@Table(name = "subtitulos_borrador")
@Getter
@Setter
@NoArgsConstructor
public class SubtituloBorrador {

    /** PENDIENTE → GENERANDO → LISTO (o FALLIDO) → APROBADO. */
    public enum Estado { PENDIENTE, GENERANDO, LISTO, FALLIDO, APROBADO }

    /** AUTOMATICO (reconocimiento de voz) o MANUAL (archivo .srt/.vtt subido). */
    public enum Origen { AUTOMATICO, MANUAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contenido_id", nullable = false, unique = true)
    private Long contenidoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Estado estado = Estado.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Origen origen = Origen.AUTOMATICO;

    /** Enlace del medio con el que se generó (si cambia, se ofrece generar otro). */
    @Column(name = "url_medio", length = 500)
    private String urlMedio;

    @Column(columnDefinition = "TEXT")
    private String vtt;

    @Column(length = 500)
    private String error;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "aprobado_por")
    private Long aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @PrePersist
    void antesDeGuardar() {
        if (fechaSolicitud == null) fechaSolicitud = LocalDateTime.now();
    }
}
