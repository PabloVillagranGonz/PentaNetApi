package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Curso.CourseRequestDTO;
import org.example.centrosnetapi.dtos.Curso.CourseResponseDTO;
import org.example.centrosnetapi.services.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // ================= CREATE =================

    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(
            @Valid @RequestBody CourseRequestDTO dto
    ) {
        CourseResponseDTO response = courseService.create(dto);

        return ResponseEntity
                .created(URI.create("/api/cursos/" + response.getId()))
                .body(response);
    }

    // ================= READ =================

    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAll() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<CourseResponseDTO>> getByCenter(
            @PathVariable Long centroId
    ) {
        return ResponseEntity.ok(courseService.findByCenter(centroId));
    }

    // ================= RELACIÓN =================

    @PostMapping("/{courseId}/asignaturas/{subjectId}")
    public ResponseEntity<Void> addSubjectToCourse(
            @PathVariable Long courseId,
            @PathVariable Long subjectId
    ) {
        courseService.addSubjectToCourse(courseId, subjectId);
        return ResponseEntity.noContent().build();
    }

    // ================= UPDATE =================

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDTO dto
    ) {
        return ResponseEntity.ok(courseService.update(id, dto));
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}