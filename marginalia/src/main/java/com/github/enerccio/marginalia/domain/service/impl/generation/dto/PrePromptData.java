package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import com.github.enerccio.marginalia.domain.service.TurnInput;

public class PrePromptData {

    private String jailbreak;
    private long jailbreakTokens;
    private String backgroundLore;
    private long backgroundLoreTokens;
    private String backgroundUserLore;
    private long backgroundUserLoreTokens;
    private String generalTemplate;
    private String pov;
    private String style;
    private String tense;
    private String systemPrompt;
    private long systemPromptTokens;
    private String userPrompt;
    private String userPromptProcessed;
    private long userPromptProcessedTokens;
    private TurnInput turnInput;
    /**
     * Tokens extensions need for text they add to the prompt after the context budget is computed (e.g. a message
     * inserted into the payload). Taken out of the room for the story in {@code PrepareContentStep}, so it must be set
     * before {@link com.github.enerccio.marginalia.domain.service.impl.generation.Events#BEFORE_SUMMARIES} completes.
     * Extensions add to it, not overwrite it.
     */
    private long reservedTokens;

    public String getJailbreak() {
        return jailbreak;
    }

    public void setJailbreak(String jailbreak) {
        this.jailbreak = jailbreak;
    }

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getGeneralTemplate() {
        return generalTemplate;
    }

    public void setGeneralTemplate(String generalTemplate) {
        this.generalTemplate = generalTemplate;
    }

    public String getPov() {
        return pov;
    }

    public void setPov(String pov) {
        this.pov = pov;
    }

    public String getTense() {
        return tense;
    }

    public void setTense(String tense) {
        this.tense = tense;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public TurnInput getTurnInput() {
        return turnInput;
    }

    public void setTurnInput(TurnInput turnInput) {
        this.turnInput = turnInput;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public long getJailbreakTokens() {
        return jailbreakTokens;
    }

    public void setJailbreakTokens(long jailbreakTokens) {
        this.jailbreakTokens = jailbreakTokens;
    }

    public long getBackgroundLoreTokens() {
        return backgroundLoreTokens;
    }

    public void setBackgroundLoreTokens(long backgroundLoreTokens) {
        this.backgroundLoreTokens = backgroundLoreTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public long getSystemPromptTokens() {
        return systemPromptTokens;
    }

    public void setSystemPromptTokens(long systemPromptTokens) {
        this.systemPromptTokens = systemPromptTokens;
    }

    public String getUserPromptProcessed() {
        return userPromptProcessed;
    }

    public void setUserPromptProcessed(String userPromptProcessed) {
        this.userPromptProcessed = userPromptProcessed;
    }

    public long getUserPromptProcessedTokens() {
        return userPromptProcessedTokens;
    }

    public void setUserPromptProcessedTokens(long userPromptProcessedTokens) {
        this.userPromptProcessedTokens = userPromptProcessedTokens;
    }

    public String getBackgroundUserLore() {
        return backgroundUserLore;
    }

    public void setBackgroundUserLore(String backgroundUserLore) {
        this.backgroundUserLore = backgroundUserLore;
    }

    public long getBackgroundUserLoreTokens() {
        return backgroundUserLoreTokens;
    }

    public void setBackgroundUserLoreTokens(long backgroundUserLoreTokens) {
        this.backgroundUserLoreTokens = backgroundUserLoreTokens;
    }

    public long getReservedTokens() {
        return reservedTokens;
    }

    public void setReservedTokens(long reservedTokens) {
        this.reservedTokens = reservedTokens;
    }
}
