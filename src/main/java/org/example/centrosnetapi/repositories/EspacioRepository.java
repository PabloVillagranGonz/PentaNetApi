package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Espacio;
import org.example.centrosnetapi.models.TipoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    // 📍 Espacios por centro
    List<Espacio> findByCentroId(Long centroId);

    // 🔒 Validar duplicado por nombre dentro del centro
    boolean existsByNombreAndCentroId(String nombre, Long centroId);

    List<Espacio> findByCentroIdAndTipo(Long centroId, TipoEspacio tipo);
}