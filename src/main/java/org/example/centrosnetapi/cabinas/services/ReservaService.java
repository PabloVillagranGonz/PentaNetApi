package org.example.centrosnetapi.cabinas.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.cabinas.models.Aula;
import org.example.centrosnetapi.cabinas.models.Reserva;
import org.example.centrosnetapi.cabinas.repositories.AulaRepository;
import org.example.centrosnetapi.cabinas.repositories.ReservaRepository;
import org.example.centrosnetapi.cabinas.repositories.UsuarioRepository;
import org.example.centrosnetapi.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaRepository aulaRepository;

    // ==========================================
    // 🚀 CREAR RESERVA
    // ==========================================
    @Transactional
    public Reserva crearReserva(Long usuarioId, Long aulaId, int duracionMin) {

        if (duracionMin < 30 || duracionMin > 60) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duración inválida (mínimo 30, máximo 60 minutos)"
            );
        }

        // 🔐 Obtener usuario autenticado correctamente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof User secretaria)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Principal inválido"
            );
        }

        // 👤 Alumno
        User usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no existe"
                        )
                );

        // 🎹 Aula
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Aula no existe"
                        )
                );

        // 🔒 Validar mismo centro
        if (secretaria.getCenter() == null ||
                aula.getCenter() == null ||
                !secretaria.getCenter().getId().equals(aula.getCenter().getId())) {

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

            if (r.getFin().isAfter(ahora)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El alumno ya tiene una reserva activa"
                );
            }

            // Si está vencida, la cerramos automáticamente
            r.setFinReal(ahora);
        }

        // ==============================
        // 🔒 VALIDAR RESERVAS DEL AULA
        // ==============================
        List<Reserva> reservasAula =
                reservaRepository.findAllByAulaIdAndFinRealIsNull(aulaId);

        for (Reserva r : reservasAula) {

            if (r.getFin().isAfter(ahora)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El aula ya está ocupada"
                );
            }

            // Si está vencida, la cerramos automáticamente
            r.setFinReal(ahora);
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

        LocalDateTime ahora = LocalDateTime.now();

        for (Reserva r : reservas) {

            if (r.getFin().isAfter(ahora)) {

                r.setFinReal(ahora);

                if (ahora.isBefore(r.getFin())) {
                    r.setFinalizadaAntes(true);
                }

                break; // solo una activa
            }
        }
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