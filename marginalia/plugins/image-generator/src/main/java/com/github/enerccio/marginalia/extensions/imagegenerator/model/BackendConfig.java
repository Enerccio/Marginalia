package com.github.enerccio.marginalia.extensions.imagegenerator.model;

import java.util.UUID;

/**
 * One place images are made. Kept in the settings of the user; the api key is stored protected
 * ({@code InferenceServices.protectSecret}), never in plain text.
 */
public class BackendConfig {

    public static final int DEFAULT_WIDTH = 1024;
    public static final int DEFAULT_HEIGHT = 1024;
    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_IDLE_SECONDS = 120;


    private String id = UUID.randomUUID().toString();
    private String name = "";
    private BackendType type = BackendType.COMFYUI;
    private String baseUrl = "";
    /** Protected with the key of the installation. */
    private String protectedApiKey;
    /** OpenAI: the {@code model} of the request. */
    private String model;
    /** ComfyUI: the workflow with the placeholders. */
    private String workflow;
    /** True when the server is on this machine or the local network: the private addresses are allowed. */
    private boolean local = true;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private String negativePrompt = "";
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    /** Called before the first image of a session. */
    private HttpHook before = new HttpHook();
    /** Called when the session is over (accepted, discarded, closed or idle). */
    private HttpHook after = new HttpHook();
    /** The session is over this many seconds after the last image; 0 for never (it ends with the window). */
    private int idleSeconds = DEFAULT_IDLE_SECONDS;

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

    public BackendType getType() {
        return type == null ? BackendType.COMFYUI : type;
    }

    public void setType(BackendType type) {
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public int getWidth() {
        return width <= 0 ? DEFAULT_WIDTH : width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height <= 0 ? DEFAULT_HEIGHT : height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getNegativePrompt() {
        return negativePrompt == null ? "" : negativePrompt;
    }

    public void setNegativePrompt(String negativePrompt) {
        this.negativePrompt = negativePrompt;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public HttpHook getBefore() {
        return before == null ? new HttpHook() : before;
    }

    public void setBefore(HttpHook before) {
        this.before = before;
    }

    public HttpHook getAfter() {
        return after == null ? new HttpHook() : after;
    }

    public void setAfter(HttpHook after) {
        this.after = after;
    }

    public int getIdleSeconds() {
        return Math.max(0, idleSeconds);
    }

    public void setIdleSeconds(int idleSeconds) {
        this.idleSeconds = idleSeconds;
    }

    @Override
    public String toString() {
        return getName();
    }
}
