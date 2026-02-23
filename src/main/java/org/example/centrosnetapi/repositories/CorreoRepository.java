package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Correo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface CorreoRepository extends JpaRepository<Correo, Long> {

    long countByEmisor_Id(Long userId);
    long countByDestinatario_Id(Long userId);

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
    ORDER BY c.fechaEnvio DESC
""")
    List<Map<String, Object>> findSentByUser(@Param("userId") Long userId);
    // 🔢 CONTADORES
    @Query("SELECT COUNT(c) FROM Correo c WHERE c.emisor.id = :userId")
    int countSent(@Param("userId") Long userId);

    @Query("SELECT COUNT(uc) FROM UsuarioCorreo uc WHERE uc.usuario.id = :userId AND uc.eliminado = false")
    int countReceived(@Param("userId") Long userId);
}