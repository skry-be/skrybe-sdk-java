package com.skrybe.sdk.exception;

/** The API could not be reached ({@code network_error}) or did not answer in time ({@code timeout}). */
public class ConnectionException extends SkrybeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param errorCode stable machine-readable slug
     * @param message human-readable message
     * @param hint what to do about it, or null
     * @param status HTTP status of the response, or null
     * @param raw the raw response body, or null
     * @param cause the underlying failure, or null
     */
    public ConnectionException(String errorCode, String message, String hint, Integer status, String raw, Throwable cause) {
        super(errorCode, message, hint, status, raw, cause);
    }

    /**
     * @param errorCode stable machine-readable slug
     * @param message human-readable message
     */
    public ConnectionException(String errorCode, String message) {
        this(errorCode, message, null, null, null, null);
    }

    @Override
    public String getType() {
        return "connection";
    }
}
