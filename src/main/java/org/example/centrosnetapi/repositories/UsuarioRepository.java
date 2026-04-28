package org.example.centrosnetapi.repositories;

import org.example.centrosnetapi.models.Rol;
import org.example.centrosnetapi.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByDni(String dni);

    // 🔎 Por rol
    List<Usuario> findByRol(Rol rol);

    // 🔎 Profesores o alumnos por centro
    List<Usuario> findByCentroIdAndRol(Long centroId, Rol rol);

    // 🔎 Usuarios por curso
    List<Usuario> findByCursoId(Long cursoId);

    boolean existsByCursoId(Long cursoId);

    // 🔎 Usuarios por curso y rol (muy útil)
    List<Usuario> findByCurso_IdAndRol(Long cursoId, Rol rol);

    @Query("""
        SELECT u FROM Usuario u
        WHERE 
            CAST(u.id as string) LIKE %:query%
            OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Usuario> buscarPorTexto(@Param("query") String query);

    List<Usuario> findByCentroId(Long centroId);

    List<Usuario> findByRolAndCursoId(Rol rol, Long cursoId);
}