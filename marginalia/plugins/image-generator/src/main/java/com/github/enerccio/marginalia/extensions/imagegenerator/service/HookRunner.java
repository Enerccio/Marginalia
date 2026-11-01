package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.imagegenerator.model.HttpHook;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Makes the request of a {@link HttpHook}. */
public final class HookRunner {

    private HookRunner() {
    }

    /**
     * @param local whether the private addresses are allowed (the setting of the backend)
     * @throws ImageBackend.ImageException when the address is refused, the server not reachable or it answers with an error
     */
    public static void run(HttpHook hook, boolean local) throws ImageBackend.ImageException {
        if (hook == null || hook.isEmpty()) {
            return;
        }
        URI uri = UrlGuard.check(hook.getUrl(), local);
        String method = hook.getMethod().toUpperCase();
        HttpRequest.BodyPublisher body = hook.getBody() == null || hook.getBody().isBlank()
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(hook.getBody());
        HttpRequest.Builder builder = Http.request(uri, 60).method(method, "GET".equals(method) ? HttpRequest.BodyPublishers.noBody() : body);
        boolean contentType = false;
        if (hook.getHeaders() != null) {
            for (String line : hook.getHeaders().split("\\R")) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String name = line.substring(0, colon).strip();
                    try {
                        builder.header(name, line.substring(colon + 1).strip());
                    } catch (IllegalArgumentException e) {
                        throw new ImageBackend.ImageException("Not a header: " + name, e);
                    }
                    contentType |= name.equalsIgnoreCase("Content-Type");
                }
            }
        }
        if (!contentType && hook.getBody() != null && !hook.getBody().isBlank()) {
            builder.header("Content-Type", "application/json");
        }
        HttpResponse<byte[]> response = Http.send(builder.build());
        Http.requireOk(response, "The hook " + uri.getHost());
    }
}
