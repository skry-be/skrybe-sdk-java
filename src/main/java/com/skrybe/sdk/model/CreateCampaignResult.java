package com.skrybe.sdk.model;

/** What {@code campaigns().create()} did. */
public final class CreateCampaignResult {
    /** Whether the campaign was left a draft, sent, or scheduled. */
    public enum Outcome {
        /** Created as a draft. */
        CREATED,
        /** Created and now sending. */
        SENDING,
        /** Created and scheduled. */
        SCHEDULED
    }

    private final Outcome outcome;
    private final String campaignId;

    /**
     * @param outcome what happened
     * @param campaignId the new campaign's id, or null
     */
    public CreateCampaignResult(Outcome outcome, String campaignId) {
        this.outcome = outcome;
        this.campaignId = campaignId;
    }

    /** @return what happened */
    public Outcome getOutcome() {
        return outcome;
    }

    /**
     * @return the new campaign's id, only for a plain create: the sending and
     *     scheduling paths of create.php answer with bare prose and no id.
     *     Otherwise null.
     */
    public String getCampaignId() {
        return campaignId;
    }

    @Override
    public String toString() {
        return "CreateCampaignResult{outcome=" + outcome + ", campaignId=" + campaignId + "}";
    }
}
