package com.muse_ai.rest.ai.dto;

import java.util.List;

public record SculptureDescriptionResponseDto(
        String language,
        String name,
        List<String> labels,
        String description,
        boolean cached
) {
}
