package com.muse_ai.rest.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SceneJsonSizeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSceneJsonSize {
    String message() default "sceneJson exceeds maximum size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
