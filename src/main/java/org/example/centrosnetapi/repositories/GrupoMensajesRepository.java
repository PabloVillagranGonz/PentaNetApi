package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.GrupoMensajes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoMensajesRepository
        extends JpaRepository<GrupoMensajes, Long> {

}