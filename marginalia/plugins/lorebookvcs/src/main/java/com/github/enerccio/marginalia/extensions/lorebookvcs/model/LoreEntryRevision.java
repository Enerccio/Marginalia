package com.github.enerccio.marginalia.extensions.lorebookvcs.model;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoreEntryRevision {

    private String revisionId;
    private long lastModified;

    private String name = "";
    private boolean enabled = true;
    private int order = 100;
    private String payload = "";
    private String comment = "";
    private FilteringMode filteringMode = FilteringMode.TEXT;
    private String filtering = "";
    private InsertionMode insertionMode = InsertionMode.IN_LORE_BLOCK;
    private List<String> positiveTags = new ArrayList<>();
    private List<String> negativeTags = new ArrayList<>();

    public LoreEntryRevision() {
        this.revisionId = "rev-" + System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }

    public static LoreEntryRevision snapshotOf(LorebookEntry entry, List<String> positiveTags, List<String> negativeTags) {
        LoreEntryRevision rev = new LoreEntryRevision();
        rev.setName(entry.getName() != null ? entry.getName() : "");
        rev.setEnabled(entry.isEnabled());
        rev.setOrder(entry.getOrder());
        rev.setPayload(entry.getPayload() != null ? entry.getPayload() : "");
        rev.setComment(entry.getComment() != null ? entry.getComment() : "");
        rev.setFilteringMode(entry.getFilteringMode() != null ? entry.getFilteringMode() : FilteringMode.TEXT);
        rev.setFiltering(entry.getFiltering() != null ? entry.getFiltering() : "");
        rev.setInsertionMode(entry.getInsertionMode() != null ? entry.getInsertionMode() : InsertionMode.IN_LORE_BLOCK);
        rev.setPositiveTags(positiveTags != null ? new ArrayList<>(positiveTags) : new ArrayList<>());
        rev.setNegativeTags(negativeTags != null ? new ArrayList<>(negativeTags) : new ArrayList<>());
        return rev;
    }

    public void applyTo(LorebookEntry entry) {
        entry.setName(getName());
        entry.setEnabled(this.enabled);
        entry.setOrder(this.order);
        entry.setPayload(getPayload());
        entry.setComment(getComment());
        entry.setFilteringMode(getFilteringMode());
        entry.setFiltering(getFiltering());
        entry.setInsertionMode(getInsertionMode());
    }

    public boolean matches(LorebookEntry entry, List<String> curPosTags, List<String> curNegTags) {
        if (entry == null) return false;
        if (!Objects.equals(getName(), entry.getName() != null ? entry.getName() : "")) return false;
        if (this.enabled != entry.isEnabled()) return false;
        if (this.order != entry.getOrder()) return false;
        if (!Objects.equals(getPayload(), entry.getPayload() != null ? entry.getPayload() : "")) return false;
        if (!Objects.equals(getComment(), entry.getComment() != null ? entry.getComment() : "")) return false;
        if (getFilteringMode() != (entry.getFilteringMode() != null ? entry.getFilteringMode() : FilteringMode.TEXT)) return false;
        if (!Objects.equals(getFiltering(), entry.getFiltering() != null ? entry.getFiltering() : "")) return false;
        if (getInsertionMode() != (entry.getInsertionMode() != null ? entry.getInsertionMode() : InsertionMode.IN_LORE_BLOCK)) return false;

        List<String> p1 = getPositiveTags() != null ? new ArrayList<>(getPositiveTags()) : new ArrayList<>();
        List<String> p2 = curPosTags != null ? new ArrayList<>(curPosTags) : new ArrayList<>();
        p1.sort(String.CASE_INSENSITIVE_ORDER);
        p2.sort(String.CASE_INSENSITIVE_ORDER);
        if (p1.size() != p2.size()) return false;
        for (int i = 0; i < p1.size(); i++) {
            if (!p1.get(i).equalsIgnoreCase(p2.get(i))) return false;
        }

        List<String> n1 = getNegativeTags() != null ? new ArrayList<>(getNegativeTags()) : new ArrayList<>();
        List<String> n2 = curNegTags != null ? new ArrayList<>(curNegTags) : new ArrayList<>();
        n1.sort(String.CASE_INSENSITIVE_ORDER);
        n2.sort(String.CASE_INSENSITIVE_ORDER);
        if (n1.size() != n2.size()) return false;
        for (int i = 0; i < n1.size(); i++) {
            if (!n1.get(i).equalsIgnoreCase(n2.get(i))) return false;
        }

        return true;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getPayload() { return payload != null ? payload : ""; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getComment() { return comment != null ? comment : ""; }
    public void setComment(String comment) { this.comment = comment; }

    public FilteringMode getFilteringMode() { return filteringMode != null ? filteringMode : FilteringMode.TEXT; }
    public void setFilteringMode(FilteringMode filteringMode) { this.filteringMode = filteringMode; }

    public String getFiltering() { return filtering != null ? filtering : ""; }
    public void setFiltering(String filtering) { this.filtering = filtering; }

    public InsertionMode getInsertionMode() { return insertionMode != null ? insertionMode : InsertionMode.IN_LORE_BLOCK; }
    public void setInsertionMode(InsertionMode insertionMode) { this.insertionMode = insertionMode; }

    public List<String> getPositiveTags() { return positiveTags != null ? positiveTags : new ArrayList<>(); }
    public void setPositiveTags(List<String> positiveTags) { this.positiveTags = positiveTags; }

    public List<String> getNegativeTags() { return negativeTags != null ? negativeTags : new ArrayList<>(); }
    public void setNegativeTags(List<String> negativeTags) { this.negativeTags = negativeTags; }
}