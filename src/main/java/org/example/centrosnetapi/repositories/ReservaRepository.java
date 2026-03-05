package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Alumno con reserva activa
    List<Reserva> findAllByUsuario_IdAndFinRealIsNull(Long usuarioId);

    // Espacio con reserva activa
    List<Reserva> findAllByEspacio_IdAndFinRealIsNull(Long espacioId);

    // Para dashboard (más eficiente)
    boolean existsByEspacio_IdAndFinRealIsNullAndFinAfter(
            Long espacioId,
            LocalDateTime ahora
    );

    // Activas
    List<Reserva> findByFinRealIsNull();

    // Historial
    List<Reserva> findAllByOrderByInicioDesc();
}