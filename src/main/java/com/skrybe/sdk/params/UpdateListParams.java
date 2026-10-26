package com.skrybe.sdk.params;

/**
 * Fields {@code lists().update()} can change. Only those set change.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class UpdateListParams {
    private final String name;
    private final OptIn optIn;

    private UpdateListParams(Builder b) {
        this.name = b.name;
        this.optIn = b.optIn;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the list's name, at most 100 characters, or null when not set */
    public String getName() {
        return name;
    }

    /** @return single or double opt-in, or null when not set */
    public OptIn getOptIn() {
        return optIn;
    }

    /** Builds a {@link UpdateListParams}. */
    public static final class Builder {
        private String name;
        private OptIn optIn;

        private Builder() {
        }

        /**
         * The list's name, at most 100 characters.
         *
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Single or double opt-in.
         *
         * @return this builder
         */
        public Builder optIn(OptIn optIn) {
            this.optIn = optIn;
            return this;
        }

        /** @return the parameters */
        public UpdateListParams build() {
            return new UpdateListParams(this);
        }
    }
}
