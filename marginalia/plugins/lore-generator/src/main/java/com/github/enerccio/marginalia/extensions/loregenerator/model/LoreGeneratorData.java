package com.github.enerccio.marginalia.extensions.loregenerator.model;

import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.extensions.loregenerator.names.LegacyNameTools;

import java.util.List;
import java.util.Map;

public class LoreGeneratorData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.loregenerator";

    private String compactionPrompt;
    private Map<String, String> generationPrompts;

    /** Name of the generation prompt used last, selected when the dialog opens. */
    private String lastGenerationPrompt;
    /** The tools the model may use, chosen in the tool dialog; none checked means no tools. */
    private ToolSelection tools;

    /** The name categories saved before the tools were chosen in the tool dialog (with the prefix of their source), read once. */
    private List<String> nameCategories;
    /** The hint the user wrote for the name generator, saved before the tool dialog; read once. */
    private String nameToolsPrompt;
    /** The hint the user wrote for the name provider, saved before the tool dialog; read once. */
    private String nameProviderPrompt;

    public String getLastGenerationPrompt() {
        return lastGenerationPrompt;
    }

    public void setLastGenerationPrompt(String lastGenerationPrompt) {
        this.lastGenerationPrompt = lastGenerationPrompt;
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
