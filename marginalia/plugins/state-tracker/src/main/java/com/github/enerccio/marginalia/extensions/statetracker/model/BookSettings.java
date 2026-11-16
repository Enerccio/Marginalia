package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.storytracker.llm.LoreMode;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings of the plugin for one book.
 */
public class BookSettings {

    /** Insert the block into the prompt of every generation. */
    private boolean inPrompt = false;
    /** Messages after the block: 0 = after the instructions, 1 = right before them. */
    private int depth = 1;
    private LLMRole role = LLMRole.SYSTEM;
    /** How the present characters field is split; empty = the built-in separators. */
    private List<String> separators = new ArrayList<>();
    /** The model reads the parts that become final (after a generation, before the next prompt). */
    private boolean autoProcess = false;
    /** Every run ends in the review (guided); off = applied at once. */
    private boolean askBeforeApplying = true;
    /** N, the parts one request reads. */
    private int partsPerRun = 1;
    private LoreMode loreMode = LoreMode.RESOLVE;
    /** The model and protocol of the extraction; {@code null} = the book's. */
    private String aiUuid;
    private String protocolUuid;
    /** The tidy runs by itself at the end of a run that applies its changes without asking, every {@link #tidyEveryParts} parts. */
    private boolean autoTidy = true;
    /** K: the automatic tidy is due after this many parts, and the tidy reads the last K processed parts. */
    private int tidyEveryParts = 10;

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

    public List<String> getSeparators() {
        if (separators == null) {
            separators = new ArrayList<>();
        }
        return separators;
    }

    public void setSeparators(List<String> separators) {
        this.separators = new ArrayList<>(separators);
    }

    public boolean isAutoProcess() {
        return autoProcess;
    }

    public void setAutoProcess(boolean autoProcess) {
        this.autoProcess = autoProcess;
    }

    public boolean isAskBeforeApplying() {
        return askBeforeApplying;
    }

    public void setAskBeforeApplying(boolean askBeforeApplying) {
        this.askBeforeApplying = askBeforeApplying;
    }

    public int getPartsPerRun() {
        return Math.max(1, partsPerRun);
    }

    public void setPartsPerRun(int partsPerRun) {
        this.partsPerRun = Math.max(1, partsPerRun);
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

    public boolean isAutoTidy() {
        return autoTidy;
    }

    public void setAutoTidy(boolean autoTidy) {
        this.autoTidy = autoTidy;
    }

    public int getTidyEveryParts() {
        return tidyEveryParts <= 0 ? 10 : tidyEveryParts;
    }

    public void setTidyEveryParts(int tidyEveryParts) {
        this.tidyEveryParts = Math.max(1, tidyEveryParts);
    }
}
