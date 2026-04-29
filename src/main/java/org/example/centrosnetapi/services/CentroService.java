package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Centro.CenterRequestDTO;
import org.example.centrosnetapi.dtos.Centro.CenterResponseDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.Centro;
import org.example.centrosnetapi.models.Usuario;
import org.example.centrosnetapi.repositories.CentroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CentroService {

    private final CentroRepository centroRepository;

    // ============================================================
    // MÉTODOS PÚBLICOS (Lógica de Negocio)
    // ============================================================

    public CenterResponseDTO create(CenterRequestDTO dto, Usuario adminLogueado) {
        validarSuperAdmin(adminLogueado, "SOLO_SUPERADMIN_PUEDE_CREAR_CENTROS");
        validarEmailUnico(dto.getEmail(), null);

        Centro centro = Centro.builder()
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .horarioApertura(dto.getHorarioApertura())
                .direccion(dto.getDireccion())
                .codigoPostal(dto.getCodigoPostal())
                .ciudad(dto.getCiudad())
                .build();

        return toDTO(centroRepository.save(centro));
    }

    public List<CenterResponseDTO> findAll(Usuario adminLogueado) {
        // 🔥 CANDADO SAAS: Si es admin local, solo ve el suyo
        if (adminLogueado.getCentro() != null) {
            return List.of(toDTO(adminLogueado.getCentro()));
        }

        return centroRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public CenterResponseDTO findById(Long id, Usuario adminLogueado) {
        validarAccesoAlCentro(adminLogueado, id, "ACCESO_DENEGADO");
        Centro centro = buscarCentro(id);

        return toDTO(centro);
    }

    public CenterResponseDTO update(Long id, CenterRequestDTO dto, Usuario adminLogueado) {
        validarAccesoAlCentro(adminLogueado, id, "NO_PUEDES_EDITAR_OTRO_CENTRO");
        Centro centro = buscarCentro(id);
        validarEmailUnico(dto.getEmail(), id);

        centro.setNombre(dto.getNombre());
        centro.setTelefono(dto.getTelefono());
        centro.setEmail(dto.getEmail());
        centro.setWebsite(dto.getWebsite());
        centro.setHorarioApertura(dto.getHorarioApertura());
        centro.setDireccion(dto.getDireccion());
        centro.setCodigoPostal(dto.getCodigoPostal());
        centro.setCiudad(dto.getCiudad());

        return toDTO(centroRepository.save(centro));
    }

    public void delete(Long id, Usuario adminLogueado) {
        validarSuperAdmin(adminLogueado, "SOLO_SUPERADMIN_PUEDE_BORRAR_CENTROS");
        Centro centro = buscarCentro(id); // Reutilizamos el buscador para asegurar que existe

        centroRepository.delete(centro);
    }

    // ============================================================
    // MÉTODOS PRIVADOS (Validaciones y Buscadores)
    // ============================================================

    private Centro buscarCentro(Long id) {
        return centroRepository.findById(id)
                .orElseThrow(() -> new ApiException("CENTRO_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private void validarSuperAdmin(Usuario usuario, String mensajeError) {
        if (usuario.getCentro() != null) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private void validarAccesoAlCentro(Usuario usuario, Long centroIdObjetivo, String mensajeError) {
        if (usuario.getCentro() != null && !usuario.getCentro().getId().equals(centroIdObjetivo)) {
            throw new ApiException(mensajeError, HttpStatus.FORBIDDEN);
        }
    }

    private void validarEmailUnico(String email, Long idExcluido) {
        if (email == null) return; // Si no hay email, no validamos duplicados

        boolean existe = (idExcluido == null)
                ? centroRepository.existsByEmail(email)
                : centroRepository.existsByEmailAndIdNot(email, idExcluido);

        if (existe) {
            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private CenterResponseDTO toDTO(Centro c) {
        return CenterResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .telefono(c.getTelefono())
                .email(c.getEmail())
                .website(c.getWebsite())
                .horarioApertura(c.getHorarioApertura())
                .direccion(c.getDireccion())
                .codigoPostal(c.getCodigoPostal())
                .ciudad(c.getCiudad())
                .build();
    }
}