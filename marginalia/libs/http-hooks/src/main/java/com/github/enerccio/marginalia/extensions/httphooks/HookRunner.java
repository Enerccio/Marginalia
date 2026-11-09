package com.github.enerccio.marginalia.extensions.httphooks;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.UnaryOperator;

/** Makes the request of a {@link HttpHook}, or the requests of a list of them, one after the other. */
public final class HookRunner {

    /** A hook answers with little; a longer answer is an error page or an attack. */
    private static final int MAX_ANSWER_BYTES = 1024 * 1024;

    private HookRunner() {
    }

    /**
     * @param local  whether the private addresses are allowed (the setting of the backend)
     * @param reveal opens the protected headers of the hook (null when the caller has none)
     * @throws BackendException when the address is refused, the server not reachable or it answers with an error
     */
    public static void run(HttpHook hook, boolean local, UnaryOperator<String> reveal) throws BackendException {
        if (hook == null || hook.isEmpty()) {
            return;
        }
        URI uri = UrlGuard.check(hook.getUrl(), local);
        String method = hook.getMethod().toUpperCase();
        HttpRequest.BodyPublisher body = hook.getBody() == null || hook.getBody().isBlank()
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(hook.getBody());
        HttpRequest.Builder builder = Http.request(uri, hook.getTimeoutSeconds()).method(method, "GET".equals(method) ? HttpRequest.BodyPublishers.noBody() : body);
        boolean contentType = false;
        String headers = hook.headersText(reveal);
        if (headers != null) {
            for (String line : headers.split("\\R")) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String name = line.substring(0, colon).strip();
                    try {
                        builder.header(name, line.substring(colon + 1).strip());
                    } catch (IllegalArgumentException e) {
                        throw new BackendException("Not a header: " + name, e);
                    }
                    contentType |= name.equalsIgnoreCase("Content-Type");
                }
            }
        }
        if (!contentType && hook.getBody() != null && !hook.getBody().isBlank()) {
            builder.header("Content-Type", "application/json");
        }
        HttpResponse<byte[]> response = Http.send(builder.build(), MAX_ANSWER_BYTES);
        Http.requireOk(response, "The hook " + uri.getHost());
        if (hook.getWaitSeconds() > 0) {
            try {
                Thread.sleep(hook.getWaitSeconds() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BackendException("Interrupted", e);
            }
        }
    }

    /**
     * Runs the requests of the list in order and stops at the first one that fails (what comes before the work: if the model could not be
     * unloaded the next request must not load another one). Hooks without an address are skipped.
     *
     * @throws BackendException the failure, naming the number of the request when there are several
     */
    public static void runAll(List<HttpHook> hooks, boolean local, UnaryOperator<String> reveal) throws BackendException {
        List<HttpHook> todo = hooks == null ? List.of() : hooks.stream().filter(h -> h != null && !h.isEmpty()).toList();
        for (int i = 0; i < todo.size(); i++) {
            try {
                run(todo.get(i), local, reveal);
            } catch (BackendException e) {
                throw todo.size() == 1 ? e : new BackendException("Request " + (i + 1) + " of " + todo.size() + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Runs all the requests of the list in order whatever happens to one of them (what comes after the work: every container is to be stopped,
     * also when the first stop failed).
     *
     * @return the message of the first failure, null when all went well
     */
    public static String runAllRegardless(List<HttpHook> hooks, boolean local, UnaryOperator<String> reveal) {
        List<HttpHook> todo = hooks == null ? List.of() : hooks.stream().filter(h -> h != null && !h.isEmpty()).toList();
        String first = null;
        for (int i = 0; i < todo.size(); i++) {
            try {
                run(todo.get(i), local, reveal);
            } catch (BackendException e) {
                if (first == null) {
                    first = todo.size() == 1 ? e.getMessage() : "Request " + (i + 1) + " of " + todo.size() + ": " + e.getMessage();
                }
            }
        }
        return first;
    }
}
