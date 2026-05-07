package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Reserva.CrearReservaDTO;
import org.example.centrosnetapi.dtos.Reserva.ReservaResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.ReservaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@CrossOrigin
@PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA')") // Solo personal autorizado
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ReservaResponseDTO crearReserva(
            @Valid @RequestBody CrearReservaDTO dto,
            @AuthenticationPrincipal Usuario secretarioLogueado
    ) {
        return reservaService.crearReserva(dto, secretarioLogueado);
    }

    @PostMapping("/finalizar/{espacioId}")
    public void finalizarReservaPorEspacio(
            @PathVariable Long espacioId,
            @AuthenticationPrincipal Usuario secretarioLogueado
    ) {
        reservaService.finalizarReservaPorEspacio(espacioId, secretarioLogueado);
    }

    @GetMapping("/activas")
    public List<ReservaResponseDTO> obtenerReservasActivas(
            @AuthenticationPrincipal Usuario secretarioLogueado
    ) {
        return reservaService.obtenerReservasActivas(secretarioLogueado);
    }

    @GetMapping("/historial")
    public List<ReservaResponseDTO> historial(
            @AuthenticationPrincipal Usuario secretarioLogueado
    ) {
        return reservaService.obtenerHistorial(secretarioLogueado);
    }
}