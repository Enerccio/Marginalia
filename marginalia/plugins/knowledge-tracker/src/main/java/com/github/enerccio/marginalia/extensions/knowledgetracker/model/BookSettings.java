package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings of the book, saved in the state.
 */
public class BookSettings {

    /** Whether the block is inserted into the prompt by the plugin (the macro works either way). */
    private boolean inPrompt = false;
    /** Number of prompt messages that follow the block, as for the Author's Note. */
    private int depth = 1;
    private LLMRole role = LLMRole.SYSTEM;
    /** The "knows nothing else" line of the block. */
    private boolean closedWorld = true;
    /**
     * Whether the parts written since the last time are processed by themselves, just before the prompt of the next
     * generation is built. Off: nothing runs by itself (the state that exists is still inserted).
     */
    private boolean autoProcess = false;
    /**
     * Whether processing asks the model what the tracked characters learned. Off (the default): processing only notes who
     * has met whom, which costs nothing.
     */
    private boolean extractWithModel = false;
    /** Parts read by one run of the extraction. */
    private int partsPerRun = 1;
    /** Whether the changes the model proposes are shown for review (guided) or applied at once and only logged. */
    private boolean askBeforeApplying = true;
    /** The tidy pass reads this many of the last processed parts for context. */
    private int tidyContextParts = 10;
    /** A character with fewer items that the tidy pass may touch is left alone. */
    private int tidyMinItems = 8;
    private LoreMode loreMode = LoreMode.RESOLVE;
    /** The model for the extraction (uuid), {@code null} = the book's model. */
    private String aiUuid;
    /** The protocol for the extraction (uuid), {@code null} = the book's protocol. */
    private String protocolUuid;
    /** Separators of the present characters field; empty = the user's setting, then the built-in ones. */
    private List<String> separators = new ArrayList<>();

    public boolean isInPrompt() {
        return inPrompt;
    }

    public void setInPrompt(boolean inPrompt) {
        this.inPrompt = inPrompt;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = Math.max(0, depth);
    }

    public LLMRole getRole() {
        return role == null ? LLMRole.SYSTEM : role;
    }

    public void setRole(LLMRole role) {
        this.role = role;
    }

    public boolean isAutoProcess() {
        return autoProcess;
    }

    public void setAutoProcess(boolean autoProcess) {
        this.autoProcess = autoProcess;
    }

    public boolean isExtractWithModel() {
        return extractWithModel;
    }

    public void setExtractWithModel(boolean extractWithModel) {
        this.extractWithModel = extractWithModel;
    }

    public int getPartsPerRun() {
        return Math.max(1, partsPerRun);
    }

    public void setPartsPerRun(int partsPerRun) {
        this.partsPerRun = Math.max(1, partsPerRun);
    }

    public int getTidyContextParts() {
        return Math.max(1, tidyContextParts == 0 ? 10 : tidyContextParts);
    }

    public void setTidyContextParts(int tidyContextParts) {
        this.tidyContextParts = Math.max(1, tidyContextParts);
    }

    public int getTidyMinItems() {
        return Math.max(2, tidyMinItems == 0 ? 8 : tidyMinItems);
    }

    public void setTidyMinItems(int tidyMinItems) {
        this.tidyMinItems = Math.max(2, tidyMinItems);
    }

    public boolean isAskBeforeApplying() {
        return askBeforeApplying;
    }

    public void setAskBeforeApplying(boolean askBeforeApplying) {
        this.askBeforeApplying = askBeforeApplying;
    }

    public LoreMode getLoreMode() {
        return loreMode == null ? LoreMode.RESOLVE : loreMode;
    }

    public void setLoreMode(LoreMode loreMode) {
        this.loreMode = loreMode;
    }

    public String getAiUuid() {
        return aiUuid;
    }

    public void setAiUuid(String aiUuid) {
        this.aiUuid = aiUuid;
    }

    public String getProtocolUuid() {
        return protocolUuid;
    }

    public void setProtocolUuid(String protocolUuid) {
        this.protocolUuid = protocolUuid;
    }

    public boolean isClosedWorld() {
        return closedWorld;
    }

    public void setClosedWorld(boolean closedWorld) {
        this.closedWorld = closedWorld;
    }

    public List<String> getSeparators() {
        if (separators == null) {
            separators = new ArrayList<>();
        }
        return separators;
    }

    public void setSeparators(List<String> separators) {
        this.separators = new ArrayList<>(separators);
    }
}
