package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Centro.CenterRequestDTO;
import org.example.centrosnetapi.dtos.Centro.CenterResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Centro;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentroService {

    private final CentroRepository centroRepository;

    // ============================================================
    // CREATE
    // ============================================================

    public CenterResponseDTO create(CenterRequestDTO dto) {

        if (dto.getEmail() != null &&
                centroRepository.existsByEmail(dto.getEmail())) {
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

    public List<CenterResponseDTO> findAll() {
        return centroRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // READ BY ID
    // ============================================================

    public CenterResponseDTO findById(Long id) {

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        return toDTO(centro);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public CenterResponseDTO update(Long id, CenterRequestDTO dto) {

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        if (dto.getEmail() != null &&
                centroRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
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

    public void delete(Long id) {

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