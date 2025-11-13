package com.muse_ai.rest.sculpture.dto;

import com.muse_ai.logic.entity.sculpture.Sculpture;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SculptureResponse(
        UUID id,
        String name,
        String slug,
        List<String> tags,
        String metadata,
        String sceneJson,
        Instant createdAt,
        Instant updatedAt
) {

    public static SculptureResponse from(Sculpture sculpture) {
        return new SculptureResponse(
                sculpture.getId(),
                sculpture.getName(),
                sculpture.getSlug(),
                List.copyOf(sculpture.getTags()),
                sculpture.getMetadata(),
                sculpture.getSceneJson(),
                sculpture.getCreatedAt(),
                sculpture.getUpdatedAt()
        );
    }
}
