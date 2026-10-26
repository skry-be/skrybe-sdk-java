package com.skrybe.sdk.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Parse;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.Autoresponder;
import com.skrybe.sdk.model.AutoresponderEmail;
import com.skrybe.sdk.model.AutoresponderEmailStats;
import com.skrybe.sdk.model.AutoresponderWithEmails;
import com.skrybe.sdk.params.CreateAutoresponderEmailParams;
import com.skrybe.sdk.params.CreateAutoresponderParams;

import java.util.ArrayList;
import java.util.List;

/** Autoresponders: drip sequences, and annual or one-off sends on a date field. */
public final class Autoresponders {
    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.autoresponders()}.
     *
     * @param transport the client's transport
     */
    public Autoresponders(Transport transport) {
        this.transport = transport;
    }

    /** @return the brand's autoresponders */
    public List<Autoresponder> list() {
        return list(null);
    }

    /** @return one list's autoresponders, or the brand's for null */
    public List<Autoresponder> list(String listId) {
        String body = transport.text("api/autoresponders/get-autoresponders.php", new Fields().with("list_id", listId), true);
        JsonNode rows = Parse.jsonObject(body, "autoresponders").path("autoresponders");
        List<Autoresponder> out = new ArrayList<>();
        for (JsonNode row : rows) {
            out.add(Parse.bind(row, "autoresponders", body, Autoresponder.class));
        }
        return out;
    }

    /** @return one autoresponder with its emails */
    public AutoresponderWithEmails get(int autoresponderId) {
        String body = transport.text("api/autoresponders/get-autoresponder-emails.php", new Fields()
                .with("autoresponder_id", autoresponderId), true);
        return Parse.jsonObject(body, "autoresponder", AutoresponderWithEmails.class);
    }

    /** @return one autoresponder email's opens, clicks, bounces and the rest */
    public AutoresponderEmailStats emailStats(int emailId) {
        String body = transport.text("api/autoresponders/autoresponder-stats.php", new Fields().with("email_id", emailId), true);
        return Parse.jsonObject(body, "autoresponder email stats", AutoresponderEmailStats.class);
    }

    /** Creates an autoresponder with no emails; add them with {@link #createEmail}. Not retried. */
    public Autoresponder create(CreateAutoresponderParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams("name", params.getName(), "type", params.getType(), "listId", params.getListId());
        String body = transport.text("api/autoresponders/create-autoresponder.php", new Fields()
                .with("name", params.getName())
                .with("type", params.getType())
                .with("list_id", params.getListId())
                .with("custom_field", params.getCustomField()), false);
        return Parse.jsonObject(body, "autoresponder", Autoresponder.class);
    }

    /** Adds an email to an autoresponder. Not retried: a repeat would add it twice. */
    public AutoresponderEmail createEmail(int autoresponderId, CreateAutoresponderEmailParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams("subject", params.getSubject(), "htmlText", params.getHtmlText());
        String body = transport.text("api/autoresponders/create-autoresponder-email.php", new Fields()
                .with("autoresponder_id", autoresponderId)
                .with("subject", params.getSubject())
                .with("html_text", params.getHtmlText())
                .with("plain_text", params.getPlainText())
                .with("preheader", params.getPreheader())
                .with("when", params.getWhen())
                .with("from_name", params.getFromName())
                .with("from_email", params.getFromEmail())
                .with("reply_to", params.getReplyTo())
                .with("query_string", params.getQueryString())
                .with("track_opens", params.getTrackOpens())
                .with("track_clicks", params.getTrackClicks())
                .with("enabled", Fields.flag(params.isEnabled())), false);
        return Parse.jsonObject(body, "autoresponder email", AutoresponderEmail.class);
    }
}
