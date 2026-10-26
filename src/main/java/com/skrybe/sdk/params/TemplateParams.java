package com.skrybe.sdk.params;

/**
 * Template fields for {@code templates().create()} (which requires name and htmlText) and {@code templates().update()} (where only the fields set change).
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class TemplateParams {
    private final String name;
    private final String htmlText;
    private final String plainText;
    private final String fromName;
    private final String fromEmail;
    private final String replyTo;
    private final TemplateEditor editor;

    private TemplateParams(Builder b) {
        this.name = b.name;
        this.htmlText = b.htmlText;
        this.plainText = b.plainText;
        this.fromName = b.fromName;
        this.fromEmail = b.fromEmail;
        this.replyTo = b.replyTo;
        this.editor = b.editor;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the template's name, or null when not set */
    public String getName() {
        return name;
    }

    /** @return the HTML body, or null when not set */
    public String getHtmlText() {
        return htmlText;
    }

    /** @return the plain-text body, or null when not set */
    public String getPlainText() {
        return plainText;
    }

    /** @return the default sender's name, or null when not set */
    public String getFromName() {
        return fromName;
    }

    /** @return the default sender's address, or null when not set */
    public String getFromEmail() {
        return fromEmail;
    }

    /** @return the default Reply-To address, or null when not set */
    public String getReplyTo() {
        return replyTo;
    }

    /** @return the editor the dashboard opens it in, or null when not set */
    public TemplateEditor getEditor() {
        return editor;
    }

    /** Builds a {@link TemplateParams}. */
    public static final class Builder {
        private String name;
        private String htmlText;
        private String plainText;
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private TemplateEditor editor;

        private Builder() {
        }

        /**
         * The template's name.
         *
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
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
         * The default sender's name.
         *
         * @return this builder
         */
        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        /**
         * The default sender's address.
         *
         * @return this builder
         */
        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        /**
         * The default Reply-To address.
         *
         * @return this builder
         */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * The editor the dashboard opens it in.
         *
         * @return this builder
         */
        public Builder editor(TemplateEditor editor) {
            this.editor = editor;
            return this;
        }

        /** @return the parameters */
        public TemplateParams build() {
            return new TemplateParams(this);
        }
    }
}
