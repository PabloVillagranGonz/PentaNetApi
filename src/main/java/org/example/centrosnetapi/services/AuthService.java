package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Auth.ChangePasswordRequestDTO;
import org.example.centrosnetapi.dtos.Auth.LoginRequestDTO;
import org.example.centrosnetapi.dtos.Auth.LoginResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = normalizarEmail(request.getEmail());

        Usuario usuario = buscarUsuarioPorEmail(email);

        validarCentroSaaS(usuario, request.getCentroId());
        validarContrasena(request.getPassword(), usuario.getPassword(), "INVALID_PASSWORD");
        validarUsuarioActivo(usuario);

        String token = jwtService.generateToken(usuario);

        return toLoginResponseDTO(usuario, token);
    }

    public void changePassword(Usuario usuario, ChangePasswordRequestDTO dto) {
        validarContrasena(dto.getCurrentPassword(), usuario.getPassword(), "INVALID_CURRENT_PASSWORD");

        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones)
    // ============================================================

    private String normalizarEmail(String email) {
        return email != null ? email.toLowerCase().trim() : "";
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private void validarCentroSaaS(Usuario usuario, Long centroIdPeticion) {
        // Si el usuario pertenece a un centro, forzamos que el login coincida
        if (usuario.getCentro() != null) {
            if (centroIdPeticion == null || !usuario.getCentro().getId().equals(centroIdPeticion)) {
                throw new ApiException("USER_NOT_FOUND_IN_CENTRO", HttpStatus.NOT_FOUND);
            }
        }
        // Nota: El Super Admin (centro == null) pasa libremente.
    }

    private void validarContrasena(String rawPassword, String encodedPassword, String errorCode) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ApiException(errorCode, HttpStatus.UNAUTHORIZED);
        }
    }

    private void validarUsuarioActivo(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ApiException("USER_DISABLED", HttpStatus.FORBIDDEN);
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private LoginResponseDTO toLoginResponseDTO(Usuario usuario, String token) {
        return LoginResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .token(token)
                .centroId(usuario.getCentro() != null ? usuario.getCentro().getId() : null)
                .centroNombre(usuario.getCentro() != null ? usuario.getCentro().getNombre() : null)
                .cursoId(usuario.getCurso() != null ? usuario.getCurso().getId() : null)
                .cursoNombre(usuario.getCurso() != null ? usuario.getCurso().getNombre() : null)
                .instrumentoId(usuario.getInstrumento() != null ? usuario.getInstrumento().getId() : null)
                .instrumentoNombre(usuario.getInstrumento() != null ? usuario.getInstrumento().getNombre() : null)
                .build();
    }
}