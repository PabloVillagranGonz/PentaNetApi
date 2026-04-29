package org.example.centrosnetapi.services;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // 🔥 Todo protegido: Si falla la validación de un instrumento, el usuario no se guarda a medias.
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final CursoRepository cursoRepository;
    private final CentroRepository centroRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    public List<UserResponseDTO> findAll(Usuario adminLogueado) {
        List<Usuario> lista = (adminLogueado.getCentro() == null)
                ? usuarioRepository.findAll()
                : usuarioRepository.findByCentroId(adminLogueado.getCentro().getId());

        // 🔥 Filtramos a los ADMINS para no alterar los conteos del dashboard
        return lista.stream()
                .filter(u -> u.getRol() != Rol.ADMIN)
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        return toDTO(buscarUsuario(id));
    }

    public List<UserResponseDTO> findUsersByCenter(Long centroId) {
        validarCentroExiste(centroId);

        return usuarioRepository.findByCentroId(centroId).stream()
                .filter(u -> u.getRol() != Rol.ADMIN)
                .map(this::toDTO)
                .toList();
    }

    public void create(UserRequestDTO dto, Usuario adminLogueado) {
        validarPermisosCreacion(dto.getRol(), adminLogueado);

        Long centroIdDestino = resolverCentroIdSaaS(dto.getCentroId(), adminLogueado);
        String emailNormalizado = normalizarEmail(dto.getEmail());
        String dniNormalizado = normalizarYValidarDni(dto.getDni());

        validarEmailUnico(emailNormalizado, null);
        validarDniUnico(dniNormalizado, null);

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellidos(dto.getApellidos().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(dto.getRol());
        usuario.setTelefono(dto.getTelefono());
        usuario.setActivo(true);
        usuario.setDni(dniNormalizado);

        assignRelations(usuario, centroIdDestino, dto.getCursoId(), dto.getInstrumentoId());

        usuarioRepository.save(usuario);
    }

    public void update(Long id, UpdateUserDTO dto, Usuario adminLogueado) {
        Usuario usuario = buscarUsuarioValidado(id, adminLogueado, "CANNOT_EDIT_USER_FROM_OTHER_CENTER");
        validarPermisosEdicion(usuario, dto.getRol(), adminLogueado);

        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre().trim());
        if (dto.getApellidos() != null) usuario.setApellidos(dto.getApellidos().trim());

        if (dto.getEmail() != null) {
            String newEmail = normalizarEmail(dto.getEmail());
            validarEmailUnico(newEmail, usuario.getId());
            usuario.setEmail(newEmail);
        }

        if (dto.getDni() != null) {
            String newDni = normalizarYValidarDni(dto.getDni());
            validarDniUnico(newDni, usuario.getId());
            usuario.setDni(newDni);
        }

        if (dto.getRol() != null) usuario.setRol(dto.getRol());

        assignRelations(usuario, dto.getCentroId(), dto.getCursoId(), dto.getInstrumentoId());

        usuarioRepository.save(usuario);
    }

    public void deleteById(Long id, Usuario adminLogueado) {
        Usuario usuario = buscarUsuarioValidado(id, adminLogueado, "CANNOT_DELETE_USER_FROM_OTHER_CENTER");

        if (usuario.getRol() == Rol.ADMIN) {
            throw new ApiException("CANNOT_DELETE_ADMIN", HttpStatus.FORBIDDEN);
        }

        usuarioRepository.delete(usuario);
    }

    public List<UserResponseDTO> findTeachersByCenter(Long centroId) {
        return usuarioRepository.findByCentroIdAndRol(centroId, Rol.PROFESOR).stream().map(this::toDTO).toList();
    }

    public List<UserResponseDTO> findStudentsForCourse(Long courseId) {
        return usuarioRepository.findByCursoId(courseId).stream()
                .filter(u -> u.getRol() == Rol.ALUMNO)
                .map(this::toDTO)
                .toList();
    }

    public List<UserResponseDTO> buscarPorTexto(String query, Long centroId) {
        return usuarioRepository.buscarPorTexto(query, centroId).stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones de Dominio y Seguridad)
    // ============================================================

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Usuario buscarUsuarioValidado(Long id, Usuario adminLogueado, String mensajeErrorSaaS) {
        Usuario usuario = buscarUsuario(id);

        if (adminLogueado.getCentro() != null && usuario.getCentro() != null &&
                !adminLogueado.getCentro().getId().equals(usuario.getCentro().getId())) {
            throw new ApiException(mensajeErrorSaaS, HttpStatus.FORBIDDEN);
        }
        return usuario;
    }

    private void validarCentroExiste(Long centroId) {
        if (!centroRepository.existsById(centroId)) {
            throw new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
    }

    private Long resolverCentroIdSaaS(Long dtoCentroId, Usuario adminLogueado) {
        return adminLogueado.getCentro() != null ? adminLogueado.getCentro().getId() : dtoCentroId;
    }

    private void validarPermisosCreacion(Rol rolSolicitado, Usuario adminLogueado) {
        if (rolSolicitado == Rol.ADMIN && adminLogueado.getCentro() != null) {
            throw new ApiException("CENTER_ADMIN_CANNOT_CREATE_ADMINS", HttpStatus.FORBIDDEN);
        }
        if (adminLogueado.getRol() == Rol.SECRETARIA && rolSolicitado != Rol.ALUMNO) {
            throw new ApiException("SECRETARIA_CAN_ONLY_CREATE_STUDENTS", HttpStatus.FORBIDDEN);
        }
    }

    private void validarPermisosEdicion(Usuario usuarioObjetivo, Rol nuevoRol, Usuario adminLogueado) {
        if (usuarioObjetivo.getRol() == Rol.ADMIN && adminLogueado.getCentro() != null) {
            throw new ApiException("CANNOT_EDIT_ADMIN_USER", HttpStatus.FORBIDDEN);
        }
        if (nuevoRol == Rol.ADMIN && adminLogueado.getCentro() != null) {
            throw new ApiException("NO_PERMISSION_TO_ASSIGN_ADMIN", HttpStatus.FORBIDDEN);
        }
    }

    private String normalizarEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }

    private void validarEmailUnico(String email, Long idExcluido) {
        if (email == null) return;

        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (idExcluido == null || !existing.getId().equals(idExcluido)) {
                throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
            }
        });
    }

    private String normalizarYValidarDni(String dni) {
        if (dni == null || dni.isBlank()) return null;

        String dniClean = dni.toUpperCase().trim();
        if (!dniClean.matches("^[0-9]{8}[A-Z]$")) {
            throw new ApiException("DNI_FORMAT_INVALID", HttpStatus.BAD_REQUEST);
        }
        return dniClean;
    }

    private void validarDniUnico(String dni, Long idExcluido) {
        if (dni == null) return;

        usuarioRepository.findByDni(dni).ifPresent(existing -> {
            if (idExcluido == null || !existing.getId().equals(idExcluido)) {
                throw new ApiException("DNI_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
            }
        });
    }

    private void assignRelations(Usuario usuario, Long centroId, Long cursoId, Long instrumentoId) {
        if (centroId != null) {
            usuario.setCentro(centroRepository.findById(centroId)
                    .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND)));
        }
        if (cursoId != null) {
            usuario.setCurso(cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new ApiException("CURSO_NOT_FOUND", HttpStatus.NOT_FOUND)));
        }
        if (instrumentoId != null) {
            usuario.setInstrumento(instrumentoRepository.findById(instrumentoId)
                    .orElseThrow(() -> new ApiException("INSTRUMENTO_NOT_FOUND", HttpStatus.NOT_FOUND)));
        }
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
}