package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Espacio.*;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.example.centrosnetapi.repositories.EspacioRepository;
import org.example.centrosnetapi.repositories.ReservaRepository;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final CentroRepository centroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public EspacioResponseDTO createAula(EspacioRequestDTO dto) {
        return createByTipo(dto, TipoEspacio.AULA);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public EspacioResponseDTO createCabina(EspacioRequestDTO dto) {
        return createByTipo(dto, TipoEspacio.CABINA);
    }

    @PreAuthorize("isAuthenticated()")
    public List<EspacioResponseDTO> dashboard() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        Usuario user = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        Long centroId = user.getCentro().getId();

        boolean isSecretaria = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA"));

        List<Espacio> espacios;

        if (isSecretaria) {
            espacios = espacioRepository
                    .findByCentroIdAndTipo(centroId, TipoEspacio.CABINA);
        } else {
            espacios = espacioRepository
                    .findByCentroId(centroId);
        }

        return espacios.stream()
                .sorted((a,b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toDashboardDTO)
                .toList();
    }
    // ================= READ =================
    @PreAuthorize("isAuthenticated()")
    public List<EspacioResponseDTO> findByCentro(Long centroId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isSecretaria = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA"));

        List<Espacio> espacios;

        if (isSecretaria) {
            espacios = espacioRepository.findByCentroIdAndTipo(
                    centroId,
                    TipoEspacio.CABINA
            );
        } else {
            espacios = espacioRepository.findByCentroId(centroId);
        }

        return espacios.stream()
                .map(this::toDTO)
                .toList();
    }
    // ================= DELETE =================
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {

        if (!espacioRepository.existsById(id)) {
            throw new ApiException(
                    "ESPACIO_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        espacioRepository.deleteById(id);
    }

    private EspacioResponseDTO toDashboardDTO(Espacio e) {

        LocalDateTime ahora = LocalDateTime.now();

        boolean ocupada = reservaRepository
                .existsByEspacio_IdAndFinRealIsNullAndFinAfter(
                        e.getId(),
                        ahora
                );

        Reserva reservaActiva = null;

        if (ocupada) {
            reservaActiva = reservaRepository
                    .findAllByEspacio_IdAndFinRealIsNull(e.getId())
                    .stream()
                    .filter(r -> r.getFin().isAfter(ahora))
                    .findFirst()
                    .orElse(null);
        }

        return EspacioResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre()) // 👈 nombre del espacio
                .tipo(e.getTipo())
                .capacidad(e.getCapacidad())
                .centroId(e.getCentro().getId())
                .ocupada(ocupada)
                .alumnoNombre( // 👈 AQUÍ VA EL ALUMNO
                        reservaActiva != null
                                ? reservaActiva.getUsuario().getNombre()
                                : null
                )
                .inicio(reservaActiva != null ? reservaActiva.getInicio() : null)
                .fin(reservaActiva != null ? reservaActiva.getFin() : null)
                .build();
    }

    private EspacioResponseDTO toDTO(Espacio e) {

        return EspacioResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .capacidad(e.getCapacidad())
                .centroId(e.getCentro().getId())
                .build();
    }

    private EspacioResponseDTO createByTipo(EspacioRequestDTO dto, TipoEspacio tipo) {

        if (espacioRepository.existsByNombreAndCentroId(
                dto.getNombre(), dto.getCentroId()
        )) {
            throw new ApiException("ESPACIO_YA_EXISTE", HttpStatus.BAD_REQUEST);
        }

        Centro centro = centroRepository.findById(dto.getCentroId())
                .orElseThrow(() ->
                        new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        Espacio espacio = Espacio.builder()
                .nombre(dto.getNombre())
                .tipo(tipo)
                .capacidad(dto.getCapacidad() != null ? dto.getCapacidad() : 1)
                .centro(centro)
                .build();

        espacioRepository.save(espacio);

        return toDTO(espacio);
    }
}