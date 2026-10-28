package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

/**
 * A note for the author about a passage of a {@link ChatMessage part} ("weak scene", "timeline wrong", "revisit"), the
 * findings of a model, an agent or an extension. Not a part of the story: it is never in a prompt unless
 * {@link #isSendToPrompt() asked to}.
 * <p>
 * The passage is a range of offsets in the source text ({@link ChatMessage#getResponse()}), the text itself carries no
 * marker. The offsets are kept in line with the text by the {@code AnnotationService} on every change of the text
 * ({@code AnnotationAnchors}). An annotation without a range ({@link #hasRange()}) is about the part as a whole, or lost its
 * passage ({@link AnnotationStatus#ORPHANED}). Overlapping annotations are separate rows.
 */
@Entity
@Table(name = "annotations")
public class Annotation extends ExtendableEntity {

    // a soft deleted part takes its annotations with it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    @CleanupReference(Policy.OWNED_BY)
    private ChatMessage part;

    /**
     * The id of {@link #part} for the code that has no session to load it with.
     */
    @Column(name = "part_id", insertable = false, updatable = false)
    private Long partId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AnnotationStatus status = AnnotationStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AnnotationSource source = AnnotationSource.AUTHOR;

    /**
     * First character of the passage, null for an annotation of the part as a whole.
     */
    @Column(name = "startOffset")
    private Integer start;

    /**
     * End of the passage (exclusive), null for an annotation of the part as a whole.
     */
    @Column(name = "endOffset")
    private Integer end;

    /**
     * The note.
     */
    @ExtendedAttribute
    @Transient
    private String text;

    /**
     * The passage as it was when the range was last set, and some text around it: what finds the passage again when the
     * offsets mean nothing (the text was replaced as a whole, the annotation is attached to another text).
     */
    @ExtendedAttribute
    @Transient
    private String quote;

    @ExtendedAttribute
    @Transient
    private String contextBefore;

    @ExtendedAttribute
    @Transient
    private String contextAfter;

    /**
     * What kind of a note it is (a free word: "continuity", "style"), the colour of the mark follows it; null is the default.
     */
    @ExtendedAttribute
    @Transient
    private String type;

    /**
     * Who wrote it in particular: the name of the extension, the agent or the model.
     */
    @ExtendedAttribute
    @Transient
    private String label;

    /**
     * Annotations are for the author; the ones with this flag are given to the model with the part they belong to.
     */
    @ExtendedAttribute
    @Transient
    private Boolean sendToPrompt;

    /**
     * Made from a quote that was not found in the text: the annotation is about the part as a whole.
     */
    @ExtendedAttribute
    @Transient
    private Boolean notLocated;

    public ChatMessage getPart() {
        return part;
    }

    public void setPart(ChatMessage part) {
        this.part = part;
    }

    /**
     * @return the id of the part; null for an annotation that is not saved yet
     */
    public Long getPartId() {
        return part != null && part.getId() != null ? part.getId() : partId;
    }

    public AnnotationStatus getStatus() {
        return status;
    }

    public void setStatus(AnnotationStatus status) {
        this.status = status;
    }

    public AnnotationSource getSource() {
        return source;
    }

    public void setSource(AnnotationSource source) {
        this.source = source;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    /**
     * @return true when the annotation is about a passage of the text and not about the part as a whole
     */
    public boolean hasRange() {
        return start != null && end != null && end > start;
    }

    public void setRange(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getContextBefore() {
        return contextBefore;
    }

    public void setContextBefore(String contextBefore) {
        this.contextBefore = contextBefore;
    }

    public String getContextAfter() {
        return contextAfter;
    }

    public void setContextAfter(String contextAfter) {
        this.contextAfter = contextAfter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSendToPrompt() {
        return Boolean.TRUE.equals(sendToPrompt);
    }

    public void setSendToPrompt(boolean sendToPrompt) {
        this.sendToPrompt = sendToPrompt ? Boolean.TRUE : null;
    }

    public boolean isNotLocated() {
        return Boolean.TRUE.equals(notLocated);
    }

    public void setNotLocated(boolean notLocated) {
        this.notLocated = notLocated ? Boolean.TRUE : null;
    }
}
