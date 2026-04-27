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

    // ================= CREATE =================
    @PreAuthorize("hasRole('ADMIN')")
    public EspacioResponseDTO createAula(EspacioRequestDTO dto, Usuario adminLogueado) {
        return createByTipo(dto, TipoEspacio.AULA, adminLogueado);
    }

    public List<EspacioResponseDTO> findAll(Usuario adminLogueado) {
        if (adminLogueado.getCentro() == null) {
            return espacioRepository.findAll().stream().map(this::toDTO).toList();
        } else {
            return espacioRepository.findByCentroId(adminLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public EspacioResponseDTO createCabina(EspacioRequestDTO dto, Usuario adminLogueado) {
        return createByTipo(dto, TipoEspacio.CABINA, adminLogueado);
    }

    private EspacioResponseDTO createByTipo(EspacioRequestDTO dto, TipoEspacio tipo, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Si es Admin de centro, forzamos su ID de centro
        if (adminLogueado.getCentro() != null) {
            dto.setCentroId(adminLogueado.getCentro().getId());
        } else if (dto.getCentroId() == null) {
            // Si es Super Admin, DEBE elegir un centro
            throw new ApiException("DEBES_SELECCIONAR_UN_CENTRO", HttpStatus.BAD_REQUEST);
        }

        if (espacioRepository.existsByNombreAndCentroId(dto.getNombre(), dto.getCentroId())) {
            throw new ApiException("ESPACIO_YA_EXISTE", HttpStatus.BAD_REQUEST);
        }

        Centro centro = centroRepository.findById(dto.getCentroId())
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));

        Espacio espacio = Espacio.builder()
                .nombre(dto.getNombre())
                .tipo(tipo)
                .capacidad(dto.getCapacidad() != null ? dto.getCapacidad() : 1)
                .centro(centro)
                .build();

        espacioRepository.save(espacio);
        return toDTO(espacio);
    }

    // ================= READ (LISTADOS) =================
    @PreAuthorize("isAuthenticated()")
    public List<EspacioResponseDTO> findByCentro(Long centroId, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: No cotillear aulas de otro centro
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(centroId)) {
            throw new ApiException("NO_PUEDES_VER_AULAS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        List<Espacio> espacios;
        if (adminLogueado.getRol() == Rol.SECRETARIA) {
            espacios = espacioRepository.findByCentroIdAndTipo(centroId, TipoEspacio.CABINA);
        } else {
            espacios = espacioRepository.findByCentroId(centroId);
        }

        return espacios.stream().map(this::toDTO).toList();
    }

    // ================= DASHBOARD =================
    @PreAuthorize("isAuthenticated()")
    public List<EspacioResponseDTO> dashboard(Usuario adminLogueado) {

        // 🔥 EVITAMOS EL CRASH DEL SUPER ADMIN
        if (adminLogueado.getCentro() == null) {
            return List.of(); // Un Super Admin no tiene una vista de dashboard propia
        }

        Long centroId = adminLogueado.getCentro().getId();
        List<Espacio> espacios;

        if (adminLogueado.getRol() == Rol.SECRETARIA) {
            espacios = espacioRepository.findByCentroIdAndTipo(centroId, TipoEspacio.CABINA);
        } else {
            espacios = espacioRepository.findByCentroId(centroId);
        }

        return espacios.stream()
                .sorted((a,b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toDashboardDTO)
                .toList();
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id, Usuario adminLogueado) {

        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS: No borrar aulas de otro centro
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(espacio.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_BORRAR_AULAS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        espacioRepository.deleteById(id);
    }

    // ================= MAPPERS =================
    private EspacioResponseDTO toDashboardDTO(Espacio e) {
        LocalDateTime ahora = LocalDateTime.now();

        boolean ocupada = reservaRepository.existsByEspacio_IdAndFinRealIsNullAndFinAfter(e.getId(), ahora);
        Reserva reservaActiva = null;

        if (ocupada) {
            reservaActiva = reservaRepository.findAllByEspacio_IdAndFinRealIsNull(e.getId())
                    .stream()
                    .filter(r -> r.getFin().isAfter(ahora))
                    .findFirst()
                    .orElse(null);
        }

        return EspacioResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .capacidad(e.getCapacidad())
                .centroId(e.getCentro().getId())
                .ocupada(ocupada)
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