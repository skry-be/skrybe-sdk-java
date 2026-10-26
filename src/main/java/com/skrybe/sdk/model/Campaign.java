package com.skrybe.sdk.model;

/**
 * One campaign, as {@code campaigns().list()} returns it.
 */
public class Campaign extends ApiObject {
    private int id;
    private String title;
    private String subject;
    private String status;
    private String fromName;
    private String fromEmail;
    private String replyTo;
    private int recipients;
    private int toSend;
    private Long sentAt;
    private Long scheduledAt;
    private String timezone;
    private String source;

    Campaign() {
    }

    /** @return the campaign id */
    public int getId() {
        return id;
    }

    /** @return the title shown in the dashboard */
    public String getTitle() {
        return title;
    }

    /** @return the subject */
    public String getSubject() {
        return subject;
    }

    /** @return draft, scheduled, preparing, sending, sent or paused */
    public String getStatus() {
        return status;
    }

    /** @return the sender's name */
    public String getFromName() {
        return fromName;
    }

    /** @return the sender's address */
    public String getFromEmail() {
        return fromEmail;
    }

    /** @return the Reply-To address */
    public String getReplyTo() {
        return replyTo;
    }

    /** @return how many it went to */
    public int getRecipients() {
        return recipients;
    }

    /** @return how many it is going to */
    public int getToSend() {
        return toSend;
    }

    /** @return when it was sent, in Unix seconds, or null */
    public Long getSentAt() {
        return sentAt;
    }

    /** @return when it is scheduled for, in Unix seconds, only while scheduled; otherwise null */
    public Long getScheduledAt() {
        return scheduledAt;
    }

    /** @return the timezone it is scheduled in, or null */
    public String getTimezone() {
        return timezone;
    }

    /** @return {@code ui}, or {@code api} for campaigns created by {@code emails().send()} */
    public String getSource() {
        return source;
    }
}
