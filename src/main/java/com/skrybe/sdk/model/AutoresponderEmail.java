package com.skrybe.sdk.model;

/**
 * One email of an autoresponder.
 */
public class AutoresponderEmail extends ApiObject {
    private int id;
    private int autoresponderId;
    private String subject;
    private String fromName;
    private String fromEmail;
    private String when;
    private boolean enabled;
    private int recipients;
    private Long createdAt;

    AutoresponderEmail() {
    }

    /** @return the email id */
    public int getId() {
        return id;
    }

    /** @return the autoresponder's id */
    public int getAutoresponderId() {
        return autoresponderId;
    }

    /** @return the subject */
    public String getSubject() {
        return subject;
    }

    /** @return the sender's name */
    public String getFromName() {
        return fromName;
    }

    /** @return the sender's address */
    public String getFromEmail() {
        return fromEmail;
    }

    /** @return as stored: "immediately", "+3 days", "-1 days", or "" for the date itself */
    public String getWhen() {
        return when;
    }

    /** @return whether it is on */
    public boolean isEnabled() {
        return enabled;
    }

    /** @return how many it has gone to */
    public int getRecipients() {
        return recipients;
    }

    /** @return when it was created, in Unix seconds, or null */
    public Long getCreatedAt() {
        return createdAt;
    }
}
