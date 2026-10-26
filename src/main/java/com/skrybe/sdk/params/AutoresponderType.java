package com.skrybe.sdk.params;

import java.util.Locale;

/** {@code DRIP} sends each email a set time after someone joins; {@code ANNUALLY} and {@code ON_DATE} send on a Date custom field, every year or once. */
public enum AutoresponderType implements ApiValue {
    DRIP,
    ANNUALLY,
    ON_DATE;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
