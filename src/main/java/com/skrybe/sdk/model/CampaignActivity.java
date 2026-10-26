package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * One subscriber's part in a campaign. Opens and country only for opens; clicks and links only for clicks.
 */
public class CampaignActivity extends ApiObject {
    private String email;
    private String name;
    private String listId;
    private Integer opens;
    private String country;
    private Integer clicks;
    private List<String> links;

    CampaignActivity() {
    }

    /** @return the address */
    public String getEmail() {
        return email;
    }

    /** @return the subscriber's name */
    public String getName() {
        return name;
    }

    /** @return the encrypted id of their list */
    public String getListId() {
        return listId;
    }

    /** @return how many times they opened it, for opens; otherwise null */
    public Integer getOpens() {
        return opens;
    }

    /** @return where they first opened it, when known; otherwise null */
    public String getCountry() {
        return country;
    }

    /** @return how many times they clicked, for clicks; otherwise null */
    public Integer getClicks() {
        return clicks;
    }

    /** @return the links they clicked, for clicks */
    public List<String> getLinks() {
        return links == null ? Collections.emptyList() : Collections.unmodifiableList(links);
    }
}
