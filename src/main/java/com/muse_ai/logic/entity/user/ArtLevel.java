package com.muse_ai.logic.entity.user;

public enum ArtLevel {
    BEGINNER, INTERMEDIATE, ADVANCED;

    public static ArtLevel fromString(String v) {
        return switch (v.toLowerCase()) {
            case "beginner" -> BEGINNER;
            case "intermediate" -> INTERMEDIATE;
            case "advanced" -> ADVANCED;
            default -> throw new IllegalArgumentException("artLevel inválido: " + v);
        };
    }
}
