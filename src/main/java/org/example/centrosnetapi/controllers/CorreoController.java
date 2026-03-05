package org.example.centrosnetapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Correo.CorreoResponseDTO;
import org.example.centrosnetapi.dtos.Correo.SendCorreoRequestDTO;
import org.example.centrosnetapi.dtos.Correo.SendGroupCorreoRequestDTO;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.services.CorreoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/correos")
@RequiredArgsConstructor
@CrossOrigin
public class CorreoController {

    private final CorreoService correoService;

    // 📥 INBOX
    @GetMapping("/inbox")
    public ResponseEntity<List<CorreoResponseDTO>> inbox(
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(
                correoService.getInbox(usuario)
        );
    }

    // 📤 ENVIADOS
    @GetMapping("/sent")
    public ResponseEntity<List<CorreoResponseDTO>> sent(
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(
                correoService.getSent(usuario)
        );
    }

    // ✅ MARCAR COMO LEÍDO
    @PutMapping("/{correoId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long correoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        correoService.markAsRead(correoId, usuario);
        return ResponseEntity.noContent().build();
    }

    // 🗑️ BORRAR
    @DeleteMapping("/{correoId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long correoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        correoService.deleteForUser(correoId, usuario);
        return ResponseEntity.noContent().build();
    }

    // ✉️ ENVÍO INDIVIDUAL
    @PostMapping("/send")
    public ResponseEntity<Void> send(
            @Valid @RequestBody SendCorreoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        correoService.sendCorreo(usuario, dto);
        return ResponseEntity.ok().build();
    }

    // 👥 ENVÍO A GRUPO
    @PostMapping("/send/group")
    @PreAuthorize("hasAnyRole('PROFESOR','ADMIN')")
    public ResponseEntity<Void> sendGroup(
            @RequestBody SendGroupCorreoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        correoService.sendToGroup(dto, usuario);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(
                correoService.countUnread(usuario)
        );
    }
}