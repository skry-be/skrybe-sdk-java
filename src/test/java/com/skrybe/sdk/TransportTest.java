package com.skrybe.sdk;

import com.skrybe.sdk.exception.ApiException;
import com.skrybe.sdk.exception.AuthenticationException;
import com.skrybe.sdk.exception.ConnectionException;
import com.skrybe.sdk.exception.InvalidRequestException;
import com.skrybe.sdk.exception.RateLimitException;
import com.skrybe.sdk.exception.SkrybeException;
import com.skrybe.sdk.params.Recipients;
import com.skrybe.sdk.params.SendEmailParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransportTest {
    private static final String KEY = "test-api-key";

    private static Skrybe client(Stub stub) {
        return Skrybe.builder(KEY).baseUrl(stub.url).build();
    }

    // ---------------------------------------------------------- constructor

    @Test
    void requiresAnApiKey() {
        InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> new Skrybe(""));
        assertEquals("api_key_missing", e.getErrorCode());
        assertThrows(InvalidRequestException.class, () -> new Skrybe(null));
        assertThrows(InvalidRequestException.class, () -> Skrybe.builder("").build());
    }

    @Test
    void defaultsToTheProductionApi() {
        RecordingHttpClient http = new RecordingHttpClient("4");
        Skrybe skrybe = Skrybe.builder(KEY).httpClient(http).build();
        skrybe.lists().activeSubscriberCount("abc");
        assertEquals("https://dashboard.skry.be/api/subscribers/active-subscriber-count.php",
                http.requests.get(0).uri().toString());
        assertEquals(Duration.ofSeconds(30), http.requests.get(0).timeout().orElseThrow());
    }

    // ------------------------------------------------------------ transport

    @Test
    void postsAFormBodyWithTheApiKeyAndAUserAgent() throws IOException {
        try (Stub stub = Stub.start("4211")) {
            Skrybe skrybe = Skrybe.builder(KEY).baseUrl(stub.url + "///").build();

            assertEquals(4211, skrybe.lists().activeSubscriberCount("AbC892763xy"));
            Stub.Received r = stub.only();
            assertEquals("POST", r.method);
            assertEquals("/api/subscribers/active-subscriber-count.php", r.path);
            assertEquals("application/x-www-form-urlencoded", r.contentType);
            assertEquals(Map.of("list_id", "AbC892763xy", "api_key", KEY), r.fields);
            assertEquals("list_id=AbC892763xy&api_key=test-api-key", r.rawBody);
            assertEquals("skrybe-sdk-java/" + Skrybe.VERSION, r.userAgent);
        }
    }

    @Test
    void encodesFormFieldsAsUrlSearchParamsDoes() throws IOException {
        try (Stub stub = Stub.start("true")) {
            client(stub).subscribers().delete("A1", "a+b c&d@example.com");
            assertEquals("list_id=A1&email=a%2Bb+c%26d%40example.com&api_key=test-api-key", stub.only().rawBody);
        }
    }

    @Test
    void turnsAProseFailureCarriedByHttp200IntoATypedException() throws IOException {
        try (Stub stub = Stub.start("Invalid API key")) {
            Skrybe skrybe = Skrybe.builder("wrong").baseUrl(stub.url).build();
            AuthenticationException e = assertThrows(AuthenticationException.class, () -> skrybe.lists().list());
            assertInstanceOf(SkrybeException.class, e);
            assertEquals("authentication", e.getType());
            assertEquals("invalid_api_key", e.getErrorCode());
            assertEquals(200, e.getStatus());
            assertEquals("Invalid API key", e.getRaw());
            assertNotNull(e.getHint());
        }
    }

    static Stream<Arguments> proseCases() {
        return Stream.of(
                Arguments.of("List does not exist", ApiException.class, "list_not_found"),
                Arguments.of("List ID not passed", InvalidRequestException.class, "list_id_invalid"),
                Arguments.of("Quota exceeded. Please upgrade your plan.", RateLimitException.class, "quota_exceeded"),
                Arguments.of("Test send rate limit reached, try later", RateLimitException.class, "rate_limited"),
                Arguments.of("Unable to insert campaign", ApiException.class, "operation_failed"),
                Arguments.of("List already has a drip autoresponder", ApiException.class, "drip_exists"),
                Arguments.of("Domain \"example.com\" is not verified", ApiException.class, "domain_not_verified"));
    }

    @ParameterizedTest
    @MethodSource("proseCases")
    void classifiesProseByKind(String body, Class<? extends SkrybeException> kind, String code) throws IOException {
        try (Stub stub = Stub.start(body)) {
            SkrybeException e = assertThrows(SkrybeException.class, () -> client(stub).lists().get("x"));
            assertInstanceOf(kind, e, body);
            assertEquals(code, e.getErrorCode());
        }
    }

    @Test
    void readsTheErrorOutOfAJsonEndpointsEnvelopeAndStatus() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("{\"ok\":false,\"error\":\"Invalid from_email\"}").status(400))) {
            SendEmailParams params = SendEmailParams.builder()
                    .fromName("a").fromEmail("b").replyTo("c").subject("d").htmlText("e").to("x@y.z").build();
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> client(stub).emails().send(params));
            assertEquals("Invalid from_email", e.getMessage());
            assertEquals("invalid_request", e.getErrorCode());
            assertEquals(400, e.getStatus());
        }
    }

    @Test
    void mapsStatusesToTypes() throws IOException {
        int[] statuses = {401, 402, 403, 404, 422, 500};
        String[] codes = {"unauthenticated", "quota_exceeded", "forbidden", "not_found", "invalid_request", "server_error"};
        String[] types = {"authentication", "rate_limit", "authentication", "api", "invalid_request", "api"};
        for (int i = 0; i < statuses.length; i++) {
            try (Stub stub = Stub.start(Stub.Reply.of("{\"status\":\"error\",\"message\":\"Nope\"}").status(statuses[i]))) {
                SkrybeException e = assertThrows(SkrybeException.class, () -> client(stub).stats().totalEmailsSent());
                assertEquals(types[i], e.getType());
                assertEquals(statuses[i] == 422 ? "api_error" : codes[i], e.getErrorCode());
                assertEquals("Nope", e.getMessage());
            }
        }
    }

    @Test
    void retriesAReadOn503HonouringRetryAfter() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("").status(503).header("Retry-After", "0"), Stub.Reply.of("7"))) {
            assertEquals(7, client(stub).lists().activeSubscriberCount("x"));
            assertEquals(2, stub.received.size());
        }
    }

    @Test
    void neverRetriesAWrite() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("").status(503).header("Retry-After", "0"))) {
            ApiException e = assertThrows(ApiException.class,
                    () -> client(stub).campaigns().send(1, Recipients.builder().listIds("a").build()));
            assertEquals("server_error", e.getErrorCode());
            assertEquals(503, e.getStatus());
            assertEquals(1, stub.received.size());
        }
    }

    @Test
    void respectsMaxRetriesZero() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("").status(503).header("Retry-After", "0"))) {
            Skrybe skrybe = Skrybe.builder(KEY).baseUrl(stub.url).maxRetries(0).build();
            assertThrows(ApiException.class, () -> skrybe.lists().activeSubscriberCount("x"));
            assertEquals(1, stub.received.size());
        }
    }

    @Test
    void givesUpAfterMaxRetries() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("").status(429).header("Retry-After", "0"))) {
            RateLimitException e = assertThrows(RateLimitException.class, () -> client(stub).brands().list());
            assertEquals("rate_limited", e.getErrorCode());
            assertEquals(3, stub.received.size());
        }
    }

    @Test
    void timesOutAsAConnectionException() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("1").delay(1000))) {
            Skrybe skrybe = Skrybe.builder(KEY).baseUrl(stub.url).timeout(Duration.ofMillis(100)).build();
            ConnectionException e = assertThrows(ConnectionException.class, () -> skrybe.templates().delete(1));
            assertEquals("timeout", e.getErrorCode());
            assertEquals("connection", e.getType());
        }
    }

    @Test
    void timeoutsAreNotRetriedEvenOnReads() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.of("1").delay(1000))) {
            Skrybe skrybe = Skrybe.builder(KEY).baseUrl(stub.url).timeout(Duration.ofMillis(100)).build();
            ConnectionException e = assertThrows(ConnectionException.class, () -> skrybe.lists().activeSubscriberCount("x"));
            assertEquals("timeout", e.getErrorCode());
            assertEquals(1, stub.received.size());
        }
    }

    @Test
    void reportsAnUnreachableHostAsAConnectionException() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        Skrybe skrybe = Skrybe.builder(KEY).baseUrl("http://127.0.0.1:" + port).maxRetries(0).build();
        ConnectionException e = assertThrows(ConnectionException.class, () -> skrybe.brands().list());
        assertEquals("network_error", e.getErrorCode());
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    void wrapsTheInjectedClientsFailure() {
        IOException cause = new IOException("fetch failed");
        Skrybe skrybe = Skrybe.builder(KEY).httpClient(new RecordingHttpClient(cause)).maxRetries(0).build();
        ConnectionException e = assertThrows(ConnectionException.class, () -> skrybe.brands().list());
        assertEquals("network_error", e.getErrorCode());
        assertEquals(cause, e.getCause());
    }

    @Test
    void retriesADroppedConnectionOnARead() throws IOException {
        // A connection-error backoff ignores Retry-After, so this waits ~0.5s.
        try (Stub stub = Stub.start(Stub.Reply.dropped(), Stub.Reply.of("3"))) {
            Skrybe skrybe = Skrybe.builder(KEY).baseUrl(stub.url).maxRetries(1).build();
            assertEquals(3, skrybe.lists().activeSubscriberCount("x"));
            assertEquals(2, stub.received.size());
        }
    }

    @Test
    void neverRetriesADroppedWrite() throws IOException {
        try (Stub stub = Stub.start(Stub.Reply.dropped(), Stub.Reply.of("{}"))) {
            ConnectionException e = assertThrows(ConnectionException.class, () -> client(stub).campaigns().delete(1));
            assertEquals("network_error", e.getErrorCode());
            assertEquals(1, stub.received.size());
        }
    }

    // -------------------------------------------------------------- package

    @Test
    void versionMatchesTheChangelogAndPom() throws IOException {
        String changelog = Files.readString(Path.of("CHANGELOG.md"), StandardCharsets.UTF_8);
        Matcher released = Pattern.compile("^## \\[(\\d+\\.\\d+\\.\\d+)\\]", Pattern.MULTILINE).matcher(changelog);
        assertTrue(released.find(), "CHANGELOG.md has no released version heading");
        assertEquals(released.group(1), Skrybe.VERSION);

        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        Matcher version = Pattern.compile("<artifactId>skrybe-sdk-java</artifactId>\\s*<version>([^<]+)</version>").matcher(pom);
        assertTrue(version.find());
        assertEquals(version.group(1), Skrybe.VERSION);
    }

    @Test
    void hasOneRuntimeDependency() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        String deps = pom.substring(pom.indexOf("<dependencies>"), pom.indexOf("</dependencies>"));
        List<String> runtime = new ArrayList<>();
        Matcher m = Pattern.compile("<dependency>(.*?)</dependency>", Pattern.DOTALL).matcher(deps);
        while (m.find()) {
            if (!m.group(1).contains("<scope>test</scope>")) {
                Matcher a = Pattern.compile("<artifactId>([^<]+)</artifactId>").matcher(m.group(1));
                assertTrue(a.find());
                runtime.add(a.group(1));
            }
        }
        assertEquals(List.of("jackson-databind"), runtime);
    }

    @Test
    void neverPutsTheApiKeyOnTheAnonymousStatsCall() throws IOException {
        try (Stub stub = Stub.start("{\"total_emails_sent\":5}")) {
            client(stub).stats().totalEmailsSent();
            Map<String, String> none = new LinkedHashMap<>();
            assertEquals(none, stub.only().fields);
            assertEquals("", stub.only().rawBody);
        }
    }
}
