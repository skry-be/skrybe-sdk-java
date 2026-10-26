package com.skrybe.sdk.internal;

import java.util.regex.Pattern;

/**
 * String helpers with JavaScript's whitespace rules, so prose matching and
 * excerpts behave exactly as in {@code @skrybe/sdk}: {@link String#trim()}
 * misses no-break and other Unicode spaces that a PHP page can carry.
 *
 * <p>Internal: not part of the SDK's public API.
 */
public final class Text {
    private Text() {
    }

    /** JavaScript's {@code \s}: ECMAScript WhiteSpace plus LineTerminator. */
    private static final String WS = "[\\t\\n\\u000B\\f\\r \\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000\\uFEFF]";
    private static final Pattern LEADING = Pattern.compile("^" + WS + "+");
    private static final Pattern TRAILING = Pattern.compile(WS + "+$");
    private static final Pattern RUNS = Pattern.compile(WS + "+");

    /** The default excerpt length. */
    public static final int EXCERPT_LIMIT = 200;

    /** @return the string with leading and trailing whitespace removed, as JavaScript's trim() does */
    public static String trim(String s) {
        if (s == null) {
            return "";
        }
        return TRAILING.matcher(LEADING.matcher(s).replaceFirst("")).replaceFirst("");
    }

    /** @return an excerpt of at most {@link #EXCERPT_LIMIT} characters */
    public static String excerpt(String body) {
        return excerpt(body, EXCERPT_LIMIT);
    }

    /**
     * Render an unexpected response body for an error message: whitespace
     * collapsed so a multi-line PHP warning stays on one line, and truncated so
     * a stray HTML page does not swamp a log. The untruncated body stays on
     * {@code SkrybeException.getRaw()}.
     *
     * @return the excerpt, or "(an empty response)"
     */
    public static String excerpt(String body, int limit) {
        String flat = trim(RUNS.matcher(body == null ? "" : body).replaceAll(" "));
        if (flat.isEmpty()) {
            return "(an empty response)";
        }
        return flat.length() > limit ? flat.substring(0, limit) + "\u2026" : flat;
    }
}
