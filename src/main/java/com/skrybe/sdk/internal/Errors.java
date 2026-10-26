package com.skrybe.sdk.internal;

import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.exception.AuthenticationException;
import com.skrybe.sdk.exception.ConnectionException;
import com.skrybe.sdk.exception.InvalidRequestException;
import com.skrybe.sdk.exception.RateLimitException;
import com.skrybe.sdk.exception.SkrybeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns the API's prose answers into typed exceptions.
 *
 * <p>Only api/emails/send.php, api/emails/send-transactional.php and
 * api/stats/emails-sent.php use real status codes; everything else answers
 * HTTP 200 with a sentence. Kept in step with {@code @skrybe/sdk}'s
 * src/errors.ts, skrybe/sdk-php's src/Internal/Errors.php and skrybe-cli's
 * src/api/errors.ts.
 *
 * <p>Internal: not part of the SDK's public API.
 */
public final class Errors {
    private Errors() {
    }

    /** One recognised failure: its code, its type and, when known, a hint. */
    static final class Rule {
        final String code;
        final String type;
        final String hint;

        Rule(String code, String type, String hint) {
            this.code = code;
            this.type = type;
            this.hint = hint;
        }
    }

    private static final String AUTHENTICATION = "authentication";
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String RATE_LIMIT = "rate_limit";
    private static final String API = "api";

    private static final String LIST_IDS_HINT = "Call skrybe.lists().list() to see the IDs available to this API key.";
    private static final String UNDER_REVIEW_HINT =
            "Sending is paused while the brand is reviewed for a high bounce rate. Contact Skrybe support.";
    private static final String DOMAIN_HINT = "Verify the From domain for this brand in the Skrybe dashboard.";

    /**
     * Prose returned by the legacy endpoints, keyed by the lowercased trimmed
     * body.
     */
    static final Map<String, Rule> PROSE;

    /** Prose with a variable part, matched by pattern after the exact table. */
    static final List<Map.Entry<Pattern, Rule>> PROSE_PATTERNS;

