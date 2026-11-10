package com.elearning.platform.repository;

import com.elearning.platform.entity.HallazgoAccesibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** RF-049: informe del verificador de accesibilidad por contenido. */
public interface HallazgoAccesibilidadRepository extends JpaRepository<HallazgoAccesibilidad, Long> {

    List<HallazgoAccesibilidad> findByContenidoIdAndResueltoFalseOrderByIdAsc(Long contenidoId);
}
