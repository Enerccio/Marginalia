package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

/**
 * A note for the author about a passage of a {@link ChatMessage part} ("weak scene", "timeline wrong", "revisit"), the
 * findings of a model, an agent or an extension. Not a part of the story: it is never in a prompt unless
 * {@link #isSendToPrompt() asked to}.
 * <p>
 * The passage is the {@link AnchoredPassage} this extends (a range of offsets in the source text, kept in line with the text
 * on every change of it); the rest here is what makes it an editorial note. An annotation without a range ({@link #hasRange()})
 * is about the part as a whole, or lost its passage ({@link AnnotationStatus#ORPHANED}). Overlapping annotations are separate rows.
 */
@Entity
@DiscriminatorValue(AnchoredPassage.EDITORIAL)
public class Annotation extends AnchoredPassage {

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private AnnotationStatus status = AnnotationStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private AnnotationSource source = AnnotationSource.AUTHOR;

    /**
     * The note.
     */
    @ExtendedAttribute
    @Transient
    private String text;

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    @Override
    public String discriminator() {
        return EDITORIAL;
    }

    @Override
    public boolean isOrphaned() {
        return status == AnnotationStatus.ORPHANED;
    }

    /**
     * The passage is gone: an open annotation is orphaned, one the author closed stays closed, none keeps the range.
     */
    @Override
    public void orphan() {
        setRange(null, null);
        if (status == AnnotationStatus.OPEN) {
            status = AnnotationStatus.ORPHANED;
        }
    }

    @Override
    public void unorphan() {
        if (status == AnnotationStatus.ORPHANED) {
            status = AnnotationStatus.OPEN;
        }
    }

    @Override
    protected AnchoredPassage newInstance() {
        return new Annotation();
    }

    @Override
    protected void copyInto(AnchoredPassage copy) {
        super.copyInto(copy);
        Annotation annotation = (Annotation) copy;
        annotation.status = status;
        annotation.source = source;
        annotation.text = text;
        annotation.type = type;
        annotation.label = label;
        annotation.sendToPrompt = sendToPrompt;
    }
}
