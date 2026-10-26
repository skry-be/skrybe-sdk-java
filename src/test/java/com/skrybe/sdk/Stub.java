package com.skrybe.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

/**
 * An HTTP server that answers with replies in order, repeating the last one
 * once exhausted.
 *
 * <p>The point of testing against a real socket rather than a mocked client is
 * that the legacy API's quirks are wire-level (a 200 carrying a prose failure,
 * a form body, a missing Content-Type), and a mock that returns whatever the
 * client asked for would assert nothing about them.
 */
final class Stub implements AutoCloseable {
    static final class Reply {
        final String body;
        int status = 200;
        final Map<String, String> headers = new LinkedHashMap<>();
        long delayMs;
        boolean drop;

        private Reply(String body) {
            this.body = body;
        }

        static Reply of(String body) {
            return new Reply(body);
        }

        /** Close the connection without answering. */
        static Reply dropped() {
            Reply r = new Reply("");
            r.drop = true;
            return r;
        }

        Reply status(int status) {
            this.status = status;
            return this;
        }

        Reply header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /** Hold the response for this long, for exercising the client timeout. */
        Reply delay(long ms) {
            this.delayMs = ms;
            return this;
        }
    }

    static final class Received {
        String method;
        String path;
        String contentType;
        String userAgent;
        /** Form, JSON or query fields, decoded, in the order sent. */
        final Map<String, String> fields = new LinkedHashMap<>();
        String rawBody;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    final List<Received> received = Collections.synchronizedList(new ArrayList<>());
    final String url;
    private final HttpServer server;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private Stub(BiFunction<Received, Integer, Reply> pick) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> handle(exchange, pick));
        server.setExecutor(executor);
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    static Stub start(Reply... replies) throws IOException {
        return new Stub((req, i) -> replies.length == 0 ? Reply.of("") : replies[Math.min(i, replies.length - 1)]);
    }

    static Stub start(String... bodies) throws IOException {
        Reply[] replies = new Reply[bodies.length];
        for (int i = 0; i < bodies.length; i++) {
            replies[i] = Reply.of(bodies[i]);
        }
        return start(replies);
    }

    static Stub start(BiFunction<Received, Integer, Reply> pick) throws IOException {
        return new Stub(pick);
    }

    /** The only request. */
    Received only() {
        if (received.size() != 1) {
            throw new AssertionError("expected one request, got " + received.size());
        }
        return received.get(0);
    }

    private void handle(HttpExchange exchange, BiFunction<Received, Integer, Reply> pick) throws IOException {
        Received r = new Received();
        r.method = exchange.getRequestMethod();
        r.path = exchange.getRequestURI().getPath();
        r.contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        r.userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
        r.rawBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (r.contentType != null && r.contentType.contains("application/x-www-form-urlencoded")) {
            decodeForm(r.rawBody, r.fields);
        } else if (r.contentType != null && r.contentType.contains("application/json") && !r.rawBody.isEmpty()) {
            JsonNode json = MAPPER.readTree(r.rawBody);
            Iterator<Map.Entry<String, JsonNode>> it = json.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                r.fields.put(e.getKey(), e.getValue().asText());
            }
        }
        decodeForm(exchange.getRequestURI().getRawQuery(), r.fields);

        int index;
        synchronized (received) {
            index = received.size();
            received.add(r);
        }
        Reply reply = pick.apply(r, index);
        if (reply.delayMs > 0) {
            try {
                Thread.sleep(reply.delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (reply.drop) {
            exchange.close();
            return;
        }
        reply.headers.forEach((k, v) -> exchange.getResponseHeaders().add(k, v));
        byte[] bytes = reply.body.getBytes(StandardCharsets.UTF_8);
        try {
            exchange.sendResponseHeaders(reply.status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
        } catch (IOException e) {
            // The client gave up (a timeout test); nothing to answer.
        } finally {
            exchange.close();
        }
    }

    private static void decodeForm(String raw, Map<String, String> into) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String k = eq < 0 ? pair : pair.substring(0, eq);
            String v = eq < 0 ? "" : pair.substring(eq + 1);
            into.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }

    /**
     * The exact payload api/lists/get-lists.php and get-brands.php emit:
     * string-concatenated, unescaped, with their real indentation. A payload
     * built with a JSON library would never show the bug the parser exists for.
     */
    static String phpNumberedPayload(String prefix, String... idNamePairs) {
        StringBuilder out = new StringBuilder("{");
        for (int i = 0; i + 1 < idNamePairs.length; i += 2) {
            out.append('"').append(prefix).append(i / 2 + 1).append("\":\n\t\t{\n\t\t\t\"id\": \"")
                    .append(idNamePairs[i]).append("\",\n\t\t\t\"name\": \"").append(idNamePairs[i + 1]).append("\"\n\t\t},");
        }
        out.setLength(out.length() - 1);
        return out.append('}').toString();
    }
}
