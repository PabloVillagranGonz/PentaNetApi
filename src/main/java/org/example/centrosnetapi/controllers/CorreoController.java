package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SendCorreoRequestDTO;
import org.example.centrosnetapi.dtos.SendGroupCorreoRequestDTO;
import org.example.centrosnetapi.models.User;
import org.example.centrosnetapi.services.CorreoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/correos")
@RequiredArgsConstructor
@CrossOrigin
public class CorreoController {

    private final CorreoService correoService;

    // 📥 BANDEJA DE ENTRADA
    @GetMapping("/inbox")
    public List<Map<String, Object>> getInbox(
            @AuthenticationPrincipal User user
    ) {
        return correoService.getInbox(user.getId());
    }

    // 📤 ENVIADOS
    @GetMapping("/sent")
    public List<Map<String, Object>> getSent(
            @AuthenticationPrincipal User user
    ) {
        return correoService.getSent(user.getId());
    }

    // ✅ MARCAR COMO LEÍDO
    @PutMapping("/{correoId}/read")
    public void markAsRead(
            @PathVariable Long correoId,
            @AuthenticationPrincipal User user
    ) {
        correoService.markAsRead(correoId, user.getId());
    }

    // 🗑️ BORRAR CORREO PARA USUARIO
    @DeleteMapping("/{correoId}")
    public void deleteForUser(
            @PathVariable Long correoId,
            @AuthenticationPrincipal User user
    ) {
        correoService.deleteForUser(correoId, user.getId());
    }

    // 🔢 CONTADOR (recuentos)
    @GetMapping("/count")
    public Map<String, Integer> getEmailCount(
            @AuthenticationPrincipal User user
    ) {
        return correoService.getEmailCount(user.getId());
    }

    // ✉️ ENVIAR CORREO
    @PostMapping("/send")
    public void sendCorreo(
            @RequestBody SendCorreoRequestDTO dto,
            @AuthenticationPrincipal User user
    ) {
        correoService.sendCorreo(
                user.getId(),                 // 👈 emisor REAL
                dto.getDestinatarioId(),
                dto.getAsunto(),
                dto.getCuerpo()
        );
    }

    @PostMapping("/send/group")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<?> sendToGroup(
            @RequestBody SendGroupCorreoRequestDTO request,
            Authentication authentication) {

        correoService.sendToGroup(request, authentication.getName());

        return ResponseEntity.ok("Mensaje enviado al grupo correctamente");
    }
}