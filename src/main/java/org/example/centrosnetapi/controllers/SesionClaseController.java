package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseRequestDTO;
import org.example.centrosnetapi.dtos.SesionClase.SesionClaseResponseDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.services.SesionClaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionClaseController {

    private final SesionClaseService sesionClaseService;

    @PostMapping
    public ResponseEntity<Void> crear(@Valid @RequestBody SesionClaseRequestDTO dto) {

        Long id = sesionClaseService.crearSesion(dto);

        return ResponseEntity
                .created(URI.create("/api/sesiones/" + id))
                .build();
    }

    @GetMapping("/{sessionId}/alumnos")
    public List<UserResponseDTO> getStudentsForSession(@PathVariable Long sessionId) {
        return sesionClaseService.getStudentsForSession(sessionId);
    }
    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<SesionClaseResponseDTO>> porCurso(@PathVariable Long cursoId) {
        return ResponseEntity.ok(sesionClaseService.obtenerPorCurso(cursoId));
    }

    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<SesionClaseResponseDTO>> porProfesor(
            @PathVariable Long profesorId,
            @RequestParam(required = false) Integer day
    ) {

        if (day != null) {
            return ResponseEntity.ok(
                    sesionClaseService.obtenerPorProfesorYDia(profesorId, day)
            );
        }

        return ResponseEntity.ok(
                sesionClaseService.obtenerPorProfesor(profesorId)
        );
    }
}