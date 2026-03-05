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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfesorService {

    private final UsuarioRepository usuarioRepository;
    private final CentroRepository centroRepository;
    private final SesionClaseRepository sesionClaseRepository;

    // =============================
    // READ ALL TEACHERS
    // =============================
    public List<Usuario> findAllTeachers() {
        return usuarioRepository.findByRol(Rol.PROFESOR);
    }

    // =============================
    // READ TEACHERS BY CENTER
    // =============================
    public List<Usuario> findTeachersByCenter(Long centerId) {
        return usuarioRepository.findByCentroIdAndRol(centerId, Rol.PROFESOR);
    }

    // =============================
    // ASSIGN TEACHER TO CENTER
    // =============================
    public Usuario assignToCenter(Long teacherId, Long centerId) {

        Usuario teacher = usuarioRepository.findById(teacherId)
                .orElseThrow(() ->
                        new ApiException("TEACHER_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        if (teacher.getRol() != Rol.PROFESOR) {
            throw new ApiException(
                    "USER_IS_NOT_TEACHER",
                    HttpStatus.BAD_REQUEST
            );
        }

        Centro centro = centroRepository.findById(centerId)
                .orElseThrow(() ->
                        new ApiException("CENTER_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        teacher.setCentro(centro);
        return usuarioRepository.save(teacher);
    }

    // =============================
    // REMOVE TEACHER FROM CENTER
    // =============================
    public void removeFromCenter(Long teacherId) {

        Usuario teacher = usuarioRepository.findById(teacherId)
                .orElseThrow(() ->
                        new ApiException("TEACHER_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        if (teacher.getRol() != Rol.PROFESOR) {
            throw new ApiException(
                    "USER_IS_NOT_TEACHER",
                    HttpStatus.BAD_REQUEST
            );
        }

        teacher.setCentro(null);
        usuarioRepository.save(teacher);
    }

    // =============================
    // STUDENTS FOR TEACHER
    // =============================
    public List<UserResponseDTO> getStudentsForTeacher(Long teacherId) {

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
                .centroId(
                        u.getCentro() != null ? u.getCentro().getId() : null
                )
                .instrumentoId(
                        u.getInstrumento() != null ? u.getInstrumento().getId() : null
                )
                .build();
    }
}