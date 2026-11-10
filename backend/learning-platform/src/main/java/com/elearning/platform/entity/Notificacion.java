package com.elearning.platform.entity;

import com.elearning.platform.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CanalNotificacion canal;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(name = "pospuesta_hasta")
    private LocalDateTime pospuestaHasta;

    /** Ruta de la aplicación a la que lleva el botón «Ir» (p. ej. /logros). Puede ser null. */
    @Column(length = 300)
    private String enlace;

    /** RF-047: guardada para el resumen diario (pospuesta hasta la hora del resumen). */
    @Column(name = "en_resumen", nullable = false)
    private boolean enResumen = false;

    /** RF-047: urgente (obligatoria o fecha límite en menos de 24 h): nunca va al resumen. */
    @Column(nullable = false)
    private boolean urgente = false;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }
}
