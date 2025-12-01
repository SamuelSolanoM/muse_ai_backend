package com.muse_ai.rest.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SculptureDescriptionRequestDto(
        @NotBlank(message = "name is required")
        @Size(max = 160, message = "name cannot exceed 160 characters")
        String name,

        @NotEmpty(message = "labels are required")
        @Size(max = 15, message = "labels cannot exceed 15 items")
        List<@NotBlank(message = "label cannot be blank") @Size(max = 60, message = "label is too long") String> labels,

        @NotBlank(message = "language is required")
        @Size(max = 10, message = "language is too long")
        String language
) {
}
