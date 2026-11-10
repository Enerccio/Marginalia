package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.Http;
import com.github.enerccio.marginalia.extensions.httphooks.ServerGoneException;
import com.github.enerccio.marginalia.extensions.httphooks.UrlGuard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Chatterbox-TTS-Server (devnen), its {@code /tts} endpoint: the whole set of parameters (exaggeration, cfg weight, temperature, seed, speed,
 * a predefined voice or a cloned reference clip), not the {@code /v1/audio/speech} of the OpenAI API, which lacks them. The server has no
 * authentication of its own; a key is for the proxy in front of it. What was found out about the server is in {@code TODO.md}.
 */
public class ChatterboxBackend implements SpeechBackend {

    /** The server splits long text itself; a piece of a script is short, but a whole part sent at once must fit. */
    private static final int MAX_AUDIO_BYTES = 64 * 1024 * 1024;
    private static final int SHORT_SECONDS = 30;
    private static final int MAX_CLIP_BYTES = 20 * 1024 * 1024;

    private final BackendConfig config;
    private final String apiKey;

    public ChatterboxBackend(BackendConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
    }

    @Override
    public void prepare() throws BackendException {
        ServerCheck.Result result = ServerCheck.checkWithKey(config, apiKey);
        if (!result.modelLoaded()) {
            throw new BackendException("The speech server has no model loaded. Add a request before the batch that loads it (for Chatterbox-TTS-Server "
                    + "POST /restart_server, or start the container, which loads the model of its config).");
        }
    }

    @Override
    public byte[] synthesize(SpeechRequest request, Cancel cancel) throws BackendException {
        return com.github.enerccio.marginalia.extensions.httphooks.JobCalls.track(
                com.github.enerccio.marginalia.domain.service.job.JobCallType.AUDIO, "audiobook", config.getBaseUrl(),
                config.getModel(), () -> speak(request, cancel));
    }

