package com.skrybe.sdk.params;

import java.util.Locale;

/** A custom field's type. */
public enum CustomFieldType implements ApiValue {
    TEXT,
    DATE;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
