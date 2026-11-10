package com.elearning.platform.repository;

import com.elearning.platform.entity.Acomodacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** RF-041: acomodaciones de evaluación. */
public interface AcomodacionRepository extends JpaRepository<Acomodacion, Long> {

    @Query("select a from Acomodacion a join fetch a.inscripcion i join fetch i.estudiante "
            + "where i.curso.id = :cursoId and a.activa = true order by i.estudiante.nombre")
    List<Acomodacion> activasDelCurso(@Param("cursoId") Long cursoId);

    @Query("select a from Acomodacion a where a.inscripcion.estudiante.id = :estudianteId "
            + "and a.inscripcion.curso.id = :cursoId and a.activa = true")
    Optional<Acomodacion> activaDe(@Param("estudianteId") Long estudianteId, @Param("cursoId") Long cursoId);

    Optional<Acomodacion> findFirstByInscripcionIdAndActivaTrue(Long inscripcionId);
}
