package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Reserva.CrearReservaDTO;
import org.example.centrosnetapi.dtos.Reserva.ReservaResponseDTO;
import org.example.centrosnetapi.models.Reserva;
import org.example.centrosnetapi.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@CrossOrigin
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ReservaResponseDTO crearReserva(@RequestBody CrearReservaDTO dto) {
        return reservaService.crearReserva(
                dto.getUsuarioId(),
                dto.getAulaId(),
                dto.getDuracion()
        );
    }

    @PostMapping("/finalizar/{espacioId}")
    public void finalizarReservaPorEspacio(@PathVariable Long espacioId) {
        reservaService.finalizarReservaPorEspacio(espacioId);
    }

    @GetMapping("/activas")
    public List<ReservaResponseDTO> obtenerReservasActivas() {
        return reservaService.obtenerReservasActivas();
    }

    @GetMapping("/historial")
    public List<ReservaResponseDTO> historial() {
        return reservaService.obtenerHistorial();
    }
}