package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One speech server (Chatterbox-TTS-Server, or ComfyUI for Higgs Audio v3) with the requests that make room for it on the GPU. Kept in the settings of the user; the api
 * key is stored protected ({@code InferenceServices.protectSecret}), never in plain text. One backend is one model: a book that needs
 * another model (a language Turbo can't read) picks another backend, whose start requests load that model.
 */
public class BackendConfig implements Cloneable {

    /** The model ids of Chatterbox-TTS-Server ({@code repo_id} of its settings). */
    public static final String MODEL_TURBO = "chatterbox-turbo";
    public static final String MODEL_ORIGINAL = "chatterbox";
    public static final String MODEL_MULTILINGUAL = "chatterbox-multilingual";
    /**
     * Higgs Audio v3 in ComfyUI (the TTS Audio Suite nodes): not a Chatterbox server, the address is the one of ComfyUI. Emotions and sounds are tokens in
     * the text ({@code HiggsText}).
     */
    public static final String MODEL_HIGGS = "higgs-audio-v3";

    public static final List<String> MODELS = List.of(MODEL_TURBO, MODEL_ORIGINAL, MODEL_MULTILINGUAL, MODEL_HIGGS);
    /** Mp3 (joined without decoding, see {@code Mp3Joiner}) or wav (lossless, see {@code WavJoiner}); opus can't be joined. */
    public static final List<String> FORMATS = List.of("mp3", "wav");

    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final double DEFAULT_TEMPERATURE = 0.8;
    public static final double DEFAULT_CFG_WEIGHT = 0.5;
    public static final double DEFAULT_SPEED = 1.0;
    public static final int DEFAULT_CHUNK_SIZE = 240;
    public static final int MIN_CHUNK_SIZE = 50;
    public static final int MAX_CHUNK_SIZE = 500;
    public static final int DEFAULT_MAX_WORD_ERROR_PERCENT = 30;
    public static final int DEFAULT_RETRIES = 2;
    public static final int MAX_RETRIES = 5;
    /** The longest a piece may be: {@link #BASE_SECONDS} plus {@link #SECONDS_PER_WORD} for every word (normal reading is about 0.4 s a word). */
    public static final double BASE_SECONDS = 4.0;
    public static final double SECONDS_PER_WORD = 0.6;

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
     * The uuid of the GPU lock of the application every batch takes (see {@code GpuService}): the server runs on a GPU shared with the language
     * model or other servers. Null: no lock.
     */
    private String gpuLock;
    /** Higgs only: the precision of the engine; null (a backend saved before) is {@link HiggsPrecision#FP32}. */
    private HiggsPrecision precision;
    /** Remake a piece that is far longer than its text (the model went on babbling): see {@link #maxSecondsFor}. Null: on. */
    private Boolean checkLength;
    /** After the batch, transcribe every piece with the speech recognition of the settings and remake the ones whose words are off. */
    private boolean checkWords;
    /** The share of words a transcription may get wrong before the piece is made again, in percent. */
    private int maxWordErrorPercent = DEFAULT_MAX_WORD_ERROR_PERCENT;
    /** How often a piece that failed a check is made again, each time with the next seed. */
    private int retries = DEFAULT_RETRIES;
    /** Higgs only: send the text of the clip (the words spoken in it) with the clip. Experimental. */
    private boolean sendClipText;
    /** True when the server is on this machine or the local network: the private addresses are allowed. */
    private boolean local = true;
    /** How long one speech request may take, in seconds. */
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    /**
     * Restart the server (the requests after the batch and before it) after this many pieces; 0: never. For a server that leaks memory and is
     * killed by the system in the middle of a long batch.
     */
    private int restartEveryPieces;
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

    /** True when the server is a ComfyUI with Higgs Audio v3, not a Chatterbox-TTS-Server. */
    public boolean isHiggs() {
        return MODEL_HIGGS.equals(getModel());
    }

    /** The uuid of the GPU lock, null for none. */
    public String getGpuLock() {
        return gpuLock == null || gpuLock.isBlank() ? null : gpuLock;
    }

    public void setGpuLock(String gpuLock) {
        this.gpuLock = gpuLock;
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

    /** After how many pieces the server is restarted during a batch, 0 for never. */
    public int getRestartEveryPieces() {
        return Math.max(0, restartEveryPieces);
    }

    public void setRestartEveryPieces(int restartEveryPieces) {
        this.restartEveryPieces = restartEveryPieces;
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

    /**
     * A copy with its own id and the given name: everything else the same (the address, the protected keys as they are stored, the voice, the defaults
     * and the requests before and after a batch), nothing shared with this one. For a backend of another model on the same machine.
     */
    public BackendConfig duplicate(String name) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        BackendConfig copy = gson.fromJson(gson.toJson(this), BackendConfig.class);
        copy.id = UUID.randomUUID().toString();
        copy.name = name;
        return copy;
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

    public HiggsPrecision getPrecision() {
        return precision == null ? HiggsPrecision.FP32 : precision;
    }

    public void setPrecision(HiggsPrecision precision) {
        this.precision = precision;
    }

    public boolean isCheckLength() {
        return checkLength == null || checkLength;
    }

    public void setCheckLength(boolean checkLength) {
        this.checkLength = checkLength;
    }

    public boolean isCheckWords() {
        return checkWords;
    }

    public void setCheckWords(boolean checkWords) {
        this.checkWords = checkWords;
    }

    public int getMaxWordErrorPercent() {
        return maxWordErrorPercent <= 0 ? DEFAULT_MAX_WORD_ERROR_PERCENT : Math.min(100, maxWordErrorPercent);
    }

    public void setMaxWordErrorPercent(int maxWordErrorPercent) {
        this.maxWordErrorPercent = maxWordErrorPercent;
    }

    public int getRetries() {
        return Math.max(0, Math.min(MAX_RETRIES, retries));
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean isSendClipText() {
        return sendClipText;
    }

    public void setSendClipText(boolean sendClipText) {
        this.sendClipText = sendClipText;
    }

    /** The longest a piece of this many words may be, in seconds. */
    public static double maxSecondsFor(int words) {
        return BASE_SECONDS + SECONDS_PER_WORD * Math.max(0, words);
    }

    @Override
    public String toString() {
        return getName();
    }

    /** The backends by name, as the user reads them (the case and the accents do not count), for a picker. */
    public static <T extends BackendConfig> java.util.List<T> byName(java.util.Collection<T> backends) {
        java.text.Collator collator = java.text.Collator.getInstance();
        collator.setStrength(java.text.Collator.PRIMARY);
        java.util.List<T> sorted = new java.util.ArrayList<>(backends);
        sorted.sort((a, b) -> collator.compare(a.getName() == null ? "" : a.getName(), b.getName() == null ? "" : b.getName()));
        return sorted;
    }
}
