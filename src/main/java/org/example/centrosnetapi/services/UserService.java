package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.UpdateUserDTO;
import org.example.centrosnetapi.dtos.UserRequestDTO;
import org.example.centrosnetapi.dtos.UserResponseDTO;
import org.example.centrosnetapi.models.Center;
import org.example.centrosnetapi.models.Role;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.CenterRepository;
import org.example.centrosnetapi.repositories.CourseRepository;
import org.example.centrosnetapi.repositories.InstrumentRepository;
import org.example.centrosnetapi.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final CourseRepository courseRepository;
    private final CenterRepository centerRepository;

    // ============================================================
    // CREATE
    // ============================================================

    public void create(UserRequestDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = auth.getAuthorities().iterator().next().getAuthority();

        // 🔒 Nadie puede crear ADMIN
        if (dto.getRole() == Role.ADMIN) {
            throw new RuntimeException("CANNOT_CREATE_ADMIN");
        }

        // 🔒 SECRETARIA solo puede crear STUDENT
        if (currentRole.equals("ROLE_SECRETARIA")) {
            if (dto.getRole() != Role.STUDENT) {
                throw new RuntimeException("SECRETARIA_CAN_ONLY_CREATE_STUDENTS");
            }
        }

        validateDni(dto.getDni());

        User user = new User();

        user.setNombre(dto.getNombre());
        user.setApellidos(dto.getApellidos());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone());
        user.setActive(true);

        if (dto.getDni() != null) {
            user.setDni(dto.getDni().toUpperCase().trim());
        }

        if (dto.getCenter_id() != null) {
            user.setCenter(
                    centerRepository.findById(dto.getCenter_id())
                            .orElseThrow(() -> new RuntimeException("CENTER_NOT_FOUND"))
            );
        }

        if (dto.getInstrument_id() != null) {
            user.setInstrument(
                    instrumentRepository.findById(dto.getInstrument_id())
                            .orElseThrow(() -> new RuntimeException("INSTRUMENT_NOT_FOUND"))
            );
        }

        if (dto.getCourse_id() != null) {
            user.setCourse(
                    courseRepository.findById(dto.getCourse_id())
                            .orElseThrow(() -> new RuntimeException("COURSE_NOT_FOUND"))
            );
        }

        userRepository.save(user);
    }

    // ============================================================
    // READ
    // ============================================================

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserDTO)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        return toUserDTO(user);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public void update(Long id, UpdateUserDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = auth.getAuthorities().iterator().next().getAuthority();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 🔒 No se puede editar un ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("CANNOT_EDIT_ADMIN_USER");
        }

        if (dto.getNombre() != null) user.setNombre(dto.getNombre());
        if (dto.getApellidos() != null) user.setApellidos(dto.getApellidos());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());

        if (dto.getDni() != null) {
            validateDni(dto.getDni());
            user.setDni(dto.getDni().toUpperCase().trim());
        }

        if (dto.getRole() != null) {

            // 🔒 Nadie puede asignar ADMIN
            if (dto.getRole() == Role.ADMIN) {
                throw new RuntimeException("NO_PERMISSION_TO_ASSIGN_ADMIN");
            }

            // 🔒 SECRETARIA no puede cambiar roles
            if (currentRole.equals("ROLE_SECRETARIA")) {
                throw new RuntimeException("SECRETARIA_CANNOT_CHANGE_ROLES");
            }

            user.setRole(dto.getRole());
        }

        if (dto.getCenter_id() != null) {
            user.setCenter(
                    centerRepository.findById(dto.getCenter_id())
                            .orElseThrow(() -> new RuntimeException("CENTER_NOT_FOUND"))
            );
        }

        if (dto.getCourse_id() != null) {
            user.setCourse(
                    courseRepository.findById(dto.getCourse_id())
                            .orElseThrow(() -> new RuntimeException("COURSE_NOT_FOUND"))
            );
        }

        if (dto.getInstrument_id() != null) {
            user.setInstrument(
                    instrumentRepository.findById(dto.getInstrument_id())
                            .orElseThrow(() -> new RuntimeException("INSTRUMENT_NOT_FOUND"))
            );
        }

        userRepository.save(user);
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void deleteById(Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = auth.getAuthorities().iterator().next().getAuthority();

        if (currentRole.equals("ROLE_SECRETARIA")) {
            throw new RuntimeException("SECRETARIA_CANNOT_DELETE_USERS");
        }

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("USER_NOT_FOUND");
        }

        userRepository.deleteById(id);
    }

    // ============================================================
    // CUSTOM
    // ============================================================

    public List<UserResponseDTO> findTeachersByCenter(Long centerId) {
        return userRepository
                .findByRoleAndCenterId(Role.TEACHER, centerId)
                .stream()
                .map(this::toUserDTO)
                .toList();
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private UserResponseDTO toUserDTO(User u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .role(u.getRole())
                .phone(u.getPhone())
                .dni(u.getDni())
                .centerId(u.getCenter() != null ? u.getCenter().getId() : null)
                .centerName(u.getCenter() != null ? u.getCenter().getName() : null)
                .instrumentId(u.getInstrument() != null ? u.getInstrument().getId() : null)
                .instrumentName(u.getInstrument() != null ? u.getInstrument().getName() : null)
                .courseId(u.getCourse() != null ? u.getCourse().getId() : null)
                .courseName(u.getCourse() != null ? u.getCourse().getName() : null)
                .build();
    }

    // ============================================================
    // VALIDACIÓN DNI
    // ============================================================

    private void validateDni(String dni) {

        if (dni == null || dni.isBlank()) return;

        dni = dni.toUpperCase().trim();

        if (!dni.matches("^[0-9]{8}[A-Z]$")) {
            throw new RuntimeException("DNI_FORMAT_INVALID");
        }

        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int number = Integer.parseInt(dni.substring(0, 8));
        char letter = dni.charAt(8);

        if (letters.charAt(number % 23) != letter) {
            throw new RuntimeException("DNI_INVALID");
        }
    }
}