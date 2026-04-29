package org.example.centrosnetapi.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Estudiante.StudentSubjectDTO;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseRequestDTO;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class SesionClaseService {

    private final SesionClaseRepository sesionRepository;
    private final CursoRepository cursoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspacioRepository espacioRepository;
    private final AsignaturaCursoRepository asignaturaCursoRepository;

    // ============================================================
    // MÉTODOS DE ALUMNOS (Asignaturas y Horarios)
    // ============================================================

    public List<StudentSubjectDTO> findStudentSubjects(Long studentId, Usuario usuarioLogueado) {
        Usuario student = validarAccesoAlumno(studentId, usuarioLogueado);
        if (student.getCurso() == null) return List.of();

        return sesionRepository.findByCursoId(student.getCurso().getId())
                .stream()
                .map(this::toStudentSubjectDTO)
                .distinct()
                .toList();
    }

    public List<SesionClaseResponseDTO> findScheduleForStudent(Long studentId, Usuario usuarioLogueado) {
        Usuario student = validarAccesoAlumno(studentId, usuarioLogueado);
        if (student.getCurso() == null) return List.of();

        return sesionRepository.findScheduleForStudent(student.getCurso().getId(), student.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<UserResponseDTO> findTeachersForStudent(Long studentId, Usuario usuarioLogueado) {
        Usuario student = validarAccesoAlumno(studentId, usuarioLogueado);
        if (student.getCurso() == null) return List.of();

        return sesionRepository.findByCursoId(student.getCurso().getId())
                .stream()
                .map(SesionClase::getProfesor)
                .filter(Objects::nonNull) // 🔥 Código más limpio usando Objects::nonNull
                .distinct()
                .map(this::toUserDTO)
                .toList();
    }

    // ============================================================
    // CREACIÓN DE SESIONES (Core Logic)
    // ============================================================

    public Long crearSesion(SesionClaseRequestDTO dto, Usuario adminLogueado) {
        Curso curso = buscarCursoValidado(dto.getCursoId(), adminLogueado);
        Asignatura asignatura = buscarAsignatura(dto.getAsignaturaId());
        Usuario profesor = buscarUsuario(dto.getProfesorId(), "PROFESOR_NOT_FOUND");
        Espacio espacio = buscarEspacio(dto.getEspacioId());

        validarDominio(curso, asignatura, profesor, espacio);

        if (asignatura.getTipo() == TipoAsignatura.COLECTIVA) {
            return crearSesionColectiva(dto, curso, asignatura, profesor, espacio);
        } else {
            return crearSesionesIndividuales(dto, curso, asignatura, profesor, espacio);
        }
    }

    private Long crearSesionColectiva(SesionClaseRequestDTO dto, Curso curso, Asignatura asignatura, Usuario profesor, Espacio espacio) {
        validarSolapamientos(dto, profesor.getId(), espacio.getId(), null);
        SesionClase sesion = construirSesion(dto, curso, asignatura, profesor, espacio, null);

        return sesionRepository.save(sesion).getId();
    }

    private Long crearSesionesIndividuales(SesionClaseRequestDTO dto, Curso curso, Asignatura asignatura, Usuario profesor, Espacio espacio) {
        if (dto.getAlumnoId() != null) {
            // Sesión para 1 solo alumno
            Usuario alumno = buscarUsuario(dto.getAlumnoId(), "ALUMNO_NOT_FOUND");
            validarSolapamientos(dto, profesor.getId(), espacio.getId(), alumno.getId());

            SesionClase sesion = construirSesion(dto, curso, asignatura, profesor, espacio, alumno);
            return sesionRepository.save(sesion).getId();
        } else {
            // Sesión individual generada en bloque para todos los alumnos del curso
            List<Usuario> alumnos = usuarioRepository.findByCursoId(curso.getId());
            if (alumnos.isEmpty()) throw new ApiException("CURSO_SIN_ALUMNOS", HttpStatus.BAD_REQUEST);

            List<SesionClase> sesiones = new ArrayList<>();
            for (Usuario alumno : alumnos) {
                validarSolapamientos(dto, profesor.getId(), espacio.getId(), alumno.getId());
                sesiones.add(construirSesion(dto, curso, asignatura, profesor, espacio, alumno));
            }

            // 🔥 OPTIMIZACIÓN: saveAll guarda todas las sesiones en un solo viaje a la BD
            List<SesionClase> guardadas = sesionRepository.saveAll(sesiones);
            return guardadas.get(guardadas.size() - 1).getId(); // Devolvemos el último ID para mantener tu contrato original
        }
    }

    // ============================================================
    // CONSULTAS DE SESIÓN
    // ============================================================

    public List<UserResponseDTO> getStudentsForSession(Long sessionId) {
        SesionClase sesion = sesionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException("SESSION_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (sesion.getAlumno() != null) { // 🎯 CLASE INDIVIDUAL
            return List.of(toUserDTO(sesion.getAlumno()));
        }

        // 🎯 CLASE COLECTIVA
        return usuarioRepository.findByCurso_IdAndRol(sesion.getCurso().getId(), Rol.ALUMNO)
                .stream()
                .map(this::toUserDTO)
                .toList();
    }

    public List<SesionClaseResponseDTO> obtenerPorCurso(Long cursoId) {
        return sesionRepository.findByCurso_IdOrderByDiaSemanaAscHoraInicioAsc(cursoId)
                .stream().map(this::toDTO).toList();
    }

    public List<SesionClaseResponseDTO> obtenerPorProfesor(Long profesorId) {
        return sesionRepository.findByProfesorIdOrderByDiaSemanaAscHoraInicioAsc(profesorId)
                .stream().map(this::toDTO).toList();
    }

    public List<SesionClaseResponseDTO> obtenerPorProfesorYDia(Long profesorId, Integer diaSemana) {
        return sesionRepository.findByProfesorIdAndDiaSemanaOrderByHoraInicioAsc(profesorId, diaSemana)
                .stream().map(this::toDTO).toList();
    }

    public List<SesionClaseResponseDTO> findByCourseId(Long cursoId) {
        return sesionRepository.findByCursoId(cursoId)
                .stream().map(this::toDTO).toList();
    }

    // ============================================================
    // PRIVADOS: BUSCADORES Y VALIDACIONES DE DOMINIO
    // ============================================================

    private Curso buscarCursoValidado(Long cursoId, Usuario adminLogueado) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(curso.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_GESTIONAR_OTROS_CENTROS", HttpStatus.FORBIDDEN);
        }
        return curso;
    }

    private Asignatura buscarAsignatura(Long id) {
        return asignaturaRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Espacio buscarEspacio(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ApiException("ESPACIO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Usuario buscarUsuario(Long id, String errorMsg) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(errorMsg, HttpStatus.NOT_FOUND));
    }

    private Usuario validarAccesoAlumno(Long studentId, Usuario usuarioLogueado) {
        Usuario student = buscarUsuario(studentId, "USER_NOT_FOUND");

        if (!studentId.equals(usuarioLogueado.getId()) &&
                usuarioLogueado.getCentro() != null &&
                !usuarioLogueado.getCentro().getId().equals(student.getCentro().getId())) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }
        return student;
    }

    private void validarDominio(Curso curso, Asignatura asignatura, Usuario profesor, Espacio espacio) {
        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ApiException("USUARIO_NO_ES_PROFESOR", HttpStatus.BAD_REQUEST);
        }
        if (!asignaturaCursoRepository.existsByCursoIdAndAsignaturaId(curso.getId(), asignatura.getId())) {
            throw new ApiException("ASIGNATURA_NO_PERTENECE_AL_CURSO", HttpStatus.BAD_REQUEST);
        }
        if (!curso.getCentro().getId().equals(espacio.getCentro().getId())) {
            throw new ApiException("ESPACIO_DISTINTO_CENTRO", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarSolapamientos(SesionClaseRequestDTO dto, Long profesorId, Long espacioId, Long alumnoId) {
        if (sesionRepository.existsConflictingSessionForEspacio(espacioId, dto.getDiaSemana(), dto.getHoraInicio(), dto.getHoraFin())) {
            throw new ApiException("ESPACIO_OCUPADO_HORARIO", HttpStatus.CONFLICT);
        }
        if (sesionRepository.existsConflictingSessionForProfesor(profesorId, dto.getDiaSemana(), dto.getHoraInicio(), dto.getHoraFin())) {
            throw new ApiException("PROFESOR_OCUPADO_HORARIO", HttpStatus.CONFLICT);
        }
        if (alumnoId != null && sesionRepository.existsConflictingSessionForAlumno(alumnoId, dto.getDiaSemana(), dto.getHoraInicio(), dto.getHoraFin())) {
            throw new ApiException("ALUMNO_OCUPADO_HORARIO", HttpStatus.CONFLICT);
        }
    }

    private SesionClase construirSesion(SesionClaseRequestDTO dto, Curso curso, Asignatura asignatura, Usuario profesor, Espacio espacio, Usuario alumno) {
        return SesionClase.builder()
                .curso(curso)
                .asignatura(asignatura)
                .profesor(profesor)
                .espacio(espacio)
                .alumno(alumno)
                .diaSemana(dto.getDiaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .notas(dto.getNotas())
                .build();
    }

    // ============================================================
    // MAPPERS
    // ============================================================

    private StudentSubjectDTO toStudentSubjectDTO(SesionClase cs) {
        return new StudentSubjectDTO(
                cs.getId(),
                cs.getAsignatura().getNombre(),
                cs.getProfesor() != null ? cs.getProfesor().getNombre() + " " + cs.getProfesor().getApellidos() : "No asignado",
                cs.getEspacio() != null ? cs.getEspacio().getNombre() : "No asignado"
        );
    }

    private SesionClaseResponseDTO toDTO(SesionClase s) {
        return SesionClaseResponseDTO.builder()
                .id(s.getId())
                .cursoId(s.getCurso().getId())
                .cursoNombre(s.getCurso().getNombre())
                .asignaturaId(s.getAsignatura().getId())
                .asignaturaNombre(s.getAsignatura().getNombre())
                .tipoAsignatura(s.getAsignatura().getTipo().name())
                .profesorId(s.getProfesor().getId())
                .profesorNombreCompleto(s.getProfesor().getNombre() + " " + s.getProfesor().getApellidos())
                .alumnoId(s.getAlumno() != null ? s.getAlumno().getId() : null)
                .alumnoNombreCompleto(s.getAlumno() != null ? s.getAlumno().getNombre() + " " + s.getAlumno().getApellidos() : null)
                .espacioId(s.getEspacio().getId())
                .espacioNombre(s.getEspacio().getNombre())
                .diaSemana(s.getDiaSemana())
                .horaInicio(s.getHoraInicio())
                .horaFin(s.getHoraFin())
                .notas(s.getNotas())
                .build();
    }

    private UserResponseDTO toUserDTO(Usuario u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .rol(u.getRol())
                .centroId(u.getCentro() != null ? u.getCentro().getId() : null)
                .instrumentoId(u.getInstrumento() != null ? u.getInstrumento().getId() : null)
                .build();
    }
}