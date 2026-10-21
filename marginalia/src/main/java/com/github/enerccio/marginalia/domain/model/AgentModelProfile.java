package com.github.enerccio.marginalia.domain.model;

import java.util.ArrayList;
import java.util.List;
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
    private boolean images;
    private Integer contextCap;
    private Integer concurrency;
    private List<String> commands;

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
     * Whether the model can call commands. A profile without tools is only used for single text requests: a sub-agent on it has no tools at all.
     */
    public boolean isTools() {
        return tools;
    }

    public void setTools(boolean tools) {
        this.tools = tools;
    }

    /**
     * Whether the model can look at images (the author says so, the API of the model does not tell). The agent then gets
     * the tool that shows it an image of its workspace; without it it never receives one.
     */
    public boolean isImages() {
        return images;
    }

    public void setImages(boolean images) {
        this.images = images;
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

    /**
     * The commands a sub-agent that runs on this profile may use, at most the ones the agent is allowed: null is all of them
     * (what the agent can), an empty list none. The profile of the overseer is the one of the sub-agents that run on the same
     * model as it does. The file tools of the workspace are not commands, every sub-agent has them.
     */
    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands == null ? null : new ArrayList<>(commands);
    }

    /**
     * A copy of the profile to edit without touching it.
     */
    public AgentModelProfile copy() {
        AgentModelProfile copy = new AgentModelProfile(role, aiUuid, protocolUuid);
        copy.setDescription(description);
        copy.setTools(tools);
        copy.setImages(images);
        copy.setContextCap(contextCap);
        copy.setConcurrency(concurrency);
        copy.setCommands(commands);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgentModelProfile that)) return false;
        return tools == that.tools && images == that.images && Objects.equals(role, that.role) && Objects.equals(aiUuid, that.aiUuid)
                && Objects.equals(protocolUuid, that.protocolUuid) && Objects.equals(description, that.description)
                && Objects.equals(contextCap, that.contextCap) && Objects.equals(concurrency, that.concurrency)
                && Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, aiUuid, protocolUuid, description, tools, images, contextCap, concurrency, commands);
    }
}
