package org.example.centrosnetapi.services;

import org.springframework.transaction.annotation.Transactional;
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
@Transactional // Todo el servicio es transaccional para evitar inconsistencias
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspacioRepository espacioRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    public ReservaResponseDTO crearReserva(CrearReservaDTO dto, Usuario secretarioLogueado) {
        validarDuracion(dto.getDuracion());

        Usuario usuario = buscarUsuario(dto.getUsuarioId());
        Espacio espacio = buscarEspacioValidado(dto.getAulaId(), secretarioLogueado, "FORBIDDEN_OTHER_CENTER");

        LocalDateTime ahora = LocalDateTime.now();

        // Limpieza perezosa y validación
        validarYLimpiarReservasAlumno(usuario.getId(), ahora);
        validarYLimpiarReservasEspacio(espacio.getId(), ahora);

        Reserva reserva = Reserva.builder()
                .usuario(usuario)
                .espacio(espacio)
                .centro(espacio.getCentro())
                .inicio(ahora)
                .fin(ahora.plusMinutes(dto.getDuracion()))
                .finalizadaAntes(false)
                .build();

        return toDTO(reservaRepository.save(reserva));
    }

    public List<ReservaResponseDTO> obtenerReservasActivas(Usuario secretarioLogueado) {
        if (secretarioLogueado.getCentro() != null) {
            return reservaRepository.findByCentroIdAndFinRealIsNull(secretarioLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }
        return reservaRepository.findByFinRealIsNull().stream().map(this::toDTO).toList();
    }

    public List<ReservaResponseDTO> obtenerHistorial(Usuario secretarioLogueado) {
        if (secretarioLogueado.getCentro() != null) {
            return reservaRepository.findByCentroIdOrderByInicioDesc(secretarioLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }
        return reservaRepository.findAllByOrderByInicioDesc().stream().map(this::toDTO).toList();
    }

    public void finalizarReservaPorEspacio(Long espacioId, Usuario secretarioLogueado) {
        buscarEspacioValidado(espacioId, secretarioLogueado, "ACCESO_DENEGADO");

        reservaRepository.findAllByEspacio_IdAndFinRealIsNull(espacioId).stream()
                .filter(r -> r.getFin().isAfter(LocalDateTime.now()))
                .findFirst()
                .ifPresent(this::cerrarReservaAnticipadamente);
    }

    // ============================================================
    // CRON JOBS (Procesos de Sistema)
    // ============================================================

    @Scheduled(fixedRate = 10000)
    public void cerrarReservasVencidas() {
        /* * 💡 TIP DE RENDIMIENTO PARA EL FUTURO:
         * Si el sistema crece mucho, en lugar de cargar las entidades en memoria con findByFinRealIsNull(),
         * añade este método en ReservaRepository:
         * * @Modifying
         * @Query("UPDATE Reserva r SET r.finReal = :ahora WHERE r.finReal IS NULL AND r.fin < :ahora")
         * void cerrarVencidasEnBloque(@Param("ahora") LocalDateTime ahora);
         * * Y simplemente llámalo aquí. Pasará de N consultas a 1 sola.
         */

        List<Reserva> activas = reservaRepository.findByFinRealIsNull();
        LocalDateTime ahora = LocalDateTime.now();

        activas.stream()
                .filter(r -> r.getFin().isBefore(ahora))
                .forEach(r -> r.setFinReal(ahora)); // Dirty checking de Hibernate hará el UPDATE automático
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Buscadores, Validadores y Helpers)
    // ============================================================

    private void validarDuracion(int duracion) {
        if (duracion < 30 || duracion > 60) {
            throw new ApiException("DURACION_INVALIDA", HttpStatus.BAD_REQUEST);
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException("USUARIO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Espacio buscarEspacioValidado(Long espacioId, Usuario adminLogueado, String mensajeErrorSaaS) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(espacio.getCentro().getId())) {
            throw new ApiException(mensajeErrorSaaS, HttpStatus.FORBIDDEN);
        }
        return espacio;
    }

    private void validarYLimpiarReservasAlumno(Long usuarioId, LocalDateTime ahora) {
        List<Reserva> reservas = reservaRepository.findAllByUsuario_IdAndFinRealIsNull(usuarioId);
        reservas.forEach(r -> procesarReservaExistente(r, ahora, "ALUMNO_YA_TIENE_RESERVA_ACTIVA"));
    }

    private void validarYLimpiarReservasEspacio(Long espacioId, LocalDateTime ahora) {
        List<Reserva> reservas = reservaRepository.findAllByEspacio_IdAndFinRealIsNull(espacioId);
        reservas.forEach(r -> procesarReservaExistente(r, ahora, "ESPACIO_OCUPADO"));
    }

    private void procesarReservaExistente(Reserva r, LocalDateTime ahora, String mensajeErrorConflict) {
        if (r.getFin().isAfter(ahora)) {
            throw new ApiException(mensajeErrorConflict, HttpStatus.CONFLICT);
        }
        r.setFinReal(ahora); // Fallback: Si estaba vencida y el Cron no la pilló, la cerramos aquí
    }

    private void cerrarReservaAnticipadamente(Reserva reserva) {
        LocalDateTime ahora = LocalDateTime.now();
        reserva.setFinReal(ahora);

        if (ahora.isBefore(reserva.getFin())) {
            reserva.setFinalizadaAntes(true);
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================

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