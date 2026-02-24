package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.CourseResponseDTO;
import org.example.centrosnetapi.dtos.UpdateUserDTO;
import org.example.centrosnetapi.dtos.UserRequestDTO;
import org.example.centrosnetapi.dtos.UserResponseDTO;
import org.example.centrosnetapi.models.Course;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.repositories.UserRepository;
import org.example.centrosnetapi.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody UserRequestDTO dto) {
        userService.create(dto);
    }

    @GetMapping("/teachers/center/{centerId}")
    public List<UserResponseDTO> getTeachersByCenter(
            @PathVariable Long centerId
    ) {
        return userService.findTeachersByCenter(centerId);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/email")
    public UserResponseDTO getUserByEmail(@RequestParam String email) {

        email = email.toLowerCase().trim();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        return UserResponseDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellidos(user.getApellidos())
                .email(user.getEmail())
                .role(user.getRole())
                .centerId(user.getCenter() != null ? user.getCenter().getId() : null)
                .build();
    }

    @PutMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserDTO dto
    ) {
        userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }
}