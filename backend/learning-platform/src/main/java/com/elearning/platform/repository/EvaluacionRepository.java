package com.elearning.platform.repository;

import com.elearning.platform.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findByModuloIdOrderByIdAsc(Long moduloId);

    List<Evaluacion> findByModuloIdInOrderByIdAsc(Collection<Long> moduloIds);

    /** RF-011: evaluaciones de cursos publicados cuya fecha límite cae en el rango. */
    @org.springframework.data.jpa.repository.Query("select e from Evaluacion e join fetch e.modulo m join fetch m.curso c "
            + "where e.fechaLimite between :desde and :hasta "
            + "and c.estado = com.elearning.platform.enums.EstadoCurso.PUBLICADO order by e.fechaLimite asc")
    List<Evaluacion> conFechaLimiteEntre(@org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,
                                         @org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta);
}
