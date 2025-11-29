package com.muse_ai.logic.entity.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Long> {

    List<QuizAttemptAnswer> findByAttemptId(Long attemptId);

    List<QuizAttemptAnswer> findByQuestionId(Long questionId);

    List<QuizAttemptAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
