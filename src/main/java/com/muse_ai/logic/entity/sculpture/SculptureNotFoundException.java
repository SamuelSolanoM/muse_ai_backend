package com.muse_ai.logic.entity.sculpture;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SculptureNotFoundException extends RuntimeException {
    public SculptureNotFoundException(String message) {
        super(message);
    }
}
