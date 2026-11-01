package com.github.enerccio.marginalia.extensions.imagegenerator.model;

import java.util.ArrayList;
import java.util.List;

/** What the user keeps for the plugin, in the attributes of the user settings. */
public class ImageSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.imagegenerator";

    public static final String DEFAULT_PROMPT_INSTRUCTION =
            "You write prompts for an image generation model. From the scene below, write one prompt that describes the picture "
                    + "of its most visual moment: who is there (looks, clothes, pose), where, the light and the mood. Use short "
                    + "comma separated phrases, no sentences, no names of people (describe them), no text in the picture. "
                    + "Answer with the prompt only.";

    private List<BackendConfig> backends = new ArrayList<>();
    private String selectedBackendId;
    /** The model that writes the prompts, null for the one of the book. */
    private Long selectedAiId;
    private Long selectedProtocolId;
    /** Replaces the built-in instruction of the prompt writer. */
    private String promptInstruction;
    /** Whether the lorebook entries a part activates go to the prompt writer; remembered from the last window. */
    private boolean sendLore;
    /** Added in front of every prompt, for the style of the book. */
    private String stylePrefix = "";

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

    public String getPromptInstruction() {
        return promptInstruction == null || promptInstruction.isBlank() ? DEFAULT_PROMPT_INSTRUCTION : promptInstruction;
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

    public BackendConfig find(String id) {
        if (id == null) {
            return null;
        }
        return getBackends().stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }
}
