package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Espacio.EspacioRequestDTO;
import org.example.centrosnetapi.dtos.Espacio.EspacioResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.EspacioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
@CrossOrigin
public class EspacioController {

    private final EspacioService espacioService;

    // ============================================================
    // READ ALL (Faltaba este método para el Superadmin)
    // ============================================================
    @GetMapping
    public List<EspacioResponseDTO> getAll(@AuthenticationPrincipal Usuario adminLogueado) {
        return espacioService.findAll(adminLogueado);
    }

    @PostMapping("/aulas")
    public EspacioResponseDTO createAula(
            @RequestBody EspacioRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return espacioService.createAula(dto, adminLogueado);
    }

    @PostMapping("/cabinas")
    public EspacioResponseDTO createCabina(
            @RequestBody EspacioRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return espacioService.createCabina(dto, adminLogueado);
    }

    @GetMapping("/centro/{centroId}")
    public List<EspacioResponseDTO> getByCentro(
            @PathVariable Long centroId,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        return espacioService.findByCentro(centroId, adminLogueado);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        espacioService.delete(id, adminLogueado);
    }

    @GetMapping("/dashboard")
    public List<EspacioResponseDTO> dashboard(@AuthenticationPrincipal Usuario adminLogueado) {
        return espacioService.dashboard(adminLogueado);
    }
}