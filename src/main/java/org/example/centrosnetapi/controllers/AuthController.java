package org.example.centrosnetapi.controllers;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final CentroRepository centroRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
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
            @RequestBody ChangePasswordRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        authService.changePassword(usuario, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/debug/hash")
    public String hash(@RequestParam String raw) {
        return passwordEncoder.encode(raw);
    }
}