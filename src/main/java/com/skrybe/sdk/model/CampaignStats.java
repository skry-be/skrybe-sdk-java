package com.skrybe.sdk.model;

/**
 * A campaign's stats, from {@code campaigns().stats()}.
 */
public class CampaignStats extends EngagementStats {
    private int id;
    private String status;

    CampaignStats() {
    }

    /** @return the campaign id */
    public int getId() {
        return id;
    }

    /** @return the campaign's status */
    public String getStatus() {
        return status;
    }
}
