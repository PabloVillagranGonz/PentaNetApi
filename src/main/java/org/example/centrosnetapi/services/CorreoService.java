package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Correo.CorreoResponseDTO;
import org.example.centrosnetapi.dtos.Correo.SendCorreoRequestDTO;
import org.example.centrosnetapi.dtos.Correo.SendGroupCorreoRequestDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CorreoService {

    private final MensajeRepository mensajesRepository;
    private final UsuarioMensajeRepository usuarioMensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final CursoRepository cursoRepository;
    private final GrupoMensajesRepository grupoMensajesRepository;
    private final EmailNotificationService emailNotificationService;

    // ============================================================
    // 📥 LECTURA DE BANDEJAS
    // ============================================================

    public List<CorreoResponseDTO> getInbox(Usuario usuario) {
        return usuarioMensajeRepository.findInboxByUsuarioId(usuario.getId())
                .stream()
                .map(um -> this.toDTO(um, false)) // false = sin cuerpo
                .toList();
    }


    public List<CorreoResponseDTO> getSent(Usuario usuario) {
        return mensajesRepository.findByRemitenteId(usuario.getId())
                .stream()
                .map(m -> toDTOFromMensaje(m, false)) // false = sin cuerpo
                .toList();
    }

    public CorreoResponseDTO getById(Long id, Usuario usuario) {
        // Buscamos el estado para este usuario (para saber si es el emisor o destinatario y si tiene permiso)
        UsuarioMensaje um = usuarioMensajeRepository.findByMensajeIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ApiException("MENSAJE_NOT_FOUND", HttpStatus.NOT_FOUND));

        return toDTO(um, true); // true = con cuerpo
    }


    public long countUnread(Usuario usuario) {
        return usuarioMensajeRepository.countUnreadByUsuarioId(usuario.getId());
    }

    // ============================================================
    // ✅ ACCIONES DE ESTADO
    // ============================================================

    public void markAsRead(Long mensajeId, Usuario usuario) {
        UsuarioMensaje estado = buscarEstadoMensajeUsuario(mensajeId, usuario.getId());
        estado.setLeido(true);
    }

    public void deleteForUser(Long mensajeId, Usuario usuario) {
        UsuarioMensaje estado = buscarEstadoMensajeUsuario(mensajeId, usuario.getId());
        estado.setEliminado(true);
    }

    // ============================================================
    // ✉️ ENVÍO DE MENSAJES
    // ============================================================

    public void sendCorreo(Usuario emisor, SendCorreoRequestDTO dto) {
        Usuario destinatario = usuarioRepository.findById(dto.getDestinatarioId())
                .orElseThrow(() -> new ApiException("DESTINATARIO_NOT_FOUND", HttpStatus.NOT_FOUND));

        // CANDADO SAAS: Evitar que hablen con usuarios de otros centros
        validarMismoCentro(emisor, destinatario);

        Mensaje mensaje = Mensaje.builder()
                .remitente(emisor)
                .destinatario(destinatario)
                .asunto(dto.getAsunto())
                .cuerpo(dto.getCuerpo())
                .fechaEnvio(LocalDateTime.now())
                .build();

        mensajesRepository.save(mensaje);

        // Guardamos los estados en bloque
        usuarioMensajeRepository.saveAll(List.of(
                construirEstado(mensaje, destinatario, false),
                construirEstado(mensaje, emisor, true)
        ));

        // Enviar notificación por email
        emailNotificationService.sendNewMessageNotification(
                destinatario.getEmail(),
                destinatario.getNombre(),
                emisor.getNombre() + " " + emisor.getApellidos(),
                dto.getAsunto()
        );
    }

    public void sendToGroup(SendGroupCorreoRequestDTO dto, Usuario emisor) {
        Asignatura asignatura = buscarAsignatura(dto.getAsignaturaId());
        Curso curso = buscarCursoDeAsignatura(asignatura.getId());

        GrupoMensajes grupo = grupoMensajesRepository.save(
                GrupoMensajes.builder().asignatura(asignatura).creadoPor(emisor).build()
        );

        Mensaje mensaje = mensajesRepository.save(
                Mensaje.builder()
                        .remitente(emisor)
                        .grupo(grupo)
                        .asunto(dto.getAsunto())
                        .cuerpo(dto.getCuerpo())
                        .fechaEnvio(LocalDateTime.now())
                        .build()
        );

        List<Usuario> alumnos = usuarioRepository.findByCurso_IdAndRol(curso.getId(), Rol.ALUMNO);
        List<UsuarioMensaje> estadosAGuardar = new ArrayList<>();

        for (Usuario alumno : alumnos) {
            if (!alumno.getId().equals(emisor.getId())) {
                estadosAGuardar.add(construirEstado(mensaje, alumno, false));
            }
        }
        estadosAGuardar.add(construirEstado(mensaje, emisor, true));

        // OPTIMIZACIÓN: 1 sola consulta a BD para todos los alumnos en vez de N consultas
        usuarioMensajeRepository.saveAll(estadosAGuardar);

        // Enviar notificaciones por email a todo el grupo
        for (Usuario alumno : alumnos) {
            if (!alumno.getId().equals(emisor.getId())) {
                emailNotificationService.sendNewMessageNotification(
                        alumno.getEmail(),
                        alumno.getNombre(),
                        emisor.getNombre() + " " + emisor.getApellidos(),
                        dto.getAsunto()
                );
            }
        }
    }

    // ============================================================
    // 🛠 MÉTODOS PRIVADOS (Buscadores y Validadores)
    // ============================================================

    private UsuarioMensaje buscarEstadoMensajeUsuario(Long mensajeId, Long usuarioId) {
        return usuarioMensajeRepository.findByMensajeIdAndUsuarioId(mensajeId, usuarioId)
                .orElseThrow(() -> new ApiException("MENSAJE_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Asignatura buscarAsignatura(Long asignaturaId) {
        return asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private Curso buscarCursoDeAsignatura(Long asignaturaId) {
        return cursoRepository.findFirstByAsignaturasId(asignaturaId)
                .orElseThrow(() -> new ApiException("ASIGNATURA_SIN_CURSO", HttpStatus.BAD_REQUEST));
    }

    private void validarMismoCentro(Usuario emisor, Usuario destinatario) {
        if (emisor.getCentro() != null && destinatario.getCentro() != null &&
                !emisor.getCentro().getId().equals(destinatario.getCentro().getId())) {
            throw new ApiException("USUARIOS_DE_DISTINTOS_CENTROS", HttpStatus.FORBIDDEN);
        }
    }

    private UsuarioMensaje construirEstado(Mensaje mensaje, Usuario usuario, boolean leido) {
        return UsuarioMensaje.builder()
                .mensaje(mensaje)
                .usuario(usuario)
                .leido(leido)
                .eliminado(false)
                .archivado(false)
                .build();
    }

    // ============================================================
    // 🗺 MAPPERS
    // ============================================================

    private CorreoResponseDTO toDTO(UsuarioMensaje um, boolean incluirCuerpo) {
        Mensaje m = um.getMensaje();
        return construirDTOBase(m, incluirCuerpo)
                .leido(um.getLeido())
                .archivado(um.getArchivado())
                .build();
    }

    private CorreoResponseDTO toDTOFromMensaje(Mensaje m, boolean incluirCuerpo) {
        return construirDTOBase(m, incluirCuerpo).build();
    }

    private CorreoResponseDTO.CorreoResponseDTOBuilder construirDTOBase(Mensaje m, boolean incluirCuerpo) {
        String cuerpoResponse = incluirCuerpo ? m.getCuerpo() : "(...)";
        
        return CorreoResponseDTO.builder()
                .id(m.getId())
                .asunto(m.getAsunto())
                .cuerpo(cuerpoResponse)
                .emisorId(m.getRemitente().getId())
                .emisorNombre(obtenerNombreCompleto(m.getRemitente()))
                .destinatarioId(m.getDestinatario() != null ? m.getDestinatario().getId() : null)
                .destinatarioNombre(m.getDestinatario() != null ? obtenerNombreCompleto(m.getDestinatario()) : "Grupo")
                .fechaEnvio(m.getFechaEnvio());
    }


    private String obtenerNombreCompleto(Usuario u) {
        return u.getNombre() + " " + u.getApellidos();
    }
}