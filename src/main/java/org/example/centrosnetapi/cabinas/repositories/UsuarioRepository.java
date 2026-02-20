package org.example.centrosnetapi.cabinas.repositories;

import org.example.centrosnetapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE CAST(u.id as string) LIKE CONCAT(:query, '%')")
    List<User> buscarPorIdParcial(@Param("query") String query);
}
