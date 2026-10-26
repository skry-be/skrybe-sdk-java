package com.skrybe.sdk.internal;

import com.skrybe.sdk.params.ApiValue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.StringJoiner;

/**
 * Request fields in the order they are sent. Null values are left out of the
 * request, so an optional parameter that was not given is not sent at all.
 *
 * <p>Internal: not part of the SDK's public API.
 */
public final class Fields extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * Put a field, returning this for chaining. A null value is not sent, and an
     * {@link ApiValue} is sent as the API spells it.
     */
    public Fields with(String name, Object value) {
        put(name, value instanceof ApiValue ? ((ApiValue) value).value() : value);
        return this;
    }

    /** @return the ids comma-joined, or null for an empty or missing list so the field is not sent */
    public static String joinIds(Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        StringJoiner joined = new StringJoiner(",");
        for (Object id : ids) {
            joined.add(String.valueOf(id));
        }
        return joined.toString();
    }

    /** @return 1 when set, so a false flag leaves the field out rather than sending 0 */
    public static Integer flag(boolean value) {
        return value ? 1 : null;
    }
}
