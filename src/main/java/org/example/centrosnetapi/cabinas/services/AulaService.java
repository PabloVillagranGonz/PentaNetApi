package org.example.centrosnetapi.cabinas.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.cabinas.dtos.AulaDisponibilidadDTO;
import org.example.centrosnetapi.cabinas.dtos.AulaResponseDTO;
import org.example.centrosnetapi.cabinas.dtos.EstadisticasAulasDTO;
import org.example.centrosnetapi.cabinas.models.Aula;
import org.example.centrosnetapi.cabinas.models.EstadoAula;
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

    public List<Aula> obtenerLibres() {
        return aulaRepository.findByEstado(EstadoAula.libre);
    }

    public List<AulaDisponibilidadDTO> obtenerDisponibilidadPorCentro(Long centerId) {

        List<Aula> aulas = aulaRepository.findByCenterId(centerId);

        return aulas.stream().map(aula -> {

            Optional<Reserva> activa =
                    reservaRepository
                            .findByAulaIdAndFinRealIsNull(aula.getId())
                            .filter(r -> r.getFin().isAfter(LocalDateTime.now()));

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

    public List<AulaResponseDTO> obtenerAulasDashboard(Long centerId) {

        List<Aula> aulas = aulaRepository.findByCenterId(centerId);
        LocalDateTime ahora = LocalDateTime.now();

        return aulas.stream().map(aula -> {

            Optional<Reserva> reservaActiva =
                    reservaRepository
                            .findByAulaIdAndFinRealIsNull(aula.getId())
                            .filter(r -> r.getFin().isAfter(ahora));

            if (reservaActiva.isPresent()) {

                Reserva r = reservaActiva.get();

                return new AulaResponseDTO(
                        aula.getId(),
                        aula.getNumero(),
                        aula.getTipo(),
                        "ocupada",
                        aula.getInstrumentoActual() != null
                                ? aula.getInstrumentoActual().getName()
                                : null,
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
                    null,
                    null
            );

        }).toList();
    }

    public EstadisticasAulasDTO obtenerEstadisticas(Long centerId) {

        long total = aulaRepository.countByCenterId(centerId);
        long libres = aulaRepository.countByCenterIdAndEstado(centerId, EstadoAula.libre);
        long ocupadas = total - libres;

        long reservasHoy =
                reservaRepository.countByCenterIdAndInicioBetween(
                        centerId,
                        LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(23,59,59)
                );

        return new EstadisticasAulasDTO(total, libres, ocupadas, reservasHoy);
    }
}