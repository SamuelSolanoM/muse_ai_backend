package com.muse_ai.logic.entity.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(
            JavaMailSender mailSender,
            @Value("${password.reset.email-from:no-reply@museai.local}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("MuseAI - Código de recuperación de contraseña");
        message.setText(buildMessageBody(code));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send password reset code to {}", to, ex);
            throw ex;
        }
    }

    private String buildMessageBody(String code) {
        return """
                Hola,

                Hemos recibido una solicitud para restablecer tu contraseña en MuseAI.
                Usa el siguiente código para completar el proceso:

                Código: %s

                Si no solicitaste este cambio, puedes ignorar este correo.

                Equipo MuseAI
                """.formatted(code);
    }
}

