package com.muse_ai.logic.ai.usage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {

    @Query("""
            select new com.muse_ai.logic.ai.usage.AiUsageSummary(
                FUNCTION('DATE', u.createdAt),
                u.module,
                u.userEmail,
                sum(coalesce(u.promptTokens, 0)),
                sum(coalesce(u.completionTokens, 0)),
                sum(coalesce(u.totalTokens, 0))
            )
            from AiUsageLog u
            where u.createdAt between :start and :end
            group by FUNCTION('DATE', u.createdAt), u.module, u.userEmail
            order by FUNCTION('DATE', u.createdAt) desc
            """)
    List<AiUsageSummary> summarize(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
