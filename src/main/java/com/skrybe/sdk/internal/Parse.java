package com.skrybe.sdk.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skrybe.sdk.exception.ApiException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsers for the API's response bodies.
 *
 * <p>{@code api/lists/get-lists.php} and {@code api/brands/get-brands.php}
 * build their payload by string concatenation:
 *
 * <pre>
 * $output .= '"list'.$i.'": { "id": "'.encrypt_val($id).'", "name": "'.$name.'" },'
 * </pre>
 *
 * so entries are keyed {@code list1}, {@code list2}, ... rather than being an
 * array, and the name is NOT escaped: a list called {@code Q4 "Big" Push}
 * emits invalid JSON. The id is safe to anchor on: encrypt_val() base64s and
 * then substitutes {@code /} and {@code +} and strips {@code =}, so it never
 * contains a quote.
 *
 * <p>Internal: not part of the SDK's public API.
 */
public final class Parse {
    private Parse() {
    }

    /** An {@code {id, name}} pair from a numbered payload. */
    public static final class NamedEntity {
        public final String id;
        public final String name;

        public NamedEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /**
     * Parse a {@code {"list1": {...}, "list2": {...}}} payload into an ordered
     * list.
     *
     * @param prefix the key prefix the endpoint uses: {@code list} or {@code brand}
     * @throws ApiException if the body is neither valid JSON nor structurally recognisable
     */
    public static List<NamedEntity> numberedObject(String body, String prefix) {
        String trimmed = Text.trim(body);
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }

        // Happy path, and the path this takes once the endpoints use json_encode().
        JsonNode parsed = Json.tryParse(trimmed);
        if (parsed instanceof ObjectNode) {
            List<long[]> order = new ArrayList<>();
            List<NamedEntity> rows = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = parsed.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                if (!key.startsWith(prefix) || !value.isObject()) {
                    continue;
                }
                String suffix = key.substring(prefix.length());
                long sort = suffix.matches("\\d{1,18}") ? Long.parseLong(suffix) : rows.size();
                order.add(new long[] {sort, rows.size()});
                rows.add(new NamedEntity(scalar(value.get("id")), scalar(value.get("name"))));
            }
            // PHP emits list1..listN in order already; this is belt-and-braces
            // for an endpoint that ever reorders them (list10 sorts before list2
            // as a string). The sort is stable.
            order.sort(Comparator.comparingLong(o -> o[0]));
            List<NamedEntity> sorted = new ArrayList<>(rows.size());
            for (long[] o : order) {
                sorted.add(rows.get((int) o[1]));
            }
            return sorted;
        }

        // Almost certainly an unescaped quote in a name.
        List<NamedEntity> recovered = extractByStructure(trimmed, prefix);
        if (!recovered.isEmpty()) {
            return recovered;
        }

        // The body is the whole diagnosis here: usually a PHP warning or an
        // interstitial from something in front of the install.
        throw new ApiException(
                "malformed_response",
                "Could not parse the " + prefix + " payload returned by the API.",
                "The server sent: " + Text.excerpt(trimmed),
                null,
                body,
                null);
    }

    /**
     * Structural fallback for payloads a JSON parser rejects. The trailing
     * lookahead is what lets an embedded quote survive: the lazy name match can
     * only stop at a {@code "} followed by the entry's closing brace AND either
     * the next numbered key or the end of the object.
     */
    private static List<NamedEntity> extractByStructure(String body, String prefix) {
        String p = Pattern.quote(prefix);
        Pattern entry = Pattern.compile(
                "\"" + p + "\\d+\"\\s*:\\s*\\{\\s*"
                        + "\"id\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*"
                        + "\"name\"\\s*:\\s*\"([\\s\\S]*?)\"\\s*\\}"
                        + "(?=\\s*,\\s*\"" + p + "\\d+\"\\s*:|\\s*\\}\\s*\\z)");
        List<NamedEntity> out = new ArrayList<>();
        Matcher m = entry.matcher(body);
        while (m.find()) {
            out.add(new NamedEntity(m.group(1), m.group(2)));
        }
        return out;
    }

    /**
     * Parse a numbered payload from an endpoint that builds it with
     * json_encode() ({@code api/campaigns/get-campaigns.php}), so rows are kept
     * whole, in the order of their numeric suffix.
     */
    public static List<JsonNode> numberedRecords(String body, String prefix) {
        ObjectNode parsed = jsonObject(body, prefix + " payload");
        Pattern key = Pattern.compile("^" + Pattern.quote(prefix) + "(\\d{1,18})$");
        List<Map.Entry<Long, JsonNode>> rows = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = parsed.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            Matcher m = key.matcher(field.getKey());
            if (m.matches() && field.getValue().isObject()) {
                rows.add(Map.entry(Long.parseLong(m.group(1)), field.getValue()));
            }
        }
        rows.sort(Map.Entry.comparingByKey());
        List<JsonNode> out = new ArrayList<>(rows.size());
        for (Map.Entry<Long, JsonNode> row : rows) {
            out.add(row.getValue());
        }
        return out;
    }

    /**
     * Parse a body that must be a JSON object, e.g. {@code api/campaigns/stats.php}.
     * Anything else is almost always a PHP warning or a proxy page, so the body
     * itself goes in the hint.
     *
     * @param what what the body was meant to be, for the message
     */
    public static ObjectNode jsonObject(String body, String what) {
        String trimmed = Text.trim(body);
        JsonNode parsed = Json.tryParse(trimmed);
        if (parsed instanceof ObjectNode) {
            return (ObjectNode) parsed;
        }
        throw malformed(what, body, null);
    }

    /** Parse a body that must be a JSON object and bind it to a model class. */
    public static <T> T jsonObject(String body, String what, Class<T> type) {
        return bind(jsonObject(body, what), what, body, type);
    }

    /** Bind an already-parsed node to a model class. */
    public static <T> T bind(JsonNode node, String what, String body, Class<T> type) {
        try {
            return Json.MAPPER.treeToValue(node, type);
        } catch (Exception e) {
            throw malformed(what, body, e);
        }
    }

    private static ApiException malformed(String what, String body, Throwable cause) {
        return new ApiException(
                "malformed_response",
                "Could not parse the " + what + " returned by the API.",
                "The server sent: " + Text.excerpt(Text.trim(body)),
                null,
                body,
                cause);
    }

    /** JavaScript's {@code String(value ?? '')} for a JSON scalar. */
    private static String scalar(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        return node.isValueNode() ? node.asText() : node.toString();
    }
}
