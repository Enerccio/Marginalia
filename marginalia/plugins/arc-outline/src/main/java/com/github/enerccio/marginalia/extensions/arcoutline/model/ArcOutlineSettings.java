package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.extensions.arcoutline.names.LegacyNameTools;

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
    /** The tools the model may use, chosen in the tool dialog; none checked means no tools. */
    private ToolSelection tools;
    /** The name categories (with the prefix of their source) saved before the tools were chosen in the tool dialog; read once. */
    private List<String> nameCategories;
    /** The hint the user wrote for the name generator, saved before the tool dialog; read once. */
    private String nameToolsPrompt;
    /** The hint the user wrote for the name provider, saved before the tool dialog; read once. */
    private String nameProviderPrompt;

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

    public ToolSelection getTools() {
        return tools;
    }

    public void setTools(ToolSelection tools) {
        this.tools = tools;
    }

    /**
     * Turns what was saved before the tool dialog into {@link #getTools()} when there is no choice yet, and forgets it:
     * what is saved next has only the new form.
     */
    public void migrateLegacyNameTools() {
        if (tools == null) {
            tools = LegacyNameTools.migrate(nameCategories, nameToolsPrompt, nameProviderPrompt);
        }
        nameCategories = null;
        nameToolsPrompt = null;
        nameProviderPrompt = null;
    }
}
