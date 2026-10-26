package com.skrybe.sdk.params;

/** Open and click tracking: off, on, or anonymous (counted without tying them to a subscriber). */
public enum Tracking implements ApiValue {
    /** 0: not tracked. */
    OFF("0"),
    /** 1: tracked per subscriber. */
    ON("1"),
    /** 2: counted, but not tied to a subscriber. */
    ANONYMOUS("2");

    private final String value;

    Tracking(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
