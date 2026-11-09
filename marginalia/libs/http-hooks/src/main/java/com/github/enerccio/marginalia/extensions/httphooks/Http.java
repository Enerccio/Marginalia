package com.github.enerccio.marginalia.extensions.httphooks;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

/** The HTTP calls a plugin makes to a server of the user, with the limits that go with them: no redirects, a timeout, a cap on the answer. */
public final class Http {

    /** More than an image, or a minute of audio, can be; a bigger answer is not what was asked for. */
    public static final int MAX_BYTES = 32 * 1024 * 1024;

    /**
     * HTTP/1.1 only: the client otherwise offers an upgrade to HTTP/2 on a plain connection ({@code Upgrade: h2c}), and an ASGI server (uvicorn
     * with httptools: LiteLLM, FastAPI servers such as Chatterbox-TTS-Server) then takes the body that follows the headers for upgrade data and
     * hands the application an empty request (a 422 "field required").
     */
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private Http() {
    }

    public static HttpRequest.Builder request(URI uri, int timeoutSeconds) {
        return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(timeoutSeconds));
    }

    public static HttpResponse<byte[]> send(HttpRequest request) throws BackendException {
        return send(request, MAX_BYTES);
    }

    /**
     * Sends the request and reads the answer, but not more than {@code maxBytes} of it: the reading stops at the limit (or at once
     * when the headers announce more), so a server can't make the plugin allocate what it likes.
     */
    public static HttpResponse<byte[]> send(HttpRequest request, int maxBytes) throws BackendException {
        try {
            return CLIENT.send(request, info -> new CappedBody(maxBytes,
                    info.headers().firstValueAsLong("Content-Length").orElse(-1) > maxBytes));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackendException("Interrupted", e);
        } catch (IOException e) {
            if (e instanceof CappedBody.TooBig || e.getCause() instanceof CappedBody.TooBig) {
                throw new BackendException("The answer of " + request.uri().getHost() + " is too big", e);
            }
            throw new BackendException("Cannot reach " + request.uri().getHost() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Like {@link #send(HttpRequest, int)}, but gives up as soon as {@code cancelled} turns true (asked every 200 ms): the request is
     * abandoned and its connection closed, so the caller does not wait for a long answer it no longer wants. The server may go on with
     * what it was doing until it notices the closed connection.
     */
    public static HttpResponse<byte[]> send(HttpRequest request, int maxBytes, BooleanSupplier cancelled) throws BackendException {
        if (cancelled == null) {
            return send(request, maxBytes);
        }
        CompletableFuture<HttpResponse<byte[]>> future = CLIENT.sendAsync(request, info -> new CappedBody(maxBytes,
                info.headers().firstValueAsLong("Content-Length").orElse(-1) > maxBytes));
        try {
            while (true) {
                try {
                    return future.get(200, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    if (cancelled.getAsBoolean()) {
                        future.cancel(true);
                        throw new BackendException("Cancelled");
                    }
                }
            }
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new BackendException("Interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof CappedBody.TooBig || cause.getCause() instanceof CappedBody.TooBig) {
                throw new BackendException("The answer of " + request.uri().getHost() + " is too big", e);
            }
            throw new BackendException("Cannot reach " + request.uri().getHost() + ": " + cause.getMessage(), e);
        }
    }

    /** The answer as text for an error message, cut short. */
    public static String excerpt(HttpResponse<byte[]> response) {
        String text = response.body() == null ? "" : new String(response.body(), StandardCharsets.UTF_8);
        text = text.strip();
        return text.length() > 400 ? text.substring(0, 400) + "..." : text;
    }

    public static void requireOk(HttpResponse<byte[]> response, String what) throws BackendException {
        if (response.statusCode() / 100 != 2) {
            throw new BackendException(what + " failed: HTTP " + response.statusCode() + " " + excerpt(response));
        }
    }
}
