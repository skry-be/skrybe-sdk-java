package com.skrybe.sdk.exception;

public class ValidationException extends SkrybeException {

    private final Object errors;

    public ValidationException(Object errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public ValidationException(String message, Object errors) {
        super(message);
        this.errors = errors;
    }

    public Object getErrors() {
        return errors;
    }
} 