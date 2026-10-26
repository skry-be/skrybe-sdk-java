package com.skrybe.sdk.model;

/**
 * A brand: its id and name.
 */
public class Brand extends ApiObject {
    private String id;
    private String name;

    Brand() {
    }

    /** @return the brand id */
    public String getId() {
        return id;
    }

    /** @return the brand's name */
    public String getName() {
        return name;
    }
}
