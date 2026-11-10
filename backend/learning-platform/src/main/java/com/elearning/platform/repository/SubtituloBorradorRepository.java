package com.elearning.platform.repository;

import com.elearning.platform.entity.SubtituloBorrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** RF-037: borradores de subtítulos (uno por contenido). */
public interface SubtituloBorradorRepository extends JpaRepository<SubtituloBorrador, Long> {

    Optional<SubtituloBorrador> findByContenidoId(Long contenidoId);
}
