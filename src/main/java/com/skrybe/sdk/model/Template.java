package com.skrybe.sdk.model;

/**
 * A template.
 */
public class Template extends ApiObject {
    private int id;
    private String name;
    private String fromName;
    private String fromEmail;
    private String replyTo;
    private String editor;
    private String htmlText;
    private String plainText;

    Template() {
    }

    /** @return the template id */
    public int getId() {
        return id;
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @return the default sender's name */
    public String getFromName() {
        return fromName;
    }

    /** @return the default sender's address */
    public String getFromEmail() {
        return fromEmail;
    }

    /** @return the default Reply-To address */
    public String getReplyTo() {
        return replyTo;
    }

    /** @return {@code html} or {@code dragdrop} */
    public String getEditor() {
        return editor;
    }

    /** @return the HTML body, only from get() with includeContent and from create and update; otherwise null */
    public String getHtmlText() {
        return htmlText;
    }

    /** @return the plain-text body, when the HTML is included; otherwise null */
    public String getPlainText() {
        return plainText;
    }
}
