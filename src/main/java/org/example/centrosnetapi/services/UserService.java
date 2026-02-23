package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.UserRequestDTO;
import org.example.centrosnetapi.dtos.UserResponseDTO;
import org.example.centrosnetapi.models.Center;
import org.example.centrosnetapi.models.Role;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.CenterRepository;
import org.example.centrosnetapi.repositories.CourseRepository;
import org.example.centrosnetapi.repositories.InstrumentRepository;
import org.example.centrosnetapi.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final CourseRepository courseRepository;
    private final CenterRepository centerRepository;

    // =====================
    // CREATE
    // =====================
    public void create(UserRequestDTO dto) {

        if (dto.getRole() == null) {
            throw new RuntimeException("ROLE_REQUIRED");
        }

        // 🔥 VALIDACIÓN DNI
        validateDni(dto.getDni());

        User user = new User();

        user.setNombre(dto.getNombre());
        user.setApellidos(dto.getApellidos());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone());

        // Normalizamos antes de guardar
        if (dto.getDni() != null) {
            user.setDni(dto.getDni().toUpperCase().trim());
        }

        user.setActive(true);

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

    // =====================
    // READ
    // =====================
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

    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        return toUserDTO(user);
    }

    // =====================
    // UPDATE (PATCH)
    // =====================
    public void updatePartial(Long id, Map<String, Object> data) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (data.containsKey("nombre")) {
            user.setNombre((String) data.get("nombre"));
        }

        if (data.containsKey("apellidos")) {
            user.setApellidos((String) data.get("apellidos"));
        }

        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        }

        if (data.containsKey("role")) {
            user.setRole(Role.valueOf(data.get("role").toString()));
        }

        if (data.containsKey("dni")) {
            user.setDni((String) data.get("dni"));
        }

        if (data.containsKey("phone")) {
            user.setPhone((String) data.get("phone"));
        }

        if (data.containsKey("center_id")) {
            Object centerId = data.get("center_id");
            if (centerId == null) {
                user.setCenter(null);
            } else {
                Center center = new Center();
                center.setId(Long.valueOf(centerId.toString()));
                user.setCenter(center);
            }
        }

        if (data.containsKey("course_id")) {
            Object courseId = data.get("course_id");
            if (courseId == null) {
                user.setCourse(null);
            } else {
                user.setCourse(
                        courseRepository.findById(Long.valueOf(courseId.toString()))
                                .orElseThrow(() -> new RuntimeException("COURSE_NOT_FOUND"))
                );
            }
        }

        userRepository.save(user);
    }

    // =====================
    // DELETE
    // =====================
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
        userRepository.deleteById(id);
    }

    // =====================
    // CUSTOM QUERIES
    // =====================
    public List<UserResponseDTO> findTeachersByCenter(Long centerId) {
        return userRepository
                .findByRoleAndCenterId(Role.TEACHER, centerId)
                .stream()
                .map(this::toUserDTO)
                .toList();
    }

    // =====================
    // MAPPER
    // =====================
    private UserResponseDTO toUserDTO(User u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .role(Role.valueOf(u.getRole().name()))
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

    private void validateDni(String dni) {

        if (dni == null || dni.isBlank()) {
            return; // si no es obligatorio
        }

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