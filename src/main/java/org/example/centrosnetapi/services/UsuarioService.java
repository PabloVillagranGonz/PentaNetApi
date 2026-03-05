package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Usuario.UpdateUserDTO;
import org.example.centrosnetapi.dtos.Usuario.UserRequestDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.centrosnetapi.dtos.Usuario.UserResponseDTO;


import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final CursoRepository cursoRepository;
    private final CentroRepository centroRepository;


    public List<UserResponseDTO> findStudentsForCourse(Long courseId) {

        return usuarioRepository.findByCursoId(courseId)
                .stream()
                .filter(u -> u.getRol() == Rol.ALUMNO)
                .map(this::toDTO)
                .toList();
    }
    // ============================================================
    // CREATE
    // ============================================================

    public void create(UserRequestDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = auth.getAuthorities().iterator().next().getAuthority();

        if (dto.getRol() == Rol.ADMIN)
            throw new ApiException("CANNOT_CREATE_ADMIN", HttpStatus.FORBIDDEN);

        if (currentRole.equals("ROLE_SECRETARIA") && dto.getRol() != Rol.ALUMNO)
            throw new ApiException("SECRETARIA_CAN_ONLY_CREATE_STUDENTS", HttpStatus.FORBIDDEN);

        // 🔹 Validar DNI formato
        validateDni(dto.getDni());

        String email = dto.getEmail().toLowerCase().trim();

        // 🔹 Email duplicado
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent())
            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);

        // 🔹 DNI duplicado
        if (dto.getDni() != null && !dto.getDni().isBlank()) {
            String dni = dto.getDni().toUpperCase().trim();

            if (usuarioRepository.findByDni(dni).isPresent())
                throw new ApiException("DNI_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }

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
    // READ
    // ============================================================

    public List<UserResponseDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        return toDTO(usuario);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Transactional
    public void update(Long id, UpdateUserDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();

        Usuario currentUser = usuarioRepository.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new ApiException("CURRENT_USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🚫 No permitir editar ADMIN
        if (usuario.getRol() == Rol.ADMIN)
            throw new ApiException("CANNOT_EDIT_ADMIN_USER", HttpStatus.FORBIDDEN);

        // 🚫 No permitir editar usuarios de otro centro
        if (currentUser.getCentro() != null &&
                usuario.getCentro() != null &&
                !currentUser.getCentro().getId().equals(usuario.getCentro().getId())) {

            throw new ApiException("CANNOT_EDIT_USER_FROM_OTHER_CENTER", HttpStatus.FORBIDDEN);
        }

        // ================= CAMPOS BÁSICOS =================

        if (dto.getNombre() != null)
            usuario.setNombre(dto.getNombre().trim());

        if (dto.getApellidos() != null)
            usuario.setApellidos(dto.getApellidos().trim());

        // 🔹 EMAIL
        if (dto.getEmail() != null) {

            String newEmail = dto.getEmail().toLowerCase().trim();

            usuarioRepository.findByEmailIgnoreCase(newEmail)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(usuario.getId()))
                            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
                    });

            usuario.setEmail(newEmail);
        }

        if (dto.getTelefono() != null)
            usuario.setTelefono(dto.getTelefono().trim());

        // 🔹 DNI
        if (dto.getDni() != null) {

            validateDni(dto.getDni());

            String newDni = dto.getDni().toUpperCase().trim();

            usuarioRepository.findByDni(newDni)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(usuario.getId()))
                            throw new ApiException("DNI_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
                    });

            usuario.setDni(newDni);
        }

        // ================= ROL =================

        if (dto.getRol() != null) {

            if (dto.getRol() == Rol.ADMIN)
                throw new ApiException("NO_PERMISSION_TO_ASSIGN_ADMIN", HttpStatus.FORBIDDEN);

            if (currentUser.getRol() == Rol.SECRETARIA)
                throw new ApiException("SECRETARIA_CANNOT_CHANGE_ROLES", HttpStatus.FORBIDDEN);

            usuario.setRol(dto.getRol());
        }

        // ================= RELACIONES =================

        assignRelations(usuario,
                dto.getCentroId(),
                dto.getCursoId(),
                dto.getInstrumentoId()
        );

        usuarioRepository.save(usuario);
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void deleteById(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (usuario.getRol() == Rol.ADMIN)
            throw new ApiException("CANNOT_DELETE_ADMIN", HttpStatus.FORBIDDEN);

        usuarioRepository.delete(usuario);
    }

    // ============================================================
    // MAPPER
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

    // ============================================================
    // VALIDACIÓN DNI
    // ============================================================

    private void validateDni(String dni) {

        if (dni == null || dni.isBlank()) return;

        dni = dni.toUpperCase().trim();

        if (!dni.matches("^[0-9]{8}[A-Z]$"))
            throw new ApiException("DNI_FORMAT_INVALID", HttpStatus.BAD_REQUEST);

        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int number = Integer.parseInt(dni.substring(0, 8));
        char letter = dni.charAt(8);

        if (letters.charAt(number % 23) != letter)
            throw new ApiException("DNI_INVALID", HttpStatus.BAD_REQUEST);
    }

    // ============================================================
    // PROFESORES POR CENTRO
    // ============================================================

    public List<UserResponseDTO> findTeachersByCenter(Long centroId) {

        // Validar que el centro existe
        if (!centroRepository.existsById(centroId)) {
            throw new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        List<Usuario> profesores =
                usuarioRepository.findByCentroIdAndRol(centroId, Rol.PROFESOR);

        return profesores.stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // RELACIONES
    // ============================================================

    private void assignRelations(Usuario usuario,
                                 Long centroId,
                                 Long cursoId,
                                 Long instrumentoId) {

        if (centroId != null)
            usuario.setCentro(
                    centroRepository.findById(centroId)
                            .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND))
            );
        else usuario.setCentro(null);

        if (cursoId != null)
            usuario.setCurso(
                    cursoRepository.findById(cursoId)
                            .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND))
            );
        else usuario.setCurso(null);

        if (instrumentoId != null)
            usuario.setInstrumento(
                    instrumentoRepository.findById(instrumentoId)
                            .orElseThrow(() -> new ApiException("INSTRUMENTO_NOT_FOUND", HttpStatus.NOT_FOUND))
            );
        else usuario.setInstrumento(null);
    }
}