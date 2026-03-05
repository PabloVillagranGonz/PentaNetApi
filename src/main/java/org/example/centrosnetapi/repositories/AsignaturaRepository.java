package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {

    // ================= POR CENTRO =================

    List<Asignatura> findByCentroId(Long centroId);

    boolean existsByNombreAndCentroId(String nombre, Long centroId);

    boolean existsByNombreAndCentroIdAndIdNot(
            String nombre,
            Long centroId,
            Long id
    );

    // ================= POR PROFESOR =================

    @Query("""
        SELECT DISTINCT s.asignatura
        FROM SesionClase s
        WHERE s.profesor.id = :profesorId
    """)
    List<Asignatura> findSubjectsByTeacherId(
            @Param("profesorId") Long profesorId
    );
}