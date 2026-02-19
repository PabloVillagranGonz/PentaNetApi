package org.example.centrosnetapi.cabinas.repositories;

import org.example.centrosnetapi.cabinas.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByUsuarioIdAndFinRealIsNull(Integer usuarioId);

    Optional<Reserva> findByAulaIdAndFinRealIsNull(Long aulaId);

    List<Reserva> findByFinRealIsNull();

    List<Reserva> findAllByOrderByInicioDesc();

    long countByCenterIdAndInicioBetween(
            Long centerId,
            LocalDateTime inicio,
            LocalDateTime fin
    );
}
