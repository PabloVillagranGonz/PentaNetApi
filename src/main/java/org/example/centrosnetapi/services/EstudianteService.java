package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.models.SesionClase;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.example.centrosnetapi.repositories.SesionClaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final SesionClaseRepository sesionClaseRepository;

    // ==========================================
    // 👨‍🏫 PROFESORES DEL ALUMNO
    // ==========================================
    public List<UserResponseDTO> getTeachersForStudent(Long studentId) {

        Usuario student = usuarioRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (student.getCurso() == null) {
            return List.of();
        }

        List<SesionClase> sesiones =
                sesionClaseRepository.findByCursoId(student.getCurso().getId());

        return sesiones.stream()
                .map(SesionClase::getProfesor)
                .filter(p -> p != null)
                .distinct()
                .map(this::toUserDTO)
                .toList();
    }

    // ==========================================
    // 📚 SESIONES DEL ALUMNO (BASE PARA HORARIO)
    // ==========================================
    public List<SesionClase> getSesionesForStudent(Long studentId) {

        Usuario student = usuarioRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (student.getCurso() == null) {
            return List.of();
        }

        return sesionClaseRepository.findByCursoId(student.getCurso().getId());
    }

    // ==========================================
    // 🎓 CURSO DEL ALUMNO
    // ==========================================
    public Usuario getStudent(Long studentId) {

        return usuarioRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    // ==========================================
    // 🔁 MAPPER
    // ==========================================
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