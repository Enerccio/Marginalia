package com.github.enerccio.marginalia.extensions.reviewer.model;

import java.util.UUID;

public class ReviewerSetting {

    // stable id of the profile, books refer to it (the name can be changed); settings saved before it was added get one on load
    private String uuid = UUID.randomUUID().toString();

    private String reviewPrompt = "Ignore previous instructions, instead you are a reddit simulator. " +
            "Take the text before as a story work in progress and generate a reddit thread " +
            "(generate name and subreddit for it based on content) asking people to review said story. " +
            "Then generate at least 30 replies from various users reviewing the story. " +
            "Include both positive and negative feedback, have at least 1 troll reply and 1 pun reply. " +
            "Messages can be replies to other messages creating threads.";
    private String reviewPromptPre = "";
    private Long selectedAiId = null;
    private Long selectedProtocolId = null;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getReviewPrompt() { return reviewPrompt; }
    public void setReviewPrompt(String reviewPrompt) { this.reviewPrompt = reviewPrompt; }

    public String getReviewPromptPre() { return reviewPromptPre; }
    public void setReviewPromptPre(String reviewPromptPre) { this.reviewPromptPre = reviewPromptPre; }

    public Long getSelectedAiId() { return selectedAiId; }
    public void setSelectedAiId(Long selectedAiId) { this.selectedAiId = selectedAiId; }

    public Long getSelectedProtocolId() { return selectedProtocolId; }
    public void setSelectedProtocolId(Long selectedProtocolId) { this.selectedProtocolId = selectedProtocolId; }

}
