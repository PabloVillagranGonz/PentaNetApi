package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseRequestDTO;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;

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


    public List<SesionClaseResponseDTO> findScheduleForStudent(
            Long cursoId,
            Long alumnoId
    ) {
        return sesionRepository
                .findScheduleForStudent(cursoId, alumnoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<UserResponseDTO> getStudentsForSession(Long sessionId) {

        SesionClase sesion = sesionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));

        // =========================
        // 🎯 CLASE INDIVIDUAL
        // =========================
        if (sesion.getAlumno() != null) {

            Usuario alumno = sesion.getAlumno();

            return List.of(
                    UserResponseDTO.builder()
                            .id(alumno.getId())
                            .nombre(alumno.getNombre())
                            .apellidos(alumno.getApellidos())
                            .email(alumno.getEmail())
                            .rol(alumno.getRol())
                            .centroId(
                                    alumno.getCentro() != null ? alumno.getCentro().getId() : null
                            )
                            .cursoId(
                                    alumno.getCurso() != null ? alumno.getCurso().getId() : null
                            )
                            .build()
            );
        }

        // =========================
        // 🎯 CLASE COLECTIVA
        // =========================
        return usuarioRepository
                .findByCurso_IdAndRol(
                        sesion.getCurso().getId(),
                        Rol.ALUMNO
                )
                .stream()
                .map(this::toUserDTO) // ya lo tienes abajo en tu service
                .toList();
    }

    public Long crearSesion(SesionClaseRequestDTO dto) {

        Curso curso = cursoRepository.findById(dto.getCursoId())
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        Asignatura asignatura = asignaturaRepository.findById(dto.getAsignaturaId())
                .orElseThrow(() -> new IllegalArgumentException("Asignatura no encontrada"));

        Usuario profesor = usuarioRepository.findById(dto.getProfesorId())
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        Espacio espacio = espacioRepository.findById(dto.getEspacioId())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado"));

        validarDominio(curso, asignatura, profesor, espacio);

        if (asignatura.getTipo() == TipoAsignatura.COLECTIVA) {

            validarSolapamientos(dto, profesor.getId(), espacio.getId(), null);

            SesionClase sesion = construirSesion(dto, curso, asignatura, profesor, espacio, null);
            return sesionRepository.save(sesion).getId();

        } else {

            if (dto.getAlumnoId() != null) {

                Usuario alumno = usuarioRepository.findById(dto.getAlumnoId())
                        .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));

                validarSolapamientos(dto, profesor.getId(), espacio.getId(), alumno.getId());

                SesionClase sesion = construirSesion(dto, curso, asignatura, profesor, espacio, alumno);
                return sesionRepository.save(sesion).getId();

            } else {

                List<Usuario> alumnos = usuarioRepository.findByCursoId(curso.getId());

                if (alumnos.isEmpty()) {
                    throw new IllegalArgumentException("El curso no tiene alumnos");
                }

                Long lastId = null;

                for (Usuario alumno : alumnos) {

                    validarSolapamientos(dto, profesor.getId(), espacio.getId(), alumno.getId());

                    SesionClase sesion = construirSesion(dto, curso, asignatura, profesor, espacio, alumno);
                    lastId = sesionRepository.save(sesion).getId();
                }

                return lastId;
            }
        }
    }

    // ================= VALIDACIONES =================

    private void validarDominio(Curso curso,
                                Asignatura asignatura,
                                Usuario profesor,
                                Espacio espacio) {

        if (profesor.getRol() != Rol.PROFESOR) {
            throw new IllegalArgumentException("El usuario no tiene rol de profesor");
        }

        if (!asignaturaCursoRepository
                .existsByCursoIdAndAsignaturaId(curso.getId(), asignatura.getId())) {

            throw new IllegalArgumentException("La asignatura no pertenece al curso");
        }

        if (!curso.getCentro().getId().equals(espacio.getCentro().getId())) {
            throw new IllegalArgumentException("El espacio no pertenece al mismo centro");
        }
    }

    private void validarSolapamientos(SesionClaseRequestDTO dto,
                                      Long profesorId,
                                      Long espacioId,
                                      Long alumnoId) {

        if (sesionRepository.existsConflictingSessionForEspacio(
                espacioId,
                dto.getDiaSemana(),
                dto.getHoraInicio(),
                dto.getHoraFin())) {

            throw new IllegalArgumentException("El espacio ya está ocupado en ese horario");
        }

        if (sesionRepository.existsConflictingSessionForProfesor(
                profesorId,
                dto.getDiaSemana(),
                dto.getHoraInicio(),
                dto.getHoraFin())) {

            throw new IllegalArgumentException("El profesor ya tiene clase en ese horario");
        }

        if (alumnoId != null &&
                sesionRepository.existsConflictingSessionForAlumno(
                        alumnoId,
                        dto.getDiaSemana(),
                        dto.getHoraInicio(),
                        dto.getHoraFin())) {

            throw new IllegalArgumentException("El alumno ya tiene clase en ese horario");
        }
    }

    private SesionClase construirSesion(SesionClaseRequestDTO dto,
                                        Curso curso,
                                        Asignatura asignatura,
                                        Usuario profesor,
                                        Espacio espacio,
                                        Usuario alumno) {

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

    // ================= CONSULTAS =================

    public List<SesionClaseResponseDTO> obtenerPorCurso(Long cursoId) {

        List<SesionClase> sesiones =
                sesionRepository
                        .findByCurso_IdOrderByDiaSemanaAscHoraInicioAsc(cursoId);

        return sesiones.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<SesionClaseResponseDTO> obtenerPorProfesor(Long profesorId) {

        List<SesionClase> sesiones =
                sesionRepository
                        .findByProfesorIdOrderByDiaSemanaAscHoraInicioAsc(profesorId);

        return sesiones.stream()
                .map(this::toDTO)
                .toList();
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
                .profesorNombreCompleto(
                        s.getProfesor().getNombre() + " " +
                                s.getProfesor().getApellidos()
                )
                .alumnoId(s.getAlumno() != null ? s.getAlumno().getId() : null)
                .alumnoNombreCompleto(
                        s.getAlumno() != null
                                ? s.getAlumno().getNombre() + " " + s.getAlumno().getApellidos()
                                : null
                )
                .espacioId(s.getEspacio().getId())
                .espacioNombre(s.getEspacio().getNombre())
                .diaSemana(s.getDiaSemana())
                .horaInicio(s.getHoraInicio())
                .horaFin(s.getHoraFin())
                .notas(s.getNotas())
                .build();
    }

    // ==========================================
    // 📚 SESIONES POR CURSO (para horario)
    // ==========================================
        public List<SesionClaseResponseDTO> findByCourseId(Long cursoId) {

        return sesionRepository.findByCursoId(cursoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ==========================================
// 👨‍🏫 PROFESORES DEL ALUMNO
// ==========================================
    public List<UserResponseDTO> findTeachersForStudent(Long studentId) {

        Usuario student = usuarioRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (student.getCurso() == null) {
            return List.of();
        }

        return sesionRepository.findByCursoId(student.getCurso().getId())
                .stream()
                .map(SesionClase::getProfesor)
                .filter(p -> p != null)
                .distinct()
                .map(this::toUserDTO)
                .toList();
    }

    public List<SesionClaseResponseDTO> obtenerPorProfesorYDia(
            Long profesorId,
            Integer diaSemana
    ) {
        return sesionRepository
                .findByProfesorIdAndDiaSemanaOrderByHoraInicioAsc(
                        profesorId,
                        diaSemana
                )
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private UserResponseDTO toUserDTO(Usuario u) {

        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .rol(u.getRol())
                .centroId(
                        u.getCentro() != null ? u.getCentro().getId() : null
                )
                .instrumentoId(
                        u.getInstrumento() != null ? u.getInstrumento().getId() : null
                )
                .build();
    }
}