package com.muse_ai.logic.ai.usage;

import java.sql.Date;
import java.time.LocalDate;

public record AiUsageSummary(
        Date date,
        AiModule module,
        String userEmail,
        Long promptTokens,
        Long completionTokens,
        Long totalTokens
) {
    public long totalTokensSafe() {
        return totalTokens == null ? 0 : totalTokens;
    }

    public LocalDate dateAsLocalDate() {
        return date == null ? null : date.toLocalDate();
    }
}
