package com.elearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** RF-043: enlace de desbloqueo de un solo uso. Solo se guarda el hash del token. */
@Entity
@Table(name = "tokens_desbloqueo")
@Getter
@Setter
@NoArgsConstructor
public class TokenDesbloqueo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "token_hash", nullable = false, length = 128, unique = true)
    private String tokenHash;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean usado = false;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @PrePersist
    void antesDeGuardar() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    public boolean vigente() {
        return !usado && fechaExpiracion.isAfter(LocalDateTime.now());
    }
}
