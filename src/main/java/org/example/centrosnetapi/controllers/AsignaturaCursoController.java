package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.AsignaturaCursoService;
import org.example.centrosnetapi.dtos.AsignaturaCurso.AsignaturaCursoRequestDTO;
import org.example.centrosnetapi.dtos.AsignaturaCurso.AsignaturaCursoResponseDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas-curso")
@RequiredArgsConstructor
@CrossOrigin
public class AsignaturaCursoController {

    private final AsignaturaCursoService asignaturaCursoService;

    @PostMapping
    public AsignaturaCursoResponseDTO asignar(
            @Valid @RequestBody AsignaturaCursoRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return asignaturaCursoService.asignar(dto, adminLogueado);
    }

    @GetMapping("/curso/{cursoId}")
    public List<AsignaturaCursoResponseDTO> obtenerPorCurso(
            @PathVariable Long cursoId,
            @AuthenticationPrincipal Usuario usuarioLogueado
    ) {
        return asignaturaCursoService.obtenerPorCurso(cursoId, usuarioLogueado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        asignaturaCursoService.eliminar(id, adminLogueado);
    }
}