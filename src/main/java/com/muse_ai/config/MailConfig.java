package com.muse_ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

/**
 * Configuración de respaldo para JavaMailSender
 * Evita errores si no se define un bean de correo.
 */
@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender fallbackMailSender() {
        return new NoOpMailSender();
    }

    private static final class NoOpMailSender implements JavaMailSender {
        private static final Logger log = LoggerFactory.getLogger(NoOpMailSender.class);

        @Override
        public MimeMessage createMimeMessage() {
            throw new UnsupportedOperationException("MIME messages no soportados en NoOpMailSender.");
        }

        @Override
        public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
            throw new UnsupportedOperationException("MIME messages no soportados en NoOpMailSender.");
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            log.info("🟡 Skipping MIME mail send (NoOpMailSender activo)");
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            log.info("🟡 Skipping multiple MIME mail sends (NoOpMailSender activo)");
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            log.info("🟡 Skipping simple mail send (NoOpMailSender activo)");
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            log.info("🟡 Skipping multiple simple mail sends (NoOpMailSender activo)");
        }
    }
}
