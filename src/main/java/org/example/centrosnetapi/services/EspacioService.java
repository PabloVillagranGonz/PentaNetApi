package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Espacio.*;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.example.centrosnetapi.repositories.EspacioRepository;
import org.example.centrosnetapi.repositories.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final CentroRepository centroRepository;
    private final ReservaRepository reservaRepository;

    // ============================================================
    // CREATE
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    public EspacioResponseDTO createAula(EspacioRequestDTO dto, Usuario adminLogueado) {
        return procesarCreacion(dto, TipoEspacio.AULA, adminLogueado);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public EspacioResponseDTO createCabina(EspacioRequestDTO dto, Usuario adminLogueado) {
        return procesarCreacion(dto, TipoEspacio.CABINA, adminLogueado);
    }

    private EspacioResponseDTO procesarCreacion(EspacioRequestDTO dto, TipoEspacio tipo, Usuario adminLogueado) {
        Long centroId = resolverCentroIdSaaS(dto.getCentroId(), adminLogueado);
        validarNoDuplicado(dto.getNombre(), centroId);

        Centro centro = buscarCentro(centroId);

        Espacio espacio = Espacio.builder()
                .nombre(dto.getNombre())
                .tipo(tipo)
                .capacidad(dto.getCapacidad() != null ? dto.getCapacidad() : 1)
                .centro(centro)
                .build();

        return toDTO(espacioRepository.save(espacio));
    }

    // ============================================================
    // READ & DASHBOARD
    // ============================================================

    public List<EspacioResponseDTO> findAll(Usuario adminLogueado) {
        if (adminLogueado.getCentro() == null) {
            return espacioRepository.findAll().stream().map(this::toDTO).toList();
        }
        return espacioRepository.findByCentroId(adminLogueado.getCentro().getId())
                .stream().map(this::toDTO).toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<EspacioResponseDTO> findByCentro(Long centroId, Usuario adminLogueado) {
        validarAccesoSaaS(adminLogueado, centroId, "NO_PUEDES_VER_AULAS_DE_OTRO_CENTRO");

        return obtenerEspaciosPorRol(centroId, adminLogueado.getRol())
                .stream().map(this::toDTO).toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<EspacioResponseDTO> dashboard(Usuario adminLogueado) {
        if (adminLogueado.getCentro() == null) {
            return List.of(); // Evitamos el crash del Super Admin
        }

        Long centroId = adminLogueado.getCentro().getId();

        return obtenerEspaciosPorRol(centroId, adminLogueado.getRol())
                .stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toDashboardDTO)
                .toList();
    }

    // ============================================================
    // DELETE
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id, Usuario adminLogueado) {
        Espacio espacio = buscarEspacioValidado(id, adminLogueado, "NO_PUEDES_BORRAR_AULAS_DE_OTRO_CENTRO");
        espacioRepository.delete(espacio);
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Buscadores y Validadores)
    // ============================================================

    private Long resolverCentroIdSaaS(Long dtoCentroId, Usuario adminLogueado) {
        if (adminLogueado.getCentro() != null) {
            return adminLogueado.getCentro().getId();
        } else if (dtoCentroId == null) {
            throw new ApiException("DEBES_SELECCIONAR_UN_CENTRO", HttpStatus.BAD_REQUEST);
        }
        return dtoCentroId;
    }

    private void validarNoDuplicado(String nombre, Long centroId) {
        if (espacioRepository.existsByNombreAndCentroId(nombre, centroId)) {
            throw new ApiException("ESPACIO_YA_EXISTE", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarAccesoSaaS(Usuario usuario, Long centroIdObjetivo, String mensajeError) {
        if (usuario.getCentro() != null && !usuario.getCentro().getId().equals(centroIdObjetivo)) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private Centro buscarCentro(Long centroId) {
        return centroRepository.findById(centroId)
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Espacio buscarEspacioValidado(Long id, Usuario adminLogueado, String mensajeErrorSaaS) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        validarAccesoSaaS(adminLogueado, espacio.getCentro().getId(), mensajeErrorSaaS);
        return espacio;
    }

    private List<Espacio> obtenerEspaciosPorRol(Long centroId, Rol rol) {
        if (rol == Rol.SECRETARIA) {
            return espacioRepository.findByCentroIdAndTipo(centroId, TipoEspacio.CABINA);
        }
        return espacioRepository.findByCentroId(centroId);
    }

    // ============================================================
    // MAPPERS
    // ============================================================

    private EspacioResponseDTO toDashboardDTO(Espacio e) {
        LocalDateTime ahora = LocalDateTime.now();

        // OPTIMIZACIÓN: Buscamos directamente la reserva activa en 1 sola consulta
        // en lugar de usar un exists() y luego un findAll() como hacías antes.
        Reserva reservaActiva = reservaRepository.findAllByEspacio_IdAndFinRealIsNull(e.getId())
                .stream()
                .filter(r -> r.getFin().isAfter(ahora))
                .findFirst()
                .orElse(null);

        return EspacioResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .capacidad(e.getCapacidad())
                .centroId(e.getCentro().getId())
                .ocupada(reservaActiva != null)
                .alumnoNombre(reservaActiva != null ? reservaActiva.getUsuario().getNombre() : null)
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
}