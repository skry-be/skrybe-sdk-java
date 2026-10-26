package com.skrybe.sdk.model;

/**
 * A list's custom field.
 */
public class CustomField extends ApiObject {
    private String name;
    private String type;

    CustomField() {
    }

    /** @return the name, as used in personalization tags: {@code [Name,fallback=]} */
    public String getName() {
        return name;
    }

    /** @return {@code Text} or {@code Date}, as the API stores it */
    public String getType() {
        return type;
    }
}
