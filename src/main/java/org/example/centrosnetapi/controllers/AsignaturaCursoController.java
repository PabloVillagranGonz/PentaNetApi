package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.services.AsignaturaCursoService;
import org.example.centrosnetapi.dtos.AsignaturaCurso.AsignaturaCursoRequestDTO;
import org.example.centrosnetapi.dtos.AsignaturaCurso.AsignaturaCursoResponseDTO;
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
            @RequestBody AsignaturaCursoRequestDTO dto
    ) {
        return asignaturaCursoService.asignar(dto);
    }

    @GetMapping("/curso/{cursoId}")
    public List<AsignaturaCursoResponseDTO> obtenerPorCurso(
            @PathVariable Long cursoId
    ) {
        return asignaturaCursoService.obtenerPorCurso(cursoId);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        asignaturaCursoService.eliminar(id);
    }
}