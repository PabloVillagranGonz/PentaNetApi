package org.example.centrosnetapi.cabinas.controllers;

import org.example.centrosnetapi.cabinas.dtos.AulaDisponibilidadDTO;
import org.example.centrosnetapi.cabinas.dtos.AulaResponseDTO;
import org.example.centrosnetapi.cabinas.dtos.EstadisticasAulasDTO;
import org.example.centrosnetapi.cabinas.models.Aula;
import org.example.centrosnetapi.cabinas.services.AulaService;
import org.example.centrosnetapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/aulas")
public class AulaController {

    private Long obtenerCenterIdDesdeJWT(Authentication auth) {

        User user = (User) auth.getPrincipal();

        return user.getCenter().getId();
    }

    @Autowired
    private AulaService aulaService;

    @GetMapping
    public List<Aula> listarAulas() {
        return aulaService.obtenerTodas();
    }

    @GetMapping("/libres")
    public List<Aula> aulasLibres() {
        return aulaService.obtenerLibres();
    }

    @GetMapping("/dashboard")
    public List<AulaResponseDTO> obtenerDashboard(Authentication auth) {

        Long centerId = obtenerCenterIdDesdeJWT(auth);

        return aulaService.obtenerAulasDashboard(centerId);
    }
    @GetMapping("/disponibilidad")
    public List<AulaDisponibilidadDTO> disponibilidad(Authentication auth) {

        Long centerId = obtenerCenterIdDesdeJWT(auth);

        return aulaService.obtenerDisponibilidadPorCentro(centerId);
    }

    @GetMapping("/estadisticas")
    public EstadisticasAulasDTO estadisticas(Authentication auth) {

        Long centerId = obtenerCenterIdDesdeJWT(auth);

        return aulaService.obtenerEstadisticas(centerId);
    }
}
