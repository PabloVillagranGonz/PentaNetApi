package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UpdateUserDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.models.Usuario; // 🔥 Importante
import org.example.centrosnetapi.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 🔥 Importante
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
            @AuthenticationPrincipal Usuario adminLogueado // 👈 Inyectamos quién pide la lista
    ) {
        // Ahora el servicio podrá filtrar: si eres Super Admin -> todos, si no -> solo tu centro
        return ResponseEntity.ok(userService.findAll(adminLogueado));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO dto,
            @AuthenticationPrincipal Usuario adminLogueado // 👈 También aquí para validar permisos
    ) {
        userService.update(id, dto, adminLogueado);
        return ResponseEntity.noContent().build();
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario adminLogueado // 👈 Y aquí
    ) {
        userService.deleteById(id, adminLogueado);
        return ResponseEntity.noContent().build();
    }
}