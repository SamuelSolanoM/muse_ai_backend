package com.muse_ai.logic.ai.translation;

import com.muse_ai.logic.ai.AiResponse;
import com.muse_ai.logic.ai.usage.AiModule;
import com.muse_ai.logic.ai.usage.AiUsageTrackingService;
import com.muse_ai.rest.ai.dto.TranslationRequestDto;
import com.muse_ai.rest.ai.dto.TranslationResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DescriptionTranslationService {

    private static final Logger log = LoggerFactory.getLogger(DescriptionTranslationService.class);
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("es", "en", "fr");

    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
    private final AiTranslationClient aiTranslationClient;
    private final AiUsageTrackingService aiUsageTrackingService;

    public DescriptionTranslationService(
            AiTranslationClient aiTranslationClient,
            AiUsageTrackingService aiUsageTrackingService
    ) {
        this.aiTranslationClient = aiTranslationClient;
        this.aiUsageTrackingService = aiUsageTrackingService;
    }

    public TranslationResponseDto translate(TranslationRequestDto request) {
        Objects.requireNonNull(request, "TranslationRequestDto is required");
        String artworkId = normalizeId(request.artworkId());
        String targetLanguage = normalizeLanguage(request.targetLanguage());
        String sourceLanguage = normalizeLanguage(request.sourceLanguage());
        String originalText = Objects.requireNonNull(request.originalText(), "originalText is required");

        validateTargetLanguage(targetLanguage);
        validateSourceLanguage(sourceLanguage);
        if (artworkId.isBlank()) {
            throw new IllegalArgumentException("artworkId is required");
        }

        if (targetLanguage.equals(sourceLanguage)) {
            return new TranslationResponseDto(artworkId, targetLanguage, originalText, false);
        }

        String cacheKey = cacheKey(artworkId, targetLanguage, originalText);
        AtomicBoolean servedFromCache = new AtomicBoolean(true);
        AtomicReference<AiResponse> aiResponse = new AtomicReference<>();
        String translation = cache.computeIfAbsent(cacheKey, key -> {
            servedFromCache.set(false);
            AiResponse response = aiTranslationClient.translateDescription(originalText, sourceLanguage, targetLanguage);
            aiResponse.set(response);
            return response.content();
        });
        if (servedFromCache.get()) {
            log.debug("Returning cached translation for {}", cacheKey);
        } else if (aiResponse.get() != null) {
            aiUsageTrackingService.recordUsage(AiModule.TRANSLATION, aiResponse.get().model(), aiResponse.get().usage());
        }
        return new TranslationResponseDto(artworkId, targetLanguage, translation, servedFromCache.get());
    }

    private String cacheKey(String artworkId, String targetLanguage, String originalText) {
        return artworkId + "|" + targetLanguage + "|" + hash(originalText);
    }

    private String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash text", e);
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            throw new IllegalArgumentException("Language cannot be blank");
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeId(String artworkId) {
        return artworkId == null ? "" : artworkId.trim();
    }

    private void validateTargetLanguage(String language) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language + ". Allowed: es, en, fr");
        }
    }

    private void validateSourceLanguage(String language) {
        if ("auto".equalsIgnoreCase(language)) {
            return;
        }
        validateTargetLanguage(language);
    }
}
