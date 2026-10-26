package com.skrybe.sdk.params;

import java.util.Locale;

/** The kinds of campaign activity {@code campaigns().activity()} reports. */
public enum ActivityType implements ApiValue {
    OPENS,
    CLICKS,
    BOUNCES,
    COMPLAINTS,
    UNSUBSCRIBES;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
