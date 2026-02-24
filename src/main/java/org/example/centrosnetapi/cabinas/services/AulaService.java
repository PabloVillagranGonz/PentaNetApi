package org.example.centrosnetapi.cabinas.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.cabinas.dtos.AulaDisponibilidadDTO;
import org.example.centrosnetapi.cabinas.dtos.AulaResponseDTO;
import org.example.centrosnetapi.cabinas.dtos.EstadisticasAulasDTO;
import org.example.centrosnetapi.cabinas.models.Aula;
import org.example.centrosnetapi.cabinas.models.Reserva;
import org.example.centrosnetapi.cabinas.repositories.AulaRepository;
import org.example.centrosnetapi.cabinas.repositories.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final ReservaRepository reservaRepository;

    public List<Aula> obtenerTodas() {
        return aulaRepository.findAll();
    }

    // ==========================================
    // 📊 DISPONIBILIDAD
    // ==========================================
    public List<AulaDisponibilidadDTO> obtenerDisponibilidadPorCentro(Long centerId) {

        List<Aula> aulas = aulaRepository.findByCenterId(centerId);
        LocalDateTime ahora = LocalDateTime.now();

        return aulas.stream().map(aula -> {

            List<Reserva> reservas =
                    reservaRepository.findAllByAulaIdAndFinRealIsNull(aula.getId());

            Optional<Reserva> activa = reservas.stream()
                    .filter(r -> r.getFin().isAfter(ahora))
                    .findFirst();

            if (activa.isPresent()) {
                return new AulaDisponibilidadDTO(
                        aula.getNumero(),
                        "ocupada",
                        activa.get().getFin()
                );
            }

            return new AulaDisponibilidadDTO(
                    aula.getNumero(),
                    "libre",
                    null
            );

        }).toList();
    }

    // ==========================================
    // 🎹 DASHBOARD
    // ==========================================
    public List<AulaResponseDTO> obtenerAulasDashboard(Long centerId) {

        List<Aula> aulas = aulaRepository.findByCenterId(centerId);
        LocalDateTime ahora = LocalDateTime.now();

        return aulas.stream().map(aula -> {

            List<Reserva> reservas =
                    reservaRepository.findAllByAulaIdAndFinRealIsNull(aula.getId());

            Optional<Reserva> activa = reservas.stream()
                    .filter(r -> r.getFin().isAfter(ahora))
                    .findFirst();

            if (activa.isPresent()) {

                Reserva r = activa.get();

                return new AulaResponseDTO(
                        aula.getId(),
                        aula.getNumero(),
                        aula.getTipo(),
                        "ocupada",
                        r.getUsuario().getNombre() + " " + r.getUsuario().getApellidos(),
                        r.getInicio(),
                        r.getFin()
                );
            }

            return new AulaResponseDTO(
                    aula.getId(),
                    aula.getNumero(),
                    aula.getTipo(),
                    "libre",
                    null,
                    null,
                    null
            );

        }).toList();
    }

    // ==========================================
    // 📈 ESTADÍSTICAS
    // ==========================================
    public EstadisticasAulasDTO obtenerEstadisticas(Long centerId) {

        List<Aula> aulas = aulaRepository.findByCenterId(centerId);
        LocalDateTime ahora = LocalDateTime.now();

        long total = aulas.size();

        long ocupadas = aulas.stream()
                .filter(aula -> {

                    List<Reserva> reservas =
                            reservaRepository.findAllByAulaIdAndFinRealIsNull(aula.getId());

                    return reservas.stream()
                            .anyMatch(r -> r.getFin().isAfter(ahora));
                })
                .count();

        long libres = total - ocupadas;

        long reservasHoy =
                reservaRepository.countByCenterIdAndInicioBetween(
                        centerId,
                        LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59)
                );

        return new EstadisticasAulasDTO(total, libres, ocupadas, reservasHoy);
    }
}