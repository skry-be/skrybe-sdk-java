package com.skrybe.sdk.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skrybe.sdk.internal.Json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base of every object the API returns. Fields the SDK does not model yet are
 * kept in {@link #getExtra()} rather than dropped, so a field added server-side
 * is reachable before the SDK catches up.
 */
public abstract class ApiObject {
    @JsonIgnore
    private Map<String, Object> extra;

    ApiObject() {
    }

    /** @return fields the API sent that this class has no getter for, by their snake_case names */
    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra == null ? Collections.emptyMap() : Collections.unmodifiableMap(extra);
    }

    @JsonAnySetter
    void putExtra(String name, Object value) {
        if (extra == null) {
            extra = new LinkedHashMap<>();
        }
        extra.put(name, value);
    }

    /** @return the object as JSON, with the API's field names */
    @Override
    public String toString() {
        return Json.write(this);
    }
}
