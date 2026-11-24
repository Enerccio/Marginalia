package com.github.enerccio.marginalia.extensions.audiobook.model;

/**
 * The speech recognition of the user: it writes down what a clip or a piece of speech says. It checks the pieces of a batch (see
 * {@link BackendConfig#isCheckWords()}) and recognises the text of a voice clip. Kept in the settings of the user; the api key is stored protected
 * ({@code InferenceServices.protectSecret}).
 */
public class RecognizerConfig {

    /** Where the speech is recognised. */
    public enum Type {
        /** ComfyUI with the TTS Audio Suite: its Granite ASR engine and ASR Transcribe node. */
        COMFYUI_GRANITE,
        /** An OpenAI-compatible {@code /v1/audio/transcriptions} (a whisper server, a proxy, the cloud). */
        OPENAI
    }

    public static final String DEFAULT_GRANITE_MODEL = "granite-speech-4.1-2b";
    public static final String DEFAULT_OPENAI_MODEL = "whisper-1";
    public static final int DEFAULT_TIMEOUT_SECONDS = 300;

    private Type type = Type.COMFYUI_GRANITE;
    private String baseUrl = "";
    private String protectedApiKey;
    private String model;
    private boolean local = true;
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    /** The uuid of the GPU lock of the application the recognition takes; null: none. */
    private String gpuLock;

    /** Whether it is set up at all (an address). */
    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank();
    }

    public Type getType() {
        return type == null ? Type.COMFYUI_GRANITE : type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getBaseUrl() {
        return baseUrl == null ? "" : baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getProtectedApiKey() {
        return protectedApiKey;
    }

    public void setProtectedApiKey(String protectedApiKey) {
        this.protectedApiKey = protectedApiKey;
    }

    /** The model, the default of the type when none is set. */
    public String getModel() {
        if (model == null || model.isBlank()) {
            return getType() == Type.OPENAI ? DEFAULT_OPENAI_MODEL : DEFAULT_GRANITE_MODEL;
        }
        return model.strip();
    }

    /** The model as stored, null for the default. */
    public String getStoredModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getGpuLock() {
        return gpuLock == null || gpuLock.isBlank() ? null : gpuLock;
    }

    public void setGpuLock(String gpuLock) {
        this.gpuLock = gpuLock;
    }
}
