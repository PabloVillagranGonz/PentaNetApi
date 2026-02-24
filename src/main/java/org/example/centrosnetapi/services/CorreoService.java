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

    // =====================================================
    // 🔎 RESOLVER USUARIO POR EMAIL (MÉTODO CENTRAL)
    // =====================================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    // =====================================================
    // 📥 INBOX
    // =====================================================

    public List<Map<String, Object>> getInboxByEmail(String email) {
        User user = getUserByEmail(email);
        return usuarioCorreoRepository.findInbox(user.getId());
    }

    // =====================================================
    // 📤 ENVIADOS
    // =====================================================

    public List<Map<String, Object>> getSentByEmail(String email) {
        User user = getUserByEmail(email);
        return usuarioCorreoRepository.findSent(user.getId());
    }

    // =====================================================
    // ✅ MARCAR COMO LEÍDO
    // =====================================================

    @Transactional
    public void markAsRead(Long correoId, String email) {
        User user = getUserByEmail(email);
        usuarioCorreoRepository.markAsRead(correoId, user.getId());
    }

    // =====================================================
    // 🗑️ BORRAR
    // =====================================================

    @Transactional
    public void deleteForUser(Long correoId, String email) {
        User user = getUserByEmail(email);
        usuarioCorreoRepository.markAsDeleted(correoId, user.getId());
    }

    // =====================================================
    // 🔢 CONTADOR
    // =====================================================

    public Map<String, Integer> getEmailCount(String email) {

        User user = getUserByEmail(email);

        int enviados = correoRepository.countSent(user.getId());
        int recibidos = correoRepository.countReceived(user.getId());

        return Map.of(
                "enviados", enviados,
                "recibidos", recibidos
        );
    }

    // =====================================================
    // ✉️ ENVÍO INDIVIDUAL
    // =====================================================

    @Transactional
    public void sendCorreo(
            String senderEmail,
            Long destinatarioId,
            String asunto,
            String cuerpo
    ) {

        User emisor = getUserByEmail(senderEmail);

        User destinatario = userRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("DESTINATARIO_NOT_FOUND"));

        Correo correo = new Correo();
        correo.setEmisor(emisor);
        correo.setDestinatario(destinatario);
        correo.setAsunto(asunto);
        correo.setCuerpo(cuerpo);

        correoRepository.save(correo);

        // Registro destinatario
        UsuarioCorreo ucDestinatario = new UsuarioCorreo();
        ucDestinatario.setCorreo(correo);
        ucDestinatario.setUsuario(destinatario);
        ucDestinatario.setLeido(false);
        ucDestinatario.setEliminado(false);
        ucDestinatario.setArchivado(false);
        usuarioCorreoRepository.save(ucDestinatario);

        // Registro emisor
        UsuarioCorreo ucEmisor = new UsuarioCorreo();
        ucEmisor.setCorreo(correo);
        ucEmisor.setUsuario(emisor);
        ucEmisor.setLeido(true);
        ucEmisor.setEliminado(false);
        ucEmisor.setArchivado(false);
        usuarioCorreoRepository.save(ucEmisor);
    }

    // =====================================================
    // 👥 ENVÍO A GRUPO
    // =====================================================

    @Transactional
    public void sendToGroup(SendGroupCorreoRequestDTO request, String senderEmail) {

        User sender = getUserByEmail(senderEmail);

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("SUBJECT_NOT_FOUND"));

        System.out.println("📌 SUBJECT ID RECIBIDO: " + request.getSubjectId());

        List<CourseSubject> courseSubjects =
                courseSubjectRepository.findBySubjectId(subject.getId());

        System.out.println("📌 COURSE SUBJECTS SIZE: " + courseSubjects.size());

        if (courseSubjects.isEmpty()) {
            throw new RuntimeException("SUBJECT_SIN_CURSO");
        }

        Course course = courseSubjects.get(0).getCourse();

        System.out.println("📌 CURSO ID: " + course.getId());

        MessageGroup group = new MessageGroup();
        group.setSubject(subject);
        group.setCreatedBy(sender);
        messageGroupRepository.save(group);

        Correo correo = new Correo();
        correo.setEmisor(sender);
        correo.setAsunto(request.getAsunto());
        correo.setCuerpo(request.getCuerpo());
        correo.setMessageGroup(group);
        correoRepository.save(correo);

        List<User> destinatarios =
                userRepository.findByCourse_IdAndRole(course.getId(), Role.STUDENT);

        System.out.println("📌 DESTINATARIOS SIZE: " + destinatarios.size());

        destinatarios.forEach(u ->
                System.out.println("👨‍🎓 ALUMNO: " + u.getId() + " - " + u.getEmail())
        );

        for (User user : destinatarios) {
            if (!user.getId().equals(sender.getId())) {

                UsuarioCorreo uc = new UsuarioCorreo();
                uc.setCorreo(correo);
                uc.setUsuario(user);
                uc.setLeido(false);
                uc.setEliminado(false);
                uc.setArchivado(false);

                usuarioCorreoRepository.save(uc);
            }
        }

        UsuarioCorreo ucSender = new UsuarioCorreo();
        ucSender.setCorreo(correo);
        ucSender.setUsuario(sender);
        ucSender.setLeido(true);
        ucSender.setEliminado(false);
        ucSender.setArchivado(false);

        usuarioCorreoRepository.save(ucSender);
    }
}