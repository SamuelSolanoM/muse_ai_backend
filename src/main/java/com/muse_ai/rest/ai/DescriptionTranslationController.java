package com.muse_ai.rest.ai;

import com.muse_ai.logic.ai.translation.DescriptionTranslationService;
import com.muse_ai.rest.ai.dto.TranslationRequestDto;
import com.muse_ai.rest.ai.dto.TranslationResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/ai/translations", "/ai/translations"})
@CrossOrigin(origins = {"${app.frontend.origin:http://localhost:4200}"})
public class DescriptionTranslationController {

    private final DescriptionTranslationService descriptionTranslationService;

    public DescriptionTranslationController(DescriptionTranslationService descriptionTranslationService) {
        this.descriptionTranslationService = descriptionTranslationService;
    }

    @PostMapping("/description")
    public ResponseEntity<TranslationResponseDto> translateDescription(@Valid @RequestBody TranslationRequestDto requestDto) {
        TranslationResponseDto response = descriptionTranslationService.translate(requestDto);
        return ResponseEntity.ok(response);
    }
}
