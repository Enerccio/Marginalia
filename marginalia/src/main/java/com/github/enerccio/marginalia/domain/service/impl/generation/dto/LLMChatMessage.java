package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

public class LLMChatMessage {

    private LLMRole role;
    private String content;

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
}
