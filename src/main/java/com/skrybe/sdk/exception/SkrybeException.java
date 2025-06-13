package com.skrybe.sdk.exception;

public class SkrybeException extends RuntimeException {
    public SkrybeException(String message) {
        super(message);
    }

    public SkrybeException(String message, Throwable cause) {
        super(message, cause);
    }
} 