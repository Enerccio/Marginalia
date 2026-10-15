package com.github.enerccio.marginalia.extensions.arcoutline.model;

import java.util.List;

/**
 * What is stored in the attributes of the user settings under {@link #KEY}. A blank prompt means the default one.
 */
public class ArcOutlineSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.arcoutline.settings";

    /** The template of the prompt for generating an arc, see {@code Defaults.GENERATION_PROMPT}. */
    private String generationPrompt;
    /** The template of the prompt for generating the chapters of an arc, see {@code Defaults.CHAPTER_GENERATION_PROMPT}. */
    private String chapterGenerationPrompt;
    /** The template of the prompt for importing an arc from the story, see {@code Defaults.IMPORT_PROMPT}. */
    private String importPrompt;
    /** Hint about the name tools added to the prompt when name categories are selected. */
    private String nameToolsPrompt;
    /** Categories of the name generator the model may use; none selected means no tools. */
    private List<String> nameCategories;

    public String getGenerationPrompt() {
        return generationPrompt;
    }

    public void setGenerationPrompt(String generationPrompt) {
        this.generationPrompt = generationPrompt;
    }

    public String getChapterGenerationPrompt() {
        return chapterGenerationPrompt;
    }

    public void setChapterGenerationPrompt(String chapterGenerationPrompt) {
        this.chapterGenerationPrompt = chapterGenerationPrompt;
    }

    public String getImportPrompt() {
        return importPrompt;
    }

    public void setImportPrompt(String importPrompt) {
        this.importPrompt = importPrompt;
    }

    public String getNameToolsPrompt() {
        return nameToolsPrompt;
    }

    public void setNameToolsPrompt(String nameToolsPrompt) {
        this.nameToolsPrompt = nameToolsPrompt;
    }

    public List<String> getNameCategories() {
        return nameCategories;
    }

    public void setNameCategories(List<String> nameCategories) {
        this.nameCategories = nameCategories;
    }
}
