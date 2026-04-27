package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
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
@Transactional
public class AsignaturaCursoService {

    private final AsignaturaCursoRepository asignaturaCursoRepository;
    private final CursoRepository cursoRepository;
    private final AsignaturaRepository asignaturaRepository;

    // ============================================================
    // ASIGNAR
    // ============================================================
    public AsignaturaCursoResponseDTO asignar(AsignaturaCursoRequestDTO dto, Usuario adminLogueado) {

        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS: El curso debe pertenecer al centro del admin
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_MODIFICAR_CURSOS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Candado adicional: curso y asignatura deben ser del mismo centro
        if (!curso.getCentro().getId().equals(asignatura.getCentro().getId())) {
            throw new ApiException("CURSO_Y_ASIGNATURA_DEBEN_SER_DEL_MISMO_CENTRO", HttpStatus.BAD_REQUEST);
        }

        if (asignaturaCursoRepository.existsByCursoIdAndAsignaturaId(dto.getCursoId(), dto.getAsignaturaId())) {
            throw new ApiException("ASIGNATURA_YA_ASIGNADA_AL_CURSO", HttpStatus.BAD_REQUEST);
        }

        AsignaturaCurso ac = AsignaturaCurso.builder()
                .curso(curso)
                .asignatura(asignatura)
                .horasSemanales(dto.getHorasSemanales())
                .build();

        return toDTO(asignaturaCursoRepository.save(ac));
    }

    // ============================================================
    // OBTENER POR CURSO
    // ============================================================
    public List<AsignaturaCursoResponseDTO> obtenerPorCurso(Long cursoId, Usuario usuarioLogueado) {

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (usuarioLogueado.getCentro() != null && !usuarioLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }

        return asignaturaCursoRepository.findByCurso_Id(cursoId)
                .stream().map(this::toDTO).toList();
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    public void eliminar(Long id, Usuario adminLogueado) {

        AsignaturaCurso ac = asignaturaCursoRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNACION_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(ac.getCurso().getCentro().getId())) {
            throw new ApiException("NO_PUEDES_ELIMINAR_ASIGNACIONES_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
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