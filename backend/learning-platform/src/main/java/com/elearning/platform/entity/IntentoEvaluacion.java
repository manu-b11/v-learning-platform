package com.elearning.platform.entity;

import com.elearning.platform.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "intentos_evaluacion")
@Getter
@Setter
@NoArgsConstructor
public class IntentoEvaluacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id")
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(precision = 8, scale = 2)
    private BigDecimal puntaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoIntento estado = EstadoIntento.EN_PROGRESO;

    /** RF-042: minutos añadidos al intento por la ampliación que permite la acomodación (RF-041). */
    @Column(name = "minutos_ampliados", nullable = false)
    private Integer minutosAmpliados = 0;

    @PrePersist
    void antesDeGuardar() {
        if (fechaInicio == null) fechaInicio = LocalDateTime.now();
    }
}
