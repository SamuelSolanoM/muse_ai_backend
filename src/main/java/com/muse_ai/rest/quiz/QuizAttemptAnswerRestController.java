package com.muse_ai.rest.quiz;

import com.muse_ai.logic.entity.http.GlobalResponseHandler;
import com.muse_ai.logic.entity.http.Meta;
import com.muse_ai.logic.entity.quiz.QuizAttemptAnswer;
import com.muse_ai.logic.entity.quiz.QuizAttemptAnswerRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/quiz-attempt-answers")
public class QuizAttemptAnswerRestController {

    @Autowired
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAnswer(
            @RequestBody QuizAttemptAnswer answer,
            HttpServletRequest request) {

        quizAttemptAnswerRepository.save(answer);

        return new GlobalResponseHandler().handleResponse(
                "Attempt answer created successfully",
                answer,
                HttpStatus.CREATED,
                request
        );
    }

    @GetMapping("/attempt/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnswersByAttempt(
            @PathVariable Long attemptId,
            HttpServletRequest request) {

        List<QuizAttemptAnswer> answers =
                quizAttemptAnswerRepository.findByAttemptId(attemptId);

        return new GlobalResponseHandler().handleResponse(
                "Attempt answers retrieved successfully",
                answers,
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/{answerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnswer(
            @PathVariable Long answerId,
            HttpServletRequest request) {

        Optional<QuizAttemptAnswer> found =
                quizAttemptAnswerRepository.findById(answerId);

        if (found.isPresent()) {
            return new GlobalResponseHandler().handleResponse(
                    "Attempt answer retrieved successfully",
                    found.get(),
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Attempt answer id " + answerId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }
}
