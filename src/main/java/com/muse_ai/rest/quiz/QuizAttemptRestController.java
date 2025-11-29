package com.muse_ai.rest.quiz;

import com.muse_ai.logic.entity.http.GlobalResponseHandler;
import com.muse_ai.logic.entity.http.Meta;
import com.muse_ai.logic.entity.quiz.QuizAttempt;
import com.muse_ai.logic.entity.quiz.QuizAttemptRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/quiz-attempts")
public class QuizAttemptRestController {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAttempt(@RequestBody QuizAttempt attempt, HttpServletRequest request) {

        quizAttemptRepository.save(attempt);

        return new GlobalResponseHandler().handleResponse(
                "Attempt created successfully",
                attempt,
                HttpStatus.CREATED,
                request
        );
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAttemptsByUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);

        return new GlobalResponseHandler().handleResponse(
                "Attempts retrieved successfully",
                attempts,
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAttemptsByQuiz(
            @PathVariable Long quizId,
            HttpServletRequest request) {

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);

        return new GlobalResponseHandler().handleResponse(
                "Attempts retrieved successfully",
                attempts,
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAttempt(
            @PathVariable Long attemptId,
            HttpServletRequest request) {

        Optional<QuizAttempt> found = quizAttemptRepository.findById(attemptId);

        if (found.isPresent()) {
            return new GlobalResponseHandler().handleResponse(
                    "Attempt retrieved successfully",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Attempt id " + attemptId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }
}
