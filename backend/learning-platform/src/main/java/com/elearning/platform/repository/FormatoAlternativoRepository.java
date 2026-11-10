package com.elearning.platform.repository;

import com.elearning.platform.entity.FormatoAlternativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** RF-032: formatos alternativos generados. */
public interface FormatoAlternativoRepository extends JpaRepository<FormatoAlternativo, Long> {

    Optional<FormatoAlternativo> findByContenidoIdAndTipo(Long contenidoId, FormatoAlternativo.Tipo tipo);

    List<FormatoAlternativo> findByContenidoId(Long contenidoId);
}
