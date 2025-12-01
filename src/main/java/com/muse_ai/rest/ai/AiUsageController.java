package com.muse_ai.rest.ai;

import com.muse_ai.logic.ai.usage.AiUsageReport;
import com.muse_ai.logic.ai.usage.AiUsageTrackingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai/usage")
@CrossOrigin(origins = {"${app.frontend.origin:http://localhost:4200}"})
public class AiUsageController {

    private final AiUsageTrackingService aiUsageTrackingService;

    public AiUsageController(AiUsageTrackingService aiUsageTrackingService) {
        this.aiUsageTrackingService = aiUsageTrackingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public AiUsageReport report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return aiUsageTrackingService.buildReport(startDate, endDate);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AiUsageReport report = aiUsageTrackingService.buildReport(startDate, endDate);

        String normalizedFormat = format == null ? "csv" : format.toLowerCase();
        byte[] payload;
        MediaType mediaType;
        String extension;

        switch (normalizedFormat) {
            case "pdf" -> {
                payload = aiUsageTrackingService.exportPdf(report);
                mediaType = MediaType.APPLICATION_PDF;
                extension = "pdf";
            }
            case "csv" -> {
                payload = aiUsageTrackingService.exportCsv(report);
                mediaType = MediaType.parseMediaType("text/csv");
                extension = "csv";
            }
            default -> throw new IllegalArgumentException("Formato no soportado: " + format + ". Use csv o pdf.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData(
                "attachment",
                "ai-usage-" + report.startDate() + "-" + report.endDate() + "." + extension
        );
        return new ResponseEntity<>(payload, headers, HttpStatus.OK);
    }
}
