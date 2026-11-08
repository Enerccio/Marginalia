package com.github.enerccio.marginalia.extensions.imagegenerator.model;

import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;

import java.util.ArrayList;
import java.util.List;
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
    /** Made in order before the first image of a session (unload the language model, start the image server...). */
    private List<HttpHook> beforeHooks;
    /** Made in order when the session is over (accepted, discarded, closed or idle): all of them, whatever happens to one. */
    private List<HttpHook> afterHooks;
    /** The one request of the settings saved before the lists: read when there is no list, dropped when the list is set. */
    private HttpHook before;
    private HttpHook after;
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

    /** The requests before the first image, in order (a copy). */
    public List<HttpHook> getBeforeHooks() {
        return hooks(beforeHooks, before);
    }

    public void setBeforeHooks(List<HttpHook> hooks) {
        this.beforeHooks = new ArrayList<>(hooks);
        this.before = null;
    }

    /** The requests after the last image, in order (a copy). */
    public List<HttpHook> getAfterHooks() {
        return hooks(afterHooks, after);
    }

    public void setAfterHooks(List<HttpHook> hooks) {
        this.afterHooks = new ArrayList<>(hooks);
        this.after = null;
    }

    private static List<HttpHook> hooks(List<HttpHook> list, HttpHook legacy) {
        if (list != null) {
            return new ArrayList<>(list);
        }
        return legacy == null || legacy.isEmpty() ? new ArrayList<>() : new ArrayList<>(List.of(legacy));
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
