package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * Opens, clicks, bounces and the rest, as campaign and autoresponder stats report them.
 */
public class EngagementStats extends ApiObject {
    /** Totals and a percentage rate. */
    public static final class Rate extends ApiObject {
        private int total;
        private int unique;
        private double rate;

        Rate() {
        }

        /** @return the total */
        public int getTotal() {
            return total;
        }

        /** @return unique subscribers */
        public int getUnique() {
            return unique;
        }

        /** @return the rate, as a percentage */
        public double getRate() {
            return rate;
        }
    }

    /** Hard and soft bounces. */
    public static final class Bounces extends ApiObject {
        private int hard;
        private int soft;

        Bounces() {
        }

        /** @return hard bounces */
        public int getHard() {
            return hard;
        }

        /** @return soft bounces */
        public int getSoft() {
            return soft;
        }
    }

    /** Clicks on one link. */
    public static final class Link extends ApiObject {
        private String url;
        private int clicks;
        private int uniqueClicks;

        Link() {
        }

        /** @return the link */
        public String getUrl() {
            return url;
        }

        /** @return total clicks */
        public int getClicks() {
            return clicks;
        }

        /** @return subscribers who clicked it */
        public int getUniqueClicks() {
            return uniqueClicks;
        }
    }

    private int recipients;
    private Rate opens;
    private Rate clicks;
    private Bounces bounces;
    private int complaints;
    private int unsubscribes;
    private List<Link> links;

    EngagementStats() {
    }

    /** @return how many it went to */
    public int getRecipients() {
        return recipients;
    }

    /** @return opens; the rate is a percentage of delivered (recipients less hard bounces) */
    public Rate getOpens() {
        return opens;
    }

    /** @return clicks; the rate is a percentage of recipients */
    public Rate getClicks() {
        return clicks;
    }

    /** @return hard and soft bounces */
    public Bounces getBounces() {
        return bounces;
    }

    /** @return spam complaints */
    public int getComplaints() {
        return complaints;
    }

    /** @return unsubscribes */
    public int getUnsubscribes() {
        return unsubscribes;
    }

    /** @return clicks per link */
    public List<Link> getLinks() {
        return links == null ? Collections.emptyList() : Collections.unmodifiableList(links);
    }
}
