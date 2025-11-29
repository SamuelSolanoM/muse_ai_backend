package com.muse_ai.logic.entity.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findByQuizId(Long quizId, Pageable pageable);
    Optional<Question> findByIdAndQuizId(Long id, Long quizId);
}
