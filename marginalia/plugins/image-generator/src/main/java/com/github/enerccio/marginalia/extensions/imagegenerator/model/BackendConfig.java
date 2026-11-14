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
    public static final double DEFAULT_LORA_STRENGTH = 1.0;


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
    /**
     * The name of the GPU the server runs on, when other servers (of this or another extension) use the same one: backends with the same name wait
     * for each other. Empty: the address of the server says it.
     */
    private String gpu = "";
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    /** Saved before the negative prompt was a setting of the user and the book: read once by the settings panel, not used otherwise. */
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
    /** How the prompt is written for this backend; null in settings saved before: see {@link #instructionFor}. */
    private PromptForm promptForm;
    /** The instruction of {@link PromptForm#CUSTOM}. */
    private String promptInstruction;
    /** ComfyUI: the LoRA last chosen in the window for {@code {{lora}}}; null for none. */
    private String lora;
    /** ComfyUI: the strength last chosen for {@code {{lora_strength}}}; null for the default. */
    private Double loraStrength;

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
     * What the requests before an image leave loaded on the GPU: this server with this model and workflow. Sessions with the same mode share the
     * load; one with another mode (another model, or another extension's) waits for them to end.
     */
    public String gateMode() {
        return "image:" + com.github.enerccio.marginalia.extensions.httphooks.UrlGuard.origin(getBaseUrl()) + "|" + type + "|" + model + "|" + workflow;
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

    /** See {@link #negativePrompt}: only for taking over an old setting. */
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

    /** The form chosen; a backend saved before the choice existed writes tags, or the instruction the user had then. */
    public PromptForm getPromptForm() {
        return promptForm == null ? PromptForm.TAGS : promptForm;
    }

    /** Whether the form was chosen (a backend saved before the choice existed has none). */
    public boolean hasPromptForm() {
        return promptForm != null;
    }

    public void setPromptForm(PromptForm promptForm) {
        this.promptForm = promptForm;
    }

    public String getPromptInstruction() {
        return promptInstruction == null ? "" : promptInstruction;
    }

    public void setPromptInstruction(String promptInstruction) {
        this.promptInstruction = promptInstruction;
    }

    /**
     * The instruction the prompt writer gets for this backend.
     *
     * @param legacy the one instruction of the user from before the backends had their own (null for none): a backend that never chose
     *               a form keeps using it
     */
    public String instructionFor(String legacy) {
        if (promptForm == null) {
            return legacy == null || legacy.isBlank() ? PromptForm.TAGS.instruction() : legacy;
        }
        if (promptForm == PromptForm.CUSTOM) {
            return getPromptInstruction().isBlank() ? PromptForm.TAGS.instruction() : promptInstruction;
        }
        return promptForm.instruction();
    }

    public String getLora() {
        return lora == null || lora.isBlank() ? null : lora;
    }

    public void setLora(String lora) {
        this.lora = lora;
    }

    public double getLoraStrength() {
        return loraStrength == null ? DEFAULT_LORA_STRENGTH : loraStrength;
    }

    public void setLoraStrength(double loraStrength) {
        this.loraStrength = loraStrength;
    }

    /** Whether the window offers the LoRA choice: a ComfyUI workflow with {@code {{lora}}}. */
    public boolean usesLora() {
        return getType() == BackendType.COMFYUI
                && com.github.enerccio.marginalia.extensions.imagegenerator.service.WorkflowTemplate.usesLora(workflow);
    }

    @Override
    public String toString() {
        return getName();
    }
}
