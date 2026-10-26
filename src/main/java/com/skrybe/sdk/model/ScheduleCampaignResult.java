package com.skrybe.sdk.model;

/**
 * What {@code campaigns().schedule()} did.
 */
public class ScheduleCampaignResult extends ApiObject {
    private String status;
    private int campaignId;
    private int recipients;
    private long scheduledAt;
    private String timezone;

    ScheduleCampaignResult() {
    }

    /** @return {@code scheduled}, or {@code dry_run} */
    public String getStatus() {
        return status;
    }

    /** @return the campaign id */
    public int getCampaignId() {
        return campaignId;
    }

    /** @return unique active subscribers it will go to */
    public int getRecipients() {
        return recipients;
    }

    /** @return when it sends, in Unix seconds */
    public long getScheduledAt() {
        return scheduledAt;
    }

    /** @return the timezone it was scheduled in */
    public String getTimezone() {
        return timezone;
    }
}
