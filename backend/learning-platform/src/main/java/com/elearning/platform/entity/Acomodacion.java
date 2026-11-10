package com.elearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RF-041: acomodaciones de evaluación de un estudiante en un curso. Se aplican a todas las
 * evaluaciones del curso, también a las que se creen después. Solo una activa por inscripción.
 */
@Entity
@Table(name = "acomodaciones")
@Getter
@Setter
@NoArgsConstructor
public class Acomodacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;

    /** 1.50 o 2.00; null = sin multiplicador. */
    @Column(name = "multiplicador_tiempo", precision = 3, scale = 2)
    private BigDecimal multiplicadorTiempo;

    @Column(name = "tiempo_ilimitado", nullable = false)
    private boolean tiempoIlimitado = false;

    /** Se guarda; no tiene efecto mientras las evaluaciones no tengan máximo de intentos. */
    @Column(name = "intentos_extra", nullable = false)
    private Integer intentosExtra = 0;

    /** Fecha límite propia para iniciar las evaluaciones, si es posterior a la de la evaluación. */
    @Column(name = "fecha_limite_especial")
    private LocalDateTime fechaLimiteEspecial;

    /** RF-042: minutos que el estudiante puede pedir una vez por intento al recibir el aviso. */
    @Column(name = "minutos_ampliacion", nullable = false)
    private Integer minutosAmpliacion = 0;

    /** Último día en que se aplica (inclusive); null = sin vencimiento. */
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @Column(name = "creada_por", nullable = false)
    private Long creadaPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private boolean activa = true;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    /** RF-041: vencida si ya pasó el último día de vigencia. */
    public boolean vencida(LocalDate hoy) {
        return vigenteHasta != null && hoy.isAfter(vigenteHasta);
    }
}
