package org.example.centrosnetapi.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Auth.ChangePasswordRequestDTO;
import org.example.centrosnetapi.dtos.Auth.LoginRequestDTO;
import org.example.centrosnetapi.dtos.Auth.LoginResponseDTO;
import org.example.centrosnetapi.dtos.Centro.CentroPublicDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.example.centrosnetapi.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final CentroRepository centroRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        } else {
            ipAddress = ipAddress.split(",")[0];
        }
        return ResponseEntity.ok(authService.login(request, ipAddress));
    }

    @GetMapping("/centros")
    public ResponseEntity<List<CentroPublicDTO>> getCentrosPublicos() {
        List<CentroPublicDTO> centros = centroRepository.findAll().stream()
                .map(c -> new CentroPublicDTO(c.getId(), c.getNombre()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(centros);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        authService.changePassword(usuario, dto);
        return ResponseEntity.noContent().build();
    }

}