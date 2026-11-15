package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.Http;
import com.github.enerccio.marginalia.extensions.httphooks.JobCalls;
import com.github.enerccio.marginalia.extensions.httphooks.UrlGuard;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * Speech recognition through an OpenAI-compatible {@code POST /v1/audio/transcriptions} (a whisper server such as faster-whisper or speaches, a proxy, the
 * OpenAI API). The address is the base of the API, with or without {@code /v1}.
 */
public class OpenAiRecognizer implements SpeechRecognizer {

    private static final int SHORT_SECONDS = 30;

    private final RecognizerConfig config;
    private final String apiKey;

    public OpenAiRecognizer(RecognizerConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
    }

    @Override
    public void test() throws BackendException {
        if (config.getBaseUrl().isBlank()) {
            throw new BackendException("The address of the speech recognition is empty");
        }
        Http.requireOk(Http.send(builder("/models", SHORT_SECONDS).GET().build()), "Connecting to the speech recognition");
    }

    @Override
    public String transcribe(byte[] audio, String extension, String language, Cancel cancel) throws BackendException {
        return JobCalls.track(JobCallType.AUDIO, "audiobook", config.getBaseUrl(), config.getModel(), () -> recognize(audio, extension, language, cancel));
    }

    private String recognize(byte[] audio, String extension, String language, Cancel cancel) throws BackendException {
        if (audio == null || audio.length == 0) {
            throw new BackendException("There is no audio to recognise");
        }
        String ext = "mp3".equalsIgnoreCase(extension) ? "mp3" : "wav";
        String boundary = "----marginalia" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        field(out, boundary, "model", config.getModel());
        field(out, boundary, "response_format", "json");
        if (language != null && !language.isBlank()) {
            field(out, boundary, "language", language.strip().toLowerCase(Locale.ROOT).split("[-_]")[0]);
        }
        out.writeBytes(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"audio." + ext + "\"\r\nContent-Type: "
                + (ext.equals("wav") ? "audio/wav" : "audio/mpeg") + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.writeBytes(audio);
        out.writeBytes(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpResponse<byte[]> response = Http.send(builder("/audio/transcriptions", config.getTimeoutSeconds())
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray())).build(), Http.MAX_BYTES,
                cancel == null ? null : cancel::isCancelled);
        Http.requireOk(response, "Recognising the speech");
        return textOf(response);
    }

    static String textOf(HttpResponse<byte[]> response) throws BackendException {
        String body = new String(response.body(), StandardCharsets.UTF_8);
        try {
            JsonElement json = JsonParser.parseString(body);
            if (json.isJsonObject() && json.getAsJsonObject().has("text")) {
                return json.getAsJsonObject().get("text").getAsString().strip();
            }
        } catch (RuntimeException e) {
            // a server that answers with plain text
            return body.strip();
        }
        throw new BackendException("The speech recognition answered without a text: " + Http.excerpt(response));
    }

    private static void field(ByteArrayOutputStream out, String boundary, String name, String value) {
        out.writeBytes(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
    }

    /** The address of a route of the API: {@code <base>/v1<path>}, also when the base ends with {@code /v1}. */
    private HttpRequest.Builder builder(String path, int timeoutSeconds) throws BackendException {
        String base = config.getBaseUrl().strip().replaceAll("/+$", "");
        if (!base.endsWith("/v1")) {
            base = base + "/v1";
        }
        URI uri = UrlGuard.check(base + path, config.isLocal());
        HttpRequest.Builder builder = Http.request(uri, timeoutSeconds);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }
}
