package com.skrybe.sdk.internal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * The SDK's one Jackson mapper. Models are read field by field with the API's
 * snake_case names, and unknown fields are kept rather than rejected so a field
 * added server-side never breaks an older SDK.
 *
 * <p>Internal: not part of the SDK's public API.
 */
public final class Json {
    private Json() {
    }

    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // JSON.parse rejects trailing garbage; so do we.
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            // PHP's json_encode() writes an empty map as [].
            .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    /** @return the parsed tree, or null when the text is not exactly one JSON value */
    public static JsonNode tryParse(String text) {
        if (text == null) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(text);
            return node == null || node.isMissingNode() ? null : node;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /** @return the value as compact JSON, as JavaScript's JSON.stringify writes it */
    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not encode a value as JSON: " + e.getOriginalMessage(), e);
        }
    }
}
