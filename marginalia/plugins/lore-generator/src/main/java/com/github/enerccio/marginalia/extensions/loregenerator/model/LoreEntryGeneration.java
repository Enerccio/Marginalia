package com.github.enerccio.marginalia.extensions.loregenerator.model;

import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.extensions.loregenerator.names.LegacyNameTools;

import java.util.List;

/**
 * The options of the generation that was copied to a lore entry, kept in the entry's attributes under
 * {@link LoreGeneratorData#KEY} so that the generator of that entry opens with them.
 */
public class LoreEntryGeneration {

    /** Name of the saved generation prompt that was selected; the text below is what was really sent. */
    private String promptName;
    private String prompt;
    /** {@code LoreScope} name. */
    private String scope;
    /** {@code EntryLimit} name. */
    private String limit;
    /** The tools the model had. */
    private ToolSelection tools;
    /** The name categories the model had, saved before the tool dialog (with the prefix of their source); read only. */
    private List<String> nameCategories;

    public String getPromptName() {
        return promptName;
    }

    public void setPromptName(String promptName) {
        this.promptName = promptName;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public ToolSelection getTools() {
        return tools;
    }

    public void setTools(ToolSelection tools) {
        this.tools = tools;
    }

    /** The tools of this generation: the saved ones, else the ones made from the categories saved before the tool dialog; null for none saved. */
    public ToolSelection tools() {
        return tools != null ? tools : LegacyNameTools.migrate(nameCategories, null, null);
    }
}
