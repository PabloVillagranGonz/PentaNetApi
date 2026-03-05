package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.AsignaturaCurso.*;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignaturaCursoService {

    private final AsignaturaCursoRepository asignaturaCursoRepository;
    private final CursoRepository cursoRepository;
    private final AsignaturaRepository asignaturaRepository;

    // ============================================================
    // ASIGNAR
    // ============================================================

    public AsignaturaCursoResponseDTO asignar(
            AsignaturaCursoRequestDTO dto
    ) {

        if (asignaturaCursoRepository.existsByCursoIdAndAsignaturaId(
                dto.getCursoId(),
                dto.getAsignaturaId()
        )) {
            throw new ApiException(
                    "ASIGNATURA_YA_ASIGNADA_AL_CURSO",
                    HttpStatus.BAD_REQUEST
            );
        }

        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() ->
                        new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() ->
                        new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        AsignaturaCurso ac = AsignaturaCurso.builder()
                .curso(curso)
                .asignatura(asignatura)
                .horasSemanales(dto.getHorasSemanales())
                .build();

        asignaturaCursoRepository.save(ac);

        return toDTO(ac);
    }

    // ============================================================
    // OBTENER POR CURSO
    // ============================================================

    public List<AsignaturaCursoResponseDTO> obtenerPorCurso(Long cursoId) {

        return asignaturaCursoRepository.findByCurso_Id(cursoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // ELIMINAR
    // ============================================================

    public void eliminar(Long id) {

        if (!asignaturaCursoRepository.existsById(id)) {
            throw new ApiException(
                    "ASIGNACION_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        asignaturaCursoRepository.deleteById(id);
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private AsignaturaCursoResponseDTO toDTO(AsignaturaCurso ac) {

        return AsignaturaCursoResponseDTO.builder()
                .id(ac.getId())
                .cursoId(ac.getCurso().getId())
                .cursoNombre(ac.getCurso().getNombre())
                .asignaturaId(ac.getAsignatura().getId())
                .asignaturaNombre(ac.getAsignatura().getNombre())
                .horasSemanales(ac.getHorasSemanales())
                .build();
    }
}