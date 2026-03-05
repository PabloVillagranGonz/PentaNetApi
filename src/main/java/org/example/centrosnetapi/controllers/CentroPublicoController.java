package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Centro.CenterResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Centro;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/centros")
@RequiredArgsConstructor
@CrossOrigin
public class CentroPublicoController {

    private final CentroRepository centroRepository;

    // 🔎 Obtener centro por ID (público)
    @GetMapping("/{id}")
    public CenterResponseDTO getCenter(@PathVariable Long id) {

        Centro centro = centroRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("CENTER_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        return toDTO(centro);
    }

    // 🔎 Obtener centro del usuario autenticado
    @GetMapping("/me")
    public CenterResponseDTO getMyCenter(
            @AuthenticationPrincipal Usuario usuario
    ) {

        if (usuario.getCentro() == null) {
            throw new ApiException(
                    "CENTER_NOT_ASSIGNED",
                    HttpStatus.NOT_FOUND
            );
        }

        return toDTO(usuario.getCentro());
    }

    private CenterResponseDTO toDTO(Centro c) {

        return CenterResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .telefono(c.getTelefono())
                .email(c.getEmail())
                .website(c.getWebsite())
                .direccion(c.getDireccion())
                .ciudad(c.getCiudad())
                .horarioApertura(c.getHorarioApertura())
                .codigoPostal(c.getCodigoPostal())
                .build();
    }
}