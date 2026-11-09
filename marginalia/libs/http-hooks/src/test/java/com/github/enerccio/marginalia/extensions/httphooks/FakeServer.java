package com.github.enerccio.marginalia.extensions.httphooks;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** A local HTTP server that answers as the test says and remembers what it was asked. */
class FakeServer implements AutoCloseable {

    record Call(String method, String path, String body, String authorization) {
    }

    final List<Call> calls = new CopyOnWriteArrayList<>();
    private final HttpServer server;

    FakeServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    void on(String path, HttpHandler handler) {
        server.createContext(path, exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            calls.add(new Call(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), body,
                    exchange.getRequestHeaders().getFirst("Authorization")));
            handler.handle(exchange);
        });
    }

    void json(String path, int status, String body) {
        on(path, exchange -> reply(exchange, status, "application/json", body.getBytes(StandardCharsets.UTF_8)));
    }

    static void reply(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", type);
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        if (body.length > 0) {
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }

    /** Answers in chunks, without a Content-Length: the size is known only when the body ends. */
    static void replyChunked(HttpExchange exchange, byte[] body) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
