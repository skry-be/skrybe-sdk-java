package com.skrybe.sdk.params;

/**
 * Parameters for {@code emails().sendTransactional()}: one email, delivered immediately. Required: toEmail, subject, and htmlBody or plainBody.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class SendTransactionalParams {
    private final String toEmail;
    private final String subject;
    private final String htmlBody;
    private final String plainBody;
    private final String fromName;
    private final String fromEmail;
    private final String replyTo;

    private SendTransactionalParams(Builder b) {
        this.toEmail = b.toEmail;
        this.subject = b.subject;
        this.htmlBody = b.htmlBody;
        this.plainBody = b.plainBody;
        this.fromName = b.fromName;
        this.fromEmail = b.fromEmail;
        this.replyTo = b.replyTo;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the recipient, or null when not set */
    public String getToEmail() {
        return toEmail;
    }

    /** @return the subject, or null when not set */
    public String getSubject() {
        return subject;
    }

    /** @return the HTML body, or null when not set */
    public String getHtmlBody() {
        return htmlBody;
    }

    /** @return the plain-text body, or null when not set */
    public String getPlainBody() {
        return plainBody;
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

    /** Builds a {@link SendTransactionalParams}. */
    public static final class Builder {
        private String toEmail;
        private String subject;
        private String htmlBody;
        private String plainBody;
        private String fromName;
        private String fromEmail;
        private String replyTo;

        private Builder() {
        }

        /**
         * The recipient. Required.
         *
         * @return this builder
         */
        public Builder toEmail(String toEmail) {
            this.toEmail = toEmail;
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
         * The HTML body. At least one of htmlBody and plainBody is required.
         *
         * @return this builder
         */
        public Builder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        /**
         * The plain-text body. At least one of htmlBody and plainBody is required.
         *
         * @return this builder
         */
        public Builder plainBody(String plainBody) {
            this.plainBody = plainBody;
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

        /** @return the parameters */
        public SendTransactionalParams build() {
            return new SendTransactionalParams(this);
        }
    }
}
