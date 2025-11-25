package com.muse_ai.logic.ai.description;

import com.muse_ai.rest.ai.dto.SculptureDescriptionRequestDto;
import com.muse_ai.rest.ai.dto.SculptureDescriptionResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SculptureDescriptionService {

    private static final Logger log = LoggerFactory.getLogger(SculptureDescriptionService.class);
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("es", "en", "fr");

    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
    private final AiSculptureDescriptionClient aiSculptureDescriptionClient;

    public SculptureDescriptionService(AiSculptureDescriptionClient aiSculptureDescriptionClient) {
        this.aiSculptureDescriptionClient = aiSculptureDescriptionClient;
    }

    public SculptureDescriptionResponseDto describe(SculptureDescriptionRequestDto request) {
        Objects.requireNonNull(request, "SculptureDescriptionRequestDto is required");
        String language = normalizeLanguage(request.language());
        String name = normalizeName(request.name());
        List<String> labels = normalizeLabels(request.labels());

        validateLanguage(language);
        if (name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (labels.isEmpty()) {
            throw new IllegalArgumentException("labels are required");
        }

        String cacheKey = cacheKey(language, name, labels);
        AtomicBoolean servedFromCache = new AtomicBoolean(true);
        String description = cache.computeIfAbsent(cacheKey, key -> {
            servedFromCache.set(false);
            return aiSculptureDescriptionClient.describeSculpture(name, labels, language);
        });
        if (servedFromCache.get()) {
            log.debug("Returning cached sculpture description for {}", cacheKey);
        }
        return new SculptureDescriptionResponseDto(language, name, labels, description, servedFromCache.get());
    }

    private String cacheKey(String language, String name, List<String> labels) {
        return language + "|" + hash(name + "|" + String.join(",", labels));
    }

    private String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash description input", e);
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            throw new IllegalArgumentException("language cannot be blank");
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private List<String> normalizeLabels(List<String> labels) {
        if (labels == null) {
            return List.of();
        }
        return labels.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .limit(15)
                .toList();
    }

    private void validateLanguage(String language) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language + ". Allowed: es, en, fr");
        }
    }
}
