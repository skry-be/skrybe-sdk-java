package com.skrybe.sdk;

import com.skrybe.sdk.exception.InvalidRequestException;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.resources.Autoresponders;
import com.skrybe.sdk.resources.Brands;
import com.skrybe.sdk.resources.Campaigns;
import com.skrybe.sdk.resources.Emails;
import com.skrybe.sdk.resources.Lists;
import com.skrybe.sdk.resources.Stats;
import com.skrybe.sdk.resources.Subscribers;
import com.skrybe.sdk.resources.Templates;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * The Skrybe API client. Thread-safe: build one and share it.
 *
 * <pre>{@code
 * Skrybe skrybe = new Skrybe(System.getenv("SKRYBE_API_KEY"));
 * List<SkrybeList> lists = skrybe.lists().list();
 *
 * Skrybe custom = Skrybe.builder(apiKey)
 *         .timeout(Duration.ofSeconds(10))
 *         .maxRetries(0)
 *         .build();
 * }</pre>
 */
public final class Skrybe {
    /** The SDK version, sent in the User-Agent. Kept in step with pom.xml and CHANGELOG.md by a test. */
    public static final String VERSION = "2.0.0";

    /** The production API. */
    public static final String DEFAULT_BASE_URL = "https://dashboard.skry.be";

    /** Per attempt, unless {@link Builder#timeout(Duration)} says otherwise. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /** Extra attempts for reads, unless {@link Builder#maxRetries(int)} says otherwise. */
    public static final int DEFAULT_MAX_RETRIES = 2;

    private final Emails emails;
    private final Campaigns campaigns;
    private final Lists lists;
    private final Subscribers subscribers;
    private final Templates templates;
    private final Autoresponders autoresponders;
    private final Brands brands;
    private final Stats stats;

    /**
     * A client with the default options.
     *
     * @param apiKey a brand's API key, from Settings in the Skrybe dashboard. Keep it server-side.
     * @throws InvalidRequestException {@code api_key_missing} when the key is null or empty
     */
    public Skrybe(String apiKey) {
        this(builder(apiKey));
    }

    private Skrybe(Builder b) {
        if (b.apiKey == null || b.apiKey.isEmpty()) {
            throw new InvalidRequestException(
                    "api_key_missing", "An API key is required.", "Generate one in the Skrybe dashboard under Settings.", null, null, null);
        }
        HttpClient http = b.httpClient != null
                ? b.httpClient
                : HttpClient.newBuilder()
                        .connectTimeout(b.timeout)
                        // A redirect would turn a POST into a GET and drop the body.
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build();
        Transport transport = new Transport(b.apiKey, b.baseUrl, b.timeout, b.maxRetries, http);

        this.emails = new Emails(transport);
        this.campaigns = new Campaigns(transport);
        this.lists = new Lists(transport);
        this.subscribers = new Subscribers(transport);
        this.templates = new Templates(transport);
        this.autoresponders = new Autoresponders(transport);
        this.brands = new Brands(transport);
        this.stats = new Stats(transport);
    }

    /**
     * @param apiKey a brand's API key, from Settings in the Skrybe dashboard. Keep it server-side.
     * @return a builder for a client with non-default options
     */
    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    /** @return transactional and bulk email */
    public Emails emails() {
        return emails;
    }

    /** @return the campaign lifecycle */
    public Campaigns campaigns() {
        return campaigns;
    }

    /** @return lists and custom fields */
    public Lists lists() {
        return lists;
    }

    /** @return subscribers */
    public Subscribers subscribers() {
        return subscribers;
    }

    /** @return templates */
    public Templates templates() {
        return templates;
    }

    /** @return autoresponders */
    public Autoresponders autoresponders() {
        return autoresponders;
    }

    /** @return brands */
    public Brands brands() {
        return brands;
    }

    /** @return install-wide stats */
    public Stats stats() {
        return stats;
    }

    /** Options for a {@link Skrybe} client. */
    public static final class Builder {
        private final String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = DEFAULT_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private HttpClient httpClient;

        private Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * @param baseUrl the install to talk to. Defaults to {@value Skrybe#DEFAULT_BASE_URL}.
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("baseUrl must not be empty");
            }
            URI.create(baseUrl); // Fail here, not on the first request.
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * @param timeout how long one attempt may take. Defaults to 30 seconds.
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            if (timeout == null || timeout.isZero() || timeout.isNegative()) {
                throw new IllegalArgumentException("timeout must be positive");
            }
            this.timeout = timeout;
            return this;
        }

        /**
         * Extra attempts for reads after a dropped connection or a
         * 429/502/503/504. Defaults to 2. Writes are never retried: the API has
         * no idempotency keys, so a retried send could go out twice.
         *
         * @param maxRetries extra attempts; negative counts as 0
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        /**
         * The HttpClient to send with, e.g. one with a proxy or an SSLContext
         * that trusts a corporate CA. The SDK's timeout still applies to each
         * request. Leave redirects off: a redirected POST loses its body.
         *
         * @param httpClient the client
         * @return this builder
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * @return the client
         * @throws InvalidRequestException {@code api_key_missing} when the key is null or empty
         */
        public Skrybe build() {
            return new Skrybe(this);
        }
    }
}
