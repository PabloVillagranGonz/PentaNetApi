package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UpdateUserDTO;
import org.example.centrosnetapi.dtos.Usuario.UserRequestDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.example.centrosnetapi.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UsuarioController {

    private final UsuarioService userService;
    private final UsuarioRepository usuarioRepository;

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

        Usuario user = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        return UserResponseDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellidos(user.getApellidos())
                .email(user.getEmail())
                .rol(user.getRol())
                .centroId(user.getCentro() != null ? user.getCentro().getId() : null)
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

    @GetMapping("/buscar")
    public List<UserResponseDTO> buscar(@RequestParam String query) {

        return usuarioRepository.buscarPorTexto(query)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private UserResponseDTO toDTO(Usuario u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .rol(u.getRol())
                .telefono(u.getTelefono())
                .dni(u.getDni())

                .centroId(u.getCentro() != null ? u.getCentro().getId() : null)
                .centroNombre(u.getCentro() != null ? u.getCentro().getNombre() : null)

                .cursoId(u.getCurso() != null ? u.getCurso().getId() : null)
                .cursoNombre(u.getCurso() != null ? u.getCurso().getNombre() : null)

                .instrumentoId(u.getInstrumento() != null ? u.getInstrumento().getId() : null)
                .instrumentoNombre(u.getInstrumento() != null ? u.getInstrumento().getNombre() : null)

                .build();
    }

    @GetMapping("/course/{id}/students")
    public List<UserResponseDTO> getStudentsByCourse(
            @PathVariable Long id) {

        return userService.findStudentsForCourse(id);
    }

    @GetMapping("/centro/{centroId}")
    public List<UserResponseDTO> getUsersByCenter(
            @PathVariable Long centroId
    ) {
        return userService.findUsersByCenter(centroId);
    }
}