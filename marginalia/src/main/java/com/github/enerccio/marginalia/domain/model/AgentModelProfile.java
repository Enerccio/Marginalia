package com.github.enerccio.marginalia.domain.model;

import java.util.Objects;

/**
 * One model of an {@code Agent}: an AI with a protocol and the description of when to use it. The first profile of an
 * agent is the main one (the overseer), the others are what it can delegate to. The AI and the protocol are named by
 * their uuids and looked up when the agent runs ({@code AgentService#resolveProfile}), a profile may point to the same
 * AI as another one with a different protocol (other sampling settings).
 */
public class AgentModelProfile {

    private String role;
    private String aiUuid;
    private String protocolUuid;
    private String description;
    private boolean tools = true;
    private Integer contextCap;
    private Integer concurrency;

    public AgentModelProfile() {
    }

    public AgentModelProfile(String role, String aiUuid, String protocolUuid) {
        this.role = role;
        this.aiUuid = aiUuid;
        this.protocolUuid = protocolUuid;
    }

    /**
     * Name of the profile, unique in the agent (case insensitive). The overseer picks a profile by it.
     */
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAiUuid() {
        return aiUuid;
    }

    public void setAiUuid(String aiUuid) {
        this.aiUuid = aiUuid;
    }

    public String getProtocolUuid() {
        return protocolUuid;
    }

    public void setProtocolUuid(String protocolUuid) {
        this.protocolUuid = protocolUuid;
    }

    /**
     * When to use the profile, shown to the overseer.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Whether the model can call commands. A profile without tools is only used for single text requests.
     */
    public boolean isTools() {
        return tools;
    }

    public void setTools(boolean tools) {
        this.tools = tools;
    }

    /**
     * Limit of the context of the profile in tokens, lower than the context of the AI; null is no limit of its own.
     */
    public Integer getContextCap() {
        return contextCap;
    }

    public void setContextCap(Integer contextCap) {
        this.contextCap = contextCap;
    }

    /**
     * How many requests of the profile can go at once; null is no limit of its own.
     */
    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgentModelProfile that)) return false;
        return tools == that.tools && Objects.equals(role, that.role) && Objects.equals(aiUuid, that.aiUuid)
                && Objects.equals(protocolUuid, that.protocolUuid) && Objects.equals(description, that.description)
                && Objects.equals(contextCap, that.contextCap) && Objects.equals(concurrency, that.concurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, aiUuid, protocolUuid, description, tools, contextCap, concurrency);
    }
}
