package com.skrybe.sdk.resources;

import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Json;
import com.skrybe.sdk.internal.Parse;
import com.skrybe.sdk.internal.Text;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.SkrybeList;
import com.skrybe.sdk.model.SkrybeListDetail;
import com.skrybe.sdk.params.CustomFieldType;
import com.skrybe.sdk.params.OptIn;
import com.skrybe.sdk.params.UpdateListParams;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists and their custom fields. A list id is the encrypted value
 * {@link #list()} returns, not the number in the dashboard URL.
 */
public final class Lists {
    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.lists()}.
     *
     * @param transport the client's transport
     */
    public Lists(Transport transport) {
        this.transport = transport;
    }

    /** @return the brand's lists, without hidden ones. An empty brand is an empty list, not an error. */
    public List<SkrybeList> list() {
        return list(false);
    }

    /** @return the brand's lists, with hidden ones when includeHidden is true */
    public List<SkrybeList> list(boolean includeHidden) {
        String body = transport.text("api/lists/get-lists.php", new Fields()
                .with("include_hidden", includeHidden ? "yes" : "no"), true);
        if (Errors.isProseEmpty(body)) {
            return new ArrayList<>();
        }
        List<SkrybeList> out = new ArrayList<>();
        for (Parse.NamedEntity row : Parse.numberedObject(body, "list")) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", row.id);
            fields.put("name", row.name);
            out.add(Json.MAPPER.convertValue(fields, SkrybeList.class));
        }
        return out;
    }

    /** @return the list with its opt-in setting, subscriber counts and custom fields */
    public SkrybeListDetail get(String listId) {
        Errors.requireParams("listId", listId);
        String body = transport.text("api/lists/get-list.php", new Fields().with("list_id", listId), true);
        return Parse.jsonObject(body, "list", SkrybeListDetail.class);
    }

    /** Creates a list with the server's default opt-in. Not retried: a repeat would create a second list. */
    public SkrybeListDetail create(String name) {
        return create(name, null);
    }

    /** Creates a list. Not retried: a repeat after a lost response would create a second list. */
    public SkrybeListDetail create(String name, OptIn optIn) {
        Errors.requireParams("name", name);
        String body = transport.text("api/lists/create.php", new Fields()
                .with("name", name)
                .with("opt_in", optIn), false);
        return Parse.jsonObject(body, "list", SkrybeListDetail.class);
    }

    /** Renames the list or changes its opt-in. Only the fields set change. */
    public SkrybeListDetail update(String listId, UpdateListParams changes) {
        Errors.requireParams("listId", listId, "changes", changes);
        String body = transport.text("api/lists/update-list.php", new Fields()
                .with("list_id", listId)
                .with("name", changes.getName())
                .with("opt_in", changes.getOptIn()), false);
        return Parse.jsonObject(body, "list", SkrybeListDetail.class);
    }

    /**
     * Deletes the list with its subscribers, segments, autoresponders and
     * rules. Refused while a scheduled or sending campaign is going to it.
     */
    public void delete(String listId) {
        Errors.requireParams("listId", listId);
        String body = transport.text("api/lists/delete.php", new Fields().with("list_id", listId), false);
        Parse.jsonObject(body, "delete result");
    }

    /**
     * Subscribers a campaign to this list would reach.
     *
     * <p>Anything but a bare integer is a failure and is thrown, never reported
     * as 0: "this list is empty" is the one wrong answer a caller cannot detect.
     */
    public long activeSubscriberCount(String listId) {
        Errors.requireParams("listId", listId);
        String body = transport.text("api/subscribers/active-subscriber-count.php", new Fields().with("list_id", listId), true);
        String trimmed = Text.trim(body);
        if (!trimmed.matches("\\d{1,18}")) {
            throw new ApiException(
                    "malformed_response",
                    "Expected a subscriber count for list " + listId + ", got: " + Text.excerpt(trimmed),
                    null, null, body, null);
        }
        return Long.parseLong(trimmed);
    }

    /** Adds a field of the server's default type, text; every subscriber gets an empty value for it. */
    public SkrybeListDetail addCustomField(String listId, String name) {
        return addCustomField(listId, name, null);
    }

    /**
     * Adds a field; every subscriber gets an empty value for it.
     *
     * @return the list with its fields
     */
    public SkrybeListDetail addCustomField(String listId, String name, CustomFieldType type) {
        Errors.requireParams("listId", listId, "name", name);
        String body = transport.text("api/lists/add-custom-field.php", new Fields()
                .with("list_id", listId)
                .with("name", name)
                .with("type", type), false);
        return Parse.jsonObject(body, "list", SkrybeListDetail.class);
    }

    /**
     * Renames a field, keeping subscribers' values. The autoresponders and
     * segment conditions that use it follow; tags already in campaigns keep the
     * old name.
     */
    public SkrybeListDetail renameCustomField(String listId, String name, String newName) {
        Errors.requireParams("listId", listId, "name", name, "newName", newName);
        String body = transport.text("api/lists/rename-custom-field.php", new Fields()
                .with("list_id", listId)
                .with("name", name)
                .with("new_name", newName), false);
        return Parse.jsonObject(body, "list", SkrybeListDetail.class);
    }

    /** Deletes a field and every subscriber's value for it. Refused while an autoresponder or segment uses it. */
    public SkrybeListDetail deleteCustomField(String listId, String name) {
        Errors.requireParams("listId", listId, "name", name);
        String body = transport.text("api/lists/delete-custom-field.php", new Fields()
                .with("list_id", listId)
                .with("name", name), false);
        return Parse.jsonObject(body, "list", SkrybeListDetail.class);
    }
}
