package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Centro;
import org.example.centrosnetapi.models.Rol;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.example.centrosnetapi.repositories.SesionClaseRepository;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 🔥 Importante para operaciones de base de datos
public class ProfesorService {

    private final UsuarioRepository usuarioRepository;
    private final CentroRepository centroRepository;
    private final SesionClaseRepository sesionClaseRepository;

    // =============================
    // READ ALL TEACHERS
    // =============================
    public List<Usuario> findAllTeachers(Usuario adminLogueado) {
        // 🔥 CANDADO SAAS: Super Admin ve todos, Admin Local ve solo los suyos
        if (adminLogueado.getCentro() == null) {
            return usuarioRepository.findByRol(Rol.PROFESOR);
        } else {
            return usuarioRepository.findByCentroIdAndRol(adminLogueado.getCentro().getId(), Rol.PROFESOR);
        }
    }

    // =============================
    // READ TEACHERS BY CENTER
    // =============================
    public List<Usuario> findTeachersByCenter(Long centerId, Usuario adminLogueado) {
        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(centerId)) {
            throw new ApiException("ACCESO_DENEGADO_CENTRO", HttpStatus.FORBIDDEN);
        }
        return usuarioRepository.findByCentroIdAndRol(centerId, Rol.PROFESOR);
    }

    // =============================
    // ASSIGN TEACHER TO CENTER
    // =============================
    public Usuario assignToCenter(Long teacherId, Long centerId, Usuario adminLogueado) {

        // 🔥 CANDADO SAAS: Solo puedes asignarlo a tu propio centro
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(centerId)) {
            throw new ApiException("SOLO_PUEDES_ASIGNAR_A_TU_CENTRO", HttpStatus.FORBIDDEN);
        }

        Usuario teacher = usuarioRepository.findById(teacherId)
                .orElseThrow(() -> new ApiException("TEACHER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (teacher.getRol() != Rol.PROFESOR) {
            throw new ApiException("USER_IS_NOT_TEACHER", HttpStatus.BAD_REQUEST);
        }

        // 🔥 PROTECCIÓN ANTI-ROBO: Si el profesor ya tiene un centro distinto al tuyo, no puedes quitárselo
        if (adminLogueado.getCentro() != null && teacher.getCentro() != null
                && !teacher.getCentro().getId().equals(adminLogueado.getCentro().getId())) {
            throw new ApiException("EL_PROFESOR_PERTENECE_A_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        Centro centro = centroRepository.findById(centerId)
                .orElseThrow(() -> new ApiException("CENTER_NOT_FOUND", HttpStatus.NOT_FOUND));

        teacher.setCentro(centro);
        return usuarioRepository.save(teacher);
    }

    // =============================
    // REMOVE TEACHER FROM CENTER
    // =============================
    public void removeFromCenter(Long teacherId, Usuario adminLogueado) {

        Usuario teacher = usuarioRepository.findById(teacherId)
                .orElseThrow(() -> new ApiException("TEACHER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (teacher.getRol() != Rol.PROFESOR) {
            throw new ApiException("USER_IS_NOT_TEACHER", HttpStatus.BAD_REQUEST);
        }

        // 🔥 CANDADO SAAS: Solo puedes desasignar profesores de TU centro
        if (adminLogueado.getCentro() != null) {
            if (teacher.getCentro() == null || !teacher.getCentro().getId().equals(adminLogueado.getCentro().getId())) {
                throw new ApiException("NO_PUEDES_MODIFICAR_PROFESORES_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
            }
        }

        teacher.setCentro(null);
        usuarioRepository.save(teacher);
    }

    // =============================
    // STUDENTS FOR TEACHER
    // =============================
    public List<UserResponseDTO> getStudentsForTeacher(Long teacherId, Usuario usuarioLogueado) {

        Usuario teacher = usuarioRepository.findById(teacherId)
                .orElseThrow(() -> new ApiException("TEACHER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS / PRIVACIDAD:
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

        return sesionClaseRepository.findStudentsForTeacher(teacherId)
                .stream()
                .distinct()
                .map(this::toUserDTO)
                .toList();
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