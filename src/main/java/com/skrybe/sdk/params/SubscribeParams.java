package com.skrybe.sdk.params;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code subscribers().subscribe()}. Required: listId, email.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class SubscribeParams {
    private final String listId;
    private final String email;
    private final String name;
    private final String country;
    private final String ipAddress;
    private final String referrer;
    private final boolean gdpr;
    private final boolean silent;
    private final Map<String, String> fields;

    private SubscribeParams(Builder b) {
        this.listId = b.listId;
        this.email = b.email;
        this.name = b.name;
        this.country = b.country;
        this.ipAddress = b.ipAddress;
        this.referrer = b.referrer;
        this.gdpr = b.gdpr;
        this.silent = b.silent;
        this.fields = b.fields == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(b.fields));
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the encrypted list id, as {@code lists().list()} returns it, or null when not set */
    public String getListId() {
        return listId;
    }

    /** @return the address, or null when not set */
    public String getEmail() {
        return email;
    }

    /** @return the subscriber's name, or null when not set */
    public String getName() {
        return name;
    }

    /** @return two-letter ISO 3166-1 code, e.g. NG, or null when not set */
    public String getCountry() {
        return country;
    }

    /** @return the subscriber's IP address, or null when not set */
    public String getIpAddress() {
        return ipAddress;
    }

    /** @return the URL they signed up from, or null when not set */
    public String getReferrer() {
        return referrer;
    }

    /** @return record GDPR consent for an EU signup, or false when not set */
    public boolean isGdpr() {
        return gdpr;
    }

    /** @return add to a double opt-in list as single opt-in, skipping confirmation, or false when not set */
    public boolean isSilent() {
        return silent;
    }

    /** @return custom fields, keyed by the name the list shows, e.g. {@code Birthday} for a field whose tag is {@code [Birthday,fallback=]}, or null when not set */
    public Map<String, String> getFields() {
        return fields;
    }

    /** Builds a {@link SubscribeParams}. */
    public static final class Builder {
        private String listId;
        private String email;
        private String name;
        private String country;
        private String ipAddress;
        private String referrer;
        private boolean gdpr;
        private boolean silent;
        private Map<String, String> fields;

        private Builder() {
        }

        /**
         * The encrypted list id, as {@code lists().list()} returns it. Required.
         *
         * @return this builder
         */
        public Builder listId(String listId) {
            this.listId = listId;
            return this;
        }

        /**
         * The address. Required.
         *
         * @return this builder
         */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /**
         * The subscriber's name.
         *
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Two-letter ISO 3166-1 code, e.g. NG.
         *
         * @return this builder
         */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /**
         * The subscriber's IP address.
         *
         * @return this builder
         */
        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /**
         * The URL they signed up from.
         *
         * @return this builder
         */
        public Builder referrer(String referrer) {
            this.referrer = referrer;
            return this;
        }

        /**
         * Record GDPR consent for an EU signup.
         *
         * @return this builder
         */
        public Builder gdpr(boolean gdpr) {
            this.gdpr = gdpr;
            return this;
        }

        /**
         * Add to a double opt-in list as single opt-in, skipping confirmation.
         *
         * @return this builder
         */
        public Builder silent(boolean silent) {
            this.silent = silent;
            return this;
        }

        /**
         * Custom fields, keyed by the name the list shows, e.g. {@code Birthday} for a field whose tag is {@code [Birthday,fallback=]}. Spaces are taken out of the names before sending, as the server matches on.
         *
         * @return this builder
         */
        public Builder fields(Map<String, String> fields) {
            this.fields = fields == null ? null : new LinkedHashMap<>(fields);
            return this;
        }

        /**
         * Set one custom field, by the name the list shows.
         *
         * @return this builder
         */
        public Builder field(String name, String value) {
            if (this.fields == null) {
                this.fields = new LinkedHashMap<>();
            }
            this.fields.put(name, value);
            return this;
        }

        /** @return the parameters */
        public SubscribeParams build() {
            return new SubscribeParams(this);
        }
    }
}
