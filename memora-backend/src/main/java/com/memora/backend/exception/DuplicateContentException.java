package com.memora.backend.exception;

public class DuplicateContentException extends RuntimeException {
    public DuplicateContentException(String message) {
        super(message);
    }
}