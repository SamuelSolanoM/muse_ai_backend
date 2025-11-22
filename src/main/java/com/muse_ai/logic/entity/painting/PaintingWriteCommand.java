package com.muse_ai.logic.entity.painting;

import java.util.List;

public record PaintingWriteCommand(
        String name,
        String metadata,
        String sceneJson,
        List<String> tags,
        String slug
) { }
