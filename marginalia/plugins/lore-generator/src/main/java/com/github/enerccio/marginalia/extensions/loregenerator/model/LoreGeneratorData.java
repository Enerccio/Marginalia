package com.github.enerccio.marginalia.extensions.loregenerator.model;

import java.util.Map;

public class LoreGeneratorData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.loregenerator";

    private String compactionPrompt;
    private Map<String, String> generationPrompts;

    public String getCompactionPrompt() {
        return compactionPrompt;
    }

    public void setCompactionPrompt(String compactionPrompt) {
        this.compactionPrompt = compactionPrompt;
    }

    public Map<String, String> getGenerationPrompts() {
        return generationPrompts;
    }

    public void setGenerationPrompts(Map<String, String> generationPrompts) {
        this.generationPrompts = generationPrompts;
    }
}
