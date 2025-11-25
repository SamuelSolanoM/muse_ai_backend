package com.muse_ai.logic.ai.description;

import com.muse_ai.rest.ai.dto.ImageDescriptionRequestDto;
import com.muse_ai.rest.ai.dto.ImageDescriptionResponseDto;
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

@Service
public class ImageDescriptionService {

    private static final Logger log = LoggerFactory.getLogger(ImageDescriptionService.class);
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("es", "en", "fr");

    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
    private final AiImageDescriptionClient aiImageDescriptionClient;

    public ImageDescriptionService(AiImageDescriptionClient aiImageDescriptionClient) {
        this.aiImageDescriptionClient = aiImageDescriptionClient;
    }

    public ImageDescriptionResponseDto describe(ImageDescriptionRequestDto request) {
        Objects.requireNonNull(request, "ImageDescriptionRequestDto is required");
        String imageBase64 = normalizeImage(request.imageBase64());
        String language = normalizeLanguage(request.language());

        validateLanguage(language);
        if (imageBase64.isBlank()) {
            throw new IllegalArgumentException("imageBase64 is required");
        }

        String cacheKey = cacheKey(language, imageBase64);
        AtomicBoolean servedFromCache = new AtomicBoolean(true);
        String description = cache.computeIfAbsent(cacheKey, key -> {
            servedFromCache.set(false);
            return aiImageDescriptionClient.describeImage(imageBase64, language);
        });
        if (servedFromCache.get()) {
            log.debug("Returning cached image description for {}", cacheKey);
        }
        return new ImageDescriptionResponseDto(language, description, servedFromCache.get());
    }

    private String cacheKey(String language, String imageBase64) {
        return language + "|" + hash(imageBase64);
    }

    private String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash image", e);
        }
    }

    private String normalizeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            throw new IllegalArgumentException("language cannot be blank");
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeImage(String imageBase64) {
        return imageBase64 == null ? "" : imageBase64.trim();
    }

    private void validateLanguage(String language) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language + ". Allowed: es, en, fr");
        }
    }
}
