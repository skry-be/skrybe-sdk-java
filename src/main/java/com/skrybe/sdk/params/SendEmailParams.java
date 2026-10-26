package com.skrybe.sdk.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameters for {@code emails().send()}: a bulk send to addresses or lists. Required: fromName, fromEmail, replyTo, subject, htmlText, and {@code to} or {@code listIds}.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class SendEmailParams {
    private final String fromName;
    private final String fromEmail;
    private final String replyTo;
    private final String subject;
    private final String htmlText;
    private final String plainText;
    private final List<String> to;
    private final Map<String, Map<String, Object>> recipientVariables;
    private final List<String> listIds;
    private final String queryString;
    private final Tracking trackOpens;
    private final Tracking trackClicks;
    private final String scheduleDateTime;
    private final String scheduleTimezone;

    private SendEmailParams(Builder b) {
        this.fromName = b.fromName;
        this.fromEmail = b.fromEmail;
        this.replyTo = b.replyTo;
        this.subject = b.subject;
        this.htmlText = b.htmlText;
        this.plainText = b.plainText;
        this.to = b.to == null ? null : Collections.unmodifiableList(new ArrayList<>(b.to));
        this.recipientVariables = b.recipientVariables == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(b.recipientVariables));
        this.listIds = b.listIds == null ? null : Collections.unmodifiableList(new ArrayList<>(b.listIds));
        this.queryString = b.queryString;
        this.trackOpens = b.trackOpens;
        this.trackClicks = b.trackClicks;
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

    /** @return the subject, which doubles as the title of the campaign the send creates, or null when not set */
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

    /** @return addresses to send to, at most 250, or null when not set */
    public List<String> getTo() {
        return to;
    }

    /** @return per-recipient values keyed by address, referenced in the body as {@code [recipient.first]}, or null when not set */
    public Map<String, Map<String, Object>> getRecipientVariables() {
        return recipientVariables;
    }

    /** @return encrypted list ids, as {@code lists().list()} returns them, or null when not set */
    public List<String> getListIds() {
        return listIds;
    }

    /** @return a query string appended to every link, e.g. utm parameters, or null when not set */
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

    /** @return schedule instead of sending now, e.g. "June 15, 2027 6:05pm", or null when not set */
    public String getScheduleDateTime() {
        return scheduleDateTime;
    }

    /** @return the timezone, e.g. "Africa/Lagos", or null when not set */
    public String getScheduleTimezone() {
        return scheduleTimezone;
    }

    /** Builds a {@link SendEmailParams}. */
    public static final class Builder {
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String subject;
        private String htmlText;
        private String plainText;
        private List<String> to;
        private Map<String, Map<String, Object>> recipientVariables;
        private List<String> listIds;
        private String queryString;
        private Tracking trackOpens;
        private Tracking trackClicks;
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
         * The subject, which doubles as the title of the campaign the send creates. Required.
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
         * Addresses to send to, at most 250. Without {@code listIds} these are added to a new list.
         *
         * @return this builder
         */
        public Builder to(Collection<String> to) {
            this.to = to == null ? null : new ArrayList<>(to);
            return this;
        }

        /**
         * Addresses to send to, at most 250. Without {@code listIds} these are added to a new list.
         *
         * @return this builder
         */
        public Builder to(String... to) {
            return to(Arrays.asList(to));
        }

        /**
         * Per-recipient values keyed by address, referenced in the body as {@code [recipient.first]}.
         *
         * @return this builder
         */
        public Builder recipientVariables(Map<String, ? extends Map<String, ?>> recipientVariables) {
            if (recipientVariables == null) {
                this.recipientVariables = null;
                return this;
            }
            this.recipientVariables = new LinkedHashMap<>();
            for (Map.Entry<String, ? extends Map<String, ?>> e : recipientVariables.entrySet()) {
                this.recipientVariables.put(e.getKey(), e.getValue() == null ? null : new LinkedHashMap<String, Object>(e.getValue()));
            }
            return this;
        }

        /**
         * Set one recipient's variable, e.g. {@code recipientVariable("ada@example.com", "first", "Ada")}
         * for {@code [recipient.first]}.
         *
         * @return this builder
         */
        public Builder recipientVariable(String email, String name, Object value) {
            if (this.recipientVariables == null) {
                this.recipientVariables = new LinkedHashMap<>();
            }
            this.recipientVariables.computeIfAbsent(email, k -> new LinkedHashMap<>()).put(name, value);
            return this;
        }

        /**
         * Encrypted list ids, as {@code lists().list()} returns them.
         *
         * @return this builder
         */
        public Builder listIds(Collection<String> listIds) {
            this.listIds = listIds == null ? null : new ArrayList<>(listIds);
            return this;
        }

        /**
         * Encrypted list ids, as {@code lists().list()} returns them.
         *
         * @return this builder
         */
        public Builder listIds(String... listIds) {
            return listIds(Arrays.asList(listIds));
        }

        /**
         * A query string appended to every link, e.g. utm parameters.
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
         * Schedule instead of sending now, e.g. "June 15, 2027 6:05pm".
         *
         * @return this builder
         */
        public Builder scheduleDateTime(String scheduleDateTime) {
            this.scheduleDateTime = scheduleDateTime;
            return this;
        }

        /**
         * The timezone, e.g. "Africa/Lagos". Falls back to the brand's timezone.
         *
         * @return this builder
         */
        public Builder scheduleTimezone(String scheduleTimezone) {
            this.scheduleTimezone = scheduleTimezone;
            return this;
        }

        /** @return the parameters */
        public SendEmailParams build() {
            return new SendEmailParams(this);
        }
    }
}
