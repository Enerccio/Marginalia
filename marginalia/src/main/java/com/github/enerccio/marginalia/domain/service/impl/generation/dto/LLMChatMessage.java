package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import java.util.ArrayList;
import java.util.List;

public class LLMChatMessage {

    private LLMRole role;
    private String content;
    private List<LLMAttachment> attachments = new ArrayList<>();

    public LLMRole getRole() {
        return role;
    }

    public void setRole(LLMRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /** Media sent along with the text; only providers/models that support them use them. Never null. */
    public List<LLMAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<LLMAttachment> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public LLMChatMessage addAttachment(LLMAttachment attachment) {
        attachments.add(attachment);
        return this;
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public static LLMChatMessage of(LLMRole role, String content) {
        LLMChatMessage message = new LLMChatMessage();
        message.role = role;
        message.content = content;
        return message;
    }

}
