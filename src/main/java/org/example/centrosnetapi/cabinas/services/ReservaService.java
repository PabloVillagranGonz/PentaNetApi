package org.example.centrosnetapi.cabinas.services;

import org.example.centrosnetapi.cabinas.models.Aula;
import org.example.centrosnetapi.cabinas.models.EstadoAula;
import org.example.centrosnetapi.cabinas.models.Reserva;
import org.example.centrosnetapi.cabinas.repositories.AulaRepository;
import org.example.centrosnetapi.cabinas.repositories.ReservaRepository;
import org.example.centrosnetapi.cabinas.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AulaRepository aulaRepository;

    public Reserva crearReserva(Integer usuarioId, Long aulaId, int duracionMin) {

        if (duracionMin < 30 || duracionMin > 60) {
            throw new RuntimeException("Duración inválida");
        }

        // 🔎 Obtener usuario
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        // 🔎 Obtener aula y verificar que esté libre
        var aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RuntimeException("Aula no existe"));

        if (aula.getEstado() != EstadoAula.libre) {
            throw new RuntimeException("Aula ocupada");
        }

        // 🔐 VALIDACIÓN IMPORTANTE
        // El alumno debe pertenecer al mismo centro que el aula
        if (!usuario.getCenter().getId().equals(aula.getCenter().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El alumno no pertenece a este centro"
            );
        }

        // 🚫 El alumno no puede tener reserva activa
        reservaRepository.findByUsuarioIdAndFinRealIsNull(usuarioId)
                .ifPresent(r -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "El alumno ya tiene una reserva activa"
                    );
                });

        // 🏗 Crear reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setAula(aula);

        // 🔥 CLAVE: asignar el centro (esto evita tu error SQL)
        reserva.setCenter(aula.getCenter());

        LocalDateTime ahora = LocalDateTime.now();
        reserva.setInicio(ahora);
        reserva.setFin(ahora.plusMinutes(duracionMin));

        // 🔄 Actualizar estado del aula
        aula.setEstado(EstadoAula.ocupada);
        aula.setInstrumentoActual(usuario.getInstrument());

        aulaRepository.save(aula);

        return reservaRepository.save(reserva);
    }

    public void finalizarReservaPorAula(Long aulaId) {

        Optional<Reserva> optionalReserva =
                reservaRepository.findByAulaIdAndFinRealIsNull(aulaId);

        if (optionalReserva.isEmpty()) {
            // No hay reserva activa → no hacemos nada
            return;
        }

        Reserva r = optionalReserva.get();

        r.setFinReal(LocalDateTime.now());

        if (LocalDateTime.now().isBefore(r.getFin())) {
            r.setFinalizadaAntes(true);
        }

        Aula aula = r.getAula();
        aula.setEstado(EstadoAula.libre);
        aula.setInstrumentoActual(null);

        aulaRepository.save(aula);
        reservaRepository.save(r);
    }
    public List<Reserva> obtenerReservasActivas() {
        return reservaRepository.findByFinRealIsNull();
    }

    @Scheduled(fixedRate = 10000)
    public void cerrarReservasVencidas() {

        List<Reserva> activas = reservaRepository.findByFinRealIsNull();

        LocalDateTime ahora = LocalDateTime.now();

        for (Reserva r : activas) {

            if (r.getFin().isBefore(ahora)) {

                r.setFinReal(ahora);

                Aula aula = r.getAula();
                aula.setEstado(EstadoAula.libre);
                aula.setInstrumentoActual(null);

                aulaRepository.save(aula);
                reservaRepository.save(r);
            }
        }
    }
    public List<Reserva> obtenerHistorial() {
        return reservaRepository.findAllByOrderByInicioDesc();
    }
}
