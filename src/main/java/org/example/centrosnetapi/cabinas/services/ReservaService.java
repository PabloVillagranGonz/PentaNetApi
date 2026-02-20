package org.example.centrosnetapi.cabinas.services;

import jakarta.transaction.Transactional;
import org.example.centrosnetapi.cabinas.models.Aula;
import org.example.centrosnetapi.cabinas.models.Reserva;
import org.example.centrosnetapi.cabinas.repositories.AulaRepository;
import org.example.centrosnetapi.cabinas.repositories.ReservaRepository;
import org.example.centrosnetapi.cabinas.repositories.UsuarioRepository;
import org.example.centrosnetapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AulaRepository aulaRepository;


    // ==========================================
    // 🚀 CREAR RESERVA
    // ==========================================
    @Transactional
    public Reserva crearReserva(Long usuarioId, Long aulaId, int duracionMin) {

        if (duracionMin < 30 || duracionMin > 60) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duración inválida"
            );
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        var secretaria = (User) auth.getPrincipal();

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no existe"
                        )
                );

        var aula = aulaRepository.findById(aulaId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Aula no existe"
                        )
                );

        if (!secretaria.getCenter().getId().equals(aula.getCenter().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes reservar aulas de otro centro"
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        // ==============================
        // 🔒 VALIDAR RESERVAS DEL ALUMNO
        // ==============================
        List<Reserva> reservasAlumno =
                reservaRepository.findAllByUsuarioIdAndFinRealIsNull(usuarioId);

        for (Reserva r : reservasAlumno) {

            if (r.getFin().isBefore(ahora)) {
                // 🔥 cerrar automáticamente si está vencida
                r.setFinReal(ahora);
                continue;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alumno ya tiene una reserva activa"
            );
        }

        // ==============================
        // 🔒 VALIDAR RESERVAS DEL AULA
        // ==============================
        List<Reserva> reservasAula =
                reservaRepository.findAllByAulaIdAndFinRealIsNull(aulaId);

        for (Reserva r : reservasAula) {

            if (r.getFin().isBefore(ahora)) {
                // 🔥 cerrar automáticamente si está vencida
                r.setFinReal(ahora);
                r.getAula().setInstrumentoActual(null);
                continue;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El aula ya está ocupada"
            );
        }

        // ==============================
        // ✅ CREAR NUEVA RESERVA
        // ==============================

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setAula(aula);
        reserva.setCenter(secretaria.getCenter());
        reserva.setInicio(ahora);
        reserva.setFin(ahora.plusMinutes(duracionMin));

        return reservaRepository.save(reserva);
    }


    // ==========================================
    // 🛑 FINALIZAR RESERVA
    // ==========================================
    @Transactional
    public void finalizarReservaPorAula(Long aulaId) {

        List<Reserva> reservas =
                reservaRepository.findAllByAulaIdAndFinRealIsNull(aulaId);

        if (reservas.isEmpty()) return;

        Reserva r = reservas.get(0); // Tomamos la primera válida

        LocalDateTime ahora = LocalDateTime.now();

        r.setFinReal(ahora);

        if (ahora.isBefore(r.getFin())) {
            r.setFinalizadaAntes(true);
        }

        r.getAula().setInstrumentoActual(null);
    }

    // ==========================================
    // 📊 RESERVAS ACTIVAS
    // ==========================================
    public List<Reserva> obtenerReservasActivas() {
        return reservaRepository.findByFinRealIsNull();
    }


    // ==========================================
    // ⏲ CIERRE AUTOMÁTICO
    // ==========================================
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void cerrarReservasVencidas() {

        List<Reserva> activas = reservaRepository.findByFinRealIsNull();
        LocalDateTime ahora = LocalDateTime.now();

        for (Reserva r : activas) {

            if (r.getFin().isBefore(ahora)) {

                r.setFinReal(ahora);

                Aula aula = r.getAula();
                aula.setInstrumentoActual(null);
            }
        }
    }


    // ==========================================
    // 📜 HISTORIAL
    // ==========================================
    public List<Reserva> obtenerHistorial() {
        return reservaRepository.findAllByOrderByInicioDesc();
    }

}