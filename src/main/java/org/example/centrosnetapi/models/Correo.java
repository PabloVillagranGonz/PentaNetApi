package org.example.centrosnetapi.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "correos")
public class Correo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User emisor;

    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private User destinatario;

    private String asunto;
    private String cuerpo;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @ManyToOne
    @JoinColumn(name = "message_group_id")
    private MessageGroup messageGroup;
}