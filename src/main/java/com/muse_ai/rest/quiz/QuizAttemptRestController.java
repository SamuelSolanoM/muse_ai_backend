package com.muse_ai.rest.quiz;

import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import com.muse_ai.rest.quiz.dto.QuizAttemptAnswerResult;
import com.muse_ai.rest.quiz.dto.QuizAttemptRequest;
import com.muse_ai.rest.quiz.dto.QuizAttemptAnswerRequest;
import com.muse_ai.logic.entity.http.GlobalResponseHandler;
import com.muse_ai.logic.entity.quiz.*;
import com.muse_ai.rest.quiz.dto.QuizAttemptResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/quiz-attempts")
public class QuizAttemptRestController {

    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private OptionRepository optionRepository;
    @Autowired private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createAttempt(
            @RequestBody QuizAttemptRequest dto,
            HttpServletRequest request
    ) {

        Quiz quiz = quizRepository.fetchByIdWithQuestions(dto.getQuizId());
        if (quiz == null) {
            return new GlobalResponseHandler().handleResponse(
                    "Quiz " + dto.getQuizId() + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        User user = userRepository.findById(dto.getUserId())
                .orElse(null);
        if (user == null) {
            return new GlobalResponseHandler().handleResponse(
                    "User " + dto.getUserId() + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setTimestamp(LocalDateTime.now());

        List<QuizAttemptAnswer> answerEntities = new ArrayList<>();

        for (QuizAttemptAnswerRequest ansDTO : dto.getAnswers()) {

            Question question = questionRepository.findById(ansDTO.getQuestionId()).orElse(null);
            Option option = optionRepository.findById(ansDTO.getSelectedOptionId()).orElse(null);

            if (question != null && option != null) {

                QuizAttemptAnswer answer = new QuizAttemptAnswer();
                answer.setAttempt(attempt);
                answer.setQuestion(question);
                answer.setSelectedOption(option);

                answerEntities.add(answer);
            }
        }

        attempt.setAnswers(answerEntities);
        attempt.setTotalQuestions(quiz.getQuestions().size());


        long correct = answerEntities.stream()
                .filter(a -> a.getSelectedOption().isCorrect())
                .count();

        attempt.setScore((int) correct);
        quizAttemptRepository.save(attempt);

        double percentage = (quiz.getQuestions().isEmpty())
                ? 0
                : (correct * 100.0) / quiz.getQuestions().size();



        QuizAttemptResponse response = new QuizAttemptResponse();
        response.setAttemptId(attempt.getId());
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getTitle());
        response.setScore(attempt.getScore());
        response.setTotalQuestions(attempt.getTotalQuestions());
        response.setPercentage(percentage);
        response.setTimestamp(attempt.getTimestamp());

        List<QuizAttemptAnswerResult> mappedAnswers = answerEntities.stream()
                .map(a -> {
                    QuizAttemptAnswerResult r = new QuizAttemptAnswerResult();
                    r.setQuestionId(a.getQuestion().getId());
                    r.setQuestionText(a.getQuestion().getText());
                    r.setSelectedOptionId(a.getSelectedOption().getId());
                    r.setSelectedOptionText(a.getSelectedOption().getText());
                    r.setCorrect(a.getSelectedOption().isCorrect());
                    return r;
                }).toList();

        response.setAnswers(mappedAnswers);

        return new GlobalResponseHandler().handleResponse(
                "Attempt created successfully",
                response,
                HttpStatus.CREATED,
                request
        );
    }





    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAttemptsByUser(
            @PathVariable Long userId, HttpServletRequest request) {

        return new GlobalResponseHandler().handleResponse(
                "Attempts retrieved successfully",
                quizAttemptRepository.findByUserId(userId),
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAttemptsByQuiz(
            @PathVariable Long quizId, HttpServletRequest request) {

        return new GlobalResponseHandler().handleResponse(
                "Attempts retrieved successfully",
                quizAttemptRepository.findByQuizId(quizId),
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAttempt(
            @PathVariable Long attemptId, HttpServletRequest request) {

        Optional<QuizAttempt> found = quizAttemptRepository.findById(attemptId);

        if (found.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Attempt id " + attemptId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Attempt retrieved successfully",
                found.get(),
                HttpStatus.OK,
                request
        );
    }

    @GetMapping("/user/{userId}/best-scores")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBestScoresByUser(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);

        if (attempts.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "No quiz attempts found for user " + userId,
                    List.of(),
                    HttpStatus.OK,
                    request
            );
        }

        Map<Long, Integer> bestScores = new HashMap<>();

        attempts.forEach(a -> {
            int score = a.getScore();
            bestScores.merge(a.getQuiz().getId(), score, Math::max);
        });

        return new GlobalResponseHandler().handleResponse(
                "Best scores retrieved successfully",
                bestScores,
                HttpStatus.OK,
                request
        );
    }

}
