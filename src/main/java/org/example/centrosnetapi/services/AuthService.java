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

    public LoginResponseDTO login(LoginRequestDTO request) {

        String email = request.getEmail().toLowerCase().trim();
        Long centroIdPeticion = request.getCentroId();

        // 1. Buscamos al usuario ÚNICAMENTE por email primero
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. 🔥 VALIDACIÓN INTELIGENTE SAAS (Excepción Super Admin)
        // Si el usuario en la BD tiene un centro asignado, verificamos que coincida con el login.
        if (usuario.getCentro() != null) {
            if (centroIdPeticion == null || !usuario.getCentro().getId().equals(centroIdPeticion)) {
                throw new ApiException("USER_NOT_FOUND_IN_CENTRO", HttpStatus.NOT_FOUND);
            }
        }
        // 💡 NOTA: Si usuario.getCentro() es NULL, saltamos el IF.
        // Esto permite que el Super Admin entre aunque centroIdPeticion sea 0, 1 o null.

        // 3. Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ApiException("INVALID_PASSWORD", HttpStatus.UNAUTHORIZED);
        }

        // 4. Verificar si está activo
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ApiException("USER_DISABLED", HttpStatus.FORBIDDEN);
        }

        // 5. Generar token
        String token = jwtService.generateToken(usuario);

        // 6. Devolver respuesta
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

    public void changePassword(Usuario usuario, ChangePasswordRequestDTO dto) {
        if (!passwordEncoder.matches(dto.getCurrentPassword(), usuario.getPassword())) {
            throw new ApiException("INVALID_CURRENT_PASSWORD", HttpStatus.UNAUTHORIZED);
        }
        usuario.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);
    }
}