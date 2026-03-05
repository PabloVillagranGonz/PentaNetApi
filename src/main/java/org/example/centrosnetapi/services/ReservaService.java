package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Reserva.ReservaResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Espacio;
import org.example.centrosnetapi.models.Reserva;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.EspacioRepository;
import org.example.centrosnetapi.repositories.ReservaRepository;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspacioRepository espacioRepository;

    // ==========================================
    // 🚀 CREAR RESERVA
    // ==========================================
    @Transactional
    public ReservaResponseDTO crearReserva(Long usuarioId,
                                           Long espacioId,
                                           int duracionMin) {

        if (duracionMin < 30 || duracionMin > 60) {
            throw new ApiException(
                    "DURACION_INVALIDA",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 🔐 Obtener usuario autenticado desde JWT
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        Usuario secretaria = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException("USUARIO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ApiException("USUARIO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() ->
                        new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        // 🔒 Validar que la secretaria solo actúe en su centro
        if (!secretaria.getCentro().getId()
                .equals(espacio.getCentro().getId())) {

            throw new ApiException(
                    "FORBIDDEN_OTHER_CENTER",
                    HttpStatus.FORBIDDEN
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        validarReservasAlumno(usuarioId, ahora);
        validarReservasEspacio(espacioId, ahora);

        Reserva reserva = Reserva.builder()
                .usuario(usuario)
                .espacio(espacio)
                .centro(secretaria.getCentro())
                .inicio(ahora)
                .fin(ahora.plusMinutes(duracionMin))
                .build();

        reservaRepository.save(reserva);

        return toDTO(reserva);
    }

    // ==========================================
    // VALIDACIONES
    // ==========================================
    private void validarReservasAlumno(Long usuarioId,
                                       LocalDateTime ahora) {

        List<Reserva> reservas =
                reservaRepository
                        .findAllByUsuario_IdAndFinRealIsNull(usuarioId);

        for (Reserva r : reservas) {

            if (r.getFin().isAfter(ahora)) {
                throw new ApiException(
                        "ALUMNO_YA_TIENE_RESERVA_ACTIVA",
                        HttpStatus.CONFLICT
                );
            }

            r.setFinReal(ahora);
        }
    }
    private void validarReservasEspacio(Long espacioId,
                                        LocalDateTime ahora) {

        List<Reserva> reservas =
                reservaRepository.findAllByEspacio_IdAndFinRealIsNull(espacioId);

        for (Reserva r : reservas) {

            if (r.getFin().isAfter(ahora)) {
                throw new ApiException(
                        "ESPACIO_OCUPADO",
                        HttpStatus.CONFLICT
                );
            }

            r.setFinReal(ahora);
        }
    }

    // ==========================================
    // 📊 RESERVAS ACTIVAS
    // ==========================================

    public List<ReservaResponseDTO> obtenerReservasActivas() {

        return reservaRepository.findByFinRealIsNull()
                .stream()
                .map(this::toDTO)
                .toList();
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

    public List<ReservaResponseDTO> obtenerHistorial() {

        return reservaRepository.findAllByOrderByInicioDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void finalizarReservaPorEspacio(Long espacioId) {

        List<Reserva> reservas =
                reservaRepository.findAllByEspacio_IdAndFinRealIsNull(espacioId);

        if (reservas.isEmpty()) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        for (Reserva r : reservas) {

            if (r.getFin().isAfter(ahora)) {

                r.setFinReal(ahora);

                if (ahora.isBefore(r.getFin())) {
                    r.setFinalizadaAntes(true);
                }

                break;
            }
        }
    }

    // ==========================================
    // MAPPER CORREGIDO
    // ==========================================

    private ReservaResponseDTO toDTO(Reserva r) {

        return ReservaResponseDTO.builder()
                .id(r.getId())
                .usuarioId(r.getUsuario().getId())
                .usuarioNombre(
                        r.getUsuario().getNombre() + " " +
                                r.getUsuario().getApellidos()
                )
                .aulaId(r.getEspacio().getId())
                .aulaNombre(r.getEspacio().getNombre())
                .inicio(r.getInicio())
                .fin(r.getFin())
                .finReal(r.getFinReal())
                .finalizadaAntes(r.getFinalizadaAntes())
                .build();
    }
}