package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Reserva.CrearReservaDTO;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 🔥 Todo el servicio es transaccional para evitar inconsistencias
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspacioRepository espacioRepository;

    // ==========================================
    // 🚀 CREAR RESERVA
    // ==========================================
    public ReservaResponseDTO crearReserva(CrearReservaDTO dto, Usuario secretarioLogueado) {

        if (dto.getDuracion() < 30 || dto.getDuracion() > 60) {
            throw new ApiException("DURACION_INVALIDA", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ApiException("USUARIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        Espacio espacio = espacioRepository.findById(dto.getAulaId())
                .orElseThrow(() -> new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔒 CANDADO SAAS: Validar que la reserva se haga en el centro de la secretaria
        if (secretarioLogueado.getCentro() != null &&
                !secretarioLogueado.getCentro().getId().equals(espacio.getCentro().getId())) {
            throw new ApiException("FORBIDDEN_OTHER_CENTER", HttpStatus.FORBIDDEN);
        }

        LocalDateTime ahora = LocalDateTime.now();

        validarReservasAlumno(dto.getUsuarioId(), ahora);
        validarReservasEspacio(dto.getAulaId(), ahora);

        Reserva reserva = Reserva.builder()
                .usuario(usuario)
                .espacio(espacio)
                .centro(espacio.getCentro()) // El centro de la reserva es el del espacio
                .inicio(ahora)
                .fin(ahora.plusMinutes(dto.getDuracion()))
                .finalizadaAntes(false)
                .build();

        reservaRepository.save(reserva);
        return toDTO(reserva);
    }

    // ==========================================
    // 📊 RESERVAS ACTIVAS (FILTRADAS POR CENTRO)
    // ==========================================
    public List<ReservaResponseDTO> obtenerReservasActivas(Usuario secretarioLogueado) {

        // Si no es SuperAdmin, filtramos por su centroId
        if (secretarioLogueado.getCentro() != null) {
            return reservaRepository.findByCentroIdAndFinRealIsNull(secretarioLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }

        // SuperAdmin ve todas
        return reservaRepository.findByFinRealIsNull().stream().map(this::toDTO).toList();
    }

    // ==========================================
    // 📜 HISTORIAL (FILTRADO POR CENTRO)
    // ==========================================
    public List<ReservaResponseDTO> obtenerHistorial(Usuario secretarioLogueado) {

        if (secretarioLogueado.getCentro() != null) {
            return reservaRepository.findByCentroIdOrderByInicioDesc(secretarioLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }

        return reservaRepository.findAllByOrderByInicioDesc().stream().map(this::toDTO).toList();
    }

    // ==========================================
    // 🏁 FINALIZAR MANUALMENTE
    // ==========================================
    public void finalizarReservaPorEspacio(Long espacioId, Usuario secretarioLogueado) {

        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔒 CANDADO SAAS
        if (secretarioLogueado.getCentro() != null &&
                !secretarioLogueado.getCentro().getId().equals(espacio.getCentro().getId())) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }

        List<Reserva> reservas = reservaRepository.findAllByEspacio_IdAndFinRealIsNull(espacioId);
        if (reservas.isEmpty()) return;

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
    // ⏲ CIERRE AUTOMÁTICO (System Process)
    // ==========================================
    @Scheduled(fixedRate = 10000)
    public void cerrarReservasVencidas() {
        // Esto es un proceso de sistema, limpia toda la base de datos sin importar el centro
        List<Reserva> activas = reservaRepository.findByFinRealIsNull();
        LocalDateTime ahora = LocalDateTime.now();

        for (Reserva r : activas) {
            if (r.getFin().isBefore(ahora)) {
                r.setFinReal(ahora);
            }
        }
    }

    // ==========================================
    // HELPERS & VALIDATIONS
    // ==========================================
    private void validarReservasAlumno(Long usuarioId, LocalDateTime ahora) {
        List<Reserva> reservas = reservaRepository.findAllByUsuario_IdAndFinRealIsNull(usuarioId);
        for (Reserva r : reservas) {
            if (r.getFin().isAfter(ahora)) {
                throw new ApiException("ALUMNO_YA_TIENE_RESERVA_ACTIVA", HttpStatus.CONFLICT);
            }
            r.setFinReal(ahora);
        }
    }

    private void validarReservasEspacio(Long espacioId, LocalDateTime ahora) {
        List<Reserva> reservas = reservaRepository.findAllByEspacio_IdAndFinRealIsNull(espacioId);
        for (Reserva r : reservas) {
            if (r.getFin().isAfter(ahora)) {
                throw new ApiException("ESPACIO_OCUPADO", HttpStatus.CONFLICT);
            }
            r.setFinReal(ahora);
        }
    }

    private ReservaResponseDTO toDTO(Reserva r) {
        return ReservaResponseDTO.builder()
                .id(r.getId())
                .usuarioId(r.getUsuario().getId())
                .usuarioNombre(r.getUsuario().getNombre() + " " + r.getUsuario().getApellidos())
                .aulaId(r.getEspacio().getId())
                .aulaNombre(r.getEspacio().getNombre())
                .inicio(r.getInicio())
                .fin(r.getFin())
                .finReal(r.getFinReal())
                .finalizadaAntes(r.getFinalizadaAntes())
                .build();
    }
}