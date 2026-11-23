package com.github.enerccio.marginalia.extensions.grok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * A stand-in for the xAI API: answers the requests with what the test queued, in order, and keeps what it was sent.
 */
final class FakeGrokServer implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private record Reply(int status, String contentType, String body, boolean hang) {}

    private final HttpServer server;
    private final Deque<Reply> replies = new ArrayDeque<>();
    final List<String> paths = Collections.synchronizedList(new ArrayList<>());
    final List<JsonNode> requests = Collections.synchronizedList(new ArrayList<>());
    final List<String> authorizations = Collections.synchronizedList(new ArrayList<>());
    final List<String> sessionIds = Collections.synchronizedList(new ArrayList<>());
    private final CountDownLatch over = new CountDownLatch(1);
    final CountDownLatch hanging = new CountDownLatch(1);

    FakeGrokServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                byte[] body = exchange.getRequestBody().readAllBytes();
                paths.add(exchange.getRequestURI().getPath());
                authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
                sessionIds.add(exchange.getRequestHeaders().getFirst("X-Session-ID"));
                requests.add(body.length == 0 ? MAPPER.nullNode() : MAPPER.readTree(body));
                Reply reply;
                synchronized (replies) {
                    reply = replies.poll();
                }
                if (reply == null) {
                    reply = new Reply(500, "application/json", "{\"code\":\"x\",\"error\":\"nothing was queued\"}", false);
                }
                exchange.getResponseHeaders().add("Content-Type", reply.contentType());
                byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
                if (reply.hang()) {
                    exchange.sendResponseHeaders(reply.status(), 0);
                    OutputStream out = exchange.getResponseBody();
                    out.write(bytes);
                    out.flush();
                    hanging.countDown();
                    try {
                        over.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
                exchange.sendResponseHeaders(reply.status(), bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            } catch (IOException ignored) {
                // the client went away
            }
        });
        server.start();
    }

    /** The endpoint, with the {@code /v1} of the real one. */
    String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }

    FakeGrokServer json(int status, String body) {
        synchronized (replies) {
            replies.add(new Reply(status, "application/json", body, false));
        }
        return this;
    }

    /** A streamed answer made of the chunks, ended with {@code [DONE]}. */
    FakeGrokServer stream(String... chunks) {
        synchronized (replies) {
            replies.add(new Reply(200, "text/event-stream", String.join("", chunks) + "data: [DONE]\n\n", false));
        }
        return this;
    }

    /** A streamed answer without the end marker, as a cut connection. */
    FakeGrokServer streamCut(String... chunks) {
        synchronized (replies) {
            replies.add(new Reply(200, "text/event-stream", String.join("", chunks), false));
        }
        return this;
    }

    FakeGrokServer streamThenHang(String... chunks) {
        synchronized (replies) {
            replies.add(new Reply(200, "text/event-stream", String.join("", chunks), true));
        }
        return this;
    }

    // ---- chunks

    static String chunk(String json) {
        return "data: " + json + "\n\n";
    }

    static String text(String content) {
        return chunk("{\"choices\":[{\"index\":0,\"delta\":{\"content\":" + quote(content) + "}}]}");
    }

    static String thought(String content) {
        return chunk("{\"choices\":[{\"index\":0,\"delta\":{\"reasoning_content\":" + quote(content) + "}}]}");
    }

    static String call(int index, String id, String name, String arguments) {
        return chunk("{\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":" + index + ",\"id\":" + quote(id)
                + ",\"type\":\"function\",\"function\":{\"name\":" + quote(name) + ",\"arguments\":" + quote(arguments) + "}}]}}]}");
    }

    static String finish(String reason) {
        return chunk("{\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"" + reason + "\"}]}");
    }

    static String usage(int prompt, int completion, int reasoning, int cached) {
        return chunk("{\"choices\":[],\"usage\":{\"prompt_tokens\":" + prompt + ",\"completion_tokens\":" + completion
                + ",\"total_tokens\":" + (prompt + completion + reasoning) + ",\"prompt_tokens_details\":{\"cached_tokens\":" + cached
                + "},\"completion_tokens_details\":{\"reasoning_tokens\":" + reasoning + "}}}");
    }

    private static String quote(String s) {
        try {
            return MAPPER.writeValueAsString(s);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        over.countDown();
        server.stop(0);
    }
}
