package com.github.enerccio.marginalia.extensions.reviewer.model;

public class AdvancedOptions {
    private String prompt;
    private boolean usePromptInfo = true;
    private boolean lorebook = false;
    private Integer tokenLimit = null;
    private String lorebookTrigger = "review";

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public boolean isUsePromptInfo() { return usePromptInfo; }
    public void setUsePromptInfo(boolean usePromptInfo) { this.usePromptInfo = usePromptInfo; }

    public boolean isLorebook() { return lorebook; }
    public void setLorebook(boolean lorebook) { this.lorebook = lorebook; }

    public Integer getTokenLimit() { return tokenLimit; }
    public void setTokenLimit(Integer tokenLimit) { this.tokenLimit = tokenLimit; }

    public String getLorebookTrigger() { return lorebookTrigger; }
    public void setLorebookTrigger(String lorebookTrigger) { this.lorebookTrigger = lorebookTrigger; }
}