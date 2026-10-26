package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * A page of {@code campaigns().activity()}.
 */
public class CampaignActivityPage extends ApiObject {
    private int campaignId;
    private String type;
    private int page;
    private int limit;
    private int total;
    private List<CampaignActivity> activity;

    CampaignActivityPage() {
    }

    /** @return the campaign id */
    public int getCampaignId() {
        return campaignId;
    }

    /** @return the kind of activity */
    public String getType() {
        return type;
    }

    /** @return the page, 1-based */
    public int getPage() {
        return page;
    }

    /** @return the page size */
    public int getLimit() {
        return limit;
    }

    /** @return subscribers across all pages */
    public int getTotal() {
        return total;
    }

    /** @return this page's rows */
    public List<CampaignActivity> getActivity() {
        return activity == null ? Collections.emptyList() : Collections.unmodifiableList(activity);
    }
}
