package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * What {@code campaigns().testSend()} did, one result per address.
 */
public class TestSendResult extends ApiObject {
    /** One address's test send. */
    public static final class Result extends ApiObject {
        private String email;
        private boolean ok;
        private String error;

        Result() {
        }

        /** @return the address */
        public String getEmail() {
            return email;
        }

        /** @return whether it was sent */
        public boolean isOk() {
            return ok;
        }

        /** @return why not, or null */
        public String getError() {
            return error;
        }
    }

    private int campaignId;
    private List<Result> results;

    TestSendResult() {
    }

    /** @return the campaign id */
    public int getCampaignId() {
        return campaignId;
    }

    /** @return one result per address, in the order sent */
    public List<Result> getResults() {
        return results == null ? Collections.emptyList() : Collections.unmodifiableList(results);
    }
}
