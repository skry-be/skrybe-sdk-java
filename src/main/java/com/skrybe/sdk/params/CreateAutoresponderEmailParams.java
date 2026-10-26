package com.skrybe.sdk.params;

/**
 * Parameters for {@code autoresponders().createEmail()}. Required: subject, htmlText.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class CreateAutoresponderEmailParams {
    private final String subject;
    private final String htmlText;
    private final String plainText;
    private final String preheader;
    private final String when;
    private final String fromName;
    private final String fromEmail;
    private final String replyTo;
    private final String queryString;
    private final Tracking trackOpens;
    private final Tracking trackClicks;
    private final boolean enabled;

    private CreateAutoresponderEmailParams(Builder b) {
        this.subject = b.subject;
        this.htmlText = b.htmlText;
        this.plainText = b.plainText;
        this.preheader = b.preheader;
        this.when = b.when;
        this.fromName = b.fromName;
        this.fromEmail = b.fromEmail;
        this.replyTo = b.replyTo;
        this.queryString = b.queryString;
        this.trackOpens = b.trackOpens;
        this.trackClicks = b.trackClicks;
        this.enabled = b.enabled;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
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

    /** @return the preview text, or null when not set */
    public String getPreheader() {
        return preheader;
    }

    /** @return when it goes, or null when not set */
    public String getWhen() {
        return when;
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

    /** @return turn the email on, or false when not set */
    public boolean isEnabled() {
        return enabled;
    }

    /** Builds a {@link CreateAutoresponderEmailParams}. */
    public static final class Builder {
        private String subject;
        private String htmlText;
        private String plainText;
        private String preheader;
        private String when;
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String queryString;
        private Tracking trackOpens;
        private Tracking trackClicks;
        private boolean enabled;

        private Builder() {
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
         * The preview text.
         *
         * @return this builder
         */
        public Builder preheader(String preheader) {
            this.preheader = preheader;
            return this;
        }

        /**
         * When it goes. For a drip, "immediately" (the default) or a delay such as "+3 days"; for annually and on_date, "on" (the default, the date itself) or an offset such as "+9 hours" or "-1 days". Units: minute, hour, day, week, month.
         *
         * @return this builder
         */
        public Builder when(String when) {
            this.when = when;
            return this;
        }

        /**
         * The sender's name. Falls back to the brand's.
         *
         * @return this builder
         */
        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        /**
         * The sender's address. Falls back to the brand's.
         *
         * @return this builder
         */
        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        /**
         * The Reply-To address. Falls back to the brand's.
         *
         * @return this builder
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
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

        /**
         * Turn the email on. Emails are created off, as the dashboard creates them, unless this is set.
         *
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /** @return the parameters */
        public CreateAutoresponderEmailParams build() {
            return new CreateAutoresponderEmailParams(this);
        }
    }
}
