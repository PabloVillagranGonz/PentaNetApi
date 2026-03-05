package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Espacio.EspacioRequestDTO;
import org.example.centrosnetapi.dtos.Espacio.EspacioResponseDTO;
import org.example.centrosnetapi.services.EspacioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
@CrossOrigin
public class EspacioController {

    private final EspacioService espacioService;

    // ================= CREATE AULA =================
    @PostMapping("/aulas")
    public EspacioResponseDTO createAula(@RequestBody EspacioRequestDTO dto) {
        return espacioService.createAula(dto);
    }

    // ================= CREATE CABINA =================
    @PostMapping("/cabinas")
    public EspacioResponseDTO createCabina(@RequestBody EspacioRequestDTO dto) {
        return espacioService.createCabina(dto);
    }

    // READ BY CENTER
    @GetMapping("/centro/{centroId}")
    public List<EspacioResponseDTO> getByCentro(@PathVariable Long centroId) {
        return espacioService.findByCentro(centroId);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        espacioService.delete(id);
    }

    @GetMapping("/dashboard")
    public List<EspacioResponseDTO> dashboard() {
        return espacioService.dashboard();
    }

}