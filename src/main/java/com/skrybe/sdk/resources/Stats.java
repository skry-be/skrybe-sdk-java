package com.skrybe.sdk.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.internal.Transport;

/** Install-wide statistics. */
public final class Stats {
    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.stats()}.
     *
     * @param transport the client's transport
     */
    public Stats(Transport transport) {
        this.transport = transport;
    }

    /**
     * Emails sent across the whole installation (campaigns, autoresponders and
     * transactional), not a per-brand figure. Public, so sent without the API
     * key, and cached for 5 minutes.
     */
    public long totalEmailsSent() {
        JsonNode data = transport.json("api/stats/emails-sent.php", null, "GET", Transport.Encoding.FORM, true, true);
        JsonNode total = data.get("total_emails_sent");
        return total == null || total.isNull() ? 0 : total.asLong();
    }
}
