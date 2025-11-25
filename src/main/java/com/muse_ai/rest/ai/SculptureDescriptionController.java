package com.muse_ai.rest.ai;

import com.muse_ai.logic.ai.description.SculptureDescriptionService;
import com.muse_ai.rest.ai.dto.SculptureDescriptionRequestDto;
import com.muse_ai.rest.ai.dto.SculptureDescriptionResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/ai/descriptions", "/ai/descriptions"})
@CrossOrigin(origins = {"${app.frontend.origin:http://localhost:4200}"})
public class SculptureDescriptionController {

    private final SculptureDescriptionService sculptureDescriptionService;

    public SculptureDescriptionController(SculptureDescriptionService sculptureDescriptionService) {
        this.sculptureDescriptionService = sculptureDescriptionService;
    }

    @PostMapping("/sculpture")
    public ResponseEntity<SculptureDescriptionResponseDto> describeSculpture(
            @Valid @RequestBody SculptureDescriptionRequestDto requestDto
    ) {
        SculptureDescriptionResponseDto response = sculptureDescriptionService.describe(requestDto);
        return ResponseEntity.ok(response);
    }
}
