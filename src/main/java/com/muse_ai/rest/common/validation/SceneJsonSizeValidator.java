package com.muse_ai.rest.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SceneJsonSizeValidator implements ConstraintValidator<ValidSceneJsonSize, String> {

    @Value("${app.painting.max-scene-bytes:5242880}") // 5 MB por defecto
    private int maxBytes;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false; // @NotBlank ya cubre vacío; aquí evitamos NPE
        int bytes = value.getBytes(StandardCharsets.UTF_8).length;

        if (bytes > maxBytes) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "sceneJson exceeds " + (maxBytes / 1024 / 1024) + " MB limit"
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
