package com.github.enerccio.marginalia.extensions.grok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * The HTTP side of the xAI API: JSON requests with the key, retries of what is worth retrying (429, 5xx, a connection that
 * failed before an answer) with growing waits, and a streamed answer read as server-sent events.
 */
final class GrokClient implements AutoCloseable {

    static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json");

    private final GrokSettings settings;
    private final OkHttpClient http;

    GrokClient(GrokSettings settings) {
        this.settings = settings;
        Duration timeout = Duration.ofSeconds(settings.timeoutSeconds());
        // read is the wait for the first text and between the chunks; the call as a whole must not cut a long story in the middle
        this.http = new OkHttpClient.Builder()
                .connectTimeout(timeout.compareTo(Duration.ofSeconds(30)) < 0 ? timeout : Duration.ofSeconds(30))
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .callTimeout(Duration.ZERO)
                .retryOnConnectionFailure(true)
                .build();
    }

    // ---- plain requests

    JsonNode get(String path) {
        return readJson(send(new Request.Builder().url(url(path)).get(), null));
    }

    JsonNode post(String path, JsonNode body) {
        return readJson(send(new Request.Builder().url(url(path)).post(RequestBody.create(body.toString(), JSON)), null));
    }

    private JsonNode readJson(Response response) {
        try (response) {
            String text = response.body() == null ? "" : response.body().string();
            return text.isBlank() ? MAPPER.createObjectNode() : MAPPER.readTree(text);
        } catch (IOException e) {
            throw GrokErrors.translate(e) instanceof RuntimeException r ? r : new IllegalStateException(e);
        }
    }

    // ---- streams

    /** The answer of a request that streams: the events of the body, and the call that can be cancelled. */
    final class Stream implements AutoCloseable {
        private final Response response;
        private final Call call;
        private final BufferedReader reader;
        private boolean done;
        private boolean sawDone;

        private Stream(Response response, Call call) {
            this.response = response;
            this.call = call;
            this.reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));
        }

        /**
         * The next event (the JSON of a {@code data:} line), null when the stream ended ({@code [DONE]} or the end of the body).
         * Blocks until it arrives.
         */
        JsonNode next() throws IOException {
            if (done) {
                return null;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty()) {
                    continue;
                }
                if (data.equals("[DONE]")) {
                    done = true;
                    sawDone = true;
                    return null;
                }
                return MAPPER.readTree(data);
            }
            done = true;
            return null;
        }

        /** Whether the stream was read to its {@code [DONE]}; a body that just ends was cut short. */
        boolean finished() {
            return sawDone;
        }

        /** Cancels the call: a read that waits for the next chunk fails. */
        void cancel() {
            call.cancel();
        }

        Iterator<JsonNode> iterator() {
            return new Iterator<>() {
                private JsonNode ahead;

                @Override
                public boolean hasNext() {
                    if (ahead == null) {
                        try {
                            ahead = Stream.this.next();
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    }
                    return ahead != null;
                }

                @Override
                public JsonNode next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    JsonNode node = ahead;
                    ahead = null;
                    return node;
                }
            };
        }

        @Override
        public void close() {
            call.cancel();
            response.close();
        }
    }

    /** Sends the request and opens the stream of its answer; fails with an {@link InferenceException} when xAI refuses it. */
    Stream stream(String path, JsonNode body, String sessionId, java.util.function.Consumer<Call> track) {
        Request.Builder request = new Request.Builder().url(url(path))
                .header("Accept", "text/event-stream")
                .post(RequestBody.create(body.toString(), JSON));
        Response response = send(request, track, sessionId);
        // send gives back a successful response only
        return new Stream(response, ((CallHolder) response.request().tag(CallHolder.class)).call);
    }

    private static final class CallHolder {
        Call call;
    }

    private Response send(Request.Builder request, String sessionId) {
        return send(request, null, sessionId);
    }

    private Response send(Request.Builder request, java.util.function.Consumer<Call> track, String sessionId) {
        if (settings.apiKey().isBlank()) {
            throw new InferenceException(Type.AUTHENTICATION, null, GrokLocalization.T.localized(GrokLocalization.NO_API_KEY_IS_SET), null);
        }
        request.header("Authorization", "Bearer " + settings.apiKey());
        if (sessionId != null) {
            request.header(com.github.enerccio.marginalia.domain.service.InferenceSession.HEADER, sessionId);
        }
        CallHolder holder = new CallHolder();
        request.tag(CallHolder.class, holder);
        Request built = request.build();
        for (int attempt = 0; ; attempt++) {
            Call call = http.newCall(built);
            holder.call = call;
            if (track != null) {
                track.accept(call);
            }
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    return response;
                }
                int status = response.code();
                String body;
                try (response) {
                    body = response.body() == null ? "" : response.body().string();
                }
                boolean retry = status == 408 || status == 409 || status == 429 || status >= 500;
                if (retry && attempt < settings.maxRetries()) {
                    pause(attempt);
                    continue;
                }
                throw GrokErrors.fromResponse(status, body);
            } catch (IOException e) {
                if (call.isCanceled()) {
                    throw new java.util.concurrent.CancellationException();
                }
                if (attempt < settings.maxRetries()) {
                    pause(attempt);
                    continue;
                }
                throw (RuntimeException) GrokErrors.translate(e);
            }
        }
    }

    private static void pause(int attempt) {
        try {
            TimeUnit.MILLISECONDS.sleep(Math.min(8000, 500L << Math.min(attempt, 4)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new java.util.concurrent.CancellationException();
        }
    }

    private String url(String path) {
        return settings.endpoint() + (path.startsWith("/") ? path : "/" + path);
    }

    @Override
    public void close() {
        http.dispatcher().cancelAll();
        http.dispatcher().executorService().shutdown();
        http.connectionPool().evictAll();
        if (http.cache() != null) {
            try {
                http.cache().close();
            } catch (IOException ignored) {
                // no cache
            }
        }
    }

    static String text(JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode() ? "" : StringUtils.defaultString(node.asText());
    }
}
