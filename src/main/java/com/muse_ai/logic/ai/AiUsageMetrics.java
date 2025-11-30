package com.muse_ai.logic.ai;

public record AiUsageMetrics(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {
}
