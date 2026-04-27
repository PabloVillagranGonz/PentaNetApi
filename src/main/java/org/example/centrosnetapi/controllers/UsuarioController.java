package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UpdateUserDTO;
import org.example.centrosnetapi.dtos.Usuario.UserRequestDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.UsuarioRepository;
import org.example.centrosnetapi.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 🔥 Importante
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UsuarioController {

    private final UsuarioService userService;
    private final UsuarioRepository usuarioRepository;

    // ================= GET ALL =================
    @GetMapping
    public List<UserResponseDTO> getAllUsers(@AuthenticationPrincipal Usuario adminLogueado) {
        return userService.findAll(adminLogueado);
    }

    // ================= CREATE =================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(
            @RequestBody UserRequestDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        userService.create(dto, adminLogueado);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        userService.update(id, dto, adminLogueado);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        userService.deleteById(id, adminLogueado);
    }

    // ================= BUSCAR =================
    @GetMapping("/buscar")
    public List<UserResponseDTO> buscar(
            @RequestParam String query,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        // Filtramos la búsqueda por el centro del admin logueado
        return usuarioRepository.buscarPorTexto(query)
                .stream()
                .filter(u -> adminLogueado.getCentro() == null ||
                        (u.getCentro() != null && u.getCentro().getId().equals(adminLogueado.getCentro().getId())))
                .map(this::toDTO)
                .toList();
    }

    // ================= OTROS GETTERS =================

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/teachers/center/{centerId}")
    public List<UserResponseDTO> getTeachersByCenter(@PathVariable Long centerId) {
        return userService.findTeachersByCenter(centerId);
    }

    @GetMapping("/course/{id}/students")
    public List<UserResponseDTO> getStudentsByCourse(@PathVariable Long id) {
        return userService.findStudentsForCourse(id);
    }

    @GetMapping("/centro/{centroId}")
    public List<UserResponseDTO> getUsersByCenter(@PathVariable Long centroId) {
        return userService.findUsersByCenter(centroId);
    }

    // ================= MAPPER INTERNO =================

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
}