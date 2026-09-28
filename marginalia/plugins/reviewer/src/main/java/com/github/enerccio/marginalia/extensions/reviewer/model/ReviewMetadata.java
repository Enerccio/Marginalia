package com.github.enerccio.marginalia.extensions.reviewer.model;

public class ReviewMetadata {
    private String reasoning = "";
    private Long reasoningTime = null;
    private boolean reasoningDone = false;
    private AdvancedOptions advancedInfo;

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public Long getReasoningTime() { return reasoningTime; }
    public void setReasoningTime(Long reasoningTime) { this.reasoningTime = reasoningTime; }

    public boolean isReasoningDone() { return reasoningDone; }
    public void setReasoningDone(boolean reasoningDone) { this.reasoningDone = reasoningDone; }

    public AdvancedOptions getAdvancedInfo() { return advancedInfo; }
    public void setAdvancedInfo(AdvancedOptions advancedInfo) { this.advancedInfo = advancedInfo; }
}

