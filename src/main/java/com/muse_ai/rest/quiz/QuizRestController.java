package com.muse_ai.rest.quiz;

import com.muse_ai.logic.entity.http.GlobalResponseHandler;
import com.muse_ai.logic.entity.http.Meta;
import com.muse_ai.logic.entity.quiz.Quiz;
import com.muse_ai.logic.entity.quiz.QuizRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/quizzes")
public class QuizRestController {

    @Autowired
    private QuizRepository quizRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Quiz> quizzes = quizRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(quizzes.getTotalPages());
        meta.setTotalElements(quizzes.getTotalElements());
        meta.setPageNumber(quizzes.getNumber() + 1);
        meta.setPageSize(quizzes.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Quizzes retrieved successfully",
                quizzes.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createQuiz(@RequestBody Quiz quiz, HttpServletRequest request) {
        quizRepository.save(quiz);
        return new GlobalResponseHandler().handleResponse(
                "Quiz created successfully",
                quiz,
                HttpStatus.CREATED,
                request
        );
    }

    @GetMapping("/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getQuizById(
            @PathVariable Long quizId,
            HttpServletRequest request) {

        Optional<Quiz> foundQuiz = quizRepository.findById(quizId);

        if (foundQuiz.isPresent()) {
            return new GlobalResponseHandler().handleResponse(
                    "Quiz retrieved successfully",
                    foundQuiz.get(),
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Quiz id " + quizId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody Quiz quiz,
            HttpServletRequest request) {

        Optional<Quiz> foundQuiz = quizRepository.findById(quizId);

        if (foundQuiz.isPresent()) {
            quiz.setId(quizId);
            quizRepository.save(quiz);

            return new GlobalResponseHandler().handleResponse(
                    "Quiz updated successfully",
                    quiz,
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Quiz id " + quizId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteQuiz(
            @PathVariable Long quizId,
            HttpServletRequest request) {

        Optional<Quiz> foundQuiz = quizRepository.findById(quizId);

        if (foundQuiz.isPresent()) {
            quizRepository.deleteById(quizId);
            return new GlobalResponseHandler().handleResponse(
                    "Quiz deleted successfully",
                    foundQuiz.get(),
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Quiz id " + quizId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }
}
