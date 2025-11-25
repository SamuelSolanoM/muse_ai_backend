package com.muse_ai.rest.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImageDescriptionRequestDto(
        @NotBlank(message = "imageBase64 is required")
        @Size(max = 5_000_000, message = "imageBase64 cannot exceed 5MB encoded")
        String imageBase64,

        @NotBlank(message = "language is required")
        @Size(max = 10, message = "language is too long")
        String language
) {
}
