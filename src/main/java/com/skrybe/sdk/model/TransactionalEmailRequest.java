package com.skrybe.sdk.model;

/**
 * Request object for sending transactional emails (OTP, password reset, alerts, etc.)
 * via the send-transactional.php endpoint.
 *
 * Unlike regular emails, transactional emails are delivered immediately and not queued.
 */
public class TransactionalEmailRequest {
    private String toEmail;
    private String subject;
    private String htmlBody;
    private String plainBody;
    private String fromName;
    private String fromEmail;
    private String replyTo;

    private TransactionalEmailRequest(Builder builder) {
        this.toEmail = builder.toEmail;
        this.subject = builder.subject;
        this.htmlBody = builder.htmlBody;
        this.plainBody = builder.plainBody;
        this.fromName = builder.fromName;
        this.fromEmail = builder.fromEmail;
        this.replyTo = builder.replyTo;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getToEmail() { return toEmail; }
    public String getSubject() { return subject; }
    public String getHtmlBody() { return htmlBody; }
    public String getPlainBody() { return plainBody; }
    public String getFromName() { return fromName; }
    public String getFromEmail() { return fromEmail; }
    public String getReplyTo() { return replyTo; }

    public static class Builder {
        private String toEmail;
        private String subject;
        private String htmlBody;
        private String plainBody;
        private String fromName;
        private String fromEmail;
        private String replyTo;

        /**
         * Sets the recipient email address (required, single email only)
         */
        public Builder toEmail(String toEmail) {
            this.toEmail = toEmail;
            return this;
        }

        /**
         * Sets the email subject (required)
         */
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets the HTML body of the email (required unless plainBody is provided)
         */
        public Builder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        /**
         * Sets the plain text body of the email (required if htmlBody is omitted)
         */
        public Builder plainBody(String plainBody) {
            this.plainBody = plainBody;
            return this;
        }

        /**
         * Sets the 'From' name (optional, uses brand default if omitted)
         */
        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        /**
         * Sets the 'From' email address (optional, must be on an allowed domain for the brand)
         */
        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        /**
         * Sets the 'Reply-To' email address (optional, uses brand default if omitted)
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public TransactionalEmailRequest build() {
            // Validation
            if (toEmail == null || toEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("Recipient email (toEmail) is required");
            }
            if (subject == null || subject.trim().isEmpty()) {
                throw new IllegalArgumentException("Subject is required");
            }

            boolean hasHtml = htmlBody != null && !htmlBody.trim().isEmpty();
            boolean hasPlain = plainBody != null && !plainBody.trim().isEmpty();

            if (!hasHtml && !hasPlain) {
                throw new IllegalArgumentException("Either htmlBody or plainBody is required");
            }

            return new TransactionalEmailRequest(this);
        }
    }
}
