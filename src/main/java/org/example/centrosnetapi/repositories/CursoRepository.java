package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    // =============================
    // 📚 Cursos por centro
    // =============================
    List<Curso> findByCentroId(Long centerId);

    @Query("""
    SELECT ac.curso
    FROM AsignaturaCurso ac
    WHERE ac.asignatura.id = :asignaturaId
""")
    Optional<Curso> findFirstByAsignaturasId(@Param("asignaturaId") Long asignaturaId);
}