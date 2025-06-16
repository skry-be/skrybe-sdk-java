package com.skrybe.sdk.model;

public class Campaign {
    private String id;
    private String title;
    private String fromName;
    private String fromEmail;
    private String subject;
    private String status;

    public Campaign() {}

    public Campaign(String id, String title, String fromName, String fromEmail, String subject, String status) {
        this.id = id;
        this.title = title;
        this.fromName = fromName;
        this.fromEmail = fromEmail;
        this.subject = subject;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Campaign{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", fromName='" + fromName + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
} 