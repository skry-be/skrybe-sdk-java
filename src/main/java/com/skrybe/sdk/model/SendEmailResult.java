package com.skrybe.sdk.model;

/** What {@code emails().send()} did. */
public final class SendEmailResult {
    private final String message;
    private final String campaignId;

    /**
     * @param message the API's message
     * @param campaignId the campaign the send created, or null
     */
    public SendEmailResult(String message, String campaignId) {
        this.message = message;
        this.campaignId = campaignId;
    }

    /** @return the API's message */
    public String getMessage() {
        return message;
    }

    /** @return the id of the campaign the send created behind the scenes, or null when the API did not say */
    public String getCampaignId() {
        return campaignId;
    }

    @Override
    public String toString() {
        return "SendEmailResult{message=" + message + ", campaignId=" + campaignId + "}";
    }
}
