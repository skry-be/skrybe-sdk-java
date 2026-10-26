package com.skrybe.sdk.exception;

/**
 * Every failure the SDK reports. The Skrybe API answers most failures with
 * HTTP 200 and a prose sentence; the SDK turns them into something code can
 * branch on: a stable {@link #getErrorCode()} slug, and a subclass saying what
 * kind of failure it is.
 *
 * <ul>
 *   <li>{@link AuthenticationException}: the API key is missing or wrong.</li>
 *   <li>{@link InvalidRequestException}: the request itself is wrong; retrying it unchanged will fail again.</li>
 *   <li>{@link RateLimitException}: a quota or rate limit was hit.</li>
 *   <li>{@link ConnectionException}: the API could not be reached, or did not answer in time.</li>
 *   <li>{@link ApiException}: anything else the API refused or got wrong.</li>
 * </ul>
 *
 * Unchecked, like the rest of the SDK's failures.
 */
public abstract class SkrybeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String hint;
    private final Integer status;
    private final String raw;

    SkrybeException(String errorCode, String message, String hint, Integer status, String raw, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.hint = hint;
        this.status = status;
        this.raw = raw;
    }

    /** @return a stable machine-readable slug, e.g. {@code list_not_found} */
    public String getErrorCode() {
        return errorCode;
    }

    /** @return {@code authentication}, {@code invalid_request}, {@code rate_limit}, {@code connection} or {@code api} */
    public abstract String getType();

    /** @return what to do about it, when known; otherwise null */
    public String getHint() {
        return hint;
    }

    /** @return the HTTP status of the response, when there was one; otherwise null */
    public Integer getStatus() {
        return status;
    }

    /** @return the raw response body, kept for debugging odd endpoints; null when there was none */
    public String getRaw() {
        return raw;
    }
}
