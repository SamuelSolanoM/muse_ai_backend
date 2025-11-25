package com.muse_ai.rest.ai.dto;

public record TranslationResponseDto(
        String artworkId,
        String targetLanguage,
        String translation,
        boolean cached
) {
}
