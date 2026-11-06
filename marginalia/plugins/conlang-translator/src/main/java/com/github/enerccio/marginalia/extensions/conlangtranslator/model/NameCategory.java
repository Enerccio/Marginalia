package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A category of names the language offers to the Name generator: the words of the lexicon the author chose, which the generator trains its Markov
 * chain on, so the names it makes sound like the language. Only the choice is kept here (the ids of the entries), never the words: a word that
 * was changed is read as it is now, and one that is gone, or retired, is left out when the words are asked for ({@link Language#nameWords}).
 */
public class NameCategory {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    /** The ids of the entries chosen; may name entries that are gone. */
    private List<String> entries = new ArrayList<>();

    public NameCategory() {
    }

    public NameCategory(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEntries() {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        return entries;
    }

    public boolean has(String entryId) {
        return getEntries().contains(entryId);
    }

    /** Chooses or drops a word; returns whether the choice changed. */
    public boolean set(String entryId, boolean chosen) {
        if (chosen) {
            return !has(entryId) && getEntries().add(entryId);
        }
        return getEntries().remove(entryId);
    }
}
