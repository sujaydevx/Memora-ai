package com.memora.backend.exception;

public class MemoraServiceException extends RuntimeException {
    public MemoraServiceException(String message) {
        super(message);
    }

    public MemoraServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}