package com.elearning.platform.repository;

import com.elearning.platform.entity.Notificacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** RF-047: notificaciones guardadas para el resumen cuya hora ya llegó. */
    @Query("select n from Notificacion n join fetch n.usuario where n.enResumen = true and n.pospuestaHasta <= :ahora "
            + "order by n.usuario.id, n.fechaCreacion")
    List<Notificacion> paraResumen(@Param("ahora") LocalDateTime ahora);

    /** RF-047: al cambiar la hora del resumen, lo pendiente pasa a la nueva hora. */
    @Modifying
    @Query("update Notificacion n set n.pospuestaHasta = :hora where n.usuario.id = :usuarioId and n.enResumen = true")
    int reprogramarResumen(@Param("usuarioId") Long usuarioId, @Param("hora") LocalDateTime hora);

    /** RF-047: si se desactiva el resumen, lo pendiente se muestra de inmediato. */
    @Modifying
    @Query("update Notificacion n set n.pospuestaHasta = null, n.enResumen = false "
            + "where n.usuario.id = :usuarioId and n.enResumen = true")
    int liberarResumen(@Param("usuarioId") Long usuarioId);

    /** Notificaciones visibles: las pospuestas no aparecen hasta su fecha (RF-011). */
    @Query("select n from Notificacion n where n.usuario.id = :usuarioId "
            + "and (n.pospuestaHasta is null or n.pospuestaHasta <= :ahora) "
            + "order by n.fechaCreacion desc")
    List<Notificacion> visibles(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora,
                                Pageable pageable);

    @Query("select count(n) from Notificacion n where n.usuario.id = :usuarioId and n.leida = false "
            + "and (n.pospuestaHasta is null or n.pospuestaHasta <= :ahora)")
    long contarNoLeidas(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);

    /** Marca como leídas todas las notificaciones visibles del usuario. */
    @Modifying
    @Query("update Notificacion n set n.leida = true, n.fechaLectura = :ahora "
            + "where n.usuario.id = :usuarioId and n.leida = false "
            + "and (n.pospuestaHasta is null or n.pospuestaHasta <= :ahora)")
    int marcarTodasLeidas(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);

    /** Evita repetir el mismo recordatorio el mismo día. */
    @Query("select count(n) from Notificacion n where n.usuario.id = :usuarioId and n.titulo = :titulo "
            + "and n.fechaCreacion >= :desde")
    long contarPorTituloDesde(@Param("usuarioId") Long usuarioId, @Param("titulo") String titulo,
                              @Param("desde") LocalDateTime desde);
}
