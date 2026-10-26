package com.skrybe.sdk.exception;

/** A quota or rate limit was hit. */
public class RateLimitException extends SkrybeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param errorCode stable machine-readable slug
     * @param message human-readable message
     * @param hint what to do about it, or null
     * @param status HTTP status of the response, or null
     * @param raw the raw response body, or null
     * @param cause the underlying failure, or null
     */
    public RateLimitException(String errorCode, String message, String hint, Integer status, String raw, Throwable cause) {
        super(errorCode, message, hint, status, raw, cause);
    }

    /**
     * @param errorCode stable machine-readable slug
     * @param message human-readable message
     */
    public RateLimitException(String errorCode, String message) {
        this(errorCode, message, null, null, null, null);
    }

    @Override
    public String getType() {
        return "rate_limit";
    }
}
