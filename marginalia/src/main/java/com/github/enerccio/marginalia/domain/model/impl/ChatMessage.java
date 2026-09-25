package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

import java.util.Date;


@Entity
@Table(name = "messages", indexes = {
        @Index(name = "ix_msg_parent_script", columnList = "parentScript_id"),
        @Index(name = "ix_msg_parent_msg", columnList = "parent_id"),
        @Index(name = "ix_msg_user_id", columnList = "userId"),
        @Index(name = "ix_msg_is_deleted", columnList = "is_deleted")
})
public class ChatMessage extends ExtendableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Manuscript parentScript;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatMessage parent;

    @OneToOne
    private Summary summary;

    @ExtendedAttribute
    @Transient
    private String instructions;

    @ExtendedAttribute
    @Transient
    private String sceneSetting;

    @ExtendedAttribute
    @Transient
    private String povCharacter;

    @ExtendedAttribute
    @Transient
    private String presentCharacters;

    @ExtendedAttribute
    @Transient
    private String responseReasoning;

    @ExtendedAttribute
    @Transient
    private String response;

    // approx since model is used and can be changed
    private long tokenCount;

    @ExtendedAttribute
    @Transient
    private Long promptTokens;

    @ExtendedAttribute
    @Transient
    private Long tokenReasoningCount;

    private int wordCount;

    private boolean edited = false;

    @ExtendedAttribute
    @Transient
    private Date request;

    @ExtendedAttribute
    @Transient
    private Date ttft;

    @ExtendedAttribute
    @Transient
    private Date reasoningEnd;

    @ExtendedAttribute
    @Transient
    private String modelUsed;

    @ExtendedAttribute
    @Transient
    private String protocolUsed;

    @ExtendedAttribute
    @Transient
    private String builtPrompt;

    @ExtendedAttribute
    @Transient
    private Long builtPromptTokens;

    @ExtendedAttribute
    @Transient
    private Integer scrollPosition;

    @ExtendedAttribute
    @Transient
    private String backgroundLore;

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Manuscript getParentScript() {
        return parentScript;
    }

    public void setParentScript(Manuscript parentScript) {
        this.parentScript = parentScript;
    }

    public ChatMessage getParent() {
        return parent;
    }

    public void setParent(ChatMessage parent) {
        this.parent = parent;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSceneSetting() {
        return sceneSetting;
    }

    public void setSceneSetting(String sceneSetting) {
        this.sceneSetting = sceneSetting;
    }

    public String getPovCharacter() {
        return povCharacter;
    }

    public void setPovCharacter(String povCharacter) {
        this.povCharacter = povCharacter;
    }

    public String getPresentCharacters() {
        return presentCharacters;
    }

    public void setPresentCharacters(String presentCharacters) {
        this.presentCharacters = presentCharacters;
    }

    public String getResponseReasoning() {
        return responseReasoning;
    }

    public void setResponseReasoning(String responseReasoning) {
        this.responseReasoning = responseReasoning;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Long getTokenReasoningCount() {
        return tokenReasoningCount;
    }

    public void setTokenReasoningCount(Long tokenReasoningCount) {
        this.tokenReasoningCount = tokenReasoningCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Date getRequest() {
        return request;
    }

    public void setRequest(Date request) {
        this.request = request;
    }

    public Date getTtft() {
        return ttft;
    }

    public void setTtft(Date ttft) {
        this.ttft = ttft;
    }

    public Date getReasoningEnd() {
        return reasoningEnd;
    }

    public void setReasoningEnd(Date reasoningEnd) {
        this.reasoningEnd = reasoningEnd;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public String getProtocolUsed() {
        return protocolUsed;
    }

    public void setProtocolUsed(String protocolUsed) {
        this.protocolUsed = protocolUsed;
    }

    public String getBuiltPrompt() {
        return builtPrompt;
    }

    public void setBuiltPrompt(String builtPrompt) {
        this.builtPrompt = builtPrompt;
    }

    public Long getBuiltPromptTokens() {
        return builtPromptTokens;
    }

    public void setBuiltPromptTokens(Long builtPromptTokens) {
        this.builtPromptTokens = builtPromptTokens;
    }

    public Integer getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(Integer scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }
}
