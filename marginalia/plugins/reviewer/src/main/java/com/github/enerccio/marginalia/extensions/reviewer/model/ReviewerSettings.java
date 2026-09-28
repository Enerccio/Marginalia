package com.github.enerccio.marginalia.extensions.reviewer.model;

public class ReviewerSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.reviewer";

    private String reviewPrompt = "Ignore previous instructions, instead you are a reddit simulator. " +
            "Take the text before as a story work in progress and generate a reddit thread " +
            "(generate name and subreddit for it based on content) asking people to review said story. " +
            "Then generate at least 30 replies from various users reviewing the story. " +
            "Include both positive and negative feedback, have at least 1 troll reply and 1 pun reply. " +
            "Messages can be replies to other messages creating threads.";
    private String reviewPromptPre = "";
    private boolean removeInstruction = true;
    private boolean removeUser = false;
    private boolean removeCharacter = false;
    private boolean removeWorldInfo = false;
    private Long selectedAiId = null;
    private Long selectedProtocolId = null;

    public String getReviewPrompt() { return reviewPrompt; }
    public void setReviewPrompt(String reviewPrompt) { this.reviewPrompt = reviewPrompt; }

    public String getReviewPromptPre() { return reviewPromptPre; }
    public void setReviewPromptPre(String reviewPromptPre) { this.reviewPromptPre = reviewPromptPre; }

    public boolean isRemoveInstruction() { return removeInstruction; }
    public void setRemoveInstruction(boolean removeInstruction) { this.removeInstruction = removeInstruction; }

    public boolean isRemoveUser() { return removeUser; }
    public void setRemoveUser(boolean removeUser) { this.removeUser = removeUser; }

    public boolean isRemoveCharacter() { return removeCharacter; }
    public void setRemoveCharacter(boolean removeCharacter) { this.removeCharacter = removeCharacter; }

    public boolean isRemoveWorldInfo() { return removeWorldInfo; }
    public void setRemoveWorldInfo(boolean removeWorldInfo) { this.removeWorldInfo = removeWorldInfo; }

    public Long getSelectedAiId() { return selectedAiId; }
    public void setSelectedAiId(Long selectedAiId) { this.selectedAiId = selectedAiId; }

    public Long getSelectedProtocolId() { return selectedProtocolId; }
    public void setSelectedProtocolId(Long selectedProtocolId) { this.selectedProtocolId = selectedProtocolId; }
}