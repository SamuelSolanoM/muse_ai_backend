package com.muse_ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;

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
            throw new UnsupportedOperationException("Mime messages are not supported in NoOpMailSender.");
        }

        @Override
        public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
            throw new UnsupportedOperationException("Mime messages are not supported in NoOpMailSender.");
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            log.info("Skipping MIME mail send (NoOpMailSender).");
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            log.info("Skipping MIME mail send (NoOpMailSender).");
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) {
            log.info("Skipping MIME mail send (NoOpMailSender).");
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) {
            log.info("Skipping MIME mail send (NoOpMailSender).");
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            log.info("Pretending to send mail to {} with subject '{}'.", String.join(", ", simpleMessage.getTo()), simpleMessage.getSubject());
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            for (SimpleMailMessage message : simpleMessages) {
                send(message);
            }
        }
    }
}

