package com.skrybe.sdk.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Parameters for {@code campaigns().schedule()}: when, and to whom. Required: at, and at least one list or segment.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class ScheduleCampaignParams {
    private final String at;
    private final String timezone;
    private final List<String> listIds;
    private final List<Object> segmentIds;
    private final List<String> excludeListIds;
    private final List<Object> excludeSegmentIds;
    private final boolean dryRun;

    private ScheduleCampaignParams(Builder b) {
        this.at = b.at;
        this.timezone = b.timezone;
        this.listIds = b.listIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.listIds));
        this.segmentIds = b.segmentIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.segmentIds));
        this.excludeListIds = b.excludeListIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.excludeListIds));
        this.excludeSegmentIds = b.excludeSegmentIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.excludeSegmentIds));
        this.dryRun = b.dryRun;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return anything PHP's strtotime() reads, e.g. "2027-06-15 18:05" or "June 15, 2027 6:05pm", or null when not set */
    public String getAt() {
        return at;
    }

    /** @return the timezone, e.g. "Africa/Lagos", or null when not set */
    public String getTimezone() {
        return timezone;
    }

    /** @return encrypted list ids, or null when not set */
    public List<String> getListIds() {
        return listIds;
    }

    /** @return segment ids, or null when not set */
    public List<Object> getSegmentIds() {
        return segmentIds;
    }

    /** @return encrypted ids of lists whose subscribers are left out, or null when not set */
    public List<String> getExcludeListIds() {
        return excludeListIds;
    }

    /** @return ids of segments whose subscribers are left out, or null when not set */
    public List<Object> getExcludeSegmentIds() {
        return excludeSegmentIds;
    }

    /** @return run every check and count the recipients, but send nothing, or false when not set */
    public boolean isDryRun() {
        return dryRun;
    }

    /** Builds a {@link ScheduleCampaignParams}. */
    public static final class Builder {
        private String at;
        private String timezone;
        private List<String> listIds;
        private List<Object> segmentIds;
        private List<String> excludeListIds;
        private List<Object> excludeSegmentIds;
        private boolean dryRun;

        private Builder() {
        }

        /**
         * Anything PHP's strtotime() reads, e.g. "2027-06-15 18:05" or "June 15, 2027 6:05pm". Required.
         *
         * @return this builder
         */
        public Builder at(String at) {
            this.at = at;
            return this;
        }

        /**
         * The timezone, e.g. "Africa/Lagos". Defaults to the account's timezone.
         *
         * @return this builder
         */
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * Encrypted list ids. At least one list or segment is required.
         *
         * @return this builder
         */
        public Builder listIds(Collection<String> listIds) {
            this.listIds = listIds == null ? null : new ArrayList<>(listIds);
            return this;
        }

        /**
         * Encrypted list ids. At least one list or segment is required.
         *
         * @return this builder
         */
        public Builder listIds(String... listIds) {
            return listIds(Arrays.asList(listIds));
        }

        /**
         * Segment ids.
         *
         * @return this builder
         */
        public Builder segmentIds(Collection<?> segmentIds) {
            this.segmentIds = segmentIds == null ? null : new ArrayList<Object>(segmentIds);
            return this;
        }

        /**
         * Segment ids.
         *
         * @return this builder
         */
        public Builder segmentIds(Object... segmentIds) {
            return segmentIds(Arrays.asList(segmentIds));
        }

        /**
         * Encrypted ids of lists whose subscribers are left out.
         *
         * @return this builder
         */
        public Builder excludeListIds(Collection<String> excludeListIds) {
            this.excludeListIds = excludeListIds == null ? null : new ArrayList<>(excludeListIds);
            return this;
        }

        /**
         * Encrypted ids of lists whose subscribers are left out.
         *
         * @return this builder
         */
        public Builder excludeListIds(String... excludeListIds) {
            return excludeListIds(Arrays.asList(excludeListIds));
        }

        /**
         * Ids of segments whose subscribers are left out.
         *
         * @return this builder
         */
        public Builder excludeSegmentIds(Collection<?> excludeSegmentIds) {
            this.excludeSegmentIds = excludeSegmentIds == null ? null : new ArrayList<Object>(excludeSegmentIds);
            return this;
        }

        /**
         * Ids of segments whose subscribers are left out.
         *
         * @return this builder
         */
        public Builder excludeSegmentIds(Object... excludeSegmentIds) {
            return excludeSegmentIds(Arrays.asList(excludeSegmentIds));
        }

        /**
         * Run every check and count the recipients, but send nothing.
         *
         * @return this builder
         */
        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        /** @return the parameters */
        public ScheduleCampaignParams build() {
            return new ScheduleCampaignParams(this);
        }
    }
}
