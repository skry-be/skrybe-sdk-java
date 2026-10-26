package com.skrybe.sdk.resources;

import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Parse;
import com.skrybe.sdk.internal.Text;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.SubscribeOutcome;
import com.skrybe.sdk.model.Subscriber;
import com.skrybe.sdk.model.SubscribersPage;
import com.skrybe.sdk.params.ListSubscribersParams;
import com.skrybe.sdk.params.SubscribeParams;
import com.skrybe.sdk.params.SubscriberStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Subscribers of a list. */
public final class Subscribers {
    /** The largest page {@code list()} returns. */
    public static final int MAX_PAGE = 1000;

    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.subscribers()}.
     *
     * @param transport the client's transport
     */
    public Subscribers(Transport transport) {
        this.transport = transport;
    }

    /**
     * Adds an address to a list. Posting one that is already there updates its
     * name and custom fields, and returns {@code ALREADY_SUBSCRIBED} rather
     * than throwing, so re-running an import is safe.
     */
    public SubscribeOutcome subscribe(SubscribeParams params) {
        Errors.requireParams("params", params);
        Errors.requireParams("listId", params.getListId(), "email", params.getEmail());

        Fields body = new Fields();
        if (params.getFields() != null) {
            // Spaces are taken out, as the server matches on.
            for (Map.Entry<String, String> field : params.getFields().entrySet()) {
                body.with(field.getKey().replace(" ", ""), field.getValue());
            }
        }
        // Written after the custom fields so the documented names win a collision.
        body.with("email", params.getEmail());
        body.with("list", params.getListId());
        // Switches subscribe.php from rendering an HTML page to answering in plain text.
        body.with("boolean", "true");
        body.with("name", params.getName());
        body.with("country", params.getCountry());
        body.with("ipaddress", params.getIpAddress());
        body.with("referrer", params.getReferrer());
        if (params.isGdpr()) {
            body.with("gdpr", "true");
        }
        if (params.isSilent()) {
            body.with("silent", "true");
        }

        // At the install root, not under api/.
        String text = transport.text("subscribe", body, false);
        if (Text.trim(text).toLowerCase(Locale.ROOT).equals("already subscribed.")) {
            return SubscribeOutcome.ALREADY_SUBSCRIBED;
        }
        if (Errors.isProseSuccess(text)) {
            return SubscribeOutcome.SUBSCRIBED;
        }
        throw Errors.unexpectedResponse("subscribe", text);
    }

    /** Marks the address unsubscribed from the list. */
    public void unsubscribe(String listId, String email) {
        Errors.requireParams("listId", listId, "email", email);
        String text = transport.text("unsubscribe", new Fields()
                .with("email", email)
                .with("list", listId)
                .with("boolean", "true"), false);
        if (!Errors.isProseSuccess(text)) {
            throw Errors.unexpectedResponse("unsubscribe", text);
        }
    }

    /** Removes the subscriber outright, rather than marking it unsubscribed. */
    public void delete(String listId, String email) {
        Errors.requireParams("listId", listId, "email", email);
        String text = transport.text("api/subscribers/delete.php", new Fields()
                .with("list_id", listId)
                .with("email", email), false);
        if (!Errors.isProseSuccess(text)) {
            throw Errors.unexpectedResponse("delete", text);
        }
    }

    /**
     * One of Subscribed, Unsubscribed, Unconfirmed, Bounced, Soft bounced or
     * Complained. Returned as given, so a status added server-side surfaces
     * rather than being rejected.
     */
    public String status(String listId, String email) {
        Errors.requireParams("listId", listId, "email", email);
        String text = transport.text("api/subscribers/subscription-status.php", new Fields()
                .with("email", email)
                .with("list_id", listId), true);
        String status = Text.trim(text);
        if (status.isEmpty()) {
            throw Errors.unexpectedResponse("subscription-status", text);
        }
        return status;
    }

    /** @return the first page of the list's subscribers, oldest first */
    public SubscribersPage list(String listId) {
        return list(listId, ListSubscribersParams.builder().build());
    }

    /** @return one page of the list's subscribers, oldest first */
    public SubscribersPage list(String listId, ListSubscribersParams params) {
        Errors.requireParams("listId", listId);
        ListSubscribersParams p = params == null ? ListSubscribersParams.builder().build() : params;
        String body = transport.text("api/subscribers/get-subscribers.php", new Fields()
                .with("list_id", listId)
                .with("status", p.getStatus())
                .with("page", p.getPage())
                .with("limit", p.getLimit()), true);
        return Parse.jsonObject(body, "subscribers page", SubscribersPage.class);
    }

    /** @return every subscriber of the list, fetched page by page */
    public List<Subscriber> listAll(String listId) {
        return listAll(listId, null);
    }

    /**
     * Every subscriber with the status (or every subscriber, for null), at the
     * largest page size. Oldest first, so someone joining mid-way lands on a
     * later page instead of shifting the ones already read.
     */
    public List<Subscriber> listAll(String listId, SubscriberStatus status) {
        List<Subscriber> all = new ArrayList<>();
        for (int page = 1; ; page++) {
            SubscribersPage r = list(listId, ListSubscribersParams.builder().status(status).page(page).limit(MAX_PAGE).build());
            all.addAll(r.getSubscribers());
            if (r.getSubscribers().size() < MAX_PAGE) {
                return all;
            }
        }
    }
}
