package com.skrybe.sdk.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Who {@code campaigns().send()} sends to. At least one list or segment is required.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class Recipients {
    private final List<String> listIds;
    private final List<Object> segmentIds;
    private final List<String> excludeListIds;
    private final List<Object> excludeSegmentIds;
    private final boolean dryRun;

    private Recipients(Builder b) {
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

    /** Builds a {@link Recipients}. */
    public static final class Builder {
        private List<String> listIds;
        private List<Object> segmentIds;
        private List<String> excludeListIds;
        private List<Object> excludeSegmentIds;
        private boolean dryRun;

        private Builder() {
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
        public Recipients build() {
            return new Recipients(this);
        }
    }
}
