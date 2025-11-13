package com.muse_ai.rest.sculpture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SculptureRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "sceneJson is required")
        String sceneJson,

        @NotBlank(message = "metadata is required")
        String metadata,

        @Size(max = 25, message = "A sculpture can only have up to 25 tags")
        List<@NotBlank(message = "Tags cannot contain blank values") String> tags,

        @Size(max = 140, message = "Slug cannot exceed 140 characters")
        String slug
) {
}
