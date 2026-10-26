package com.skrybe.sdk.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Parameters for {@code campaigns().create()}. Required: fromName, fromEmail, replyTo, title, subject, htmlText.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class CreateCampaignParams {
    private final String fromName;
    private final String fromEmail;
    private final String replyTo;
    private final String title;
    private final String subject;
    private final String htmlText;
    private final String plainText;
    private final List<String> listIds;
    private final List<Object> segmentIds;
    private final List<String> excludeListIds;
    private final List<Object> excludeSegmentIds;
    private final String queryString;
    private final Tracking trackOpens;
    private final Tracking trackClicks;
    private final boolean send;
    private final String scheduleDateTime;
    private final String scheduleTimezone;

    private CreateCampaignParams(Builder b) {
        this.fromName = b.fromName;
        this.fromEmail = b.fromEmail;
        this.replyTo = b.replyTo;
        this.title = b.title;
        this.subject = b.subject;
        this.htmlText = b.htmlText;
        this.plainText = b.plainText;
        this.listIds = b.listIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.listIds));
        this.segmentIds = b.segmentIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.segmentIds));
        this.excludeListIds = b.excludeListIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.excludeListIds));
        this.excludeSegmentIds = b.excludeSegmentIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.excludeSegmentIds));
        this.queryString = b.queryString;
        this.trackOpens = b.trackOpens;
        this.trackClicks = b.trackClicks;
        this.send = b.send;
        this.scheduleDateTime = b.scheduleDateTime;
        this.scheduleTimezone = b.scheduleTimezone;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the sender's name, or null when not set */
    public String getFromName() {
        return fromName;
    }

    /** @return the sender's address, or null when not set */
    public String getFromEmail() {
        return fromEmail;
    }

    /** @return the Reply-To address, or null when not set */
    public String getReplyTo() {
        return replyTo;
    }

    /** @return the campaign's title in the dashboard, or null when not set */
    public String getTitle() {
        return title;
    }

    /** @return the subject, or null when not set */
    public String getSubject() {
        return subject;
    }

    /** @return the HTML body, or null when not set */
    public String getHtmlText() {
        return htmlText;
    }

    /** @return the plain-text body, or null when not set */
    public String getPlainText() {
        return plainText;
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

    /** @return a query string appended to every link, or null when not set */
    public String getQueryString() {
        return queryString;
    }

    /** @return open tracking: off, on or anonymous, or null when not set */
    public Tracking getTrackOpens() {
        return trackOpens;
    }

    /** @return click tracking: off, on or anonymous, or null when not set */
    public Tracking getTrackClicks() {
        return trackClicks;
    }

    /** @return send immediately once created, or false when not set */
    public boolean isSend() {
        return send;
    }

    /** @return schedule instead, e.g. "June 15, 2027 6:05pm", or null when not set */
    public String getScheduleDateTime() {
        return scheduleDateTime;
    }

    /** @return the timezone, e.g. "America/New_York", or null when not set */
    public String getScheduleTimezone() {
        return scheduleTimezone;
    }

    /** Builds a {@link CreateCampaignParams}. */
    public static final class Builder {
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String title;
        private String subject;
        private String htmlText;
        private String plainText;
        private List<String> listIds;
        private List<Object> segmentIds;
        private List<String> excludeListIds;
        private List<Object> excludeSegmentIds;
        private String queryString;
        private Tracking trackOpens;
        private Tracking trackClicks;
        private boolean send;
        private String scheduleDateTime;
        private String scheduleTimezone;

        private Builder() {
        }

        /**
         * The sender's name. Required.
         *
         * @return this builder
         */
        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        /**
         * The sender's address. Required.
         *
         * @return this builder
         */
        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        /**
         * The Reply-To address. Required.
         *
         * @return this builder
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * The campaign's title in the dashboard. Required.
         *
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * The subject. Required.
         *
         * @return this builder
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * The HTML body. Required.
         *
         * @return this builder
         */
        public Builder htmlText(String htmlText) {
            this.htmlText = htmlText;
            return this;
        }

        /**
         * The plain-text body.
         *
         * @return this builder
         */
        public Builder plainText(String plainText) {
            this.plainText = plainText;
            return this;
        }

        /**
         * Encrypted list ids. Needed, with {@code segmentIds}, only to send or schedule.
         *
         * @return this builder
         */
        public Builder listIds(Collection<String> listIds) {
            this.listIds = listIds == null ? null : new ArrayList<>(listIds);
            return this;
        }

        /**
         * Encrypted list ids. Needed, with {@code segmentIds}, only to send or schedule.
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
         * A query string appended to every link.
         *
         * @return this builder
         */
        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        /**
         * Open tracking: off, on or anonymous.
         *
         * @return this builder
         */
        public Builder trackOpens(Tracking trackOpens) {
            this.trackOpens = trackOpens;
            return this;
        }

        /**
         * Click tracking: off, on or anonymous.
         *
         * @return this builder
         */
        public Builder trackClicks(Tracking trackClicks) {
            this.trackClicks = trackClicks;
            return this;
        }

        /**
         * Send immediately once created.
         *
         * @return this builder
         */
        public Builder send(boolean send) {
            this.send = send;
            return this;
        }

        /**
         * Schedule instead, e.g. "June 15, 2027 6:05pm". Minutes must be a multiple of 5.
         *
         * @return this builder
         */
        public Builder scheduleDateTime(String scheduleDateTime) {
            this.scheduleDateTime = scheduleDateTime;
            return this;
        }

        /**
         * The timezone, e.g. "America/New_York". Falls back to the brand's timezone.
         *
         * @return this builder
         */
        public Builder scheduleTimezone(String scheduleTimezone) {
            this.scheduleTimezone = scheduleTimezone;
            return this;
        }

        /** @return the parameters */
        public CreateCampaignParams build() {
            return new CreateCampaignParams(this);
        }
    }
}
