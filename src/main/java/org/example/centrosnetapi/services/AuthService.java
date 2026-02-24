package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.ChangePasswordRequestDTO;
import org.example.centrosnetapi.dtos.LoginRequest;
import org.example.centrosnetapi.dtos.UserResponseDTO;
import org.example.centrosnetapi.models.Role;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ================= LOGIN =================
    public UserResponseDTO login(LoginRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        System.out.println("LOGIN DEBUG");
        System.out.println("RAW: " + request.getPassword());
        System.out.println("HASH DB: " + user.getPassword());
        System.out.println("MATCH: " +
                passwordEncoder.matches(request.getPassword(), user.getPassword()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("INVALID_PASSWORD");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("USER_DISABLED");
        }

        // 🔐 GENERAR TOKEN
        String token = jwtService.generateToken(user);

        return UserResponseDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellidos(user.getApellidos())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)

                .phone(user.getPhone())
                .dni(user.getDni())

                .centerId(user.getCenter() != null ? user.getCenter().getId() : null)
                .centerName(user.getCenter() != null ? user.getCenter().getName() : null)

                .instrumentId(user.getInstrument() != null ? user.getInstrument().getId() : null)
                .instrumentName(user.getInstrument() != null ? user.getInstrument().getName() : null)

                .courseId(user.getCourse() != null ? user.getCourse().getId() : null)
                .courseName(user.getCourse() != null ? user.getCourse().getName() : null)
                .build();
    }

    // ================= CHANGE PASSWORD =================
    public void changePassword(User user, ChangePasswordRequestDTO dto) {

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("INVALID_CURRENT_PASSWORD");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}