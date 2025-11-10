package com.muse_ai.logic.entity.auth;

import com.muse_ai.logic.entity.auth.password.PasswordResetToken;
import com.muse_ai.logic.entity.auth.password.PasswordResetTokenRepository;
import com.muse_ai.logic.entity.mail.MailService;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordRecoveryService {

    private static final int CODE_LENGTH = 6;
    private static final Random RANDOM = new Random();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final long expirationMinutes;

    public PasswordRecoveryService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            @Value("${password.reset.expiration-minutes:15}") long expirationMinutes
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            // Evita revelar si el correo existe, pero termina silenciosamente.
            return;
        }

        User user = optionalUser.get();

        // Limpia tokens viejos para el usuario.
        tokenRepository.deleteByUserAndExpiresAtBefore(user, LocalDateTime.now());

        tokenRepository.findTopByUserAndUsedIsFalseOrderByCreatedAtDesc(user)
                .ifPresent(token -> {
                    token.setUsed(true);
                    tokenRepository.save(token);
                });

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode(generateCode());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        tokenRepository.save(token);

        mailService.sendPasswordResetCode(user.getEmail(), token.getCode());
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("El correo proporcionado no está registrado"));

        PasswordResetToken token = tokenRepository.findByUserAndCodeAndUsedIsFalse(user, code)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificación inválido"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código de verificación ha expirado");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int number = RANDOM.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", number);
    }
}
