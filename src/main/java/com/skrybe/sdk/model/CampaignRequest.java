package com.skrybe.sdk.model;

import java.util.List;
import java.util.ArrayList;

public class CampaignRequest {
    private String title;
    private String fromName;
    private String fromEmail;
    private String replyTo;
    private String subject;
    private String htmlText;
    private String plainText;
    private List<String> listIds;
    private boolean sendCampaign;
    private boolean trackOpens;
    private boolean trackClicks;

    private CampaignRequest(Builder builder) {
        this.title = builder.title;
        this.fromName = builder.fromName;
        this.fromEmail = builder.fromEmail;
        this.replyTo = builder.replyTo;
        this.subject = builder.subject;
        this.htmlText = builder.htmlText;
        this.plainText = builder.plainText;
        this.listIds = builder.listIds;
        this.sendCampaign = builder.sendCampaign;
        this.trackOpens = builder.trackOpens;
        this.trackClicks = builder.trackClicks;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getTitle() { return title; }
    public String getFromName() { return fromName; }
    public String getFromEmail() { return fromEmail; }
    public String getReplyTo() { return replyTo; }
    public String getSubject() { return subject; }
    public String getHtmlText() { return htmlText; }
    public String getPlainText() { return plainText; }
    public List<String> getListIds() { return listIds; }
    public boolean isSendCampaign() { return sendCampaign; }
    public boolean isTrackOpens() { return trackOpens; }
    public boolean isTrackClicks() { return trackClicks; }

    public static class Builder {
        private String title;
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String subject;
        private String htmlText;
        private String plainText;
        private List<String> listIds = new ArrayList<>();
        private boolean sendCampaign = false;
        private boolean trackOpens = true;
        private boolean trackClicks = true;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

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

        public Builder addListId(String listId) {
            this.listIds.add(listId);
            return this;
        }

        public Builder listIds(List<String> listIds) {
            this.listIds = new ArrayList<>(listIds);
            return this;
        }

        public Builder sendCampaign(boolean sendCampaign) {
            this.sendCampaign = sendCampaign;
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

        public CampaignRequest build() {
            // Validation
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
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
            if (listIds.isEmpty()) {
                throw new IllegalArgumentException("At least one list ID is required");
            }

            // Set default replyTo if not provided
            if (replyTo == null || replyTo.trim().isEmpty()) {
                replyTo = fromEmail;
            }

            return new CampaignRequest(this);
        }
    }
} 