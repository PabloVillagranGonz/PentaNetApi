package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.AsignaturaCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AsignaturaCursoRepository
        extends JpaRepository<AsignaturaCurso, Long> {

    List<AsignaturaCurso> findByCurso_Id(Long cursoId);

    boolean existsByCursoIdAndAsignaturaId(Long cursoId, Long asignaturaId);

    @Modifying
    @Query("DELETE FROM AsignaturaCurso ac WHERE ac.curso.id = :cursoId")
    void deleteByCursoId(Long cursoId);

    boolean existsByAsignaturaId(Long asignaturaId);

    boolean existsByCursoId(Long cursoId);

    Optional<AsignaturaCurso> findByCursoIdAndAsignaturaId(Long cursoId, Long asignaturaId);
}