package com.skrybe.sdk.params;

import java.util.Locale;

/** The editor the Skrybe dashboard opens a template in. */
public enum TemplateEditor implements ApiValue {
    HTML,
    DRAGDROP;

    @Override
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
