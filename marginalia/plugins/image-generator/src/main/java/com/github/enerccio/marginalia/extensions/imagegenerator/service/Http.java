package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** The few HTTP calls the plugin makes, with the limits that go with them. */
final class Http {

    /** More than an image can be (the application accepts 10 MB); a bigger answer is not an image. */
    static final int MAX_BYTES = 32 * 1024 * 1024;

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private Http() {
    }

    static HttpRequest.Builder request(URI uri, int timeoutSeconds) {
        return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(timeoutSeconds));
    }

    static HttpResponse<byte[]> send(HttpRequest request) throws ImageBackend.ImageException {
        try {
            HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.body() != null && response.body().length > MAX_BYTES) {
                throw new ImageBackend.ImageException("The answer of " + request.uri().getHost() + " is too big");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImageBackend.ImageException("Interrupted", e);
        } catch (IOException e) {
            throw new ImageBackend.ImageException("Cannot reach " + request.uri().getHost() + ": " + e.getMessage(), e);
        }
    }

    /** The answer as text for an error message, cut short. */
    static String excerpt(HttpResponse<byte[]> response) {
        String text = response.body() == null ? "" : new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
        text = text.strip();
        return text.length() > 400 ? text.substring(0, 400) + "..." : text;
    }

    static void requireOk(HttpResponse<byte[]> response, String what) throws ImageBackend.ImageException {
        if (response.statusCode() / 100 != 2) {
            throw new ImageBackend.ImageException(what + " failed: HTTP " + response.statusCode() + " " + excerpt(response));
        }
    }
}
