package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One speech server (Chatterbox-TTS-Server) with the requests that make room for it on the GPU. Kept in the settings of the user; the api
 * key is stored protected ({@code InferenceServices.protectSecret}), never in plain text. One backend is one model: a book that needs
 * another model (a language Turbo can't read) picks another backend, whose start requests load that model.
 */
public class BackendConfig implements Cloneable {

    /** The model ids of Chatterbox-TTS-Server ({@code repo_id} of its settings). */
    public static final String MODEL_TURBO = "chatterbox-turbo";
    public static final String MODEL_ORIGINAL = "chatterbox";
    public static final String MODEL_MULTILINGUAL = "chatterbox-multilingual";

    public static final List<String> MODELS = List.of(MODEL_TURBO, MODEL_ORIGINAL, MODEL_MULTILINGUAL);
    /** Mp3 (joined without decoding, see {@code Mp3Joiner}) or wav (lossless, see {@code WavJoiner}); opus can't be joined. */
    public static final List<String> FORMATS = List.of("mp3", "wav");

    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final double DEFAULT_TEMPERATURE = 0.8;
    public static final double DEFAULT_CFG_WEIGHT = 0.5;
    public static final double DEFAULT_SPEED = 1.0;
    public static final int DEFAULT_CHUNK_SIZE = 240;
    public static final int MIN_CHUNK_SIZE = 50;
    public static final int MAX_CHUNK_SIZE = 500;

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String baseUrl = "";
    /** Protected with the key of the installation. */
    private String protectedApiKey;
    /** The key the requests before and after a batch carry as {@code Authorization: Bearer}, protected the same way; empty: the api key is used. */
    private String protectedHooksKey;
    /** One of {@link #MODELS}: the model the server is asked to have loaded. */
    private String model = MODEL_TURBO;
    /**
     * The name of the GPU the server runs on, when other servers (of this or another extension) use the same one: backends with the same name wait
     * for each other. Empty: the address of the server says it.
     */
    private String gpu = "";
    /** True when the server is on this machine or the local network: the private addresses are allowed. */
    private boolean local = true;
    /** How long one speech request may take, in seconds. */
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    /**
     * The voice of the narrator for the plain script: {@code predefined:Henry.wav} (a voice that comes with the server) or {@code clip:<uuid of a
     * resource>} (a reference clip of the user). The cast of the book replaces it (see TODO.md).
     */
    private String narratorVoice = "";
    /** The format of the audio the server makes, one of {@link #FORMATS}. */
    private String format = "mp3";
    /** The defaults of a segment of the script (the studio changes them per segment). */
    private double temperature = DEFAULT_TEMPERATURE;
    private double cfgWeight = DEFAULT_CFG_WEIGHT;
    private double speed = DEFAULT_SPEED;
    private int chunkSize = DEFAULT_CHUNK_SIZE;
    /** Made in order before a batch (unload the language model, start the speech server, load the model...). */
    private List<HttpHook> beforeHooks = new ArrayList<>();
    /** Made in order when the batch is over: all of them, whatever happens to one (stop the containers). */
    private List<HttpHook> afterHooks = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getProtectedHooksKey() {
        return protectedHooksKey;
    }

    public void setProtectedHooksKey(String protectedHooksKey) {
        this.protectedHooksKey = protectedHooksKey;
    }

    public String getModel() {
        return model == null || !MODELS.contains(model) ? MODEL_TURBO : model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getGpu() {
        return gpu == null ? "" : gpu;
    }

    public void setGpu(String gpu) {
        this.gpu = gpu;
    }

    /** The gate of the GPU the server uses, see {@link #getGpu()}. */
    public com.github.enerccio.marginalia.extensions.httphooks.GpuGate gate() {
        return com.github.enerccio.marginalia.extensions.httphooks.GpuGate.forBackend(getGpu(), getBaseUrl());
    }

    /**
     * What the requests before a batch leave loaded on the GPU: this extension's speech model of this server. Batches with the same mode share the
     * load, a batch with another one (another model, or another extension's) waits for them.
     */
    public String gateMode() {
        return "audiobook:" + com.github.enerccio.marginalia.extensions.httphooks.UrlGuard.origin(getBaseUrl()) + "|" + getModel();
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

    public String getNarratorVoice() {
        return narratorVoice == null ? "" : narratorVoice;
    }

    public void setNarratorVoice(String narratorVoice) {
        this.narratorVoice = narratorVoice;
    }

    public String getFormat() {
        return format == null || !FORMATS.contains(format) ? "mp3" : format;
    }

    /** A copy that asks the server for this format: the storage of the installation decides it, not the backend. */
    public BackendConfig withFormat(String format) {
        try {
            BackendConfig copy = (BackendConfig) super.clone();
            copy.format = format;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public double getTemperature() {
        return temperature <= 0 ? DEFAULT_TEMPERATURE : temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getCfgWeight() {
        return cfgWeight <= 0 ? DEFAULT_CFG_WEIGHT : cfgWeight;
    }

    public void setCfgWeight(double cfgWeight) {
        this.cfgWeight = cfgWeight;
    }

    public double getSpeed() {
        return speed <= 0 ? DEFAULT_SPEED : speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /** The size of the pieces the server splits a long text into, kept in the range the server accepts. */
    public int getChunkSize() {
        return chunkSize <= 0 ? DEFAULT_CHUNK_SIZE : Math.max(MIN_CHUNK_SIZE, Math.min(MAX_CHUNK_SIZE, chunkSize));
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /** The requests before a batch, in order (a copy). */
    public List<HttpHook> getBeforeHooks() {
        return beforeHooks == null ? new ArrayList<>() : new ArrayList<>(beforeHooks);
    }

    public void setBeforeHooks(List<HttpHook> hooks) {
        this.beforeHooks = new ArrayList<>(hooks);
    }

    /** The requests after a batch, in order (a copy). */
    public List<HttpHook> getAfterHooks() {
        return afterHooks == null ? new ArrayList<>() : new ArrayList<>(afterHooks);
    }

    public void setAfterHooks(List<HttpHook> hooks) {
        this.afterHooks = new ArrayList<>(hooks);
    }

    @Override
    public String toString() {
        return getName();
    }
}
