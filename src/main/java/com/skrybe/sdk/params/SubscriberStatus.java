package com.skrybe.sdk.params;

import java.util.Locale;

/** Every subscriber is in exactly one of these; {@code ACTIVE} is who a campaign sends to. */
public enum SubscriberStatus implements ApiValue {
    ACTIVE,
    UNCONFIRMED,
    UNSUBSCRIBED,
    BOUNCED,
    COMPLAINED;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
