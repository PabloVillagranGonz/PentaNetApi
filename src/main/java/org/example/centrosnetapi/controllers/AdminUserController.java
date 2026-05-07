package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UpdateUserDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UsuarioService userService;

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @AuthenticationPrincipal Usuario adminLogueado // Inyecto el usuario logueado para verificar su centro
    ) {
        // Llamo al servicio, el cual se encargará de filtrar por centro si es necesario
        return ResponseEntity.ok(userService.findAll(adminLogueado));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado // Reutilizo el usuario para validar permisos
    ) {
        userService.update(id, dto, adminLogueado);
        return ResponseEntity.noContent().build();
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado
    ) {
        userService.deleteById(id, adminLogueado);
        return ResponseEntity.noContent().build();
    }
}