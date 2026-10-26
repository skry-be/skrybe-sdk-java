package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * One campaign with its settings, as {@code campaigns().get()}, {@code update()} and {@code duplicate()} return it.
 */
public class CampaignDetail extends Campaign {
    private String preheader;
    private String queryString;
    private int trackOpens;
    private int trackClicks;
    private List<String> listIds;
    private List<String> excludeListIds;
    private List<Integer> segmentIds;
    private List<Integer> excludeSegmentIds;
    private String webVersion;
    private String htmlText;
    private String plainText;

    CampaignDetail() {
    }

    /** @return the preview text */
    public String getPreheader() {
        return preheader;
    }

    /** @return the query string appended to every link */
    public String getQueryString() {
        return queryString;
    }

    /** @return open tracking: 0 off, 1 on, 2 anonymous */
    public int getTrackOpens() {
        return trackOpens;
    }

    /** @return click tracking: 0 off, 1 on, 2 anonymous */
    public int getTrackClicks() {
        return trackClicks;
    }

    /** @return the encrypted ids of the lists it goes to */
    public List<String> getListIds() {
        return listIds == null ? Collections.emptyList() : Collections.unmodifiableList(listIds);
    }

    /** @return the encrypted ids of the lists left out */
    public List<String> getExcludeListIds() {
        return excludeListIds == null ? Collections.emptyList() : Collections.unmodifiableList(excludeListIds);
    }

    /** @return the segments it goes to */
    public List<Integer> getSegmentIds() {
        return segmentIds == null ? Collections.emptyList() : Collections.unmodifiableList(segmentIds);
    }

    /** @return the segments left out */
    public List<Integer> getExcludeSegmentIds() {
        return excludeSegmentIds == null ? Collections.emptyList() : Collections.unmodifiableList(excludeSegmentIds);
    }

    /** @return the web version's URL */
    public String getWebVersion() {
        return webVersion;
    }

    /** @return the HTML body, only with includeContent; otherwise null */
    public String getHtmlText() {
        return htmlText;
    }

    /** @return the plain-text body, only with includeContent; otherwise null */
    public String getPlainText() {
        return plainText;
    }
}
