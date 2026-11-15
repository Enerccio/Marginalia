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
    public static final double DEFAULT_LORA_STRENGTH = 1.0;


    private String id = UUID.randomUUID().toString();
    private String name = "";
    private BackendType type = BackendType.COMFYUI;
    private String baseUrl = "";
    /** Protected with the key of the installation. */
    private String protectedApiKey;
    /**
     * The key of the requests before and after the images, protected like the api key: sent as {@code Authorization: Bearer} with every
     * request that has none of its own. Null: the api key.
     */
    private String protectedHooksKey;
    /** OpenAI: the {@code model} of the request. */
    private String model;
    /** ComfyUI: the workflow with the placeholders. */
    private String workflow;
    /** True when the server is on this machine or the local network: the private addresses are allowed. */
    private boolean local = true;
    /**
     * The uuid of the GPU lock of the application every image takes (see {@code GpuService}): the server runs on a GPU shared with the language
     * model or other servers. Null: no lock.
     */
    private String gpuLock;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    /** Saved before the negative prompt was a setting of the user and the book: read once by the settings panel, not used otherwise. */
    private String negativePrompt = "";
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    /** Made in order before every image, inside the GPU lock (start a container, wake the server...). */
    private List<HttpHook> beforeHooks;
    /** Made in order after every image, inside the GPU lock: all of them, whatever happens to one. */
    private List<HttpHook> afterHooks;
    /** The one request of the settings saved before the lists: read when there is no list, dropped when the list is set. */
    private HttpHook before;
    private HttpHook after;
    /** How the prompt is written for this backend; null in settings saved before: see {@link #instructionFor}. */
    private PromptForm promptForm;
    /** The instruction of {@link PromptForm#CUSTOM}. */
    private String promptInstruction;
    /** ComfyUI: the LoRA last chosen in the window for {@code {{lora}}}; null for none. */
    private String lora;
    /**
     * ComfyUI: the LoRA files the server listed the last time it could be asked. A server that shares the GPU is often not running
     * when a window opens (its before hooks start it), so the window offers these until it can ask again.
     */
    private List<String> knownLoras;
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

    public String getProtectedHooksKey() {
        return protectedHooksKey;
    }

    public void setProtectedHooksKey(String protectedHooksKey) {
        this.protectedHooksKey = protectedHooksKey;
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

    /** The LoRA files the server listed last (a copy), empty when it never could. */
    public List<String> getKnownLoras() {
        return knownLoras == null ? new ArrayList<>() : new ArrayList<>(knownLoras);
    }

    public void setKnownLoras(List<String> knownLoras) {
        this.knownLoras = knownLoras == null ? null : new ArrayList<>(knownLoras);
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

    /** The backends by name, as the user reads them (the case and the accents do not count), for a picker. */
    public static <T extends BackendConfig> java.util.List<T> byName(java.util.Collection<T> backends) {
        java.text.Collator collator = java.text.Collator.getInstance();
        collator.setStrength(java.text.Collator.PRIMARY);
        java.util.List<T> sorted = new java.util.ArrayList<>(backends);
        sorted.sort((a, b) -> collator.compare(a.getName() == null ? "" : a.getName(), b.getName() == null ? "" : b.getName()));
        return sorted;
    }
}
