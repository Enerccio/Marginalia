package com.github.enerccio.marginalia.extensions.anthropic;

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
 * A stand-in for the Messages API: answers the requests with what the test queued, in order, and keeps what it was sent. The
 * real SDK talks to it, so the streaming format, the errors and the request bodies are the ones of the wire.
 */
final class FakeAnthropicServer implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private record Reply(int status, String contentType, String body, boolean hang) {}

    private final HttpServer server;
    private final Deque<Reply> replies = new ArrayDeque<>();
    final List<String> paths = Collections.synchronizedList(new ArrayList<>());
    final List<JsonNode> requests = Collections.synchronizedList(new ArrayList<>());
    // released when the test is over, so that a hanging reply lets go of its thread
    private final CountDownLatch over = new CountDownLatch(1);
    final CountDownLatch hanging = new CountDownLatch(1);

    FakeAnthropicServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                byte[] body = exchange.getRequestBody().readAllBytes();
                paths.add(exchange.getRequestURI().getPath());
                requests.add(body.length == 0 ? MAPPER.nullNode() : MAPPER.readTree(body));
                Reply reply;
                synchronized (replies) {
                    reply = replies.poll();
                }
                if (reply == null) {
                    reply = new Reply(500, "application/json", error("api_error", "nothing was queued"), false);
                }
                exchange.getResponseHeaders().add("Content-Type", reply.contentType());
                byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
                if (reply.hang()) {
                    // the first events, then nothing until the test ends
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

    String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    FakeAnthropicServer reply(int status, String body) {
        synchronized (replies) {
            replies.add(new Reply(status, "application/json", body, false));
        }
        return this;
    }

    FakeAnthropicServer error(int status, String type, String message) {
        return reply(status, error(type, message));
    }

    /** A streamed answer made of the events. */
    FakeAnthropicServer stream(String... events) {
        synchronized (replies) {
            replies.add(new Reply(200, "text/event-stream", String.join("", events), false));
        }
        return this;
    }

    /** A streamed answer that sends the events and then stalls. */
    FakeAnthropicServer streamThenHang(String... events) {
        synchronized (replies) {
            replies.add(new Reply(200, "text/event-stream", String.join("", events), true));
        }
        return this;
    }

    static String error(String type, String message) {
        return "{\"type\":\"error\",\"error\":{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}}";
    }

    // ---- events

    static String event(String name, String data) {
        return "event: " + name + "\ndata: " + data + "\n\n";
    }

    static String messageStart(int input, int cacheRead) {
        return event("message_start", "{\"type\":\"message_start\",\"message\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5-5\",\"content\":[],\"stop_reason\":null,\"stop_sequence\":null,\"usage\":{\"input_tokens\":"
                + input + ",\"output_tokens\":1,\"cache_read_input_tokens\":" + cacheRead + ",\"cache_creation_input_tokens\":0}}}");
    }

    static String thinking(int index, String text, String signature) {
        return event("content_block_start", "{\"type\":\"content_block_start\",\"index\":" + index
                + ",\"content_block\":{\"type\":\"thinking\",\"thinking\":\"\",\"signature\":\"\"}}")
                + event("content_block_delta", "{\"type\":\"content_block_delta\",\"index\":" + index
                + ",\"delta\":{\"type\":\"thinking_delta\",\"thinking\":\"" + text + "\"}}")
                + event("content_block_delta", "{\"type\":\"content_block_delta\",\"index\":" + index
                + ",\"delta\":{\"type\":\"signature_delta\",\"signature\":\"" + signature + "\"}}")
                + event("content_block_stop", "{\"type\":\"content_block_stop\",\"index\":" + index + "}");
    }

    static String text(int index, String... pieces) {
        StringBuilder events = new StringBuilder(event("content_block_start", "{\"type\":\"content_block_start\",\"index\":" + index
                + ",\"content_block\":{\"type\":\"text\",\"text\":\"\"}}"));
        for (String piece : pieces) {
            events.append(event("content_block_delta", "{\"type\":\"content_block_delta\",\"index\":" + index
                    + ",\"delta\":{\"type\":\"text_delta\",\"text\":" + MAPPER.valueToTree(piece) + "}}"));
        }
        return events + event("content_block_stop", "{\"type\":\"content_block_stop\",\"index\":" + index + "}");
    }

    static String toolUse(int index, String id, String name, String... jsonPieces) {
        StringBuilder events = new StringBuilder(event("content_block_start", "{\"type\":\"content_block_start\",\"index\":" + index
                + ",\"content_block\":{\"type\":\"tool_use\",\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"input\":{}}}"));
        for (String piece : jsonPieces) {
            events.append(event("content_block_delta", "{\"type\":\"content_block_delta\",\"index\":" + index
                    + ",\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":" + MAPPER.valueToTree(piece) + "}}"));
        }
        return events + event("content_block_stop", "{\"type\":\"content_block_stop\",\"index\":" + index + "}");
    }

    static String end(String stopReason, int output) {
        return event("message_delta", "{\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"" + stopReason
                + "\",\"stop_sequence\":null},\"usage\":{\"output_tokens\":" + output + "}}")
                + event("message_stop", "{\"type\":\"message_stop\"}");
    }

    @Override
    public void close() {
        over.countDown();
        server.stop(0);
    }
}
