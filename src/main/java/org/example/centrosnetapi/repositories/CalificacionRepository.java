package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {


    // Buscar una nota específica para no duplicar (usando la UNIQUE KEY de la base de datos)
    Optional<Calificacion> findByCriterioIdAndAlumnoId(Long criterioId, Long alumnoId);
}