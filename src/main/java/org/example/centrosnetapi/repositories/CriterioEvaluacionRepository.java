package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.CriterioEvaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriterioEvaluacionRepository extends JpaRepository<CriterioEvaluacion, Long> {

    List<CriterioEvaluacion> findByAsignaturaIdAndCursoId(Long asignaturaId, Long cursoId);
}