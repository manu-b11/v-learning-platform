package com.elearning.platform.entity;

import com.elearning.platform.enums.SeveridadHallazgo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** RF-049: hallazgo del verificador de accesibilidad de un contenido (informe por contenido). */
@Entity
@Table(name = "hallazgos_accesibilidad")
@Getter
@Setter
@NoArgsConstructor
public class HallazgoAccesibilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contenido_id", nullable = false)
    private Long contenidoId;

    @Column(nullable = false, length = 40)
    private String regla;

    @Column(name = "criterio_wcag", nullable = false, length = 20)
    private String criterioWcag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SeveridadHallazgo severidad;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "como_corregir", nullable = false, length = 500)
    private String comoCorregir;

    @Column(nullable = false)
    private boolean resuelto = false;

    @Column(name = "fecha_verificacion", nullable = false)
    private LocalDateTime fechaVerificacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    void antesDeGuardar() {
        if (fechaVerificacion == null) fechaVerificacion = LocalDateTime.now();
    }
}
