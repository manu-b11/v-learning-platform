package com.elearning.platform.repository;

import com.elearning.platform.entity.TokenDesbloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** RF-043: tokens de desbloqueo de cuenta. */
public interface TokenDesbloqueoRepository extends JpaRepository<TokenDesbloqueo, Long> {

    Optional<TokenDesbloqueo> findByTokenHash(String tokenHash);

    Optional<TokenDesbloqueo> findFirstByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    @Modifying
    @Query("update TokenDesbloqueo t set t.usado = true where t.usuarioId = :usuarioId and t.usado = false")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId);
}
