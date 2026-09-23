package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entries")
public class LorebookEntry extends ExtendableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Lorebook lorebook;

    @Lob
    private String name;

    @ExtendedAttribute
    @Transient
    private String payload;

    @ExtendedAttribute
    @Transient
    private String comment;

    private boolean enabled = true;

    private int ordinal = 100;

    @Transient
    private List<String> cachedTags = new ArrayList<>();

    @Transient
    private List<String> cachedNegativeTags = new ArrayList<>();

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

    @Override
    public String toString() {
        return "LorebookEntry{" +
                "name='" + name + '\'' +
                '}';
    }
}