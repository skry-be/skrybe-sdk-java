package com.skrybe.sdk.params;

/** A parameter value with a fixed spelling on the wire. */
public interface ApiValue {
    /** @return the value as the API spells it, e.g. {@code on_date} */
    String value();
}
