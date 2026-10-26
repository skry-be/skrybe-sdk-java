package com.skrybe.sdk.params;

import java.util.Locale;

/** A campaign's status, as the brand's campaign list labels it. */
public enum CampaignStatus implements ApiValue {
    DRAFT,
    SCHEDULED,
    PREPARING,
    SENDING,
    SENT,
    PAUSED;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
