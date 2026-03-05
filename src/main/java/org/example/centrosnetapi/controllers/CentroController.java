package org.example.centrosnetapi.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Centro.CenterRequestDTO;
import org.example.centrosnetapi.dtos.Centro.CenterResponseDTO;
import org.example.centrosnetapi.services.CentroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    public ResponseEntity<CenterResponseDTO> create(
            @Valid @RequestBody CenterRequestDTO dto
    ) {

        CenterResponseDTO created = centroService.create(dto);

        return ResponseEntity
                .created(URI.create("/api/centers/" + created.getId()))
                .body(created);
    }

    // ============================================================
    // READ ALL
    // ============================================================

    @GetMapping
    public ResponseEntity<List<CenterResponseDTO>> getAll() {
        return ResponseEntity.ok(centroService.findAll());
    }

    // ============================================================
    // READ BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<CenterResponseDTO> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(centroService.findById(id));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/{id}")
    public ResponseEntity<CenterResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CenterRequestDTO dto
    ) {

        return ResponseEntity.ok(centroService.update(id, dto));
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        centroService.delete(id);
    }
}