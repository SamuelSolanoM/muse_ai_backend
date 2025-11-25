package com.muse_ai.rest.ai;

import com.muse_ai.logic.ai.description.ImageDescriptionService;
import com.muse_ai.rest.ai.dto.ImageDescriptionRequestDto;
import com.muse_ai.rest.ai.dto.ImageDescriptionResponseDto;
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
public class ImageDescriptionController {

    private final ImageDescriptionService imageDescriptionService;

    public ImageDescriptionController(ImageDescriptionService imageDescriptionService) {
        this.imageDescriptionService = imageDescriptionService;
    }

    @PostMapping("/image")
    public ResponseEntity<ImageDescriptionResponseDto> describeImage(@Valid @RequestBody ImageDescriptionRequestDto requestDto) {
        ImageDescriptionResponseDto response = imageDescriptionService.describe(requestDto);
        return ResponseEntity.ok(response);
    }
}
