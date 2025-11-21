package com.muse_ai.rest.sculpture.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class SceneJsonSizeValidator implements ConstraintValidator<ValidSceneJsonSize, String> {

    private static final double BYTES_PER_MEGABYTE = 1024d * 1024d;

    private final int maxBytes;

    public SceneJsonSizeValidator(@Value("${app.sculpture.max-scene-bytes:5242880}") int maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        int providedBytes = value.getBytes(StandardCharsets.UTF_8).length;
        if (providedBytes <= maxBytes) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(buildViolationMessage(providedBytes))
                .addConstraintViolation();
        return false;
    }

    private String buildViolationMessage(int providedBytes) {
        double limitMb = maxBytes / BYTES_PER_MEGABYTE;
        double providedMb = providedBytes / BYTES_PER_MEGABYTE;
        return String.format(Locale.US,
                "sceneJson exceeds %.2f MB limit (%.2f MB provided)",
                limitMb,
                providedMb
        );
    }
}
