package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.TeacherResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.ProfesorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@CrossOrigin
public class ProfesorController {

    private final ProfesorService teacherService;

    // =============================
    // ALL TEACHERS
    // =============================
    @GetMapping
    public List<TeacherResponseDTO> getAll() {
        return teacherService.findAllTeachers()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =============================
    // TEACHERS BY CENTER
    // =============================
    @GetMapping("/center/{centerId}")
    public List<TeacherResponseDTO> getByCenter(@PathVariable Long centerId) {
        return teacherService.findTeachersByCenter(centerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =============================
    // ASSIGN TO CENTER
    // =============================
    @PutMapping("/{teacherId}/center/{centerId}")
    public TeacherResponseDTO assignToCenter(
            @PathVariable Long teacherId,
            @PathVariable Long centerId
    ) {
        return toDTO(
                teacherService.assignToCenter(teacherId, centerId)
        );
    }

    // =============================
    // REMOVE FROM CENTER
    // =============================
    @DeleteMapping("/{teacherId}/center")
    public void removeFromCenter(@PathVariable Long teacherId) {
        teacherService.removeFromCenter(teacherId);
    }

    // =============================
    // STUDENTS FOR TEACHER
    // =============================
    @GetMapping("/{id}/students")
    public List<UserResponseDTO> getStudents(@PathVariable Long id) {
        return teacherService.getStudentsForTeacher(id);
    }

    private TeacherResponseDTO toDTO(Usuario u) {
        return TeacherResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .centroId(u.getCentro() != null ? u.getCentro().getId() : null)
                .centroNombre(u.getCentro() != null ? u.getCentro().getNombre() : null)
                .build();
    }
}