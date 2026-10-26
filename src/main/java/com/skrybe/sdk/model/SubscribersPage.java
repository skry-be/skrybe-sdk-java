package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * A page of {@code subscribers().list()}.
 */
public class SubscribersPage extends ApiObject {
    private String listId;
    private String status;
    private int page;
    private int limit;
    private int total;
    private List<Subscriber> subscribers;

    SubscribersPage() {
    }

    /** @return the list id */
    public String getListId() {
        return listId;
    }

    /** @return the status filter, or null */
    public String getStatus() {
        return status;
    }

    /** @return the page, 1-based */
    public int getPage() {
        return page;
    }

    /** @return the page size */
    public int getLimit() {
        return limit;
    }

    /** @return subscribers matching the filter, across all pages */
    public int getTotal() {
        return total;
    }

    /** @return this page's subscribers */
    public List<Subscriber> getSubscribers() {
        return subscribers == null ? Collections.emptyList() : Collections.unmodifiableList(subscribers);
    }
}
