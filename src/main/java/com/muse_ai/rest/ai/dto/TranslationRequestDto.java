package com.muse_ai.rest.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TranslationRequestDto(
        @NotBlank(message = "artworkId is required")
        String artworkId,

        @NotBlank(message = "targetLanguage is required")
        String targetLanguage,

        @NotBlank(message = "originalText is required")
        @Size(max = 5000, message = "originalText cannot exceed 5000 characters")
        String originalText,

        @NotBlank(message = "sourceLanguage is required")
        String sourceLanguage
) {
}
