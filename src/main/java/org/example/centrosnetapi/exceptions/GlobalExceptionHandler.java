package org.example.centrosnetapi.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // ApiException → Devuelve el HTTP status correcto (404, 403, 409...)
    // ============================================================

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        log.warn("ApiException: {} (Status: {})", ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "error", ex.getMessage(),
                "status", ex.getStatus().value(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ============================================================
    // IllegalArgumentException → Validaciones de dominio (@PrePersist)
    // ============================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getMessage(),
                "status", 400,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ============================================================
    // MethodArgumentNotValidException → Validaciones de DTOs (@Valid)
    // ============================================================

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");
                
        log.warn("Validation error: {}", errorMessage);

        return ResponseEntity.badRequest().body(Map.of(
                "error", errorMessage,
                "status", 400,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ============================================================
    // Genérico → Cualquier otra excepción no controlada
    // ============================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled Exception:", ex);
        return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error interno del servidor",
                "detail", ex.getMessage() != null ? ex.getMessage() : "Sin detalle",
                "status", 500,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
