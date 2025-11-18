package com.muse_ai.rest.painting.dto;

import com.muse_ai.logic.entity.painting.Painting;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaintingResponse(
        UUID id,
        String name,
        String slug,
        List<String> tags,
        String metadata,
        String sceneJson,
        Instant createdAt,
        Instant updatedAt
) {

    public static PaintingResponse from(Painting painting) {
        return new PaintingResponse(
                painting.getId(),
                painting.getName(),
                painting.getSlug(),
                List.copyOf(painting.getTags()),
                painting.getMetadata(),
                painting.getSceneJson(),
                painting.getCreatedAt(),
                painting.getUpdatedAt()
        );
    }
}
