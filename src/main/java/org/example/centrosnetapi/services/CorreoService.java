package org.example.centrosnetapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.dtos.SendGroupCorreoRequestDTO;
import org.example.centrosnetapi.models.*;
import org.example.centrosnetapi.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private final CorreoRepository correoRepository;
    private final UsuarioCorreoRepository usuarioCorreoRepository;
    private final UserRepository userRepository;
    private final CourseSubjectRepository courseSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final MessageGroupRepository messageGroupRepository;

    // 📥 INBOX
    public List<Map<String, Object>> getInbox(Long userId) {
        return usuarioCorreoRepository.findInbox(userId);
    }

    // 📤 ENVIADOS
    public List<Map<String, Object>> getSent(Long userId) {
        return correoRepository.findSentByUser(userId);
    }

    // ✅ MARCAR COMO LEÍDO
    @Transactional
    public void markAsRead(Long correoId, Long userId) {
        usuarioCorreoRepository.markAsRead(correoId, userId);
    }

    // 🗑️ BORRAR
    @Transactional
    public void deleteForUser(Long correoId, Long userId) {
        usuarioCorreoRepository.markAsDeleted(correoId, userId);
    }

    // 🔢 CONTADOR
    public Map<String, Integer> getEmailCount(Long userId) {
        int enviados = correoRepository.countSent(userId);
        int recibidos = correoRepository.countReceived(userId);

        return Map.of(
                "enviados", enviados,
                "recibidos", recibidos
        );
    }

    @Transactional
    public void sendCorreo(
            Long emisorId,
            Long destinatarioId,
            String asunto,
            String cuerpo
    ) {

        User emisor = userRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("EMISOR_NOT_FOUND"));

        User destinatario = userRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("DESTINATARIO_NOT_FOUND"));

        // 1️⃣ Crear correo
        Correo correo = new Correo();
        correo.setEmisor(emisor);
        correo.setDestinatario(destinatario);
        correo.setAsunto(asunto);
        correo.setCuerpo(cuerpo);

        correoRepository.save(correo);

        // 2️⃣ Registro para destinatario (NO leído)
        UsuarioCorreo ucDestinatario = new UsuarioCorreo();
        ucDestinatario.setCorreo(correo);
        ucDestinatario.setUsuario(destinatario);
        ucDestinatario.setLeido(false);
        ucDestinatario.setEliminado(false);
        ucDestinatario.setArchivado(false);

        usuarioCorreoRepository.save(ucDestinatario);

        // 3️⃣ Registro para emisor (YA leído)
        UsuarioCorreo ucEmisor = new UsuarioCorreo();
        ucEmisor.setCorreo(correo);
        ucEmisor.setUsuario(emisor);
        ucEmisor.setLeido(true);
        ucEmisor.setEliminado(false);
        ucEmisor.setArchivado(false);

        usuarioCorreoRepository.save(ucEmisor);
    }

    @Transactional
    public void sendToGroup(SendGroupCorreoRequestDTO request, String senderEmail) {

        if (request.getSubjectId() == null) {
            throw new RuntimeException("Debe especificar subjectId");
        }

        // 1️⃣ Obtener emisor
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("USUARIO_NO_ENCONTRADO"));

        // 2️⃣ Obtener asignatura
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("SUBJECT_NOT_FOUND"));

        // 3️⃣ Obtener relación curso-asignatura
        List<CourseSubject> courseSubjects =
                courseSubjectRepository.findBySubjectId(subject.getId());

        if (courseSubjects.isEmpty()) {
            throw new RuntimeException("SUBJECT_SIN_CURSO");
        }

        // 🔥 Asumimos que una asignatura pertenece a un curso
        Course course = courseSubjects.get(0).getCourse();

        // 4️⃣ Crear grupo
        MessageGroup group = new MessageGroup();
        group.setSubject(subject);
        group.setCreatedBy(sender);
        messageGroupRepository.save(group);

        // 5️⃣ Crear correo base
        Correo correo = new Correo();
        correo.setEmisor(sender);
        correo.setAsunto(request.getAsunto());
        correo.setCuerpo(request.getCuerpo());
        correo.setMessageGroup(group);
        correoRepository.save(correo);

        // 6️⃣ Obtener alumnos del curso
        List<User> destinatarios =
                userRepository.findByCourseIdAndRole(course.getId(), Role.STUDENT);

        // 7️⃣ Crear registros UsuarioCorreo
        for (User user : destinatarios) {

            if (!user.getId().equals(sender.getId())) {

                UsuarioCorreo uc = new UsuarioCorreo();
                uc.setCorreo(correo);
                uc.setUsuario(user);
                uc.setLeido(false);

                usuarioCorreoRepository.save(uc);
            }
        }
    }
}