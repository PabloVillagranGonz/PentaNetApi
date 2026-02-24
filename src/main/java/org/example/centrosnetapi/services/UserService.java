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
import java.util.Map;

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
        user.setEmail(dto.getEmail().toLowerCase().trim());
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
        String currentEmail = auth.getName();

        User currentUser = userRepository.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new RuntimeException("CURRENT_USER_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 🔒 No se puede editar un ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("CANNOT_EDIT_ADMIN_USER");
        }

        // 🔒 Solo se pueden editar usuarios del mismo centro
        if (currentUser.getCenter() != null &&
                user.getCenter() != null &&
                !currentUser.getCenter().getId().equals(user.getCenter().getId())) {
            throw new RuntimeException("CANNOT_EDIT_USER_FROM_OTHER_CENTER");
        }

        // ================= CAMPOS BÁSICOS =================

        if (dto.getNombre() != null) {
            user.setNombre(dto.getNombre().trim());
        }

        if (dto.getApellidos() != null) {
            user.setApellidos(dto.getApellidos().trim());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().toLowerCase().trim());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone().trim());
        }

        if (dto.getDni() != null) {
            validateDni(dto.getDni());
            user.setDni(dto.getDni().toUpperCase().trim());
        }

        // ================= ROLE =================

        if (dto.getRole() != null) {

            // 🔒 Nadie puede asignar ADMIN
            if (dto.getRole() == Role.ADMIN) {
                throw new RuntimeException("NO_PERMISSION_TO_ASSIGN_ADMIN");
            }

            // 🔒 SECRETARIA no puede cambiar roles
            if (currentUser.getRole() == Role.SECRETARIA) {
                throw new RuntimeException("SECRETARIA_CANNOT_CHANGE_ROLES");
            }

            user.setRole(dto.getRole());
        }

        // ================= RELACIONES =================

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
    // UPDATE PARTIAL (PATCH dinámico)
    // ============================================================

    public void updatePartial(Long id, Map<String, Object> data) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = auth.getAuthorities().iterator().next().getAuthority();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 🔒 No se puede editar un ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("CANNOT_EDIT_ADMIN_USER");
        }

        // 🔒 SECRETARIA no puede editar usuarios que no sean STUDENT
        if (currentRole.equals("ROLE_SECRETARIA")) {

            if (user.getRole() != Role.STUDENT) {
                throw new RuntimeException("SECRETARIA_CAN_ONLY_EDIT_STUDENTS");
            }

            // 🔒 SECRETARIA no puede cambiar roles
            if (data.containsKey("role")) {
                throw new RuntimeException("SECRETARIA_CANNOT_CHANGE_ROLES");
            }

            // 🔒 SECRETARIA no puede cambiar centro
            if (data.containsKey("center_id")) {
                throw new RuntimeException("SECRETARIA_CANNOT_CHANGE_CENTER");
            }
        }

        // ================= CAMPOS BÁSICOS =================

        if (data.containsKey("nombre")) {
            user.setNombre((String) data.get("nombre"));
        }

        if (data.containsKey("apellidos")) {
            user.setApellidos((String) data.get("apellidos"));
        }

        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        }

        if (data.containsKey("phone")) {
            user.setPhone((String) data.get("phone"));
        }

        if (data.containsKey("dni")) {
            String dni = (String) data.get("dni");
            validateDni(dni);
            user.setDni(dni.toUpperCase().trim());
        }

        // ================= ROLE =================

        if (data.containsKey("role")) {

            Role newRole = Role.valueOf(data.get("role").toString());

            // 🔒 Nadie puede asignar ADMIN
            if (newRole == Role.ADMIN) {
                throw new RuntimeException("NO_PERMISSION_TO_ASSIGN_ADMIN");
            }

            user.setRole(newRole);
        }

        // ================= CENTER =================

        if (data.containsKey("center_id")) {

            Object centerId = data.get("center_id");

            if (centerId == null) {
                user.setCenter(null);
            } else {
                user.setCenter(
                        centerRepository.findById(Long.valueOf(centerId.toString()))
                                .orElseThrow(() -> new RuntimeException("CENTER_NOT_FOUND"))
                );
            }
        }

        // ================= COURSE =================

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

        // ================= INSTRUMENT =================

        if (data.containsKey("instrument_id")) {

            Object instrumentId = data.get("instrument_id");

            if (instrumentId == null) {
                user.setInstrument(null);
            } else {
                user.setInstrument(
                        instrumentRepository.findById(Long.valueOf(instrumentId.toString()))
                                .orElseThrow(() -> new RuntimeException("INSTRUMENT_NOT_FOUND"))
                );
            }
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