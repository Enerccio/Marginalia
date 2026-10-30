package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import jakarta.persistence.*;

/**
 * A version of the text of a {@link ChatMessage part}: who wrote it, when, and for a text written by a model the reasoning
 * that came with it. The versions of a part are ordered by {@code creation}, then {@code id} (two made in the same millisecond); the newest live one mirrors the part. They
 * live apart from the part, the parts of a branch are all loaded together and a history grows without a bound: the text is
 * only read when somebody asks for it (the history list reads the other columns only, see {@code RevisionRow}). The text is
 * in the extended content, which is stored gzipped. {@link #isDeleted() Deleted} means in the trash of the history: it counts
 * toward the limit of the part until it is purged. See {@code MessageRevisionService}.
 */
@Entity
@Table(name = "message_revisions")
public class MessageRevision extends ExtendableEntity {

    // a soft deleted part takes its versions with it; nullable: the cleanup nulls the reference before it purges the part
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    @CleanupReference(Policy.OWNED_BY)
    private ChatMessage message;

    /**
     * The id of {@link #message} for the code that has no session to load it with.
     */
    @Column(name = "message_id", insertable = false, updatable = false)
    private Long messageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RevisionSource source = RevisionSource.HUMAN;

    /**
     * An agent did it (also on the versions of the other sources an agent made).
     */
    @Column(name = "is_agent", nullable = false)
    private boolean agent;

    /**
     * A short word about the source: "model · protocol" of a generated version, the name of the extension or of the command,
     * the version a restore put back; null for none.
     */
    @Column(length = 255)
    private String sourceDetail;

    /**
     * The name of a version saved by hand or marked afterwards; a version with a label is kept when the history is pruned.
     */
    @Column(length = 255)
    private String label;

    private int wordCount;

    private long tokenCount;

    @ExtendedAttribute
    @Fulltextable
    @Transient
    private String response;

    /**
     * The reasoning that came with the text, only for the text a model wrote.
     */
    @ExtendedAttribute
    @Transient
    private String responseReasoning;

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public Long getMessageId() {
        return messageId;
    }

    public RevisionSource getSource() {
        return source;
    }

    public void setSource(RevisionSource source) {
        this.source = source;
    }

    public boolean isAgent() {
        return agent;
    }

    public void setAgent(boolean agent) {
        this.agent = agent;
    }

    public String getSourceDetail() {
        return sourceDetail;
    }

    public void setSourceDetail(String sourceDetail) {
        this.sourceDetail = sourceDetail;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponseReasoning() {
        return responseReasoning;
    }

    public void setResponseReasoning(String responseReasoning) {
        this.responseReasoning = responseReasoning;
    }
}
