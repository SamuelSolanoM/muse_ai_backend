package com.muse_ai.config;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuración de respaldo para JavaMailSender
 * Evita errores si no se define un bean de correo.
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true", matchIfMissing = true)
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailProperties.getHost());
        if (mailProperties.getPort() != null) {
            sender.setPort(mailProperties.getPort());
        }
        sender.setUsername(mailProperties.getUsername());
        sender.setPassword(mailProperties.getPassword());
        sender.setProtocol(mailProperties.getProtocol());

        if (mailProperties.getProperties() != null && !mailProperties.getProperties().isEmpty()) {
            Properties javaMailProps = new Properties();
            javaMailProps.putAll(mailProperties.getProperties());
            sender.setJavaMailProperties(javaMailProps);
        }

        if (mailProperties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }
        return sender;
    }

    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false")
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
