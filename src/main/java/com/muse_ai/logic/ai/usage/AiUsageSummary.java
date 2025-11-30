package com.muse_ai.logic.ai.usage;

import java.time.LocalDate;

public record AiUsageSummary(
        LocalDate date,
        AiModule module,
        String userEmail,
        Long promptTokens,
        Long completionTokens,
        Long totalTokens
) {
    public long totalTokensSafe() {
        return totalTokens == null ? 0 : totalTokens;
    }
}
