package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Asignatura.SubjectRequestDTO;
import org.example.centrosnetapi.dtos.Asignatura.SubjectResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final CentroRepository centroRepository;

    // ================= CREATE =================

    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO create(SubjectRequestDTO dto) {

        // ===== VALIDACIONES BÁSICAS =====

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ApiException("SUBJECT_NAME_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        if (dto.getCentroId() == null) {
            throw new ApiException("CENTRO_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new ApiException("TIPO_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        // ===== CENTRO =====

        Centro centro = centroRepository.findById(dto.getCentroId())
                .orElseThrow(() ->
                        new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        String nombreNormalizado = dto.getNombre().trim();

        // ===== DUPLICADO EN EL MISMO CENTRO =====

        if (asignaturaRepository.existsByNombreAndCentroId(
                nombreNormalizado,
                dto.getCentroId()
        )) {
            throw new ApiException(
                    "ASIGNATURA_YA_EXISTE_EN_CENTRO",
                    HttpStatus.BAD_REQUEST
            );
        }

        // ===== TIPO ENUM SEGURO =====

        TipoAsignatura tipo;

        try {
            tipo = TipoAsignatura
                    .valueOf(dto.getTipo().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(
                    "TIPO_ASIGNATURA_INVALIDO",
                    HttpStatus.BAD_REQUEST
            );
        }

        // ===== CREACIÓN =====

        Asignatura asignatura = Asignatura.builder()
                .nombre(nombreNormalizado)
                .descripcion(dto.getDescripcion() != null
                        ? dto.getDescripcion().trim()
                        : null)
                .duracionMinutos(dto.getDuracionMinutos() != null
                        ? dto.getDuracionMinutos()
                        : 60)
                .tipo(tipo)
                .centro(centro)
                .build();

        Asignatura saved = asignaturaRepository.save(asignatura);

        return toDTO(saved);
    }

    // ================= UPDATE =================

    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO update(Long id, SubjectRequestDTO dto) {

        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        if (dto.getNombre() != null) {

            String nuevoNombre = dto.getNombre().trim();

            if (asignaturaRepository.existsByNombreAndCentroIdAndIdNot(
                    nuevoNombre,
                    asignatura.getCentro().getId(),
                    id
            )) {
                throw new ApiException(
                        "ASIGNATURA_YA_EXISTE_EN_CENTRO",
                        HttpStatus.BAD_REQUEST
                );
            }

            asignatura.setNombre(nuevoNombre);
        }

        if (dto.getDescripcion() != null) {
            asignatura.setDescripcion(dto.getDescripcion().trim());
        }

        if (dto.getDuracionMinutos() != null) {
            asignatura.setDuracionMinutos(dto.getDuracionMinutos());
        }

        if (dto.getTipo() != null) {
            try {
                TipoAsignatura tipo = TipoAsignatura
                        .valueOf(dto.getTipo().trim().toUpperCase());

                asignatura.setTipo(tipo);

            } catch (IllegalArgumentException e) {
                throw new ApiException(
                        "TIPO_ASIGNATURA_INVALIDO",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        return toDTO(asignaturaRepository.save(asignatura));
    }

    // ================= READ =================

    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findAll() {
        return asignaturaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findByCenter(Long centerId) {
        return asignaturaRepository.findByCentroId(centerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public SubjectResponseDTO findById(Long id) {

        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        return toDTO(asignatura);
    }

    // ================= DELETE =================

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {

        if (!asignaturaRepository.existsById(id)) {
            throw new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        asignaturaRepository.deleteById(id);
    }

    // ================= TEACHER SUBJECTS =================

    @PreAuthorize("hasRole('PROFESOR')")
    public List<SubjectResponseDTO> getSubjectsForTeacher(Usuario profesor) {

        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ApiException("NOT_A_TEACHER", HttpStatus.FORBIDDEN);
        }

        return asignaturaRepository
                .findSubjectsByTeacherId(profesor.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ================= DTO =================

    private SubjectResponseDTO toDTO(Asignatura a) {
        return SubjectResponseDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .descripcion(a.getDescripcion())
                .duracionMinutos(a.getDuracionMinutos())
                .tipo(a.getTipo() != null ? a.getTipo().name() : null)
                .centroId(a.getCentro() != null ? a.getCentro().getId() : null)
                .build();
    }
}