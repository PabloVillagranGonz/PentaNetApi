package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.UsuarioMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioMensajeRepository extends JpaRepository<UsuarioMensaje, Long> {

    // ============================================================
    // 📥 INBOX
    // ============================================================

    @Query("""
    SELECT um
    FROM UsuarioMensaje um
    WHERE um.usuario.id = :userId
      AND um.eliminado = false
      AND um.archivado = false
      AND um.mensaje.remitente.id <> :userId
    ORDER BY um.mensaje.fechaEnvio DESC
""")
    List<UsuarioMensaje> findInboxByUsuarioId(@Param("userId") Long userId);

    // ============================================================
    // BUSCAR ESTADO POR MENSAJE Y USUARIO
    // ============================================================

    Optional<UsuarioMensaje> findByMensajeIdAndUsuarioId(
            Long mensajeId,
            Long usuarioId
    );

    @Query("""
    SELECT COUNT(um)
    FROM UsuarioMensaje um
    WHERE um.usuario.id = :userId
      AND um.leido = false
      AND um.eliminado = false
      AND um.mensaje.remitente.id <> :userId
""")
    long countUnreadByUsuarioId(@Param("userId") Long userId);
}