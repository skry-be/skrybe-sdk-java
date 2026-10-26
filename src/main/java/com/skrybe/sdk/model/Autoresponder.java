package com.skrybe.sdk.model;

/**
 * An autoresponder.
 */
public class Autoresponder extends ApiObject {
    private int id;
    private String name;
    private String type;
    private String listId;
    private String customField;
    private int emails;
    private int enabledEmails;

    Autoresponder() {
    }

    /** @return the autoresponder id */
    public int getId() {
        return id;
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @return drip, annually or on_date; {@code unknown} for a type this SDK predates */
    public String getType() {
        return type;
    }

    /** @return the encrypted id of its list */
    public String getListId() {
        return listId;
    }

    /** @return the date field an annually or on_date autoresponder sends on, or null */
    public String getCustomField() {
        return customField;
    }

    /** @return how many emails it has */
    public int getEmails() {
        return emails;
    }

    /** @return how many of them are on */
    public int getEnabledEmails() {
        return enabledEmails;
    }
}
