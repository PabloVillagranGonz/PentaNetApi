package org.example.centrosnetapi.services;

import org.springframework.transaction.annotation.Transactional;
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
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    public AsignaturaCursoResponseDTO asignar(AsignaturaCursoRequestDTO dto, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(dto.getCursoId(), adminLogueado, "NO_PUEDES_MODIFICAR_CURSOS_DE_OTRO_CENTRO");
        Asignatura asignatura = buscarAsignatura(dto.getAsignaturaId());

        validarCompatibilidadCentro(curso, asignatura);
        validarNoDuplicado(dto.getCursoId(), dto.getAsignaturaId());

        AsignaturaCurso ac = AsignaturaCurso.builder()
                .curso(curso)
                .asignatura(asignatura)
                .horasSemanales(dto.getHorasSemanales())
                .build();

        return toDTO(asignaturaCursoRepository.save(ac));
    }

    public List<AsignaturaCursoResponseDTO> obtenerPorCurso(Long cursoId, Usuario usuarioLogueado) {
        buscarCursoValidado(cursoId, usuarioLogueado, "ACCESO_DENEGADO");

        return asignaturaCursoRepository.findByCurso_Id(cursoId)
                .stream().map(this::toDTO).toList();
    }

    public void eliminar(Long id, Usuario adminLogueado) {
        AsignaturaCurso ac = buscarAsignacion(id);
        validarAccesoSaaS(adminLogueado, ac.getCurso().getCentro().getId(), "NO_PUEDES_ELIMINAR_ASIGNACIONES_DE_OTRO_CENTRO");

        asignaturaCursoRepository.deleteById(id);
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones y Buscadores)
    // ============================================================

    private Curso buscarCursoValidado(Long cursoId, Usuario usuario, String mensajeErrorSaaS) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        validarAccesoSaaS(usuario, curso.getCentro().getId(), mensajeErrorSaaS);
        return curso;
    }

    private Asignatura buscarAsignatura(Long asignaturaId) {
        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private AsignaturaCurso buscarAsignacion(Long id) {
        return asignaturaCursoRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNACION_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private void validarAccesoSaaS(Usuario usuario, Long centroIdObjetivo, String mensajeError) {
        if (usuario.getCentro() != null && !usuario.getCentro().getId().equals(centroIdObjetivo)) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private void validarCompatibilidadCentro(Curso curso, Asignatura asignatura) {
        if (!curso.getCentro().getId().equals(asignatura.getCentro().getId())) {
            throw new ApiException("CURSO_Y_ASIGNATURA_DEBEN_SER_DEL_MISMO_CENTRO", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarNoDuplicado(Long cursoId, Long asignaturaId) {
        if (asignaturaCursoRepository.existsByCursoIdAndAsignaturaId(cursoId, asignaturaId)) {
            throw new ApiException("ASIGNATURA_YA_ASIGNADA_AL_CURSO", HttpStatus.BAD_REQUEST);
        }
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