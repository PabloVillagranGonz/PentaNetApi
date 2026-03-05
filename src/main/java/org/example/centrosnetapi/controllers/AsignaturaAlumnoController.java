package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Estudiante.StudentSubjectDTO;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.example.centrosnetapi.services.SesionClaseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin
public class AsignaturaAlumnoController {

    private final SesionClaseService sesionClaseService;
    private final UsuarioRepository userRepository;

    // 🔹 PROFESORES DEL ALUMNO
    @GetMapping("/{id}/teachers")
    public List<UserResponseDTO> getTeachers(@PathVariable Long id) {

        return sesionClaseService.findTeachersForStudent(id);
    }

    // 🔹 ASIGNATURAS DEL ALUMNO
    @GetMapping("/{id}/subjects")
    public List<StudentSubjectDTO> getStudentSubjects(@PathVariable Long id) {

        Usuario student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (student.getCurso() == null) {
            return List.of();
        }

        return sesionClaseService.findByCourseId(student.getCurso().getId())
                .stream()
                .map(cs -> new StudentSubjectDTO(
                        cs.getId(),
                        cs.getAsignaturaNombre(),
                        cs.getProfesorId() != null
                                ? cs.getProfesorNombreCompleto()
                                : "No asignado",
                        cs.getEspacioId() != null
                                ? cs.getEspacioNombre()
                                : "No asignado"
                ))
                .distinct()
                .toList();
    }

    // 🔹 HORARIO
    // 🔹 HORARIO
    @GetMapping("/{id}/schedule")
    public List<SesionClaseResponseDTO> getSchedule(@PathVariable Long id) {

        Usuario student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (student.getCurso() == null) {
            return List.of();
        }

        return sesionClaseService.findScheduleForStudent(
                student.getCurso().getId(),
                student.getId()
        );
    }
}