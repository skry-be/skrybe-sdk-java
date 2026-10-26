package com.skrybe.sdk.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.Skrybe;
import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.exception.ConnectionException;
import com.skrybe.sdk.exception.SkrybeException;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends requests to the Skrybe API and turns failures into exceptions.
 *
 * <p>Each attempt is logged at DEBUG to the {@code com.skrybe.sdk} System.Logger:
 * method, path, status and timing, or the connection failure. Request bodies are
 * never logged: they carry the API key.
 *
 * <p>Internal: not part of the SDK's public API.
 */
public final class Transport {
    private static final Logger LOG = System.getLogger("com.skrybe.sdk");
    private static final Set<Integer> RETRYABLE_STATUS = Set.of(429, 502, 503, 504);

    /** How a POST body is encoded. */
    public enum Encoding {
        /** application/x-www-form-urlencoded, which every endpoint wants but one. */
        FORM,
        /** A raw JSON body: only api/emails/send-transactional.php, which reads php://input. */
        JSON
    }

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final HttpClient http;

    public Transport(String apiKey, String baseUrl, Duration timeout, int maxRetries, HttpClient http) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.timeout = timeout;
        this.maxRetries = Math.max(0, maxRetries);
        this.http = http;
    }

    /**
     * Perform a form POST and return the body as text.
     *
     * <p>Legacy endpoints answer HTTP 200 with a prose error, so a 2xx here is
     * not proof of success: recognised prose errors are thrown, and anything
     * else is handed back for the caller to parse.
     *
     * @param retryable opt in to retrying. Off by default because most
     *     endpoints create or send something and none take an idempotency key:
     *     a 504 from a send that actually landed would send twice on retry. A
     *     dropped connection cannot be told apart from a response never seen, so
     *     only reads set this.
     */
    public String text(String path, Map<String, ?> body, boolean retryable) {
        HttpResponse<String> response = send("POST", path, body, Encoding.FORM, false, retryable);
        String text = bodyOf(response);
        if (!ok(response)) {
            throw errorFromResponse(response.statusCode(), text);
        }
        SkrybeException prose = Errors.classifyProse(text, response.statusCode());
        if (prose != null) {
            throw prose;
        }
        return text;
    }

    /** Perform a request against one of the endpoints that returns real JSON. */
    public JsonNode json(String path, Map<String, ?> body, String method, Encoding encoding, boolean anonymous, boolean retryable) {
        HttpResponse<String> response = send(method, path, body, encoding, anonymous, retryable);
        String text = bodyOf(response);
        if (!ok(response)) {
            throw errorFromResponse(response.statusCode(), text);
        }
        JsonNode parsed = Json.tryParse(text);
        if (parsed == null) {
            throw new ApiException(
                    "malformed_response", "The API returned a response that was not valid JSON.", null, response.statusCode(), text, null);
        }
        return parsed;
    }

    private HttpResponse<String> send(
            String method, String path, Map<String, ?> body, Encoding encoding, boolean anonymous, boolean retryable) {
        String target = baseUrl + "/" + path.replaceAll("^/+", "");

        Map<String, String> fields = new LinkedHashMap<>();
        if (body != null) {
            for (Map.Entry<String, ?> field : body.entrySet()) {
                if (field.getValue() != null) {
                    fields.put(field.getKey(), String.valueOf(field.getValue()));
                }
            }
        }
        if (!anonymous) {
            fields.put("api_key", apiKey);
        }

        String url = target;
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();
        String contentType = null;
        if ("GET".equals(method)) {
            String query = formEncode(fields);
            if (!query.isEmpty()) {
                url = target + "?" + query;
            }
        } else if (encoding == Encoding.JSON) {
            contentType = "application/json";
            publisher = HttpRequest.BodyPublishers.ofString(Json.write(fields), StandardCharsets.UTF_8);
        } else {
            contentType = "application/x-www-form-urlencoded";
            publisher = HttpRequest.BodyPublishers.ofString(formEncode(fields), StandardCharsets.UTF_8);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("User-Agent", "skrybe-sdk-java/" + Skrybe.VERSION)
                .method(method, publisher);
        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }
        HttpRequest request = builder.build();

        int attempts = retryable ? maxRetries + 1 : 1;
        IOException lastError = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            long started = System.nanoTime();
            HttpResponse<String> response;
            try {
                response = http.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (HttpTimeoutException e) {
                warn(method, path, e);
                throw new ConnectionException(
                        "timeout", "Request to " + target + " timed out after " + timeout.toMillis() + "ms.", null, null, null, e);
            } catch (IOException e) {
                // Logged without the body: it carries the API key.
                warn(method, path, e);
                lastError = e;
                if (attempt < attempts) {
                    sleep(backoffMs(attempt, null), target);
                    continue;
                }
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ConnectionException("network_error", "Interrupted while waiting for " + target + ".", null, null, null, e);
            }

            int status = response.statusCode();
            long ms = (System.nanoTime() - started) / 1_000_000;
            LOG.log(Level.DEBUG, () -> "Skrybe API request " + method + " " + path + " -> " + status + " in " + ms + "ms");
            if (RETRYABLE_STATUS.contains(status) && attempt < attempts) {
                sleep(backoffMs(attempt, response.headers().firstValue("Retry-After").orElse(null)), target);
                continue;
            }
            return response;
        }

        String reason = lastError == null ? "unknown error"
                : lastError.getMessage() != null ? lastError.getMessage() : lastError.getClass().getSimpleName();
        throw new ConnectionException("network_error", "Could not reach " + baseUrl + ": " + reason, null, null, null, lastError);
    }

    /**
     * Build an exception from a non-2xx response. The JSON endpoints use
     * {@code {ok:false,error:...}} (api/emails/send.php) or
     * {@code {status:"error",message:...}} (api/emails/send-transactional.php).
     */
    private static SkrybeException errorFromResponse(int status, String text) {
        String message = Text.trim(text);
        JsonNode parsed = Json.tryParse(text);
        if (parsed != null && parsed.isObject()) {
            JsonNode extracted = parsed.get("error");
            if (extracted == null || extracted.isNull()) {
                extracted = parsed.get("message");
            }
            if (extracted != null && extracted.isTextual() && !extracted.asText().isEmpty()) {
                message = extracted.asText();
            }
        }
        if (message.isEmpty()) {
            message = "HTTP " + status + " from the Skrybe API.";
        }
        return Errors.create(Errors.typeForStatus(status), Errors.codeForStatus(status), message, null, status, text, null);
    }

    /** Honour Retry-After when present, else exponential backoff with jitter. */
    static long backoffMs(int attempt, String retryAfter) {
        if (retryAfter != null) {
            String value = retryAfter.trim();
            if (value.matches("\\d{1,9}")) {
                return Math.min(Long.parseLong(value) * 1000, 30_000);
            }
        }
        return (1L << (attempt - 1)) * 500 + ThreadLocalRandom.current().nextInt(250);
    }

    private static void sleep(long ms, String target) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException("network_error", "Interrupted while waiting to retry " + target + ".", null, null, null, e);
        }
    }

    private static void warn(String method, String path, Exception e) {
        LOG.log(Level.DEBUG, () -> "Skrybe API request " + method + " " + path + " failed: " + e);
    }

    private static boolean ok(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private static String bodyOf(HttpResponse<String> response) {
        return response.body() == null ? "" : response.body();
    }

    /** As URLSearchParams serialises: space as +, and only {@code *-._} left unescaped besides letters and digits. */
    private static String formEncode(Map<String, String> fields) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> field : fields.entrySet()) {
            if (out.length() > 0) {
                out.append('&');
            }
            out.append(URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8));
        }
        return out.toString();
    }
}
