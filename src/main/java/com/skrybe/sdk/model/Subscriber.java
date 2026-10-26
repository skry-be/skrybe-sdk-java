package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.Map;

/**
 * One subscriber of a list.
 */
public class Subscriber extends ApiObject {
    private String email;
    private String name;
    private String status;
    private Long joinedAt;
    private Map<String, String> customFields;

    Subscriber() {
    }

    /** @return the address */
    public String getEmail() {
        return email;
    }

    /** @return the subscriber's name */
    public String getName() {
        return name;
    }

    /** @return active, unconfirmed, unsubscribed, bounced or complained */
    public String getStatus() {
        return status;
    }

    /** @return when they joined, in Unix seconds, or null */
    public Long getJoinedAt() {
        return joinedAt;
    }

    /** @return custom field values by field name, in the list's order. Dates are YYYY-MM-DD; empty values are null */
    public Map<String, String> getCustomFields() {
        return customFields == null ? Collections.emptyMap() : Collections.unmodifiableMap(customFields);
    }
}
