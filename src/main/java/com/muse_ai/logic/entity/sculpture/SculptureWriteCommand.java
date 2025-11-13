package com.muse_ai.logic.entity.sculpture;

import java.util.List;

public record SculptureWriteCommand(
        String name,
        String metadata,
        String sceneJson,
        List<String> tags,
        String slug
) {
}
