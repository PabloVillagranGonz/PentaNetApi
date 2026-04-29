package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Asignatura.SubjectRequestDTO;
import org.example.centrosnetapi.dtos.Asignatura.SubjectResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final CentroRepository centroRepository;
    private final AsignaturaCursoRepository asignaturaCursoRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO create(SubjectRequestDTO dto, Usuario adminLogueado) {
        validarDatosBasicos(dto);
        Long centroId = resolverCentroIdSaaS(dto.getCentroId(), adminLogueado);
        String nombreNormalizado = dto.getNombre().trim();

        validarNoDuplicado(nombreNormalizado, centroId, null);

        Centro centro = centroRepository.findById(centroId)
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));

        Asignatura asignatura = Asignatura.builder()
                .nombre(nombreNormalizado)
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .duracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 60)
                .tipo(parseTipoAsignatura(dto.getTipo()))
                .centro(centro)
                .build();

        return toDTO(asignaturaRepository.save(asignatura));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO update(Long id, SubjectRequestDTO dto, Usuario adminLogueado) {
        Asignatura asignatura = buscarAsignaturaValidada(id, adminLogueado, "NO_PUEDES_EDITAR_ASIGNATURAS_DE_OTRO_CENTRO");

        if (dto.getNombre() != null) {
            String nuevoNombre = dto.getNombre().trim();
            validarNoDuplicado(nuevoNombre, asignatura.getCentro().getId(), id);
            asignatura.setNombre(nuevoNombre);
        }

        if (dto.getDescripcion() != null) asignatura.setDescripcion(dto.getDescripcion().trim());
        if (dto.getDuracionMinutos() != null) asignatura.setDuracionMinutos(dto.getDuracionMinutos());
        if (dto.getTipo() != null) asignatura.setTipo(parseTipoAsignatura(dto.getTipo()));

        return toDTO(asignaturaRepository.save(asignatura));
    }

    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findAll(Usuario adminLogueado) {
        if (adminLogueado.getCentro() == null) {
            return asignaturaRepository.findAll().stream().map(this::toDTO).toList();
        }
        return asignaturaRepository.findByCentroId(adminLogueado.getCentro().getId())
                .stream().map(this::toDTO).toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findByCenter(Long centerId, Usuario adminLogueado) {
        validarAccesoSaaS(adminLogueado, centerId, "NO_PUEDES_VER_ASIGNATURAS_DE_OTRO_CENTRO");
        return asignaturaRepository.findByCentroId(centerId).stream().map(this::toDTO).toList();
    }

    @PreAuthorize("isAuthenticated()")
    public SubjectResponseDTO findById(Long id, Usuario adminLogueado) {
        Asignatura asignatura = buscarAsignaturaValidada(id, adminLogueado, "ACCESO_DENEGADO");
        return toDTO(asignatura);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id, Usuario adminLogueado) {
        Asignatura asignatura = buscarAsignaturaValidada(id, adminLogueado, "NO_PUEDES_BORRAR_ASIGNATURAS_DE_OTRO_CENTRO");
        validarSinDependencias(id);

        asignaturaRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('PROFESOR')")
    public List<SubjectResponseDTO> getSubjectsForTeacher(Usuario profesor) {
        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ApiException("NOT_A_TEACHER", HttpStatus.FORBIDDEN);
        }
        return asignaturaRepository.findSubjectsByTeacherId(profesor.getId())
                .stream().map(this::toDTO).toList();
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones y Buscadores)
    // ============================================================

    private Asignatura buscarAsignaturaValidada(Long id, Usuario adminLogueado, String mensajeErrorSaaS) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));

        validarAccesoSaaS(adminLogueado, asignatura.getCentro().getId(), mensajeErrorSaaS);
        return asignatura;
    }

    private void validarAccesoSaaS(Usuario usuario, Long centroIdObjetivo, String mensajeError) {
        if (usuario.getCentro() != null && !usuario.getCentro().getId().equals(centroIdObjetivo)) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private Long resolverCentroIdSaaS(Long dtoCentroId, Usuario adminLogueado) {
        if (adminLogueado.getCentro() != null) {
            return adminLogueado.getCentro().getId();
        } else if (dtoCentroId == null) {
            throw new ApiException("CENTRO_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        return dtoCentroId;
    }

    private void validarDatosBasicos(SubjectRequestDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ApiException("SUBJECT_NAME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new ApiException("TIPO_REQUIRED", HttpStatus.BAD_REQUEST);
        }
    }

    private TipoAsignatura parseTipoAsignatura(String tipoStr) {
        try {
            return TipoAsignatura.valueOf(tipoStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("TIPO_ASIGNATURA_INVALIDO", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarNoDuplicado(String nombre, Long centroId, Long idExcluido) {
        boolean existe;
        if (idExcluido == null) {
            existe = asignaturaRepository.existsByNombreAndCentroId(nombre, centroId);
        } else {
            existe = asignaturaRepository.existsByNombreAndCentroIdAndIdNot(nombre, centroId, idExcluido);
        }

        if (existe) {
            throw new ApiException("ASIGNATURA_YA_EXISTE_EN_CENTRO", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarSinDependencias(Long id) {
        if (asignaturaCursoRepository.existsByAsignaturaId(id)) {
            throw new ApiException("ASIGNATURA_CON_DEPENDENCIAS", HttpStatus.CONFLICT);
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private SubjectResponseDTO toDTO(Asignatura a) {
        return SubjectResponseDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .descripcion(a.getDescripcion())
                .duracionMinutos(a.getDuracionMinutos())
                .tipo(a.getTipo() != null ? a.getTipo().name() : null)
                .centroId(a.getCentro() != null ? a.getCentro().getId() : null)
                .build();
    }
}