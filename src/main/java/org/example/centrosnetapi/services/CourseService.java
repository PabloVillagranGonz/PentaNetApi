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
@Transactional // 🔥 Protege la base de datos si algo falla a medias
public class CourseService {

    private final CursoRepository cursoRepository;
    private final CentroRepository centroRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final AsignaturaCursoRepository asignaturaCursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SesionClaseRepository sesionClaseRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    public CourseResponseDTO create(CourseRequestDTO dto, Usuario adminLogueado) {
        Long centroId = resolverCentroIdSaaS(dto.getCentroId(), adminLogueado);
        validarNoDuplicado(dto.getNombre(), centroId, null);

        Centro centro = buscarCentro(centroId);

        Curso curso = Curso.builder()
                .nombre(dto.getNombre())
                .anio(dto.getAnio())
                .notas(dto.getNotas())
                .centro(centro)
                .build();

        return toDTO(cursoRepository.save(curso));
    }

    public CourseResponseDTO update(Long id, CourseRequestDTO dto, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(id, adminLogueado, "NO_PUEDES_EDITAR_CURSOS_DE_OTRO_CENTRO");
        validarNoDuplicado(dto.getNombre(), curso.getCentro().getId(), id);

        curso.setNombre(dto.getNombre());
        curso.setAnio(dto.getAnio());
        curso.setNotas(dto.getNotas());

        return toDTO(cursoRepository.save(curso));
    }

    public List<CourseResponseDTO> findAll(Usuario adminLogueado) {
        if (adminLogueado.getCentro() == null) {
            return cursoRepository.findAll().stream().map(this::toDTO).toList();
        }
        return cursoRepository.findByCentroId(adminLogueado.getCentro().getId())
                .stream().map(this::toDTO).toList();
    }

    public List<CourseResponseDTO> findByCenter(Long centroId, Usuario adminLogueado) {
        validarAccesoSaaS(adminLogueado, centroId, "NO_PUEDES_VER_CURSOS_DE_OTRO_CENTRO");

        if (!centroRepository.existsById(centroId)) {
            throw new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        return cursoRepository.findByCentroId(centroId).stream().map(this::toDTO).toList();
    }

    public CourseResponseDTO findById(Long id, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(id, adminLogueado, "ACCESO_DENEGADO");
        return toDTO(curso);
    }

    public void delete(Long id, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(id, adminLogueado, "NO_PUEDES_BORRAR_CURSOS_DE_OTRO_CENTRO");
        validarSinDependencias(id);

        cursoRepository.delete(curso);
    }

    // ============================================================
    // GESTIÓN DE ASIGNATURAS EN CURSOS
    // ============================================================

    public void addSubjectToCourse(Long cursoId, Long asignaturaId, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(cursoId, adminLogueado, "ACCESO_DENEGADO");
        Asignatura asignatura = buscarAsignatura(asignaturaId);

        if (!curso.getCentro().getId().equals(asignatura.getCentro().getId())) {
            throw new ApiException("CENTRO_MISMATCH", HttpStatus.BAD_REQUEST);
        }

        if (asignaturaCursoRepository.existsByCursoIdAndAsignaturaId(cursoId, asignaturaId)) {
            throw new ApiException("ASIGNATURA_YA_ASIGNADA", HttpStatus.BAD_REQUEST);
        }

        AsignaturaCurso relacion = AsignaturaCurso.builder()
                .curso(curso)
                .asignatura(asignatura)
                .horasSemanales(BigDecimal.ZERO)
                .build();

        asignaturaCursoRepository.save(relacion);
    }

    public List<Long> getSubjectIdsByCourse(Long cursoId) {
        return asignaturaCursoRepository.findByCurso_Id(cursoId)
                .stream()
                .map(ac -> ac.getAsignatura().getId())
                .toList();
    }

    public void syncSubjects(Long cursoId, List<Long> subjectIds, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(cursoId, adminLogueado, "ACCESO_DENEGADO");

        // 1. Limpieza total
        asignaturaCursoRepository.deleteByCursoId(cursoId);

        // 2. 🔥 OPTIMIZACIÓN N+1: Buscamos todas las asignaturas de golpe y guardamos en bloque
        List<Asignatura> asignaturas = asignaturaRepository.findAllById(subjectIds);

        List<AsignaturaCurso> nuevasRelaciones = asignaturas.stream()
                .map(asignatura -> AsignaturaCurso.builder()
                        .curso(curso)
                        .asignatura(asignatura)
                        .horasSemanales(BigDecimal.ZERO)
                        .build())
                .toList();

        asignaturaCursoRepository.saveAll(nuevasRelaciones);
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones y Buscadores)
    // ============================================================

    private Curso buscarCursoValidado(Long cursoId, Usuario adminLogueado, String mensajeErrorSaaS) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        validarAccesoSaaS(adminLogueado, curso.getCentro().getId(), mensajeErrorSaaS);
        return curso;
    }

    private Centro buscarCentro(Long centroId) {
        return centroRepository.findById(centroId)
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Asignatura buscarAsignatura(Long asignaturaId) {
        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Long resolverCentroIdSaaS(Long dtoCentroId, Usuario adminLogueado) {
        if (adminLogueado.getCentro() != null) {
            return adminLogueado.getCentro().getId();
        } else if (dtoCentroId == null) {
            throw new ApiException("CENTRO_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        return dtoCentroId;
    }

    private void validarAccesoSaaS(Usuario usuario, Long centroIdObjetivo, String mensajeError) {
        if (usuario.getCentro() != null && !usuario.getCentro().getId().equals(centroIdObjetivo)) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private void validarNoDuplicado(String nombre, Long centroId, Long idExcluido) {
        boolean existe = (idExcluido == null)
                ? cursoRepository.existsByNombreAndCentroId(nombre, centroId)
                : cursoRepository.existsByNombreAndCentroIdAndIdNot(nombre, centroId, idExcluido);

        if (existe) {
            throw new ApiException("CURSO_YA_EXISTE", HttpStatus.CONFLICT);
        }
    }

    private void validarSinDependencias(Long id) {
        boolean tieneAlumnos = usuarioRepository.existsByCursoId(id);
        boolean tieneAsignaturas = asignaturaCursoRepository.existsByCursoId(id);
        boolean tieneSesiones = sesionClaseRepository.existsByCursoId(id);

        if (tieneAlumnos || tieneAsignaturas || tieneSesiones) {
            throw new ApiException("CURSO_CON_DEPENDENCIAS", HttpStatus.CONFLICT);
        }
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