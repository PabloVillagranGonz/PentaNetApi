package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Curso.*;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 🔥 Buena práctica: Protege la base de datos si algo falla a medias
public class CourseService {

    private final CursoRepository cursoRepository;
    private final CentroRepository centroRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final AsignaturaCursoRepository asignaturaCursoRepository;

    // ============================================================
    // CREATE
    // ============================================================
    public CourseResponseDTO create(CourseRequestDTO dto, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Forzar centro si es Admin Local
        if (adminLogueado.getCentro() != null) {
            dto.setCentroId(adminLogueado.getCentro().getId());
        } else if (dto.getCentroId() == null) {
            throw new ApiException("CENTRO_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        Centro centro = centroRepository.findById(dto.getCentroId())
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));

        Curso curso = Curso.builder()
                .nombre(dto.getNombre())
                .anio(dto.getAnio())
                .notas(dto.getNotas())
                .centro(centro)
                .build();

        curso = cursoRepository.save(curso);
        return toDTO(curso);
    }

    // ============================================================
    // READ
    // ============================================================
    public List<CourseResponseDTO> findAll(Usuario adminLogueado) {
        // 🔥 CANDADO SAAS: Super Admin ve todo, Admin Local ve lo suyo
        if (adminLogueado.getCentro() == null) {
            return cursoRepository.findAll().stream().map(this::toDTO).toList();
        } else {
            return cursoRepository.findByCentroId(adminLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }
    }

    public List<CourseResponseDTO> findByCenter(Long centroId, Usuario adminLogueado) {
        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(centroId)) {
            throw new ApiException("NO_PUEDES_VER_CURSOS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        if (!centroRepository.existsById(centroId))
            throw new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND);

        return cursoRepository.findByCentroId(centroId).stream().map(this::toDTO).toList();
    }

    public CourseResponseDTO findById(Long id, Usuario adminLogueado) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }

        return toDTO(curso);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public CourseResponseDTO update(Long id, CourseRequestDTO dto, Usuario adminLogueado) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_EDITAR_CURSOS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        curso.setNombre(dto.getNombre());
        curso.setAnio(dto.getAnio());
        curso.setNotas(dto.getNotas());

        return toDTO(cursoRepository.save(curso));
    }

    // ============================================================
    // DELETE
    // ============================================================
    public void delete(Long id, Usuario adminLogueado) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_BORRAR_CURSOS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        cursoRepository.deleteById(id);
    }

    // ============================================================
    // RELACIÓN ASIGNATURA - CURSO
    // ============================================================
    public void addSubjectToCourse(Long cursoId, Long asignaturaId, Usuario adminLogueado) {

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }

        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Validar que pertenecen al mismo centro
        if (!curso.getCentro().getId().equals(asignatura.getCentro().getId()))
            throw new ApiException("CENTRO_MISMATCH", HttpStatus.BAD_REQUEST);

        // Validar que no esté ya añadida
        if (asignaturaCursoRepository.existsByCursoIdAndAsignaturaId(cursoId, asignaturaId))
            throw new ApiException("ASIGNATURA_YA_ASIGNADA", HttpStatus.BAD_REQUEST);

        AsignaturaCurso relacion = AsignaturaCurso.builder()
                .curso(curso)
                .asignatura(asignatura)
                .horasSemanales(BigDecimal.valueOf(0.0))
                .build();

        asignaturaCursoRepository.save(relacion);
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private CourseResponseDTO toDTO(Curso curso) {
        return CourseResponseDTO.builder()
                .id(curso.getId())
                .nombre(curso.getNombre())
                .anio(curso.getAnio())
                .notas(curso.getNotas())
                .centroId(curso.getCentro() != null ? curso.getCentro().getId() : null)
                .centroNombre(curso.getCentro() != null ? curso.getCentro().getNombre() : null)
                .build();
    }
}