    private byte[] speak(SpeechRequest request, Cancel cancel) throws BackendException {
        if (request.text() == null || request.text().isBlank()) {
            throw new BackendException("There is nothing to say");
        }
        if (request.voice() == null || request.voice().isBlank()) {
            throw new BackendException("The piece has no voice");
        }
        if (cancel != null && cancel.isCancelled()) {
            throw new BackendException("Cancelled");
        }
        HttpResponse<byte[]> response = Http.send(builder(endpoint("/tts"), config.getTimeoutSeconds())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body(request))).build(), MAX_AUDIO_BYTES,
                cancel == null ? null : cancel::isCancelled);
        if (response.statusCode() == 503) {
            throw new ServerGoneException("The speech server has no model loaded (503). Either nothing loaded it (add a request before the batch that does), or something unloaded it during the batch: look for /api/unload in the log of the server and compare it with the log of this extension.");
        }
        Http.requireOk(response, "Making the speech");
        String type = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        if (type.contains("json") || type.startsWith("text/") || response.body().length == 0) {
            throw new BackendException("The server did not return audio: " + Http.excerpt(response));
        }
        return response.body();
    }

    /** The JSON body of {@code /tts}. The language goes only to the multilingual model: the others have no use for it. */
    String body(SpeechRequest request) {
        return bodyOf(request, config.getModel());
    }

    /** The body for a request, for the model that makes it (the studio shows it to the user). */
    public static String bodyOf(SpeechRequest request, String model) {
        JsonObject body = new JsonObject();
        body.addProperty("text", request.text());
        body.addProperty("voice_mode", request.voiceMode().wire());
        body.addProperty(request.voiceMode() == SpeechRequest.VoiceMode.CLONE ? "reference_audio_filename" : "predefined_voice_id", request.voice());
        body.addProperty("output_format", request.format());
        body.addProperty("split_text", true);
        body.addProperty("chunk_size", request.chunkSize());
        body.addProperty("temperature", request.temperature());
        body.addProperty("exaggeration", request.exaggeration());
        body.addProperty("cfg_weight", request.cfgWeight());
        body.addProperty("speed_factor", request.speed());
        if (request.seed() != 0) {
            body.addProperty("seed", request.seed());
        }
        if (request.language() != null && !request.language().isBlank() && BackendConfig.MODEL_MULTILINGUAL.equals(model)) {
            body.addProperty("language", request.language());
        }
        return body.toString();
    }

    @Override
    public List<String> referenceFiles() throws BackendException {
        HttpResponse<byte[]> response = Http.send(builder(endpoint("/get_reference_files"), SHORT_SECONDS).GET().build());
        Http.requireOk(response, "Listing the reference clips");
        return strings(parse(response), response);
    }

    @Override
    public List<String> predefinedVoices() throws BackendException {
        HttpResponse<byte[]> response = Http.send(builder(endpoint("/get_predefined_voices"), SHORT_SECONDS).GET().build());
        Http.requireOk(response, "Listing the voices");
        List<String> names = new ArrayList<>();
        for (JsonElement entry : parse(response).getAsJsonArray()) {
            if (entry.isJsonObject()) {
                // the entries are objects ({"display_name", "filename"}); the file name is what a request takes
                JsonObject voice = entry.getAsJsonObject();
                String name = text(voice, "filename") != null ? text(voice, "filename") : text(voice, "name");
                if (name != null) {
                    names.add(name);
                }
            } else if (entry.isJsonPrimitive()) {
                names.add(entry.getAsString());
            }
        }
        return names;
    }

    @Override
    public String ensureReference(byte[] audio, String extension) throws BackendException {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT).replace(".", "");
        if (!ext.equals("wav") && !ext.equals("mp3")) {
            throw new BackendException("A reference clip must be wav or mp3");
        }
        if (audio == null || audio.length == 0 || audio.length > MAX_CLIP_BYTES) {
            throw new BackendException("A reference clip must be between 1 byte and " + MAX_CLIP_BYTES / (1024 * 1024) + " MB");
        }
        String name = referenceName(audio, ext);
        if (referenceFiles().contains(name)) {
            return name;
        }
        Multipart form = new Multipart().file("files", name, ext.equals("wav") ? "audio/wav" : "audio/mpeg", audio);
        HttpResponse<byte[]> response = Http.send(builder(endpoint("/upload_reference"), config.getTimeoutSeconds())
                .header("Content-Type", form.contentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(form.body())).build());
        Http.requireOk(response, "Uploading the reference clip");
        JsonObject answer = parse(response).isJsonObject() ? parse(response).getAsJsonObject() : new JsonObject();
        JsonElement errors = answer.get("errors");
        if (errors != null && errors.isJsonArray() && !errors.getAsJsonArray().isEmpty()) {
            throw new BackendException("The server refused the reference clip: " + errors);
        }
        JsonElement uploaded = answer.get("uploaded_files");
        if (uploaded == null || !uploaded.isJsonArray() || !strings(uploaded, response).contains(name)) {
            throw new BackendException("The server did not keep the reference clip: " + Http.excerpt(response));
        }
        return name;
    }

    /** The name a clip has on the server: made from its bytes, so the same clip is the same file and no two users collide. */
    public static String referenceName(byte[] audio, String ext) {
        return "m-" + hash(audio) + "." + ext;
    }

    private static String hash(byte[] audio) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(audio), 0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JsonElement parse(HttpResponse<byte[]> response) throws BackendException {
        try {
            return JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw new BackendException("The server answered with something that is not JSON: " + Http.excerpt(response), e);
        }
    }

    private static List<String> strings(JsonElement element, HttpResponse<byte[]> response) throws BackendException {
        if (!element.isJsonArray()) {
            throw new BackendException("The server answered with something unexpected: " + Http.excerpt(response));
        }
        JsonArray array = element.getAsJsonArray();
        List<String> list = new ArrayList<>();
        for (JsonElement e : array) {
            if (e.isJsonPrimitive()) {
                list.add(e.getAsString());
            }
        }
        return list;
    }

    private static String text(JsonObject o, String name) {
        JsonElement e = o.get(name);
        return e != null && e.isJsonPrimitive() && !e.getAsString().isBlank() ? e.getAsString() : null;
    }

    private URI endpoint(String path) throws BackendException {
        return UrlGuard.check(config.getBaseUrl().strip().replaceAll("/+$", "") + path, config.isLocal());
    }

    private HttpRequest.Builder builder(URI uri, int timeoutSeconds) {
        HttpRequest.Builder builder = Http.request(uri, timeoutSeconds);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }
}