    static {
        Map<String, Rule> p = new LinkedHashMap<>();

        // Authentication
        rule(p, "api key not passed", "api_key_missing", AUTHENTICATION, null);
        rule(p, "invalid api key", "invalid_api_key", AUTHENTICATION,
                "Generate a key in the Skrybe dashboard under Settings. Keys are cleared for every login of a brand placed under review.");

        // Request shape
        rule(p, "no data passed", "no_data_passed", INVALID_REQUEST, null);
        rule(p, "some fields are missing.", "missing_fields", INVALID_REQUEST, null);

        // Brands and lists
        rule(p, "brand id not passed", "brand_id_missing", INVALID_REQUEST, null);
        // Also the answer to the install's main account key, which no brand is tied to.
        rule(p, "brand does not exist", "brand_not_found", API,
                "Use an API key from a brand login; the main account key is not tied to a brand.");
        rule(p, "list does not exist", "list_not_found", API, LIST_IDS_HINT);
        rule(p, "invalid list id.", "list_not_found", API, LIST_IDS_HINT);
        // Also what the server produces from an id it cannot decrypt, so this is
        // the answer to a malformed id as much as to a missing one.
        rule(p, "list id not passed", "list_id_invalid", INVALID_REQUEST,
                "A list ID is the encrypted value skrybe.lists().list() returns, not the number in the dashboard URL.");

        // Autoresponders
        rule(p, "autoresponder id not passed", "autoresponder_id_missing", INVALID_REQUEST, null);
        rule(p, "autoresponder does not exist", "autoresponder_not_found", API, null);
        rule(p, "email id not passed", "email_id_missing", INVALID_REQUEST, null);
        rule(p, "autoresponder email does not exist", "autoresponder_email_not_found", API, null);
        rule(p, "autoresponder name not passed", "autoresponder_name_missing", INVALID_REQUEST, null);
        rule(p, "type must be drip, annually or on_date", "invalid_autoresponder_type", INVALID_REQUEST, null);
        rule(p, "list already has a drip autoresponder", "drip_exists", API,
                "A list can have one drip autoresponder. Add emails to the existing one.");
        rule(p, "custom_field not passed", "custom_field_missing", INVALID_REQUEST,
                "annually and on_date autoresponders send on a Date custom field of the list.");
        rule(p, "field is not a date field", "field_not_date", INVALID_REQUEST, null);
        rule(p, "field already has an autoresponder", "field_has_autoresponder", API, null);
        rule(p, "subject not passed", "subject_missing", INVALID_REQUEST, null);
        rule(p, "a drip email cannot go before signup", "invalid_when", INVALID_REQUEST, null);
        rule(p, "when must be immediately, or a delay such as +3 days", "invalid_when", INVALID_REQUEST, null);
        rule(p, "when must be on, or an offset such as +9 hours or -1 days", "invalid_when", INVALID_REQUEST, null);

        // Templates
        rule(p, "template id not passed", "template_id_missing", INVALID_REQUEST, null);
        rule(p, "template does not exist", "template_not_found", API, null);
        rule(p, "template name not passed", "template_name_missing", INVALID_REQUEST, null);
        rule(p, "template name cannot be empty", "template_name_missing", INVALID_REQUEST, null);
        rule(p, "html not passed", "html_missing", INVALID_REQUEST, null);
        rule(p, "editor must be html or dragdrop", "invalid_editor", INVALID_REQUEST, null);

        // List management
        rule(p, "list name not passed", "list_name_missing", INVALID_REQUEST, null);
        rule(p, "list name is too long", "list_name_too_long", INVALID_REQUEST,
                "A list name can be at most 100 characters.");
        rule(p, "opt_in must be single or double", "invalid_opt_in", INVALID_REQUEST, null);
        rule(p, "nothing to update", "nothing_to_update", INVALID_REQUEST, "Pass at least one field to change.");
        rule(p, "list is used by a scheduled or sending campaign", "list_in_use", API,
                "Unschedule the campaign, or wait for it to finish sending, then delete the list.");

        // Custom fields
        rule(p, "field name not passed", "field_name_missing", INVALID_REQUEST, null);
        rule(p, "invalid field name", "invalid_field_name", INVALID_REQUEST,
                "Up to 100 characters, without : % [ ] , \" < or >.");
        rule(p, "name and email are built-in fields", "reserved_field_name", INVALID_REQUEST, null);
        rule(p, "type must be text or date", "invalid_field_type", INVALID_REQUEST, null);
        rule(p, "field already exists", "field_exists", API, "Field names are case-insensitive.");
        rule(p, "field does not exist", "field_not_found", API, null);
        rule(p, "field is used by an autoresponder", "field_in_use", API,
                "Change the autoresponder in the Skrybe dashboard first.");
        rule(p, "field is used by a segment", "field_in_use", API,
                "Remove it from the segment in the Skrybe dashboard first.");
        rule(p, "list was changed by another request. try again.", "conflict", API, null);

        // Campaigns
        rule(p, "campaign id not passed", "campaign_id_missing", INVALID_REQUEST, null);
        rule(p, "campaign does not exist", "campaign_not_found", API, null);
        rule(p, "invalid status", "invalid_status", INVALID_REQUEST, null);
        rule(p, "invalid page", "invalid_page", INVALID_REQUEST, null);
        rule(p, "invalid limit", "invalid_limit", INVALID_REQUEST, null);
        rule(p, "invalid type", "invalid_type", INVALID_REQUEST, null);

        // Sending a campaign
        rule(p, "list or segment id(s) not passed", "recipients_missing", INVALID_REQUEST,
                "Pass at least one list or segment ID.");
        rule(p, "one or more list ids are invalid", "list_not_found", API, LIST_IDS_HINT);
        rule(p, "one or more segment ids are invalid", "segment_not_found", API, null);
        rule(p, "campaign has already been sent", "campaign_not_draft", API,
                "It has already started sending, so it can no longer be sent, scheduled or edited.");
        rule(p, "campaign is scheduled", "campaign_scheduled", API,
                "Unschedule it first, or leave it to send at its scheduled time.");
        rule(p, "campaign is not scheduled", "campaign_not_scheduled", API, null);

        // Editing and deleting campaigns
        rule(p, "campaign is sending", "campaign_sending", API,
                "Stop it in the Skrybe dashboard first, or wait for it to finish.");
        rule(p, "subject cannot be empty", "subject_empty", INVALID_REQUEST, null);
        rule(p, "html cannot be empty", "html_empty", INVALID_REQUEST, null);
        rule(p, "invalid from_email", "invalid_from_email", INVALID_REQUEST, null);
        rule(p, "invalid reply_to", "invalid_reply_to", INVALID_REQUEST, null);
        rule(p, "track_opens and track_clicks must be 0, 1 or 2", "invalid_tracking", INVALID_REQUEST,
                "0 off, 1 on, 2 anonymous.");
        rule(p, "campaign was changed by another request. try again.", "conflict", API, null);

        // Scheduling
        rule(p, "schedule_date_time not passed", "schedule_missing", INVALID_REQUEST, null);
        rule(p, "schedule_date_time is invalid", "invalid_schedule", INVALID_REQUEST,
                "Use a date and time such as \"2027-06-15 18:05\" or \"June 15, 2027 6:05pm\".");
        rule(p, "schedule_date_time is in the past", "schedule_in_past", INVALID_REQUEST,
                "The time is read in the timezone given, or the account's timezone if none is.");
        rule(p, "schedule_timezone is invalid", "invalid_timezone", INVALID_REQUEST,
                "Use an IANA name such as Africa/Lagos or America/New_York.");

        // Test sends
        rule(p, "email addresses not passed", "emails_missing", INVALID_REQUEST, null);
        rule(p, "too many email addresses", "too_many_emails", INVALID_REQUEST,
                "A test send goes to at most 5 addresses.");
        rule(p, "quota exceeded. please upgrade your plan.", "quota_exceeded", RATE_LIMIT, null);
        rule(p, "no active subscribers to send to", "no_recipients", API,
                "Every subscriber on those lists is unsubscribed, bounced, unconfirmed or excluded.");

        // Subscribers
        rule(p, "subscriber does not exist", "subscriber_not_found", API, null);
        rule(p, "email does not exist in list", "subscriber_not_found", API, null);
        rule(p, "email not passed", "email_missing", INVALID_REQUEST, null);
        rule(p, "email address not passed", "email_missing", INVALID_REQUEST, null);
        rule(p, "invalid email address.", "invalid_email", INVALID_REQUEST, null);
        rule(p, "bounced email address.", "bounced_email", API, null);
        rule(p, "email is suppressed.", "suppressed_email", API, null);
        // unsubscribe's wording for an address that is not on the list.
        rule(p, "email does not exist.", "subscriber_not_found", API, null);
        rule(p, "ip address is invalid.", "invalid_ip_address", INVALID_REQUEST, null);
        rule(p, "country must be a valid 2 letter country code", "invalid_country", INVALID_REQUEST,
                "Use a two-letter ISO 3166-1 code, e.g. NG.");
        rule(p, "referrer is not a valid url", "invalid_referrer", INVALID_REQUEST, null);
        rule(p, "consent not given.", "consent_not_given", INVALID_REQUEST,
                "The list requires GDPR consent; pass gdpr(true).");
        rule(p, "failed recaptcha test.", "recaptcha_failed", API,
                "The list has reCAPTCHA enabled on its subscribe form, which an API call cannot satisfy.");

        // Sending
        rule(p, "unauthorised from email domain", "unauthorised_from_domain", API,
                "The From domain must be verified for this brand, or listed in its allowed domains.");

        PROSE = Collections.unmodifiableMap(p);

        List<Map.Entry<Pattern, Rule>> patterns = new ArrayList<>();
        pattern(patterns, "^error: this campaign would exceed", "quota_exceeded", RATE_LIMIT, null);
        pattern(patterns, "^account under review:", "account_under_review", API, UNDER_REVIEW_HINT);
        pattern(patterns, "^brand is under review", "account_under_review", API, UNDER_REVIEW_HINT);
        pattern(patterns, "^a field is too long", "field_too_long", INVALID_REQUEST, null);
        pattern(patterns, "^test send rate limit reached", "rate_limited", RATE_LIMIT,
                "Test sends are limited to 20 an hour per brand, shared with the Skrybe dashboard.");
        pattern(patterns, "^domain not verified in ses", "domain_not_verified", API, DOMAIN_HINT);
        pattern(patterns, "^domain \".*\" is not verified", "domain_not_verified", API, DOMAIN_HINT);
        PROSE_PATTERNS = Collections.unmodifiableList(patterns);
    }

