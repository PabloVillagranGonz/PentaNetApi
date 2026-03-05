package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {


}