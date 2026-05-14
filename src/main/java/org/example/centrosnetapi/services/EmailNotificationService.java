package org.example.centrosnetapi.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendNewMessageNotification(String toEmail, String recipientName, String senderName, String subject) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Nuevo mensaje en PentaNet: " + subject);
            
            String htmlContent = String.format(
                "<div style='font-family: sans-serif; padding: 20px; color: #333;'>" +
                "<h2>¡Hola, %s!</h2>" +
                "<p>Has recibido un nuevo mensaje en la plataforma <b>PentaNet</b>.</p>" +
                "<div style='background: #f4f4f4; padding: 15px; border-left: 4px solid #6C63FF; margin: 20px 0;'>" +
                "<strong>Remitente:</strong> %s<br>" +
                "<strong>Asunto:</strong> %s" +
                "</div>" +
                "<p>Puedes leer el mensaje completo e iniciar sesión pulsando el siguiente botón:</p>" +
                "<a href='http://94.143.141.91:8080' style='display: inline-block; padding: 12px 24px; background: #6C63FF; color: white; text-decoration: none; border-radius: 8px; font-weight: bold;'>IR A PENTANET</a>" +
                "<p style='margin-top: 30px; font-size: 12px; color: #888;'>Este es un mensaje automático, por favor no respondas a este correo.</p>" +
                "</div>",
                recipientName, senderName, subject
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            
        } catch (MessagingException e) {
            // Logueamos el error pero no cortamos el flujo de la app
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }
}
