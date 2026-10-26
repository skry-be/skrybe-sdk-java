package com.skrybe.sdk.model;

/**
 * What {@code campaigns().send()} did.
 */
public class SendCampaignResult extends ApiObject {
    private String status;
    private int campaignId;
    private int recipients;

    SendCampaignResult() {
    }

    /** @return {@code sending}, or {@code dry_run} */
    public String getStatus() {
        return status;
    }

    /** @return the campaign id */
    public int getCampaignId() {
        return campaignId;
    }

    /** @return unique active subscribers the campaign goes (or would go) to */
    public int getRecipients() {
        return recipients;
    }
}
