package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.TurnInput;

import java.util.ArrayList;
import java.util.List;

public class PrePromptData {

    private String jailbreak;
    private long jailbreakTokens;
    private List<LorebookEntry> allEntries = new ArrayList<>();
    private List<LorebookEntry> activatedEntries = new ArrayList<>();
    private String backgroundLore;
    private long backgroundLoreTokens;
    private String generalTemplate;
    private String pov;
    private String style;
    private String tense;
    private String fullPrompt;
    private long fullPromptTokens;
    private String userPrompt;
    private String userPromptProcessed;
    private long userPromptProcessedTokens;
    private TurnInput turnInput;

    public String getJailbreak() {
        return jailbreak;
    }

    public void setJailbreak(String jailbreak) {
        this.jailbreak = jailbreak;
    }

    public List<LorebookEntry> getAllEntries() {
        return allEntries;
    }

    public void setAllEntries(List<LorebookEntry> allEntries) {
        this.allEntries = allEntries;
    }

    public List<LorebookEntry> getActivatedEntries() {
        return activatedEntries;
    }

    public void setActivatedEntries(List<LorebookEntry> activatedEntries) {
        this.activatedEntries = activatedEntries;
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

    public String getFullPrompt() {
        return fullPrompt;
    }

    public void setFullPrompt(String fullPrompt) {
        this.fullPrompt = fullPrompt;
    }

    public long getFullPromptTokens() {
        return fullPromptTokens;
    }

    public void setFullPromptTokens(long fullPromptTokens) {
        this.fullPromptTokens = fullPromptTokens;
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
}
