package com.skrybe.sdk.internal;

import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.exception.AuthenticationException;
import com.skrybe.sdk.exception.InvalidRequestException;
import com.skrybe.sdk.exception.RateLimitException;
import com.skrybe.sdk.exception.SkrybeException;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorsTest {
    @Test
    void everyExactRuleMatchesCaseInsensitivelyAndTrimmed() {
        assertEquals(84, Errors.PROSE.size(), "kept in step with @skrybe/sdk's 84 exact rules");
        for (Map.Entry<String, Errors.Rule> rule : Errors.PROSE.entrySet()) {
            String body = "  " + rule.getKey().toUpperCase(Locale.ROOT) + "\n";
            SkrybeException e = Errors.classifyProse(body, 200);
            assertNotNull(e, rule.getKey());
            assertEquals(rule.getValue().code, e.getErrorCode());
            assertEquals(rule.getValue().type, e.getType());
            assertEquals(rule.getValue().hint, e.getHint());
            assertEquals(body.trim(), e.getMessage());
            assertEquals(body, e.getRaw());
            assertEquals(200, e.getStatus());
        }
    }

    @Test
    void typesMapToSubclasses() {
        assertInstanceOf(AuthenticationException.class, Errors.classifyProse("API key not passed", 200));
        assertInstanceOf(InvalidRequestException.class, Errors.classifyProse("Some fields are missing.", 200));
        assertInstanceOf(RateLimitException.class, Errors.classifyProse("Quota exceeded. Please upgrade your plan.", 200));
        assertInstanceOf(ApiException.class, Errors.classifyProse("Campaign does not exist", 200));
    }

    @Test
    void patternRules() {
        String[][] cases = {
                {"Error: This campaign would exceed your sending quota. Reduce the recipients.", "quota_exceeded", "rate_limit"},
                {"Account under review: bounce rate 12%", "account_under_review", "api"},
                {"Brand is under review for bounces", "account_under_review", "api"},
                {"A field is too long: name", "field_too_long", "invalid_request"},
                {"Test send rate limit reached. Try again in an hour.", "rate_limited", "rate_limit"},
                {"Domain not verified in SES: example.com", "domain_not_verified", "api"},
                {"Domain \"example.com\" is not verified for this brand", "domain_not_verified", "api"},
        };
        for (String[] c : cases) {
            SkrybeException e = Errors.classifyProse(c[0], 200);
            assertNotNull(e, c[0]);
            assertEquals(c[1], e.getErrorCode(), c[0]);
            assertEquals(c[2], e.getType(), c[0]);
        }
        // Anchored at the start.
        assertNull(Errors.classifyProse("Note: account under review: no", 200));
    }

    @Test
    void unableToIsAnOperationFailure() {
        SkrybeException e = Errors.classifyProse("Unable to update list", 200);
        assertInstanceOf(ApiException.class, e);
        assertEquals("operation_failed", e.getErrorCode());
    }

    @Test
    void dataIsNotAnError() {
        assertNull(Errors.classifyProse("4211", 200));
        assertNull(Errors.classifyProse("{\"id\":1}", 200));
        assertNull(Errors.classifyProse("Subscribed", 200));
    }

    @Test
    void trimsUnicodeWhitespaceAsJavaScriptDoes() {
        assertNotNull(Errors.classifyProse(" ﻿Invalid API key ", 200));
        assertTrue(Errors.isProseEmpty(" No lists found\n"));
        assertTrue(Errors.isProseEmpty("NO CAMPAIGNS FOUND"));
        assertTrue(Errors.isProseEmpty("no brands found"));
        assertFalse(Errors.isProseEmpty("no templates found"));
        assertTrue(Errors.isProseSuccess("1"));
        assertTrue(Errors.isProseSuccess(" true\n"));
        assertTrue(Errors.isProseSuccess("Already subscribed."));
        assertFalse(Errors.isProseSuccess("0"));
    }

    @Test
    void statusMapping() {
        assertEquals("authentication", Errors.typeForStatus(401));
        assertEquals("authentication", Errors.typeForStatus(403));
        assertEquals("rate_limit", Errors.typeForStatus(402));
        assertEquals("rate_limit", Errors.typeForStatus(429));
        assertEquals("invalid_request", Errors.typeForStatus(400));
        assertEquals("invalid_request", Errors.typeForStatus(405));
        assertEquals("invalid_request", Errors.typeForStatus(422));
        assertEquals("api", Errors.typeForStatus(404));
        assertEquals("api", Errors.typeForStatus(500));
        assertEquals("invalid_request", Errors.codeForStatus(400));
        assertEquals("unauthenticated", Errors.codeForStatus(401));
        assertEquals("quota_exceeded", Errors.codeForStatus(402));
        assertEquals("forbidden", Errors.codeForStatus(403));
        assertEquals("not_found", Errors.codeForStatus(404));
        assertEquals("method_not_allowed", Errors.codeForStatus(405));
        assertEquals("rate_limited", Errors.codeForStatus(429));
        assertEquals("server_error", Errors.codeForStatus(502));
        assertEquals("api_error", Errors.codeForStatus(418));
    }

    @Test
    void excerpt() {
        assertEquals("(an empty response)", Text.excerpt(" \n\t"));
        assertEquals("a b c", Text.excerpt(" a\n\n b  c "));
        assertEquals("x".repeat(200) + "…", Text.excerpt("x".repeat(201)));
        assertEquals("x".repeat(200), Text.excerpt("x".repeat(200)));
        assertEquals("abc…", Text.excerpt("abcdef", 3));
    }

    @Test
    void missingParamsNamesEveryMissingOne() {
        InvalidRequestException one = org.junit.jupiter.api.Assertions.assertThrows(
                InvalidRequestException.class, () -> Errors.requireParams("a", "x", "b", ""));
        assertEquals("missing_params", one.getErrorCode());
        assertEquals("Missing required parameter: b.", one.getMessage());
        Errors.requireParams("a", "x", "b", 0);
    }
}
