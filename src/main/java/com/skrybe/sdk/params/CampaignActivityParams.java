package com.skrybe.sdk.params;

/**
 * Parameters for {@code campaigns().activity()}.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class CampaignActivityParams {
    private final ActivityType type;
    private final Integer page;
    private final Integer limit;

    private CampaignActivityParams(Builder b) {
        this.type = b.type;
        this.page = b.page;
        this.limit = b.limit;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the kind of activity, or null when not set */
    public ActivityType getType() {
        return type;
    }

    /** @return the page, 1-based, or null when not set */
    public Integer getPage() {
        return page;
    }

    /** @return rows per page, at most 1000, or null when not set */
    public Integer getLimit() {
        return limit;
    }

    /** Builds a {@link CampaignActivityParams}. */
    public static final class Builder {
        private ActivityType type;
        private Integer page;
        private Integer limit;

        private Builder() {
        }

        /**
         * The kind of activity. The server defaults to opens.
         *
         * @return this builder
         */
        public Builder type(ActivityType type) {
            this.type = type;
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
        public CampaignActivityParams build() {
            return new CampaignActivityParams(this);
        }
    }
}
