package com.elearning.platform.entity;

import com.elearning.platform.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "configuraciones_accesibilidad")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionAccesibilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_accesibilidad_id")
    private PerfilAccesibilidad perfilAccesibilidad;

    @Column(name = "alto_contraste", nullable = false)
    private boolean altoContraste = false;

    @Column(name = "tamano_fuente", nullable = false)
    private Integer tamanoFuente = 16;

    @Column(length = 100)
    private String tipografia;

    @Column(name = "espaciado_linea", precision = 5, scale = 2)
    private BigDecimal espaciadoLinea;

    @Column(name = "navegacion_teclado", nullable = false)
    private boolean navegacionTeclado = false;

    @Column(name = "lector_pantalla", nullable = false)
    private boolean lectorPantalla = false;

    @Column(nullable = false)
    private boolean subtitulos = false;

    @Column(nullable = false)
    private boolean transcripcion = false;

    @Column(name = "texto_a_voz", nullable = false)
    private boolean textoAVoz = false;

    @Column(name = "tiempo_adicional", nullable = false)
    private Integer tiempoAdicional = 0;

    /** RF-035: subrayar todos los enlaces para no depender del color (WCAG 1.4.1). */
    @Column(name = "subrayar_enlaces", nullable = false)
    private boolean subrayarEnlaces = false;

    /** RF-046: abrir las lecturas en modo lectura sin distracciones. */
    @Column(name = "modo_lectura_predeterminado", nullable = false)
    private boolean modoLectura = false;

    /** RF-034: anunciar mensajes y notificaciones nuevas al lector de pantalla. */
    @Column(name = "anunciar_mensajes", nullable = false)
    private boolean anunciarMensajes = true;

    @Column(name = "fecha_configuracion", nullable = false)
    private LocalDateTime fechaConfiguracion;

    @PrePersist
    void antesDeGuardar() {
        if (fechaConfiguracion == null) fechaConfiguracion = LocalDateTime.now();
    }
}
