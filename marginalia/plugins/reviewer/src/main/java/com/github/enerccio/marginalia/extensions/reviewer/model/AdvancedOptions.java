package com.github.enerccio.marginalia.extensions.reviewer.model;

public class AdvancedOptions {
    private String prompt;
    private boolean usePromptInfo = true;
    private boolean lorebook = false;
    /** Send the summaries of the story before the reviewed part, then the parts after the newest summary. */
    private boolean summaries = false;
    /** Send the text of the parts as it is stored (with the translated speech) instead of the source a model reads. */
    private boolean originalText = false;
    private Integer tokenLimit = null;

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public boolean isUsePromptInfo() { return usePromptInfo; }
    public void setUsePromptInfo(boolean usePromptInfo) { this.usePromptInfo = usePromptInfo; }

    public boolean isLorebook() { return lorebook; }
    public void setLorebook(boolean lorebook) { this.lorebook = lorebook; }

    public boolean isSummaries() { return summaries; }
    public void setSummaries(boolean summaries) { this.summaries = summaries; }

    public boolean isOriginalText() { return originalText; }
    public void setOriginalText(boolean originalText) { this.originalText = originalText; }

    public Integer getTokenLimit() { return tokenLimit; }
    public void setTokenLimit(Integer tokenLimit) { this.tokenLimit = tokenLimit; }
}