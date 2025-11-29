package com.muse_ai.logic.entity.quiz;

import com.muse_ai.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserId(Long userId);

    List<QuizAttempt> findByUser(User user);

    List<QuizAttempt> findByQuizId(Long quizId);

    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
}
