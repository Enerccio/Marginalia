package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.extensions.audiobook.comfy.ComfyClient;
import com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.JobCalls;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;
import java.util.Map;

/**
 * Speech recognition in ComfyUI with the TTS Audio Suite: the audio is uploaded into the input folder, a workflow of {@code GraniteASREngineNode},
 * {@code UnifiedASRTranscribeNode} and the core {@code PreviewAny} (which puts the text into the history) is queued and the text read from
 * {@code /history}. The engine computes in float32: in bf16 the recognitions after the first came out as gibberish on an AMD R9700. Every recognition ends with {@code POST /free} that unloads the models: on the author's AMD R9700 jobs of the suite that ran one
 * after the other without it crashed the GPU.
 */
public class ComfyGraniteRecognizer implements SpeechRecognizer {

    static final String ENGINE_NODE = "GraniteASREngineNode";
    static final String TRANSCRIBE_NODE = "UnifiedASRTranscribeNode";
    static final String PREVIEW_NODE = "PreviewAny";

    private static final long POLL_MILLIS = 1000;

    /** The languages the transcribe node lists, by code; any other code is recognised as {@code Auto}. */
    private static final Map<String, String> LANGUAGES = Map.ofEntries(
            Map.entry("en", "English"), Map.entry("zh", "Chinese"), Map.entry("de", "German"), Map.entry("fr", "French"), Map.entry("es", "Spanish"),
            Map.entry("pt", "Portuguese"), Map.entry("it", "Italian"), Map.entry("ko", "Korean"), Map.entry("ru", "Russian"), Map.entry("ja", "Japanese"),
            Map.entry("nl", "Dutch"), Map.entry("pl", "Polish"), Map.entry("cs", "Czech"), Map.entry("tr", "Turkish"), Map.entry("ar", "Arabic"),
            Map.entry("sv", "Swedish"), Map.entry("da", "Danish"), Map.entry("fi", "Finnish"), Map.entry("el", "Greek"), Map.entry("hu", "Hungarian"),
            Map.entry("ro", "Romanian"), Map.entry("hi", "Hindi"), Map.entry("th", "Thai"), Map.entry("vi", "Vietnamese"), Map.entry("id", "Indonesian"));

    private final RecognizerConfig config;
    private final String apiKey;
    private final long pollMillis;

    public ComfyGraniteRecognizer(RecognizerConfig config, String apiKey) {
        this(config, apiKey, POLL_MILLIS);
    }

    ComfyGraniteRecognizer(RecognizerConfig config, String apiKey, long pollMillis) {
        this.config = config;
        this.apiKey = apiKey;
        this.pollMillis = pollMillis;
    }

    @Override
    public void test() throws BackendException {
        client().requireNodes(ENGINE_NODE, TRANSCRIBE_NODE, PREVIEW_NODE);
    }

    @Override
    public String transcribe(byte[] audio, String extension, String language, Cancel cancel) throws BackendException {
        return JobCalls.track(JobCallType.AUDIO, "audiobook", config.getBaseUrl(), config.getModel(), () -> recognize(audio, extension, language, cancel));
    }

    private String recognize(byte[] audio, String extension, String language, Cancel cancel) throws BackendException {
        if (audio == null || audio.length == 0) {
            throw new BackendException("There is no audio to recognise");
        }
        ComfyClient client = client();
        boolean mp3 = "mp3".equalsIgnoreCase(extension);
        String name = client.upload(audio, "asr-", mp3 ? "mp3" : "wav", mp3 ? "audio/mpeg" : "audio/wav");
        try {
            String text = client.run(workflow(name, config.getModel(), language), "the recognition", ComfyGraniteRecognizer::textOf,
                    cancel == null ? null : cancel::isCancelled);
            return text.strip();
        } finally {
            client.free();
        }
    }

    private ComfyClient client() {
        return new ComfyClient(config.getBaseUrl(), config.isLocal(), apiKey, config.getTimeoutSeconds(), pollMillis);
    }

    /** The workflow of one recognition in the API format of ComfyUI. */
    static JsonObject workflow(String inputFile, String model, String language) {
        JsonObject engine = new JsonObject();
        engine.addProperty("model_name", model);
        engine.addProperty("device", "auto");
        engine.addProperty("max_new_tokens", 1024);
        // bf16 (the default on an AMD R9700 with ROCm) gave gibberish from the second recognition on, as it did for Higgs
        engine.addProperty("dtype", "float32");
        JsonObject load = new JsonObject();
        load.addProperty("audio", inputFile);
        JsonObject transcribe = new JsonObject();
        transcribe.add("engine", link("engine"));
        transcribe.add("audio", link("clip"));
        transcribe.addProperty("language", languageName(language));
        JsonObject preview = new JsonObject();
        preview.add("source", link("asr"));
        JsonObject workflow = new JsonObject();
        workflow.add("engine", node(ENGINE_NODE, engine));
        workflow.add("clip", node("LoadAudio", load));
        workflow.add("asr", node(TRANSCRIBE_NODE, transcribe));
        workflow.add("text", node(PREVIEW_NODE, preview));
        return workflow;
    }

    static String languageName(String code) {
        if (code == null || code.isBlank()) {
            return "Auto";
        }
        return LANGUAGES.getOrDefault(code.strip().toLowerCase(Locale.ROOT).split("[-_]")[0], "Auto");
    }

    /** The text the preview node put into the outputs, null when it is not there (yet). */
    static String textOf(JsonObject entry) {
        if (!entry.has("outputs") || !entry.get("outputs").isJsonObject()) {
            return null;
        }
        for (var output : entry.getAsJsonObject("outputs").entrySet()) {
            JsonElement value = output.getValue();
            if (value.isJsonObject() && value.getAsJsonObject().has("text") && value.getAsJsonObject().get("text").isJsonArray()) {
                JsonArray texts = value.getAsJsonObject().getAsJsonArray("text");
                StringBuilder all = new StringBuilder();
                for (JsonElement text : texts) {
                    if (text.isJsonPrimitive()) {
                        all.append(all.isEmpty() ? "" : " ").append(text.getAsString());
                    }
                }
                return all.toString();
            }
        }
        return null;
    }

    private static JsonObject node(String type, JsonObject inputs) {
        JsonObject node = new JsonObject();
        node.addProperty("class_type", type);
        node.add("inputs", inputs);
        return node;
    }

    private static JsonArray link(String node) {
        JsonArray link = new JsonArray();
        link.add(node);
        link.add(0);
        return link;
    }
}
