package org.example.centrosnetapi.cabinas.repositories;

import org.example.centrosnetapi.cabinas.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // 🔥 Alumno
    List<Reserva> findAllByUsuarioIdAndFinRealIsNull(Long usuarioId);

    // 🔥 Aula
    List<Reserva> findAllByAulaIdAndFinRealIsNull(Long aulaId);

    // 🔥 Todas las activas
    List<Reserva> findByFinRealIsNull();

    // 🔥 Historial
    List<Reserva> findAllByOrderByInicioDesc();

    // 🔥 Estadísticas
    long countByCenterIdAndInicioBetween(
            Long centerId,
            LocalDateTime inicio,
            LocalDateTime fin
    );
}