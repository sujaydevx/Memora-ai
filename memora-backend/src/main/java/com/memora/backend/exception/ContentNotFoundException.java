package com.memora.backend.exception;

import java.util.UUID;

public class ContentNotFoundException extends RuntimeException {
    public ContentNotFoundException(UUID contentId) {
        super("Content not found with id: " + contentId.toString());
    }
}