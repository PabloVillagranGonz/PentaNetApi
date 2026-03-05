package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.Correo.CorreoResponseDTO;
import org.example.centrosnetapi.dtos.Correo.SendCorreoRequestDTO;
import org.example.centrosnetapi.dtos.Correo.SendGroupCorreoRequestDTO;
import org.example.centrosnetapi.exceptions.ApiException;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // ============================================================
    // 📥 INBOX
    // ============================================================

    public List<CorreoResponseDTO> getInbox(Usuario usuario) {

        return usuarioMensajeRepository
                .findInboxByUsuarioId(usuario.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // 📤 ENVIADOS
    // ============================================================

    public List<CorreoResponseDTO> getSent(Usuario usuario) {

        return mensajesRepository
                .findByRemitenteId(usuario.getId())
                .stream()
                .map(this::toDTOFromMensaje)
                .toList();
    }

    // ============================================================
    // ✅ MARCAR COMO LEÍDO
    // ============================================================

    public void markAsRead(Long mensajeId, Usuario usuario) {

        UsuarioMensaje estado = usuarioMensajeRepository
                .findByMensajeIdAndUsuarioId(mensajeId, usuario.getId())
                .orElseThrow(() ->
                        new ApiException("MENSAJE_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        estado.setLeido(true);
    }

    // ============================================================
    // 🗑️ BORRAR PARA USUARIO
    // ============================================================

    public void deleteForUser(Long mensajeId, Usuario usuario) {

        UsuarioMensaje estado = usuarioMensajeRepository
                .findByMensajeIdAndUsuarioId(mensajeId, usuario.getId())
                .orElseThrow(() ->
                        new ApiException("MENSAJE_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        estado.setEliminado(true);
    }

    // ============================================================
    // ✉️ ENVÍO INDIVIDUAL
    // ============================================================

    public void sendCorreo(Usuario emisor, SendCorreoRequestDTO dto) {

        Usuario destinatario = usuarioRepository.findById(dto.getDestinatarioId())
                .orElseThrow(() ->
                        new ApiException("DESTINATARIO_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        Mensaje mensaje = Mensaje.builder()
                .remitente(emisor)
                .destinatario(destinatario)
                .asunto(dto.getAsunto())
                .cuerpo(dto.getCuerpo())
                .fechaEnvio(LocalDateTime.now())
                .build();

        mensajesRepository.save(mensaje);

        crearEstadoMensaje(mensaje, destinatario, false);
        crearEstadoMensaje(mensaje, emisor, true);
    }

    // ============================================================
    // 👥 ENVÍO A GRUPO (POR ASIGNATURA)
    // ============================================================

    public void sendToGroup(SendGroupCorreoRequestDTO dto,
                            Usuario emisor) {

        Asignatura asignatura = asignaturaRepository
                .findById(dto.getAsignaturaId())
                .orElseThrow(() ->
                        new ApiException("ASIGNATURA_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        // Obtener curso asociado a la asignatura
        Curso curso = cursoRepository
                .findFirstByAsignaturasId(asignatura.getId())
                .orElseThrow(() ->
                        new ApiException("ASIGNATURA_SIN_CURSO", HttpStatus.BAD_REQUEST)
                );

        GrupoMensajes grupo = GrupoMensajes.builder()
                .asignatura(asignatura)
                .creadoPor(emisor)
                .build();

        grupoMensajesRepository.save(grupo);

        Mensaje mensaje = Mensaje.builder()
                .remitente(emisor)
                .grupo(grupo)
                .asunto(dto.getAsunto())
                .cuerpo(dto.getCuerpo())
                .build();

        mensajesRepository.save(mensaje);

        // Buscar alumnos del curso
        List<Usuario> alumnos =
                usuarioRepository.findByCurso_IdAndRol(
                        curso.getId(),
                        Rol.ALUMNO
                );

        for (Usuario alumno : alumnos) {

            if (!alumno.getId().equals(emisor.getId())) {

                crearEstadoMensaje(mensaje, alumno, false);
            }
        }

        crearEstadoMensaje(mensaje, emisor, true);
    }

    // ============================================================
    // 🛠 MÉTODOS PRIVADOS
    // ============================================================

    private void crearEstadoMensaje(Mensaje mensaje,
                                    Usuario usuario,
                                    boolean leido) {

        UsuarioMensaje estado = UsuarioMensaje.builder()
                .mensaje(mensaje)
                .usuario(usuario)
                .leido(leido)
                .eliminado(false)
                .archivado(false)
                .build();

        usuarioMensajeRepository.save(estado);
    }

    private CorreoResponseDTO toDTO(UsuarioMensaje um) {

        Mensaje m = um.getMensaje();

        return CorreoResponseDTO.builder()
                .id(m.getId())
                .asunto(m.getAsunto())
                .cuerpo(m.getCuerpo())

                .emisorId(m.getRemitente().getId())
                .emisorNombre(
                        m.getRemitente().getNombre() + " " +
                                m.getRemitente().getApellidos()
                )

                .destinatarioId(
                        m.getDestinatario() != null
                                ? m.getDestinatario().getId()
                                : null
                )

                .destinatarioNombre(
                        m.getDestinatario() != null
                                ? m.getDestinatario().getNombre() + " " +
                                m.getDestinatario().getApellidos()
                                : "Grupo"
                )

                .leido(um.getLeido())
                .archivado(um.getArchivado())
                .fechaEnvio(m.getFechaEnvio())
                .build();
    }

    private CorreoResponseDTO toDTOFromMensaje(Mensaje m) {

        return CorreoResponseDTO.builder()
                .id(m.getId())
                .asunto(m.getAsunto())
                .cuerpo(m.getCuerpo())

                .emisorId(m.getRemitente().getId())
                .emisorNombre(
                        m.getRemitente().getNombre() + " " +
                                m.getRemitente().getApellidos()
                )

                .destinatarioId(
                        m.getDestinatario() != null
                                ? m.getDestinatario().getId()
                                : null
                )

                .destinatarioNombre(
                        m.getDestinatario() != null
                                ? m.getDestinatario().getNombre() + " " +
                                m.getDestinatario().getApellidos()
                                : "Grupo"
                )

                .fechaEnvio(m.getFechaEnvio())
                .build();
    }

    public long countUnread(Usuario usuario) {

        return usuarioMensajeRepository.countUnreadByUsuarioId(
                usuario.getId()
        );
    }
}