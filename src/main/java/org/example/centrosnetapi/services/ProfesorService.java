package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Centro;
import org.example.centrosnetapi.models.Rol;
import org.example.centrosnetapi.models.SesionClase;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.example.centrosnetapi.repositories.SesionClaseRepository;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfesorService {

    private final UsuarioRepository usuarioRepository;
    private final CentroRepository centroRepository;
    private final SesionClaseRepository sesionClaseRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    public List<Usuario> findAllTeachers(Usuario adminLogueado) {
        if (adminLogueado.getCentro() == null) {
            return usuarioRepository.findByRol(Rol.PROFESOR);
        }
        return usuarioRepository.findByCentroIdAndRol(adminLogueado.getCentro().getId(), Rol.PROFESOR);
    }

    public List<Usuario> findTeachersByCenter(Long centerId, Usuario adminLogueado) {
        validarAccesoAlCentro(adminLogueado, centerId, "ACCESO_DENEGADO_CENTRO");

        return usuarioRepository.findByCentroIdAndRol(centerId, Rol.PROFESOR);
    }

    public Usuario assignToCenter(Long teacherId, Long centerId, Usuario adminLogueado) {
        validarAccesoAlCentro(adminLogueado, centerId, "SOLO_PUEDES_ASIGNAR_A_TU_CENTRO");
        Usuario teacher = buscarProfesor(teacherId);
        validarAntiRobo(teacher, adminLogueado);

        Centro centro = buscarCentro(centerId);
        teacher.setCentro(centro);

        return usuarioRepository.save(teacher);
    }

    public void removeFromCenter(Long teacherId, Usuario adminLogueado) {
        Usuario teacher = buscarProfesor(teacherId);
        validarPertenenciaDelProfesor(teacher, adminLogueado, "NO_PUEDES_MODIFICAR_PROFESORES_DE_OTRO_CENTRO");

        teacher.setCentro(null);
        usuarioRepository.save(teacher);
    }

    public List<UserResponseDTO> getStudentsForTeacher(Long teacherId, Usuario usuarioLogueado) {
        Usuario teacher = buscarProfesor(teacherId);
        validarPrivacidadAlumnos(teacherId, teacher, usuarioLogueado);

        return sesionClaseRepository.findStudentsForTeacher(teacherId)
                .stream()
                .distinct()
                .map(this::toUserDTO)
                .toList();
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones y Buscadores)
    // ============================================================

    private Usuario buscarProfesor(Long teacherId) {
        Usuario teacher = usuarioRepository.findById(teacherId)
                .orElseThrow(() -> new ApiException("TEACHER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (teacher.getRol() != Rol.PROFESOR) {
            throw new ApiException("USER_IS_NOT_TEACHER", HttpStatus.BAD_REQUEST);
        }
        return teacher;
    }

    private Centro buscarCentro(Long centerId) {
        return centroRepository.findById(centerId)
                .orElseThrow(() -> new ApiException("CENTER_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private void validarAccesoAlCentro(Usuario admin, Long centerId, String mensajeError) {
        if (admin.getCentro() != null && !admin.getCentro().getId().equals(centerId)) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private void validarAntiRobo(Usuario teacher, Usuario adminLogueado) {
        // Si el profesor ya tiene un centro distinto al tuyo, no puedes quitárselo
        if (adminLogueado.getCentro() != null && teacher.getCentro() != null
                && !teacher.getCentro().getId().equals(adminLogueado.getCentro().getId())) {
            throw new ApiException("EL_PROFESOR_PERTENECE_A_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }
    }

    private void validarPertenenciaDelProfesor(Usuario teacher, Usuario adminLogueado, String mensajeError) {
        // Para desasignar, el profesor debe pertenecer estrictamente a tu centro
        if (adminLogueado.getCentro() != null) {
            if (teacher.getCentro() == null || !teacher.getCentro().getId().equals(adminLogueado.getCentro().getId())) {
                throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
            }
        }
    }

    private void validarPrivacidadAlumnos(Long teacherId, Usuario teacher, Usuario usuarioLogueado) {
        if (usuarioLogueado.getRol() == Rol.PROFESOR) {
            // Un profesor solo puede pedir la lista de SUS propios alumnos
            if (!usuarioLogueado.getId().equals(teacherId)) {
                throw new ApiException("SOLO_PUEDES_VER_TUS_PROPIOS_ALUMNOS", HttpStatus.FORBIDDEN);
            }
        } else if (usuarioLogueado.getCentro() != null) {
            // Un Admin/Secretaria solo puede ver los alumnos de un profesor si está en SU centro
            if (teacher.getCentro() == null || !teacher.getCentro().getId().equals(usuarioLogueado.getCentro().getId())) {
                throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
            }
        }
    }

    public List<SesionClaseResponseDTO> findScheduleByTeacherId(Long teacherId) {
        List<SesionClase> sesiones = sesionClaseRepository.findByProfesorId(teacherId);

        return sesiones.stream().map(s -> SesionClaseResponseDTO.builder()
                .id(s.getId())
                .asignaturaId(s.getAsignatura().getId())
                .asignaturaNombre(s.getAsignatura().getNombre())
                .cursoId(s.getCurso().getId())
                .cursoNombre(s.getCurso().getNombre())
                .espacioNombre(s.getEspacio() != null ? s.getEspacio().getNombre() : "Sin aula")
                .diaSemana(s.getDiaSemana())
                .horaInicio(s.getHoraInicio())
                .horaFin(s.getHoraFin())
                .build()
        ).toList();
    }
    // ============================================================
    // MAPPER
    // ============================================================

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