package org.example.centrosnetapi.cabinas.models;

import jakarta.persistence.*;
import lombok.Data;
import org.example.centrosnetapi.models.Center;
import org.example.centrosnetapi.models.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;


    @ManyToOne
    @JoinColumn(name = "center_id")
    private Center center;

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private LocalDateTime finReal;

    private boolean finalizadaAntes = false;

}
