package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.AsignaturaCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignaturaCursoRepository
        extends JpaRepository<AsignaturaCurso, Long> {

    List<AsignaturaCurso> findByCurso_Id(Long cursoId);

    boolean existsByCursoIdAndAsignaturaId(Long cursoId, Long asignaturaId);

}