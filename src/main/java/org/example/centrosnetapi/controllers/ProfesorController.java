package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.TeacherResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.ProfesorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA')")
    public List<TeacherResponseDTO> getAll(@AuthenticationPrincipal Usuario adminLogueado) {
        return teacherService.findAllTeachers(adminLogueado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =============================
    // TEACHERS BY CENTER
    // =============================
    @GetMapping("/center/{centerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA')")
    public List<TeacherResponseDTO> getByCenter(
            @PathVariable Long centerId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return teacherService.findTeachersByCenter(centerId, adminLogueado)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =============================
    // ASSIGN TO CENTER
    // =============================
    @PutMapping("/{teacherId}/center/{centerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponseDTO assignToCenter(
            @PathVariable Long teacherId,
            @PathVariable Long centerId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return toDTO(
                teacherService.assignToCenter(teacherId, centerId, adminLogueado)
        );
    }

    // =============================
    // REMOVE FROM CENTER
    // =============================
    @DeleteMapping("/{teacherId}/center")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeFromCenter(
            @PathVariable Long teacherId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        teacherService.removeFromCenter(teacherId, adminLogueado);
    }

    // =============================
    // STUDENTS FOR TEACHER
    // =============================
    @GetMapping("/{id}/students")
    @PreAuthorize("isAuthenticated()")
    public List<UserResponseDTO> getStudents(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogueado
    ) {
        return teacherService.getStudentsForTeacher(id, usuarioLogueado);
    }

    // =============================
    // HORARIO (SESIONES) DEL PROFESOR
    // =============================
    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA', 'PROFESOR')")
    public List<SesionClaseResponseDTO> getTeacherSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogueado
    ) {
        // Llamamos al servicio para obtener las sesiones
        return teacherService.findScheduleByTeacherId(id);
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