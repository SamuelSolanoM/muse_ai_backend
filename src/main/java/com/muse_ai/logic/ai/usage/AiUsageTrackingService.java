package com.muse_ai.logic.ai.usage;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.muse_ai.logic.ai.AiUsageMetrics;
import com.muse_ai.logic.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AiUsageTrackingService {

    private static final Logger log = LoggerFactory.getLogger(AiUsageTrackingService.class);

    private final AiUsageLogRepository repository;
    private final AiUsageProperties properties;

    public AiUsageTrackingService(
            AiUsageLogRepository repository,
            AiUsageProperties properties
    ) {
        this.repository = repository;
        this.properties = properties;
    }

    public void recordUsage(AiModule module, String model, AiUsageMetrics usageMetrics) {
        Objects.requireNonNull(module, "module is required to record AI usage");
        AiUsageLog logEntry = new AiUsageLog();
        logEntry.setModule(module);
        logEntry.setModel(model);
        logEntry.setPromptTokens(toLong(usageMetrics == null ? null : usageMetrics.promptTokens()));
        logEntry.setCompletionTokens(toLong(usageMetrics == null ? null : usageMetrics.completionTokens()));
        logEntry.setTotalTokens(toLong(usageMetrics == null ? null : usageMetrics.totalTokens()));

        CurrentUser currentUser = resolveCurrentUser();
        if (currentUser != null) {
            logEntry.setUserId(currentUser.id());
            logEntry.setUserEmail(currentUser.email());
        }

        repository.save(logEntry);
    }

    public AiUsageReport buildReport(LocalDate startDate, LocalDate endDate) {
        LocalDate start = normalizeStart(startDate);
        LocalDate end = normalizeEnd(endDate, start);

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.plusDays(1).atStartOfDay().minusNanos(1);

        List<AiUsageSummary> summaries = repository.summarize(from, to);
        long totalUsed = summaries.stream().mapToLong(AiUsageSummary::totalTokensSafe).sum();
        long limit = properties.getTokenLimit();
        double threshold = properties.getAlertThreshold();
        double usagePercentage = limit <= 0 ? 0 : (double) totalUsed / limit;
        boolean alert = limit > 0 && usagePercentage >= threshold;

        return new AiUsageReport(
                start,
                end,
                limit,
                threshold,
                totalUsed,
                usagePercentage,
                alert,
                summaries
        );
    }

    public byte[] exportCsv(AiUsageReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("Fecha,Modulo,Usuario,PromptTokens,CompletionTokens,TotalTokens\n");
        for (AiUsageSummary summary : report.details()) {
            builder.append(summary.dateAsLocalDate()).append(',')
                    .append(summary.module()).append(',')
                    .append(safe(summary.userEmail())).append(',')
                    .append(summary.promptTokens() == null ? 0 : summary.promptTokens()).append(',')
                    .append(summary.completionTokens() == null ? 0 : summary.completionTokens()).append(',')
                    .append(summary.totalTokensSafe())
                    .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportPdf(AiUsageReport report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Reporte de consumo de tokens IA"));
            document.add(new Paragraph("Rango: " + report.startDate() + " a " + report.endDate()));
            document.add(new Paragraph("Tokens usados: " + report.totalTokensUsed() + " de " + report.tokenLimit()));
            document.add(new Paragraph("Porcentaje usado: " + String.format("%.2f%%", report.usagePercentage() * 100)));
            if (report.alert()) {
                document.add(new Paragraph("ALERTA: consumo supera el " + (int) (report.alertThreshold() * 100) + "% del límite"));
            }

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Fecha");
            addHeaderCell(table, "Módulo");
            addHeaderCell(table, "Usuario");
            addHeaderCell(table, "Prompt tokens");
            addHeaderCell(table, "Completion tokens");
            addHeaderCell(table, "Total tokens");

            for (AiUsageSummary summary : report.details()) {
                table.addCell(value(String.valueOf(summary.dateAsLocalDate())));
                table.addCell(value(moduleLabel(summary.module())));
                table.addCell(value(safe(summary.userEmail())));
                table.addCell(value(String.valueOf(summary.promptTokens() == null ? 0 : summary.promptTokens())));
                table.addCell(value(String.valueOf(summary.completionTokens() == null ? 0 : summary.completionTokens())));
                table.addCell(value(String.valueOf(summary.totalTokensSafe())));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to export PDF report", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error exporting PDF", e);
        }
    }

    private String moduleLabel(AiModule module) {
        if (module == null) {
            return "N/A";
        }
        return switch (module) {
            case TRANSLATION -> "Traducciones";
            case IMAGE_DESCRIPTION -> "Descripciones de imágenes";
            case SCULPTURE_DESCRIPTION -> "Descripciones de esculturas";
        };
    }

    private LocalDate normalizeStart(LocalDate startDate) {
        if (startDate != null) {
            return startDate;
        }
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(1);
    }

    private LocalDate normalizeEnd(LocalDate endDate, LocalDate startDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        if (end.isBefore(startDate)) {
            return startDate;
        }
        return end;
    }

    private long toLong(Integer value) {
        return value == null ? 0 : value.longValue();
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text));
        cell.setGrayFill(0.9f);
        table.addCell(cell);
    }

    private PdfPCell value(String text) {
        return new PdfPCell(new Paragraph(text));
    }

    private CurrentUser resolveCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return new CurrentUser(user.getId(), user.getEmail());
            }
            if (principal instanceof UserDetails userDetails) {
                return new CurrentUser(null, userDetails.getUsername());
            }
            String name = authentication.getName();
            if (StringUtils.hasText(name)) {
                return new CurrentUser(null, name);
            }
            return null;
        } catch (Exception ex) {
            log.warn("Could not resolve current user for AI usage log", ex);
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record CurrentUser(Long id, String email) {
    }
}
