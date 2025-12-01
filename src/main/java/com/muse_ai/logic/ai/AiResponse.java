package com.muse_ai.logic.ai;

public record AiResponse(
        String content,
        AiUsageMetrics usage,
        String model
) {
}
