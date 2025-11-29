package com.muse_ai.rest.quiz;

import com.muse_ai.logic.entity.http.GlobalResponseHandler;
import com.muse_ai.logic.entity.http.Meta;
import com.muse_ai.logic.entity.quiz.Option;
import com.muse_ai.logic.entity.quiz.OptionRepository;
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

import java.util.Optional;

@RestController
@RequestMapping("/options")
public class OptionRestController {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Option> options = optionRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(options.getTotalPages());
        meta.setTotalElements(options.getTotalElements());
        meta.setPageNumber(options.getNumber() + 1);
        meta.setPageSize(options.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Options retrieved successfully",
                options.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createOption(
            @RequestParam Long questionId,
            @RequestBody Option option,
            HttpServletRequest request) {

        Optional<Question> question = questionRepository.findById(questionId);

        if (question.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Question id " + questionId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        option.setQuestion(question.get());
        optionRepository.save(option);

        return new GlobalResponseHandler().handleResponse(
                "Option created successfully",
                option,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/{optionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateOption(
            @PathVariable Long optionId,
            @RequestBody Option option,
            HttpServletRequest request) {

        Optional<Option> foundOption = optionRepository.findById(optionId);

        if (foundOption.isPresent()) {
            Option existing = foundOption.get();
            option.setId(optionId);
            option.setQuestion(existing.getQuestion()); // keep relation
            optionRepository.save(option);

            return new GlobalResponseHandler().handleResponse(
                    "Option updated successfully",
                    option,
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Option id " + optionId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @DeleteMapping("/{optionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteOption(
            @PathVariable Long optionId,
            HttpServletRequest request) {

        Optional<Option> foundOption = optionRepository.findById(optionId);

        if (foundOption.isPresent()) {
            optionRepository.deleteById(optionId);
            return new GlobalResponseHandler().handleResponse(
                    "Option deleted successfully",
                    foundOption.get(),
                    HttpStatus.OK,
                    request
            );
        }

        return new GlobalResponseHandler().handleResponse(
                "Option id " + optionId + " not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }
}
