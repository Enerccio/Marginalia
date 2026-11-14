package com.github.enerccio.marginalia.extensions.imagegenerator.model;

import java.util.ArrayList;
import java.util.List;

/** What the user keeps for the plugin, in the attributes of the user settings. */
public class ImageSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.imagegenerator";


    private List<BackendConfig> backends = new ArrayList<>();
    private String selectedBackendId;
    /** The model that writes the prompts, null for the one of the book. */
    private Long selectedAiId;
    private Long selectedProtocolId;
    /** The one instruction of the prompt writer from before the backends had a prompt form: a backend saved then keeps it as its free text. */
    private String promptInstruction;
    /** Whether the lorebook entries a part activates go to the prompt writer; remembered from the last window. */
    private boolean sendLore;
    /** Added in front of every prompt, for the style of the book: the user level, a book or a folder can change it. */
    private String stylePrefix = "";
    /** The negative prompt of the user level (a book or a folder can change it); blank: the backend's. */
    private String negativePrompt = "";

    public List<BackendConfig> getBackends() {
        if (backends == null) {
            backends = new ArrayList<>();
        }
        return backends;
    }

    public void setBackends(List<BackendConfig> backends) {
        this.backends = backends;
    }

    public String getSelectedBackendId() {
        return selectedBackendId;
    }

    public void setSelectedBackendId(String selectedBackendId) {
        this.selectedBackendId = selectedBackendId;
    }

    public Long getSelectedAiId() {
        return selectedAiId;
    }

    public void setSelectedAiId(Long selectedAiId) {
        this.selectedAiId = selectedAiId;
    }

    public Long getSelectedProtocolId() {
        return selectedProtocolId;
    }

    public void setSelectedProtocolId(Long selectedProtocolId) {
        this.selectedProtocolId = selectedProtocolId;
    }

    /** What the user set, null when the built-in instruction is used. */
    public String getStoredPromptInstruction() {
        return promptInstruction == null || promptInstruction.isBlank() ? null : promptInstruction;
    }

    public void setPromptInstruction(String promptInstruction) {
        this.promptInstruction = promptInstruction;
    }

    public boolean isSendLore() {
        return sendLore;
    }

    public void setSendLore(boolean sendLore) {
        this.sendLore = sendLore;
    }

    public String getStylePrefix() {
        return stylePrefix == null ? "" : stylePrefix;
    }

    public void setStylePrefix(String stylePrefix) {
        this.stylePrefix = stylePrefix;
    }

    public String getNegativePrompt() {
        return negativePrompt == null ? "" : negativePrompt;
    }

    public void setNegativePrompt(String negativePrompt) {
        this.negativePrompt = negativePrompt;
    }

    public BackendConfig find(String id) {
        if (id == null) {
            return null;
        }
        return getBackends().stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }
}
