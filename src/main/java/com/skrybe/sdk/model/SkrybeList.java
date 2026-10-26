package com.skrybe.sdk.model;

/**
 * A list: its id and name.
 */
public class SkrybeList extends ApiObject {
    private String id;
    private String name;

    SkrybeList() {
    }

    /** @return the list id: an encrypted value, not the number in the dashboard URL. Pass it back as given wherever a list id is wanted */
    public String getId() {
        return id;
    }

    /** @return the list's name */
    public String getName() {
        return name;
    }
}
