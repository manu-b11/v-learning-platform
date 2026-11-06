package com.elearning.platform.repository;

import com.elearning.platform.entity.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByModuloIdOrderByIdAsc(Long moduloId);

    @Query("select t from Tarea t join fetch t.modulo m join fetch m.curso where m.id in :moduloIds order by t.id asc")
    List<Tarea> deLosModulos(@Param("moduloIds") Collection<Long> moduloIds);

    /** RF-011: tareas de cursos publicados cuya fecha límite cae en el rango. */
    @Query("select t from Tarea t join fetch t.modulo m join fetch m.curso c "
            + "where t.fechaLimite between :desde and :hasta "
            + "and c.estado = com.elearning.platform.enums.EstadoCurso.PUBLICADO order by t.fechaLimite asc")
    List<Tarea> conFechaLimiteEntre(@Param("desde") java.time.LocalDateTime desde,
                                    @Param("hasta") java.time.LocalDateTime hasta);
}
