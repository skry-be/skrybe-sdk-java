package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * One list with its settings, counts and custom fields.
 */
public class SkrybeListDetail extends SkrybeList {
    /** Subscriber counts by status. Active is who a campaign goes to: confirmed, and not unsubscribed, bounced or complained. */
    public static final class SubscriberCounts extends ApiObject {
        private int active;
        private int unconfirmed;
        private int unsubscribed;
        private int bounced;
        private int complained;

        SubscriberCounts() {
        }

        /** @return active subscribers */
        public int getActive() {
            return active;
        }

        /** @return unconfirmed subscribers */
        public int getUnconfirmed() {
            return unconfirmed;
        }

        /** @return unsubscribed subscribers */
        public int getUnsubscribed() {
            return unsubscribed;
        }

        /** @return bounced subscribers */
        public int getBounced() {
            return bounced;
        }

        /** @return subscribers who complained */
        public int getComplained() {
            return complained;
        }
    }

    private String optIn;
    private SubscriberCounts subscribers;
    private List<CustomField> customFields;

    SkrybeListDetail() {
    }

    /** @return {@code single} or {@code double} */
    public String getOptIn() {
        return optIn;
    }

    /** @return subscriber counts by status */
    public SubscriberCounts getSubscribers() {
        return subscribers;
    }

    /** @return the custom fields, in the order subscribers' values are stored */
    public List<CustomField> getCustomFields() {
        return customFields == null ? Collections.emptyList() : Collections.unmodifiableList(customFields);
    }
}
