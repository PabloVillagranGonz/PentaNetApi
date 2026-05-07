package org.example.centrosnetapi.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Centro.CenterRequestDTO;
import org.example.centrosnetapi.dtos.Centro.CenterResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.CentroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/centers")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Centers", description = "Gestión de centros")
public class CentroController {

    private final CentroService centroService;

    @PostMapping
    public ResponseEntity<CenterResponseDTO> create(
            @Valid @RequestBody CenterRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        CenterResponseDTO created = centroService.create(dto, adminLogueado);
        return ResponseEntity
                .created(URI.create("/api/centers/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<CenterResponseDTO>> getAll(
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(centroService.findAll(adminLogueado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CenterResponseDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(centroService.findById(id, adminLogueado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CenterResponseDTO> update(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody CenterRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return ResponseEntity.ok(centroService.update(id, dto, adminLogueado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        centroService.delete(id, adminLogueado);
    }
}