package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.*;

/**
 * A passage of the text of a {@link ChatMessage part} that something is attached to: the range of offsets in the source text
 * ({@link ChatMessage#getResponse()}), the passage itself with some text around it, and nothing else. The text carries no
 * marker. This is what the anchoring is made of: the offsets are kept in line with the text by the {@code AnnotationService} on
 * every change of the text (shift, relocate, move on a merge, copy to a branch), for every kind of passage the same way.
 * <p>
 * The editorial note for the author is the subclass {@link Annotation} (status, source, the note); it is the only kind with a
 * Java class. A passage of another kind (an extension's: {@link #getPassageKind()} names it, the registry of
 * {@code PassageKinds} says how it is shown) is a row of this class, the extension keeps its data in {@link #getAttributes()}.
 * Such a passage is not listed anywhere an annotation is (the badge, the tab, the margin), the kind decides what shows it.
 * <p>
 * Table {@code annotations}, one table for all kinds ({@code kind} is the discriminator: {@code EDITORIAL} or {@code PASSAGE}).
 */
@Entity
@Table(name = "annotations")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind", length = 16)
@DiscriminatorValue(AnchoredPassage.PASSAGE)
public class AnchoredPassage extends ExtendableEntity {

    /**
     * Value of the discriminator of the passages of the extensions' kinds.
     */
    public static final String PASSAGE = "PASSAGE";

    /**
     * Value of the discriminator of the {@link Annotation editorial notes}.
     */
    public static final String EDITORIAL = "EDITORIAL";

    // a soft deleted part takes its passages with it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    @CleanupReference(Policy.OWNED_BY)
    private ChatMessage part;

    /**
     * The id of {@link #part} for the code that has no session to load it with.
     */
    @Column(name = "part_id", insertable = false, updatable = false)
    private Long partId;

    /**
     * Which kind of passage a row of this class is (the id of the registered kind); null for an {@link Annotation}.
     */
    @Column(name = "passageKind", length = 64)
    private String passageKind;

    /**
     * First character of the passage, null for a passage of the part as a whole.
     */
    @Column(name = "startOffset")
    private Integer start;

    /**
     * End of the passage (exclusive), null for a passage of the part as a whole.
     */
    @Column(name = "endOffset")
    private Integer end;

    /**
     * The passage as it was when the range was last set, and some text around it: what finds the passage again when the
     * offsets mean nothing (the text was replaced as a whole, the passage is attached to another text).
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
     * Made from a quote that was not found in the text: the passage is about the part as a whole.
     */
    @ExtendedAttribute
    @Transient
    private Boolean notLocated;

    /**
     * The passage is gone from the text (deleted, rewritten beyond finding); kept with its quote in case it comes back.
     * An {@link Annotation} keeps this in its status instead.
     */
    @ExtendedAttribute
    @Transient
    private Boolean orphaned;

    public ChatMessage getPart() {
        return part;
    }

    public void setPart(ChatMessage part) {
        this.part = part;
    }

    /**
     * @return the id of the part; null for a passage that is not saved yet
     */
    public Long getPartId() {
        return part != null && part.getId() != null ? part.getId() : partId;
    }

    public String getPassageKind() {
        return passageKind;
    }

    public void setPassageKind(String passageKind) {
        this.passageKind = passageKind;
    }

    /**
     * @return the value of the discriminator column of this row
     */
    public String discriminator() {
        return PASSAGE;
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
     * @return true when the passage is a passage of the text and not of the part as a whole
     */
    public boolean hasRange() {
        return start != null && end != null && end > start;
    }

    public void setRange(Integer start, Integer end) {
        this.start = start;
        this.end = end;
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

    public boolean isNotLocated() {
        return Boolean.TRUE.equals(notLocated);
    }

    public void setNotLocated(boolean notLocated) {
        this.notLocated = notLocated ? Boolean.TRUE : null;
    }

    /**
     * @return true when the passage is gone from the text (see {@link #orphan()})
     */
    public boolean isOrphaned() {
        return Boolean.TRUE.equals(orphaned);
    }

    /**
     * The passage is gone from the text: the range is kept no longer. A closed {@link Annotation} stays closed.
     */
    public void orphan() {
        setRange(null, null);
        this.orphaned = Boolean.TRUE;
    }

    /**
     * The passage is in the text again (or was attached to a passage): it is no longer orphaned.
     */
    public void unorphan() {
        this.orphaned = null;
    }

    /**
     * Makes the passage of the same kind for another part, with the same range, quote and data (the owner, the part and the
     * extended data; not the id), for the copy of a part to a branch.
     */
    public AnchoredPassage copy() {
        AnchoredPassage copy = newInstance();
        copyInto(copy);
        return copy;
    }

    /**
     * A new, empty passage of the class of this one.
     */
    protected AnchoredPassage newInstance() {
        return new AnchoredPassage();
    }

    protected void copyInto(AnchoredPassage copy) {
        copy.setOwner(getOwner());
        copy.setPart(part);
        copy.setPassageKind(passageKind);
        copy.setRange(start, end);
        copy.setQuote(quote);
        copy.setContextBefore(contextBefore);
        copy.setContextAfter(contextAfter);
        copy.orphaned = orphaned;
        copy.notLocated = notLocated;
        JsonObject data = getAttributes();
        copy.setAttributes(data == null ? new JsonObject() : data.deepCopy());
    }
}
