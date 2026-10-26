package com.skrybe.sdk.params;

import java.util.Locale;

/** Whether a list confirms new subscribers by email. */
public enum OptIn implements ApiValue {
    SINGLE,
    DOUBLE;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
