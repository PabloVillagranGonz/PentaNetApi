package org.example.centrosnetapi.cabinas.repositories;

import org.example.centrosnetapi.cabinas.models.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    List<Aula> findByCenterId(Long centerId);

    long countByCenterId(Long centerId);
}
