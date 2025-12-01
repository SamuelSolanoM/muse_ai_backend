package com.muse_ai.logic.ai.usage;

import java.time.LocalDate;
import java.util.List;

public record AiUsageReport(
        LocalDate startDate,
        LocalDate endDate,
        long tokenLimit,
        double alertThreshold,
        long totalTokensUsed,
        double usagePercentage,
        boolean alert,
        List<AiUsageSummary> details
) {
}
