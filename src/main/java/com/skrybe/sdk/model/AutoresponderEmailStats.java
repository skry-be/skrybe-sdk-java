package com.skrybe.sdk.model;

/**
 * An autoresponder email's stats, from {@code autoresponders().emailStats()}.
 */
public class AutoresponderEmailStats extends EngagementStats {
    private int id;
    private int autoresponderId;

    AutoresponderEmailStats() {
    }

    /** @return the email id */
    public int getId() {
        return id;
    }

    /** @return the autoresponder's id */
    public int getAutoresponderId() {
        return autoresponderId;
    }
}
