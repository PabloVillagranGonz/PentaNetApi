package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.UsuarioCorreo;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface UsuarioCorreoRepository extends JpaRepository<UsuarioCorreo, Long> {

    // 📥 INBOX
    @Query("""
    SELECT new map(
        c.id as id,
        c.asunto as asunto,
        c.cuerpo as cuerpo,
        c.fechaEnvio as fecha_envio,
        u.nombre as remitenteNombre,
        u.email as remitenteEmail,
        uc.leido as leido
    )
    FROM UsuarioCorreo uc
    JOIN uc.correo c
    JOIN c.emisor u
    WHERE uc.usuario.id = :userId
      AND uc.eliminado = false
      AND c.emisor.id <> :userId
    ORDER BY c.fechaEnvio DESC
""")
    List<Map<String, Object>> findInbox(@Param("userId") Long userId);
    // ✅ LEÍDO
    @Modifying
    @Query("""
        UPDATE UsuarioCorreo uc
        SET uc.leido = true
        WHERE uc.correo.id = :correoId
          AND uc.usuario.id = :userId
    """)
    void markAsRead(
            @Param("correoId") Long correoId,
            @Param("userId") Long userId
    );

    // 🗑️ ELIMINAR
    @Modifying
    @Query("""
        UPDATE UsuarioCorreo uc
        SET uc.eliminado = true
        WHERE uc.correo.id = :correoId
          AND uc.usuario.id = :userId
    """)
    void markAsDeleted(
            @Param("correoId") Long correoId,
            @Param("userId") Long userId
    );
}