    private static void rule(Map<String, Rule> table, String prose, String code, String type, String hint) {
        if (table.put(prose, new Rule(code, type, hint)) != null) {
            throw new IllegalStateException("Duplicate prose rule: " + prose);
        }
    }

    private static void pattern(List<Map.Entry<Pattern, Rule>> list, String regex, String code, String type, String hint) {
        list.add(Map.entry(Pattern.compile(regex), new Rule(code, type, hint)));
    }

    /**
     * Bodies that mean "nothing to return" rather than a failure: these endpoints
     * answer with prose instead of an empty object when the brand has no rows.
     */
    private static final Set<String> PROSE_EMPTY = Set.of("no lists found", "no brands found", "no campaigns found");

    /** Bodies that mean success even though they are bare prose. */
    private static final Set<String> PROSE_OK = Set.of("1", "true", "already subscribed.");

    /** @return true when the body means "no rows" rather than a failure */
    public static boolean isProseEmpty(String body) {
        return PROSE_EMPTY.contains(Text.trim(body).toLowerCase(Locale.ROOT));
    }

    /** @return true when the body is bare prose that means success */
    public static boolean isProseSuccess(String body) {
        return PROSE_OK.contains(Text.trim(body).toLowerCase(Locale.ROOT));
    }

    /**
     * Classify a legacy plain-text body.
     *
     * @return the exception it stands for, or null when the body is not a
     *     recognised failure, which callers should treat as data
     */
    public static SkrybeException classifyProse(String body, Integer status) {
        String trimmed = Text.trim(body);
        String key = trimmed.toLowerCase(Locale.ROOT);

        Rule rule = PROSE.get(key);
        if (rule == null) {
            for (Map.Entry<Pattern, Rule> candidate : PROSE_PATTERNS) {
                if (candidate.getKey().matcher(key).find()) {
                    rule = candidate.getValue();
                    break;
                }
            }
        }
        if (rule != null) {
            return create(rule.type, rule.code, trimmed, rule.hint, status, body, null);
        }

        // A family of "Unable to ..." server-side failures not worth enumerating.
        if (key.startsWith("unable to")) {
            return new ApiException("operation_failed", trimmed, null, status, body, null);
        }
        return null;
    }

