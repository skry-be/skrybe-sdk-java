package com.skrybe.sdk.resources;

import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Json;
import com.skrybe.sdk.internal.Parse;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.Brand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Brands. */
public final class Brands {
    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.brands()}.
     *
     * @param transport the client's transport
     */
    public Brands(Transport transport) {
        this.transport = transport;
    }

    /** @return the key's own brand: a brand key gets exactly one row. Empty, not an error, when there is none. */
    public List<Brand> list() {
        String body = transport.text("api/brands/get-brands.php", new Fields(), true);
        if (Errors.isProseEmpty(body)) {
            return new ArrayList<>();
        }
        List<Brand> out = new ArrayList<>();
        for (Parse.NamedEntity row : Parse.numberedObject(body, "brand")) {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("id", row.id);
            fields.put("name", row.name);
            out.add(Json.MAPPER.convertValue(fields, Brand.class));
        }
        return out;
    }
}
