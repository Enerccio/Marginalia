package com.github.enerccio.marginalia.extensions.loregenerator.model;

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
    /** Categories of the name generator the model had. */
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

    public List<String> getNameCategories() {
        return nameCategories;
    }

    public void setNameCategories(List<String> nameCategories) {
        this.nameCategories = nameCategories;
    }
}
