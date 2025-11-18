package com.muse_ai.logic.entity.painting;

public class PaintingNotFoundException extends RuntimeException {
    public PaintingNotFoundException(String message) {
        super(message);
    }
}
