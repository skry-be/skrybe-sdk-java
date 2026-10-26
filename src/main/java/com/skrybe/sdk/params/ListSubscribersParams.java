package com.skrybe.sdk.params;

/**
 * Parameters for {@code subscribers().list()}.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class ListSubscribersParams {
    private final SubscriberStatus status;
    private final Integer page;
    private final Integer limit;

    private ListSubscribersParams(Builder b) {
        this.status = b.status;
        this.page = b.page;
        this.limit = b.limit;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return only subscribers with this status, or null when not set */
    public SubscriberStatus getStatus() {
        return status;
    }

    /** @return the page, 1-based, or null when not set */
    public Integer getPage() {
        return page;
    }

    /** @return rows per page, at most 1000, or null when not set */
    public Integer getLimit() {
        return limit;
    }

    /** Builds a {@link ListSubscribersParams}. */
    public static final class Builder {
        private SubscriberStatus status;
        private Integer page;
        private Integer limit;

        private Builder() {
        }

        /**
         * Only subscribers with this status.
         *
         * @return this builder
         */
        public Builder status(SubscriberStatus status) {
            this.status = status;
            return this;
        }

        /**
         * The page, 1-based.
         *
         * @return this builder
         */
        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        /**
         * Rows per page, at most 1000.
         *
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /** @return the parameters */
        public ListSubscribersParams build() {
            return new ListSubscribersParams(this);
        }
    }
}
