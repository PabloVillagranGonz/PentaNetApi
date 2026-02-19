package org.example.centrosnetapi.cabinas.controllers;

import org.example.centrosnetapi.cabinas.dtos.CrearReservaDTO;
import org.example.centrosnetapi.cabinas.models.Reserva;
import org.example.centrosnetapi.cabinas.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    public Reserva crearReserva(@RequestBody CrearReservaDTO dto) {
        return reservaService.crearReserva(
                dto.getUsuarioId(),
                dto.getAulaId(),
                dto.getDuracion()
        );
    }

    @PostMapping("/finalizar/{aulaId}")
    public void finalizarReservaPorAula(@PathVariable Long aulaId) {
        reservaService.finalizarReservaPorAula(aulaId);
    }

    @GetMapping("/activas")
    public List<Reserva> obtenerReservasActivas() {
        return reservaService.obtenerReservasActivas();
    }

    @GetMapping("/historial")
    public List<Reserva> historial() {
        return reservaService.obtenerHistorial();
    }
}