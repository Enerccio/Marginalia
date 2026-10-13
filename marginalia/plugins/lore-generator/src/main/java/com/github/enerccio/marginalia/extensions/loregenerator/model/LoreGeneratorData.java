package com.github.enerccio.marginalia.extensions.loregenerator.model;

import java.util.List;
import java.util.Map;

public class LoreGeneratorData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.loregenerator";

    private String compactionPrompt;
    private Map<String, String> generationPrompts;

    /** Name of the generation prompt used last, selected when the dialog opens. */
    private String lastGenerationPrompt;
    /** Categories of the name generator the model may use; none selected means no tools. */
    private List<String> nameCategories;

    public String getLastGenerationPrompt() {
        return lastGenerationPrompt;
    }

    public void setLastGenerationPrompt(String lastGenerationPrompt) {
        this.lastGenerationPrompt = lastGenerationPrompt;
    }

    public List<String> getNameCategories() {
        return nameCategories;
    }

    public void setNameCategories(List<String> nameCategories) {
        this.nameCategories = nameCategories;
    }

    /** Hint about the name tools added to the generation prompt; blank means {@code Defaults.NAME_TOOLS_PROMPT}. */
    private String nameToolsPrompt;

    public String getNameToolsPrompt() {
        return nameToolsPrompt;
    }

    public void setNameToolsPrompt(String nameToolsPrompt) {
        this.nameToolsPrompt = nameToolsPrompt;
    }

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
