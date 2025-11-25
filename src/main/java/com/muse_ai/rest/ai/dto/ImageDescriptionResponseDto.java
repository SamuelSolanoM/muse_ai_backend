package com.muse_ai.rest.ai.dto;

public record ImageDescriptionResponseDto(
        String language,
        String description,
        boolean cached
) {
}
