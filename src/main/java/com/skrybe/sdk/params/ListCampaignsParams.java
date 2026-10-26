package com.skrybe.sdk.params;

/**
 * Parameters for {@code campaigns().list()}.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class ListCampaignsParams {
    private final Integer page;
    private final Integer limit;
    private final CampaignStatus status;

    private ListCampaignsParams(Builder b) {
        this.page = b.page;
        this.limit = b.limit;
        this.status = b.status;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the page, 1-based, or null when not set */
    public Integer getPage() {
        return page;
    }

    /** @return rows per page, at most 100, or null when not set */
    public Integer getLimit() {
        return limit;
    }

    /** @return only campaigns with this status, or null when not set */
    public CampaignStatus getStatus() {
        return status;
    }

    /** Builds a {@link ListCampaignsParams}. */
    public static final class Builder {
        private Integer page;
        private Integer limit;
        private CampaignStatus status;

        private Builder() {
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
         * Rows per page, at most 100.
         *
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Only campaigns with this status.
         *
         * @return this builder
         */
        public Builder status(CampaignStatus status) {
            this.status = status;
            return this;
        }

        /** @return the parameters */
        public ListCampaignsParams build() {
            return new ListCampaignsParams(this);
        }
    }
}
