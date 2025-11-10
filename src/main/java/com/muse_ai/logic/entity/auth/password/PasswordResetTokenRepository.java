package com.muse_ai.logic.entity.auth.password;

import com.muse_ai.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByUserAndUsedIsFalseOrderByCreatedAtDesc(User user);

    Optional<PasswordResetToken> findByUserAndCodeAndUsedIsFalse(User user, String code);

    long deleteByUserAndExpiresAtBefore(User user, LocalDateTime threshold);
}
