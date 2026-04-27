package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Estudiante.StudentSubjectDTO;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.SesionClaseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin
public class AsignaturaAlumnoController {

    private final SesionClaseService sesionClaseService;

    // 🔹 PROFESORES DEL ALUMNO
    @GetMapping("/{id}/teachers")
    public List<UserResponseDTO> getTeachers(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogueado
    ) {
        return sesionClaseService.findTeachersForStudent(id, usuarioLogueado);
    }

    // 🔹 ASIGNATURAS DEL ALUMNO
    @GetMapping("/{id}/subjects")
    public List<StudentSubjectDTO> getStudentSubjects(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogueado
    ) {
        // Toda la lógica de buscar al alumno y sus asignaturas ahora debe estar en el Service
        return sesionClaseService.findStudentSubjects(id, usuarioLogueado);
    }

    // 🔹 HORARIO
    @GetMapping("/{id}/schedule")
    public List<SesionClaseResponseDTO> getSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogueado
    ) {
        // La lógica de buscar el curso del alumno también pasa al Service
        return sesionClaseService.findScheduleForStudent(id, usuarioLogueado);
    }
}