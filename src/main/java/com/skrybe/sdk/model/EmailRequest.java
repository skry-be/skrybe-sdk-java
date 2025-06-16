package com.skrybe.sdk.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class EmailRequest {
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
    private boolean trackOpens;
    private boolean trackClicks;
    private String scheduleDateTime;
    private String scheduleTimezone;

    private EmailRequest(Builder builder) {
        this.fromName = builder.fromName;
        this.fromEmail = builder.fromEmail;
        this.replyTo = builder.replyTo;
        this.subject = builder.subject;
        this.htmlText = builder.htmlText;
        this.plainText = builder.plainText;
        this.to = builder.to;
        this.recipientVariables = builder.recipientVariables;
        this.listIds = builder.listIds;
        this.queryString = builder.queryString;
        this.trackOpens = builder.trackOpens;
        this.trackClicks = builder.trackClicks;
        this.scheduleDateTime = builder.scheduleDateTime;
        this.scheduleTimezone = builder.scheduleTimezone;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getFromName() { return fromName; }
    public String getFromEmail() { return fromEmail; }
    public String getReplyTo() { return replyTo; }
    public String getSubject() { return subject; }
    public String getHtmlText() { return htmlText; }
    public String getPlainText() { return plainText; }
    public List<String> getTo() { return to; }
    public Map<String, Map<String, Object>> getRecipientVariables() { return recipientVariables; }
    public List<String> getListIds() { return listIds; }
    public String getQueryString() { return queryString; }
    public boolean isTrackOpens() { return trackOpens; }
    public boolean isTrackClicks() { return trackClicks; }
    public String getScheduleDateTime() { return scheduleDateTime; }
    public String getScheduleTimezone() { return scheduleTimezone; }

    public static class Builder {
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String subject;
        private String htmlText;
        private String plainText;
        private List<String> to = new ArrayList<>();
        private Map<String, Map<String, Object>> recipientVariables = new HashMap<>();
        private List<String> listIds = new ArrayList<>();
        private String queryString;
        private boolean trackOpens = true;
        private boolean trackClicks = true;
        private String scheduleDateTime;
        private String scheduleTimezone;

        public Builder fromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        public Builder fromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
            return this;
        }

        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder htmlText(String htmlText) {
            this.htmlText = htmlText;
            return this;
        }

        public Builder plainText(String plainText) {
            this.plainText = plainText;
            return this;
        }

        public Builder addRecipient(String email) {
            this.to.add(email);
            return this;
        }

        public Builder recipients(List<String> emails) {
            this.to = new ArrayList<>(emails);
            return this;
        }

        public Builder addRecipientVariable(String email, String key, Object value) {
            this.recipientVariables.computeIfAbsent(email, k -> new HashMap<>()).put(key, value);
            return this;
        }

        public Builder recipientVariables(Map<String, Map<String, Object>> recipientVariables) {
            this.recipientVariables = new HashMap<>(recipientVariables);
            return this;
        }

        public Builder addListId(String listId) {
            this.listIds.add(listId);
            return this;
        }

        public Builder listIds(List<String> listIds) {
            this.listIds = new ArrayList<>(listIds);
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder trackOpens(boolean trackOpens) {
            this.trackOpens = trackOpens;
            return this;
        }

        public Builder trackClicks(boolean trackClicks) {
            this.trackClicks = trackClicks;
            return this;
        }

        public Builder scheduleDateTime(String scheduleDateTime) {
            this.scheduleDateTime = scheduleDateTime;
            return this;
        }

        public Builder scheduleTimezone(String scheduleTimezone) {
            this.scheduleTimezone = scheduleTimezone;
            return this;
        }

        public EmailRequest build() {
            // Validation
            if (fromName == null || fromName.trim().isEmpty()) {
                throw new IllegalArgumentException("From name is required");
            }
            if (fromEmail == null || fromEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("From email is required");
            }
            if (subject == null || subject.trim().isEmpty()) {
                throw new IllegalArgumentException("Subject is required");
            }
            if (htmlText == null || htmlText.trim().isEmpty()) {
                throw new IllegalArgumentException("HTML text is required");
            }
            if (to.isEmpty()) {
                throw new IllegalArgumentException("At least one recipient email is required");
            }

            // Set default replyTo if not provided
            if (replyTo == null || replyTo.trim().isEmpty()) {
                replyTo = fromEmail;
            }

            return new EmailRequest(this);
        }
    }
} 