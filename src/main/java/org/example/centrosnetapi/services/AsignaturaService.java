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

    // ================= CREATE =================
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO create(SubjectRequestDTO dto, Usuario adminLogueado) {

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new ApiException("SUBJECT_NAME_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        // 🔥 CANDADO SAAS: Si es Admin de centro, forzamos su ID
        if (adminLogueado.getCentro() != null) {
            dto.setCentroId(adminLogueado.getCentro().getId());
        } else if (dto.getCentroId() == null) {
            throw new ApiException("CENTRO_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new ApiException("TIPO_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        Centro centro = centroRepository.findById(dto.getCentroId())
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));

        String nombreNormalizado = dto.getNombre().trim();

        if (asignaturaRepository.existsByNombreAndCentroId(nombreNormalizado, dto.getCentroId())) {
            throw new ApiException("ASIGNATURA_YA_EXISTE_EN_CENTRO", HttpStatus.BAD_REQUEST);
        }

        TipoAsignatura tipo;
        try {
            tipo = TipoAsignatura.valueOf(dto.getTipo().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("TIPO_ASIGNATURA_INVALIDO", HttpStatus.BAD_REQUEST);
        }

        Asignatura asignatura = Asignatura.builder()
                .nombre(nombreNormalizado)
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .duracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 60)
                .tipo(tipo)
                .centro(centro)
                .build();

        return toDTO(asignaturaRepository.save(asignatura));
    }

    // ================= UPDATE =================
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponseDTO update(Long id, SubjectRequestDTO dto, Usuario adminLogueado) {

        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS: No editar asignaturas de otro centro
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(asignatura.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_EDITAR_ASIGNATURAS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        if (dto.getNombre() != null) {
            String nuevoNombre = dto.getNombre().trim();
            if (asignaturaRepository.existsByNombreAndCentroIdAndIdNot(
                    nuevoNombre, asignatura.getCentro().getId(), id)) {
                throw new ApiException("ASIGNATURA_YA_EXISTE_EN_CENTRO", HttpStatus.BAD_REQUEST);
            }
            asignatura.setNombre(nuevoNombre);
        }

        if (dto.getDescripcion() != null) asignatura.setDescripcion(dto.getDescripcion().trim());
        if (dto.getDuracionMinutos() != null) asignatura.setDuracionMinutos(dto.getDuracionMinutos());

        if (dto.getTipo() != null) {
            try {
                asignatura.setTipo(TipoAsignatura.valueOf(dto.getTipo().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ApiException("TIPO_ASIGNATURA_INVALIDO", HttpStatus.BAD_REQUEST);
            }
        }

        return toDTO(asignaturaRepository.save(asignatura));
    }

    // ================= READ =================
    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findAll(Usuario adminLogueado) {
        // 🔥 CANDADO SAAS: El Super Admin ve todo, el Admin local solo lo suyo
        if (adminLogueado.getCentro() == null) {
            return asignaturaRepository.findAll().stream().map(this::toDTO).toList();
        } else {
            return asignaturaRepository.findByCentroId(adminLogueado.getCentro().getId())
                    .stream().map(this::toDTO).toList();
        }
    }

    @PreAuthorize("isAuthenticated()")
    public List<SubjectResponseDTO> findByCenter(Long centerId, Usuario adminLogueado) {
        // 🔥 CANDADO SAAS: No cotillear otros centros
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(centerId)) {
            throw new ApiException("NO_PUEDES_VER_ASIGNATURAS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }
        return asignaturaRepository.findByCentroId(centerId).stream().map(this::toDTO).toList();
    }

    @PreAuthorize("isAuthenticated()")
    public SubjectResponseDTO findById(Long id, Usuario adminLogueado) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(asignatura.getCentro().getId())) {
            throw new ApiException("ACCESO_DENEGADO", HttpStatus.FORBIDDEN);
        }
        return toDTO(asignatura);
    }

    // ================= DELETE =================
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id, Usuario adminLogueado) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 🔥 CANDADO SAAS
        if (adminLogueado.getCentro() != null && !adminLogueado.getCentro().getId().equals(asignatura.getCentro().getId())) {
            throw new ApiException("NO_PUEDES_BORRAR_ASIGNATURAS_DE_OTRO_CENTRO", HttpStatus.FORBIDDEN);
        }

        asignaturaRepository.deleteById(id);
    }

    // ================= TEACHER SUBJECTS =================
    @PreAuthorize("hasRole('PROFESOR')")
    public List<SubjectResponseDTO> getSubjectsForTeacher(Usuario profesor) {
        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ApiException("NOT_A_TEACHER", HttpStatus.FORBIDDEN);
        }
        return asignaturaRepository.findSubjectsByTeacherId(profesor.getId())
                .stream().map(this::toDTO).toList();
    }

    // ================= DTO =================
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