package com.skrybe.sdk.model;

/** What {@code subscribers().subscribe()} did. */
public enum SubscribeOutcome {
    /** The address was added. */
    SUBSCRIBED,
    /** The address was already on the list; its name and custom fields were updated. */
    ALREADY_SUBSCRIBED
}
