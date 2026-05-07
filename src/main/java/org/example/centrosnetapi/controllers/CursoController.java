package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Curso.CourseRequestDTO;
import org.example.centrosnetapi.dtos.Curso.CourseResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
@CrossOrigin
@PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
public class CursoController {

    private final CourseService courseService;

    @GetMapping("/{id}/asignaturas")
    public ResponseEntity<List<Long>> getCourseSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getSubjectIdsByCourse(id));
    }

    @PutMapping("/{id}/asignaturas")
    public ResponseEntity<Void> syncSubjects(
            @PathVariable Long id,
            @Valid @RequestBody List<Long> subjectIds,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        courseService.syncSubjects(id, subjectIds, adminLogueado);
        return ResponseEntity.noContent().build();
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(
            @Valid @RequestBody CourseRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        CourseResponseDTO response = courseService.create(dto, adminLogueado);

        return ResponseEntity
                .created(URI.create("/api/cursos/" + response.getId()))
                .body(response);
    }

    // ================= READ =================
    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAll(
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(courseService.findAll(adminLogueado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(courseService.findById(id, adminLogueado));
    }

    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<CourseResponseDTO>> getByCenter(
            @PathVariable Long centroId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(courseService.findByCenter(centroId, adminLogueado));
    }

    // ================= RELACIÓN =================
    @PostMapping("/{courseId}/asignaturas/{subjectId}")
    public ResponseEntity<Void> addSubjectToCourse(
            @PathVariable Long courseId,
            @PathVariable Long subjectId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        courseService.addSubjectToCourse(courseId, subjectId, adminLogueado);
        return ResponseEntity.noContent().build();
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(courseService.update(id, dto, adminLogueado));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        courseService.delete(id, adminLogueado);
        return ResponseEntity.noContent().build();
    }
}