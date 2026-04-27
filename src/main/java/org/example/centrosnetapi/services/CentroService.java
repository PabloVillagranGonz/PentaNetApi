package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Centro.CenterRequestDTO;
import org.example.centrosnetapi.dtos.Centro.CenterResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Centro;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CentroService {

    private final CentroRepository centroRepository;

    // ============================================================
    // CREATE
    // ============================================================
    public CenterResponseDTO create(CenterRequestDTO dto, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Solo el Super Admin (centro = null) puede crear un conservatorio nuevo
        if (adminLogueado.getCentro() != null) {
            throw new ApiException("SOLO_SUPERADMIN_PUEDE_CREAR_CENTROS", HttpStatus.FORBIDDEN);
        }

        if (dto.getEmail() != null && centroRepository.existsByEmail(dto.getEmail())) {
            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }

        Centro centro = Centro.builder()
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .horarioApertura(dto.getHorarioApertura())
                .direccion(dto.getDireccion())
                .codigoPostal(dto.getCodigoPostal())
                .ciudad(dto.getCiudad())
                .build();

        return toDTO(centroRepository.save(centro));
    }

    // ============================================================
    // READ ALL
    // ============================================================
    public List<CenterResponseDTO> findAll(Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Si un admin local intenta listar los centros, le devolvemos solo el suyo
        if (adminLogueado.getCentro() != null) {
            return List.of(toDTO(adminLogueado.getCentro()));
        }

        // Si es Super Admin, ve toda la lista
        return centroRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // READ BY ID
    // ============================================================
    public CenterResponseDTO findById(Long id, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: No puede cotillear la info privada de otro centro
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(id)) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));

        return toDTO(centro);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public CenterResponseDTO update(Long id, CenterRequestDTO dto, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Solo puede editar la info de SU centro
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(id)) {
            throw new ApiException("NO_PUEDES_EDITAR_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (dto.getEmail() != null && centroRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }

        centro.setNombre(dto.getNombre());
        centro.setTelefono(dto.getTelefono());
        centro.setEmail(dto.getEmail());
        centro.setWebsite(dto.getWebsite());
        centro.setHorarioApertura(dto.getHorarioApertura());
        centro.setDireccion(dto.getDireccion());
        centro.setCodigoPostal(dto.getCodigoPostal());
        centro.setCiudad(dto.getCiudad());

        return toDTO(centroRepository.save(centro));
    }

    // ============================================================
    // DELETE
    // ============================================================
    public void delete(Long id, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Un admin no puede borrar un conservatorio entero. Solo Super Admin.
        if (adminLogueado.getCentro() != null) {
            throw new ApiException("SOLO_SUPERADMIN_PUEDE_BORRAR_CENTROS", HttpStatus.FORBIDDEN);
        }

        if (!centroRepository.existsById(id)) {
            throw new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        centroRepository.deleteById(id);
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private CenterResponseDTO toDTO(Centro c) {
        return CenterResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .telefono(c.getTelefono())
                .email(c.getEmail())
                .website(c.getWebsite())
                .horarioApertura(c.getHorarioApertura())
                .direccion(c.getDireccion())
                .codigoPostal(c.getCodigoPostal())
                .ciudad(c.getCiudad())
                .build();
    }
}