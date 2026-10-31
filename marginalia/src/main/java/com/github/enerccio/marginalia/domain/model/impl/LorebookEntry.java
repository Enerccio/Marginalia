package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entries")
public class LorebookEntry extends ExtendableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @CleanupReference(Policy.OWNED_BY)
    private Lorebook lorebook;

    @Lob
    @Fulltextable
    private String name;

    @ExtendedAttribute
    @Transient
    @Fulltextable
    private String payload;

    @ExtendedAttribute
    @Transient
    @Fulltextable
    private String comment;

    private boolean enabled = true;

    private int ordinal = 100;

    @Transient
    private List<String> cachedTags = new ArrayList<>();

    @Transient
    private List<String> cachedNegativeTags = new ArrayList<>();

    @Transient
    @ExtendedAttribute
    private String filtering;

    @Transient
    @ExtendedAttribute
    private FilteringMode filteringMode = FilteringMode.TEXT;

    @Transient
    @ExtendedAttribute
    private InsertionMode insertionMode = InsertionMode.IN_LORE_BLOCK;

    // the content of the entry is not searched for the filters of other entries (see Lorebook#getScanDepth)
    @Transient
    @ExtendedAttribute
    private boolean preventRecursion = false;

    // what the filter is looked for in: the instructions of the turn, or the instructions and the last activationParts parts
    @Transient
    @ExtendedAttribute
    private ActivationContext activationContext = ActivationContext.INSTRUCTIONS;

    // how many of the newest parts of the story count with ActivationContext.INCLUDE_PARTS, at least 1
    @Transient
    @ExtendedAttribute
    private int activationParts = 1;

    // the entry is used only for the turns that create the part with this number (the number in the outline of the
    // story, the first part is 1) or a later one; null = from the start
    @Transient
    @ExtendedAttribute
    private Integer fromMessage;

    // ... and not for the turns that create a later part than this one; null = to the end
    @Transient
    @ExtendedAttribute
    private Integer toMessage;

    public Lorebook getLorebook() {
        return lorebook;
    }

    public void setLorebook(Lorebook lorebook) {
        this.lorebook = lorebook;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getOrder() {
        return ordinal;
    }

    public void setOrder(int ordinal) {
        this.ordinal = ordinal;
    }

    public List<String> getCachedTags() {
        return cachedTags;
    }

    public void setCachedTags(List<String> cachedTags) {
        this.cachedTags = cachedTags;
    }

    public List<String> getCachedNegativeTags() {
        return cachedNegativeTags;
    }

    public void setCachedNegativeTags(List<String> cachedNegativeTags) {
        this.cachedNegativeTags = cachedNegativeTags;
    }

    public String getFiltering() {
        return filtering;
    }

    public void setFiltering(String filtering) {
        this.filtering = filtering;
    }

    public FilteringMode getFilteringMode() {
        return filteringMode;
    }

    public void setFilteringMode(FilteringMode filteringMode) {
        this.filteringMode = filteringMode;
    }

    public InsertionMode getInsertionMode() {
        return insertionMode;
    }

    public void setInsertionMode(InsertionMode insertionMode) {
        this.insertionMode = insertionMode;
    }

    public boolean isPreventRecursion() {
        return preventRecursion;
    }

    public void setPreventRecursion(boolean preventRecursion) {
        this.preventRecursion = preventRecursion;
    }

    public ActivationContext getActivationContext() {
        return activationContext == null ? ActivationContext.INSTRUCTIONS : activationContext;
    }

    public void setActivationContext(ActivationContext activationContext) {
        this.activationContext = activationContext;
    }

    public int getActivationParts() {
        return Math.max(1, activationParts);
    }

    public void setActivationParts(int activationParts) {
        this.activationParts = Math.max(1, activationParts);
    }

    public Integer getFromMessage() {
        return fromMessage;
    }

    public void setFromMessage(Integer fromMessage) {
        this.fromMessage = fromMessage == null || fromMessage < 1 ? null : fromMessage;
    }

    public Integer getToMessage() {
        return toMessage;
    }

    public void setToMessage(Integer toMessage) {
        this.toMessage = toMessage == null || toMessage < 1 ? null : toMessage;
    }

    /**
     * Whether the entry is limited to some of the parts of the story ({@code fromMessage} or {@code toMessage} is set).
     */
    public boolean hasMessageRange() {
        return fromMessage != null || toMessage != null;
    }

    /**
     * Whether the entry may be used for the turn that creates the part with this number (1 = the first part): inside
     * {@code fromMessage} and {@code toMessage}, both included; a limit that is not set does not limit.
     */
    public boolean inMessageRange(int position) {
        return (fromMessage == null || position >= fromMessage) && (toMessage == null || position <= toMessage);
    }

    /**
     * How many parts of the story the filter is looked for in besides the instructions: 0 for
     * {@link ActivationContext#INSTRUCTIONS}.
     */
    public int partsInContext() {
        return getActivationContext() == ActivationContext.INCLUDE_PARTS ? getActivationParts() : 0;
    }

    @Override
    public String toString() {
        return "LorebookEntry{" +
                "name='" + name + '\'' +
                '}';
    }
}