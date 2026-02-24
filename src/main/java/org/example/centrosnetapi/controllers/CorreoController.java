package org.example.centrosnetapi.controllers;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SendCorreoRequestDTO;
import org.example.centrosnetapi.dtos.SendGroupCorreoRequestDTO;
import org.example.centrosnetapi.services.CorreoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    // 📥 INBOX
    @GetMapping("/inbox")
    public List<Map<String, Object>> getInbox(Authentication authentication) {
        return correoService.getInboxByEmail(authentication.getName());
    }

    // 📤 ENVIADOS
    @GetMapping("/sent")
    public List<Map<String, Object>> getSent(Authentication authentication) {
        return correoService.getSentByEmail(authentication.getName());
    }

    // ✅ MARCAR COMO LEÍDO
    @PutMapping("/{correoId}/read")
    public void markAsRead(
            @PathVariable Long correoId,
            Authentication authentication
    ) {
        correoService.markAsRead(correoId, authentication.getName());
    }

    // 🗑️ BORRAR
    @DeleteMapping("/{correoId}")
    public void deleteForUser(
            @PathVariable Long correoId,
            Authentication authentication
    ) {
        correoService.deleteForUser(correoId, authentication.getName());
    }

    // 🔢 CONTADOR
    @GetMapping("/count")
    public Map<String, Integer> getEmailCount(Authentication authentication) {
        return correoService.getEmailCount(authentication.getName());
    }

    // ✉️ ENVÍO INDIVIDUAL
    @PostMapping("/send")
    public void sendCorreo(
            @RequestBody SendCorreoRequestDTO dto,
            Authentication authentication
    ) {
        correoService.sendCorreo(
                authentication.getName(),
                dto.getDestinatarioId(),
                dto.getAsunto(),
                dto.getCuerpo()
        );
    }

    // 👥 ENVÍO A GRUPO
    @PostMapping("/send/group")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<?> sendToGroup(
            @RequestBody SendGroupCorreoRequestDTO request,
            Authentication authentication
    ) {
        correoService.sendToGroup(request, authentication.getName());
        return ResponseEntity.ok("Mensaje enviado al grupo correctamente");
    }
}