    /** @return the exception type for an HTTP status from one of the JSON endpoints */
    public static String typeForStatus(int status) {
        if (status == 401 || status == 403) {
            return AUTHENTICATION;
        }
        if (status == 402 || status == 429) {
            return RATE_LIMIT;
        }
        if (status == 400 || status == 405 || status == 422) {
            return INVALID_REQUEST;
        }
        return API;
    }

    /** @return the error code for an HTTP status from one of the JSON endpoints */
    public static String codeForStatus(int status) {
        switch (status) {
            case 400:
                return "invalid_request";
            case 401:
                return "unauthenticated";
            case 402:
                return "quota_exceeded";
            case 403:
                return "forbidden";
            case 404:
                return "not_found";
            case 405:
                return "method_not_allowed";
            case 429:
                return "rate_limited";
            default:
                return status >= 500 ? "server_error" : "api_error";
        }
    }

    /** Build the exception subclass for a type. */
    public static SkrybeException create(
            String type, String code, String message, String hint, Integer status, String raw, Throwable cause) {
        switch (type) {
            case AUTHENTICATION:
                return new AuthenticationException(code, message, hint, status, raw, cause);
            case INVALID_REQUEST:
                return new InvalidRequestException(code, message, hint, status, raw, cause);
            case RATE_LIMIT:
                return new RateLimitException(code, message, hint, status, raw, cause);
            case "connection":
                return new ConnectionException(code, message, hint, status, raw, cause);
            default:
                return new ApiException(code, message, hint, status, raw, cause);
        }
    }

    /** A body that is neither a known success nor a known failure. */
    public static ApiException unexpectedResponse(String operation, String body) {
        return new ApiException(
                "unexpected_response", "Unexpected response from " + operation + ": " + Text.excerpt(body), null, null, body, null);
    }

    /**
     * Throw {@code missing_params} for each name whose value is null or an
     * empty string, before anything is sent.
     *
     * @param namesAndValues alternating parameter names and values
     */
    public static void requireParams(Object... namesAndValues) {
        List<String> missing = new ArrayList<>();
        for (int i = 0; i + 1 < namesAndValues.length; i += 2) {
            Object value = namesAndValues[i + 1];
            if (value == null || (value instanceof CharSequence && ((CharSequence) value).length() == 0)) {
                missing.add(String.valueOf(namesAndValues[i]));
            }
        }
        if (!missing.isEmpty()) {
            throw new InvalidRequestException(
                    "missing_params",
                    "Missing required parameter" + (missing.size() > 1 ? "s" : "") + ": " + String.join(", ", missing) + ".");
        }
    }
}
