package com.muse_ai.logic.entity.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions qu WHERE SIZE(q.questions) > 0")
    Page<Quiz> findQuizzesWithQuestions(Pageable pageable);
    @Query("""
       SELECT q 
       FROM Quiz q 
       LEFT JOIN FETCH q.questions 
       WHERE q.id = :id 
         AND SIZE(q.questions) > 0
       """)
    Quiz fetchByIdWithQuestions(Long id);

}
