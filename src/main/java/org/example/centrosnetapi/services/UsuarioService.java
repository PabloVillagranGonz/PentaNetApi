package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UpdateUserDTO;
import org.example.centrosnetapi.dtos.Usuario.UserRequestDTO;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final CursoRepository cursoRepository;
    private final CentroRepository centroRepository;

    // ============================================================
    // READ (Filtrado SaaS y Corrección de Conteos)
    // ============================================================

    public List<UserResponseDTO> findAll(Usuario adminLogueado) {
        List<Usuario> lista;

        if (adminLogueado.getCentro() == null) {
            // SUPER ADMIN: Ve a todos
            lista = usuarioRepository.findAll();
        } else {
            // ADMIN DE CENTRO: Solo ve a los de su centro
            lista = usuarioRepository.findByCentroId(adminLogueado.getCentro().getId());
        }

        // 🔥 SOLUCIÓN AL CONTEO: Excluimos a los ADMINS de la lista general
        // Así la tarjeta "Users" en Flutter marcará solo personal gestionable (6 en tu caso)
        return lista.stream()
                .filter(u -> u.getRol() != Rol.ADMIN)
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));
        return toDTO(usuario);
    }

    // ============================================================
    // CREATE
    // ============================================================

    // ============================================================
    // CREATE
    // ============================================================
    public void create(UserRequestDTO dto, Usuario adminLogueado) {

        // Reglas de Rol
        if (dto.getRol() == Rol.ADMIN && adminLogueado.getCentro() != null)
            throw new ApiException("CENTER_ADMIN_CANNOT_CREATE_ADMINS", HttpStatus.FORBIDDEN);

        if (adminLogueado.getRol() == Rol.SECRETARIA && dto.getRol() != Rol.ALUMNO)
            throw new ApiException("SECRETARIA_CAN_ONLY_CREATE_STUDENTS", HttpStatus.FORBIDDEN);

        // Si es un admin de centro, forzamos que el usuario se cree en SU centro
        if (adminLogueado.getCentro() != null) {
            dto.setCentroId(adminLogueado.getCentro().getId());
        }

        validateDni(dto.getDni());

        // 🔥 ¡AQUÍ ESTÁ LA MAGIA PARA EVITAR EL ERROR 500! 🔥
        // Comprobamos en la BD antes de guardar
        if (dto.getDni() != null && !dto.getDni().isBlank()) {
            usuarioRepository.findByDni(dto.getDni().toUpperCase().trim())
                    .ifPresent(u -> {
                        throw new ApiException("DNI_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
                    });
        }

        String email = dto.getEmail().toLowerCase().trim();

        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent())
            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellidos(dto.getApellidos().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(dto.getRol());
        usuario.setTelefono(dto.getTelefono());
        usuario.setActivo(true);

        if (dto.getDni() != null)
            usuario.setDni(dto.getDni().toUpperCase().trim());

        assignRelations(usuario, dto.getCentroId(), dto.getCursoId(), dto.getInstrumentoId());
        usuarioRepository.save(usuario);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Transactional
    public void update(Long id, UpdateUserDTO dto, Usuario adminLogueado) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Seguridad: No editar admins si no eres Super Admin
        if (usuario.getRol() == Rol.ADMIN && adminLogueado.getCentro() != null)
            throw new ApiException("CANNOT_EDIT_ADMIN_USER", HttpStatus.FORBIDDEN);

        // Seguridad: No editar usuarios de otros centros
        if (adminLogueado.getCentro() != null && usuario.getCentro() != null &&
                !adminLogueado.getCentro().getId().equals(usuario.getCentro().getId())) {
            throw new ApiException("CANNOT_EDIT_USER_FROM_OTHER_CENTER", HttpStatus.FORBIDDEN);
        }

        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre().trim());
        if (dto.getApellidos() != null) usuario.setApellidos(dto.getApellidos().trim());

        if (dto.getEmail() != null) {
            String newEmail = dto.getEmail().toLowerCase().trim();
            usuarioRepository.findByEmailIgnoreCase(newEmail).ifPresent(existing -> {
                if (!existing.getId().equals(usuario.getId()))
                    throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
            });
            usuario.setEmail(newEmail);
        }

        // 🔥 COMPROBACIÓN DNI EN EL UPDATE 🔥
        if (dto.getDni() != null) {
            validateDni(dto.getDni());
            String newDni = dto.getDni().toUpperCase().trim();

            usuarioRepository.findByDni(newDni).ifPresent(existing -> {
                // Si el DNI ya existe y no es de la persona que estamos editando
                if (!existing.getId().equals(usuario.getId()))
                    throw new ApiException("DNI_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
            });
            usuario.setDni(newDni);
        }

        if (dto.getRol() != null) {
            if (dto.getRol() == Rol.ADMIN && adminLogueado.getCentro() != null)
                throw new ApiException("NO_PERMISSION_TO_ASSIGN_ADMIN", HttpStatus.FORBIDDEN);
            usuario.setRol(dto.getRol());
        }

        assignRelations(usuario, dto.getCentroId(), dto.getCursoId(), dto.getInstrumentoId());
        usuarioRepository.save(usuario);
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void deleteById(Long id, Usuario adminLogueado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (adminLogueado.getCentro() != null && usuario.getCentro() != null &&
                !adminLogueado.getCentro().getId().equals(usuario.getCentro().getId())) {
            throw new ApiException("CANNOT_DELETE_USER_FROM_OTHER_CENTER", HttpStatus.FORBIDDEN);
        }

        if (usuario.getRol() == Rol.ADMIN)
            throw new ApiException("CANNOT_DELETE_ADMIN", HttpStatus.FORBIDDEN);

        usuarioRepository.delete(usuario);
    }

    // ============================================================
// USUARIOS POR CENTRO (Filtrado para el Dashboard)
// ============================================================

    public List<UserResponseDTO> findUsersByCenter(Long centroId) {

        // 1. Validar que el centro existe
        if (!centroRepository.existsById(centroId)) {
            throw new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        // 2. Buscamos los usuarios y filtramos
        return usuarioRepository.findByCentroId(centroId)
                .stream()
                // 🔥 Filtro crítico: Excluimos a los ADMIN para que las cuentas
                // de Profesores + Alumnos + Secretarias sean exactas (el 6 vs 8)
                .filter(u -> u.getRol() != Rol.ADMIN)
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // HELPERS & MAPPERS
    // ============================================================

    private UserResponseDTO toDTO(Usuario u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .rol(u.getRol())
                .telefono(u.getTelefono())
                .dni(u.getDni())
                .centroId(u.getCentro() != null ? u.getCentro().getId() : null)
                .centroNombre(u.getCentro() != null ? u.getCentro().getNombre() : null)
                .cursoId(u.getCurso() != null ? u.getCurso().getId() : null)
                .cursoNombre(u.getCurso() != null ? u.getCurso().getNombre() : null)
                .instrumentoId(u.getInstrumento() != null ? u.getInstrumento().getId() : null)
                .instrumentoNombre(u.getInstrumento() != null ? u.getInstrumento().getNombre() : null)
                .build();
    }

    private void validateDni(String dni) {
        if (dni == null || dni.isBlank()) return;
        dni = dni.toUpperCase().trim();
        if (!dni.matches("^[0-9]{8}[A-Z]$"))
            throw new ApiException("DNI_FORMAT_INVALID", HttpStatus.BAD_REQUEST);
    }

    private void assignRelations(Usuario usuario, Long centroId, Long cursoId, Long instrumentoId) {
        if (centroId != null)
            usuario.setCentro(centroRepository.findById(centroId).orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND)));

        if (cursoId != null)
            usuario.setCurso(cursoRepository.findById(cursoId).orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND)));

        if (instrumentoId != null)
            usuario.setInstrumento(instrumentoRepository.findById(instrumentoId).orElseThrow(() -> new ApiException("INSTRUMENTO_NOT_FOUND", HttpStatus.NOT_FOUND)));
    }

    public List<UserResponseDTO> findTeachersByCenter(Long centroId) {
        return usuarioRepository.findByCentroIdAndRol(centroId, Rol.PROFESOR).stream().map(this::toDTO).toList();
    }

    public List<UserResponseDTO> findStudentsForCourse(Long courseId) {
        return usuarioRepository.findByCursoId(courseId).stream().filter(u -> u.getRol() == Rol.ALUMNO).map(this::toDTO).toList();
    }
}