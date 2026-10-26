package com.skrybe.sdk.params;

/**
 * Fields {@code campaigns().update()} can change. Only those set change.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class UpdateCampaignParams {
    private final String title;
    private final String subject;
    private final String preheader;
    private final String fromName;
    private final String fromEmail;
    private final String replyTo;
    private final String htmlText;
    private final String plainText;
    private final String queryString;
    private final Tracking trackOpens;
    private final Tracking trackClicks;

    private UpdateCampaignParams(Builder b) {
        this.title = b.title;
        this.subject = b.subject;
        this.preheader = b.preheader;
        this.fromName = b.fromName;
        this.fromEmail = b.fromEmail;
        this.replyTo = b.replyTo;
        this.htmlText = b.htmlText;
        this.plainText = b.plainText;
        this.queryString = b.queryString;
        this.trackOpens = b.trackOpens;
        this.trackClicks = b.trackClicks;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the title, or null when not set */
    public String getTitle() {
        return title;
    }

    /** @return the subject, or null when not set */
    public String getSubject() {
        return subject;
    }

    /** @return the preview text, or null when not set */
    public String getPreheader() {
        return preheader;
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

    /** @return the HTML body, or null when not set */
    public String getHtmlText() {
        return htmlText;
    }

    /** @return the plain-text body, or null when not set */
    public String getPlainText() {
        return plainText;
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

    /** Builds a {@link UpdateCampaignParams}. */
    public static final class Builder {
        private String title;
        private String subject;
        private String preheader;
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String htmlText;
        private String plainText;
        private String queryString;
        private Tracking trackOpens;
        private Tracking trackClicks;

        private Builder() {
        }

        /**
         * The title.
         *
         * @return this builder
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * The subject.
         *
         * @return this builder
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * The preview text.
         *
         * @return this builder
         */
        public Builder preheader(String preheader) {
            this.preheader = preheader;
            return this;
        }

        /**
         * The sender's name.
         *
         * @return this builder
         */
        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        /**
         * The sender's address.
         *
         * @return this builder
         */
        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        /**
         * The Reply-To address.
         *
         * @return this builder
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * The HTML body.
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

        /** @return the parameters */
        public UpdateCampaignParams build() {
            return new UpdateCampaignParams(this);
        }
    }
}
