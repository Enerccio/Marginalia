package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.Gson;
import jakarta.persistence.*;

/**
 * One message of the transcript of an {@link AgentRun}: what the model was told and what it said, one row per
 * {@link LLMChatMessage} (a user message, an assistant message with its tool calls, the result of a tool call). The
 * rows are only appended, the {@link #getOrdinal() ordinal} is the place in the transcript; the system prompt is not
 * stored, it is made from the agent at every request so that a change of the instructions applies on a resume.
 * <p>
 * The message is the JSON of the {@code LLMChatMessage} in an extended attribute, only the run and the place are
 * columns, since they are what is queried. An attachment is only a reference to an image of the workspace (its path, no bytes): a row stays small.
 */
@Entity
@Table(name = "agent_messages")
public class AgentMessage extends ExtendableEntity {

    private static final Gson GSON = new Gson();

    // a soft deleted run takes its transcript with it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    @CleanupReference(Policy.OWNED_BY)
    private AgentRun run;

    @Column(nullable = false)
    private int ordinal;

    @Transient
    @ExtendedAttribute
    private String message;

    /**
     * On the answer of the model: how big the context was when it was given (the tokens of the request and of the
     * answer), what the console shows as the current context of the run. Null on the other messages. Not a part of
     * the {@code LLMChatMessage}, that is what the models are sent.
     */
    @Transient
    @ExtendedAttribute
    private Long contextTokens;

    /**
     * On the answer of the model: what the provider sent as its reasoning ({@code reasoning_content} or
     * {@code reasoning}), for the user to read. Null when there was none. Kept apart from the {@code LLMChatMessage}: that
     * is sent back to the model with every request, and the reasoning is not.
     */
    @Transient
    @ExtendedAttribute
    private String reasoning;

    /**
     * Whether a compaction replaced this message by a summary: it stays in the transcript the user reads and in the
     * export, and is no longer sent to the model.
     */
    @Transient
    @ExtendedAttribute
    private Boolean compacted;

    /**
     * Whether this message is the summary a compaction wrote, the start of what the model reads now.
     */
    @Transient
    @ExtendedAttribute
    private Boolean summary;

    /**
     * Whether a rewind of the run went back to before this message: it stays in the transcript the user reads and in the
     * export, greyed, and is no longer sent to the model.
     */
    @Transient
    @ExtendedAttribute
    private Boolean rewound;

    public boolean isRewound() {
        return rewound != null && rewound;
    }

    public void setRewound(boolean rewound) {
        this.rewound = rewound ? Boolean.TRUE : null;
    }

    public boolean isCompacted() {
        return compacted != null && compacted;
    }

    public void setCompacted(boolean compacted) {
        this.compacted = compacted ? Boolean.TRUE : null;
    }

    public boolean isSummary() {
        return summary != null && summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary ? Boolean.TRUE : null;
    }

    public AgentRun getRun() {
        return run;
    }

    public void setRun(AgentRun run) {
        this.run = run;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public Long getContextTokens() {
        return contextTokens;
    }

    public void setContextTokens(Long contextTokens) {
        this.contextTokens = contextTokens;
    }

    /**
     * The message as JSON.
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * The message as the object the models are sent.
     */
    public LLMChatMessage toChatMessage() {
        return GSON.fromJson(message, LLMChatMessage.class);
    }

    public void setChatMessage(LLMChatMessage chatMessage) {
        this.message = GSON.toJson(chatMessage);
    }
}
