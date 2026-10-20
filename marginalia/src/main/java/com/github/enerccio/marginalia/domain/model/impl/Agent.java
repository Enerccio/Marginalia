package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AgentBatchMode;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The definition of an agent: an LLM that works on one manuscript on its own for a long time and calls commands as its
 * tools. An agent belongs to one manuscript (and the manuscript has at most one agent), what it does is an
 * {@link AgentRun}.
 * <p>
 * The manuscript is a real column, the rest is configuration read only by whoever starts or shows the agent, so it is
 * stored as extended attributes.
 */
@Entity
@Table(name = "agents")
public class Agent extends ExtendableEntity {

    @Column(nullable = false)
    private String name;

    /**
     * Whether the agent can be started; the runs it already has are not affected.
     */
    @Column(nullable = false)
    private boolean enabled = true;

    // a soft deleted manuscript takes its agent with it
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manuscript_id")
    @CleanupReference(Policy.OWNED_BY)
    private Manuscript manuscript;

    /**
     * Models of the agent, the first one is the main one. At least the main one is needed to start a run.
     */
    @Transient
    @ExtendedAttribute(inject = true, injectPrefix = "modelProfiles")
    private List<AgentModelProfile> modelProfiles = new ArrayList<>();

    /**
     * What the agent is told to be, before the instructions.
     */
    @Transient
    @ExtendedAttribute
    private String systemPrompt;

    /**
     * What the model is asked when the author compacts the run; blank follows the default of the user, then of the
     * application, then {@code Defaults.DEFAULT_AGENT_COMPACTION_PROMPT}.
     */
    @Transient
    @ExtendedAttribute
    private String compactionPrompt;

    /**
     * The instructions of the user for the agent ("agents.md", markdown), added to the system prompt of every run and
     * every thread of it. Written by the user only, the agent can't change them; the size is capped
     * ({@code AgentService#MAX_INSTRUCTIONS_LENGTH}).
     */
    @Transient
    @ExtendedAttribute
    private String instructions;

    /**
     * Names of the commands the agent can call, nothing else is offered to it.
     */
    @Transient
    @ExtendedAttribute(inject = true, injectPrefix = "allowedCommands")
    private List<String> allowedCommands = new ArrayList<>();

    /**
     * What happens with a {@code DESTRUCTIVE} command, {@link DestructiveCommandMode#ASK} when not set.
     */
    @Transient
    @ExtendedAttribute
    private DestructiveCommandMode destructiveMode;

    /**
     * Most requests (rounds) of one run, null is no limit of the agent.
     */
    @Transient
    @ExtendedAttribute
    private Integer maxRounds;

    /**
     * Most tokens (sent and generated) one run can spend, null or zero is no limit of the agent. The limit given by
     * the administrator applies too, see {@code AgentGrant}.
     */
    @Transient
    @ExtendedAttribute
    private Long tokenBudget;

    /**
     * Limit of the context of the main model in tokens, lower than the context of the AI; older rounds are
     * summarized when the transcript gets near it. Null is the context of the AI.
     */
    @Transient
    @ExtendedAttribute
    private Integer contextCap;

    /**
     * Share of the context cap (in percent) at which the runner compacts the run by itself when the agent did not do it;
     * null is {@code Defaults.AGENT_AUTO_COMPACT_PERCENT}, zero is never (the run then stops when the context is full).
     */
    @Transient
    @ExtendedAttribute
    private Integer autoCompactPercent;

    /**
     * Whether the working copy of a run (and what is asked of the scope of its commands, for example by the Lore Generator) writes
     * with the model of the agent. Off, it writes with the model of the book. Null is on.
     */
    @Transient
    @ExtendedAttribute
    private Boolean useAgentModel;

    /**
     * How many calls of one answer of the model are made, see {@link AgentBatchMode}. Null is {@code BATCHED}.
     */
    @Transient
    @ExtendedAttribute
    private AgentBatchMode batchMode;

    /**
     * How many sub-agents the agent can have working at once; null or zero is none, the agent gets no tools to spawn them.
     * With one, spawning waits for the sub-agent to finish; with more, the call returns a handle at once, until the limit is reached.
     */
    @Transient
    @ExtendedAttribute
    private Integer maxSubagents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Manuscript getManuscript() {
        return manuscript;
    }

    public void setManuscript(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    public List<AgentModelProfile> getModelProfiles() {
        return modelProfiles;
    }

    public void setModelProfiles(List<AgentModelProfile> modelProfiles) {
        this.modelProfiles = modelProfiles == null ? new ArrayList<>() : modelProfiles;
    }

    /**
     * The main profile (the overseer), null when the agent has no model yet.
     */
    public AgentModelProfile getMainProfile() {
        return modelProfiles.isEmpty() ? null : modelProfiles.getFirst();
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getCompactionPrompt() {
        return compactionPrompt;
    }

    public void setCompactionPrompt(String compactionPrompt) {
        this.compactionPrompt = compactionPrompt;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    public void setAllowedCommands(List<String> allowedCommands) {
        this.allowedCommands = allowedCommands == null ? new ArrayList<>() : allowedCommands;
    }

    public DestructiveCommandMode getDestructiveMode() {
        return destructiveMode == null ? DestructiveCommandMode.ASK : destructiveMode;
    }

    public void setDestructiveMode(DestructiveCommandMode destructiveMode) {
        this.destructiveMode = destructiveMode;
    }

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Long getTokenBudget() {
        return tokenBudget;
    }

    public void setTokenBudget(Long tokenBudget) {
        this.tokenBudget = tokenBudget;
    }

    public Boolean getUseAgentModel() {
        return useAgentModel;
    }

    public void setUseAgentModel(Boolean useAgentModel) {
        this.useAgentModel = useAgentModel;
    }

    /**
     * Whether the commands of a run write with the model of the agent and not the one of the book (the default).
     */
    public boolean isUsingAgentModel() {
        return !Boolean.FALSE.equals(useAgentModel);
    }

    public AgentBatchMode getBatchMode() {
        return batchMode;
    }

    public void setBatchMode(AgentBatchMode batchMode) {
        this.batchMode = batchMode;
    }

    /**
     * How many calls of one answer are made; {@code BATCHED} unless the agent says otherwise.
     */
    public AgentBatchMode batchModeOrDefault() {
        return batchMode == null ? AgentBatchMode.BATCHED : batchMode;
    }

    public Integer getAutoCompactPercent() {
        return autoCompactPercent;
    }

    public void setAutoCompactPercent(Integer autoCompactPercent) {
        this.autoCompactPercent = autoCompactPercent;
    }

    public Integer getMaxSubagents() {
        return maxSubagents;
    }

    public void setMaxSubagents(Integer maxSubagents) {
        this.maxSubagents = maxSubagents;
    }

    public Integer getContextCap() {
        return contextCap;
    }

    public void setContextCap(Integer contextCap) {
        this.contextCap = contextCap;
    }
}
