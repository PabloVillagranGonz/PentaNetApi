package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Asignatura.SubjectRequestDTO;
import org.example.centrosnetapi.dtos.Asignatura.SubjectResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.AsignaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin
public class AsignaturaController {

    private final AsignaturaService subjectService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<SubjectResponseDTO> create(
            @jakarta.validation.Valid @RequestBody SubjectRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(subjectService.create(dto, adminLogueado));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> update(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody SubjectRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(subjectService.update(id, dto, adminLogueado));
    }

    // ================= READ =================
    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAll(
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(subjectService.findAll(adminLogueado));
    }

    @GetMapping("/center/{centerId}")
    public ResponseEntity<List<SubjectResponseDTO>> getByCenter(
            @PathVariable Long centerId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(subjectService.findByCenter(centerId, adminLogueado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(subjectService.findById(id, adminLogueado));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        subjectService.delete(id, adminLogueado);
        return ResponseEntity.noContent().build();
    }

    // ================= MINE =================
    @GetMapping("/mine")
    public ResponseEntity<List<SubjectResponseDTO>> getMySubjects(
            @AuthenticationPrincipal Usuario usuario
    ) {
        // Este ya lo tenías bien configurado
        return ResponseEntity.ok(subjectService.getSubjectsForTeacher(usuario));
    }
}