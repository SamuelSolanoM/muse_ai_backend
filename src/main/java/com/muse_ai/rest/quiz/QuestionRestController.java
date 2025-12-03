package com.muse_ai.rest.quiz;

import com.muse_ai.logic.entity.http.GlobalResponseHandler;
import com.muse_ai.logic.entity.http.Meta;
import com.muse_ai.logic.entity.quiz.Question;
import com.muse_ai.logic.entity.quiz.QuestionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.muse_ai.logic.entity.quiz.Quiz;
import com.muse_ai.logic.entity.quiz.QuizRepository;

import java.util.Optional;

@RestController
@RequestMapping("/quizzes/{quizId}/questions")
public class QuestionRestController {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private QuizRepository quizRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllByQuiz(
            @PathVariable Long quizId,
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size,
            HttpServletRequest request){

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Question> result = questionRepository.findByQuizId(quizId, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(result.getTotalPages());
        meta.setTotalElements(result.getTotalElements());
        meta.setPageNumber(result.getNumber()+1);
        meta.setPageSize(result.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Questions retrieved successfully", result.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<?> getOne(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            HttpServletRequest request){

        Question q = questionRepository.findByIdAndQuizId(questionId, quizId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        return new GlobalResponseHandler().handleResponse(
                "Question found", q, HttpStatus.OK, request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> create(
            @PathVariable Long quizId,
            @RequestBody Question question,
            HttpServletRequest request){

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        question.setQuiz(quiz);
        if(question.getOptions()!=null)
            question.getOptions().forEach(o -> o.setQuestion(question));

        Question saved = questionRepository.save(question);

        return new GlobalResponseHandler().handleResponse(
                "Question created", saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<?> update(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestBody Question data,
            HttpServletRequest request){

        Question q = questionRepository.findByIdAndQuizId(questionId,quizId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        q.setText(data.getText());
        q.setImageUrl(data.getImageUrl());

        q.getOptions().clear();
        if(data.getOptions()!=null)
            data.getOptions().forEach(o->{ o.setQuestion(q); q.getOptions().add(o); });

        return new GlobalResponseHandler().handleResponse(
                "Question updated", questionRepository.save(q), HttpStatus.OK, request);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> delete(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            HttpServletRequest request){

        Question q = questionRepository.findByIdAndQuizId(questionId,quizId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        questionRepository.delete(q);

        return new GlobalResponseHandler().handleResponse(
                "Question deleted", q, HttpStatus.OK, request);
    }
}
