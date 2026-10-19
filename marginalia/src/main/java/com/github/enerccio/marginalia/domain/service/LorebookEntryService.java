package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;

import java.util.List;

public interface LorebookEntryService extends ExtendableService<LorebookEntry, LorebookEntryRepository> {

    List<LorebookEntry> getEntriesForLorebook(Long lorebookId) throws Exception;

    List<LorebookEntry> getEntriesForLorebook(Lorebook lorebook) throws Exception;

    /**
     * The id and {@code _fulltext} of the entries of the lorebook that are not deleted, in the order of the entries. It
     * does not check that the lorebook is the current user's, the caller does.
     *
     * @param literal the text the {@code _fulltext} has to contain (the database does not tell the case of every
     *                letter apart, check again), null for all the entries
     */
    List<EntryFulltext> findFulltextsContaining(Lorebook lorebook, String literal) throws Exception;

    record EntryFulltext(Long entryId, String fulltext) {
    }

    /**
     * Makes a new entry of the lorebook with the fields of {@code fields} (its lorebook and identity are ignored). A problem
     * of the template or of the filter does not stop the save, the author may be in the middle of writing and the
     * generation uses a broken payload as plain text; the problems are returned.
     *
     * @throws IllegalArgumentException when the lorebook is not the user's
     */
    EntrySaved create(Lorebook lorebook, LorebookEntry fields) throws Exception;

    /**
     * Saves the entry with its new fields, after the same check as {@link #create}.
     *
     * @throws IllegalArgumentException when the entry is not the user's
     */
    EntrySaved update(LorebookEntry entry) throws Exception;

    /**
     * What is wrong with the fields of the entry; empty when nothing is.
     */
    List<EntryProblem> validate(LorebookEntry entry) throws Exception;

    /**
     * Deletes the entry of the lorebook (to the trash, with its tags).
     *
     * @throws IllegalArgumentException when the lorebook is not the user's or the entry is not one of its entries
     */
    void deleteEntry(Lorebook lorebook, LorebookEntry entry) throws Exception;

    /**
     * Moves the entry to another lorebook.
     *
     * @throws IllegalArgumentException when the entry or the lorebook is not the user's
     */
    LorebookEntry move(LorebookEntry entry, Lorebook target) throws Exception;

    record EntrySaved(LorebookEntry entry, List<EntryProblem> problems) {
    }

    /**
     * @param field   the field it is about: {@code payload} or {@code filtering}
     * @param message what is wrong, in English, for the model; the screens show their own text for the kind
     * @param names   for {@link EntryProblemKind#UNKNOWN_NAME} the names the template uses that do not exist
     */
    record EntryProblem(EntryProblemKind kind, String field, String message, List<String> names) {
    }

    enum EntryProblemKind {
        /** The payload is not a valid template. */
        INVALID_TEMPLATE,
        /** The payload uses names that are not defined for lorebook entries and render as an error. */
        UNKNOWN_NAME,
        /** The filter is a regular expression that does not compile, the entry would never be used. */
        INVALID_REGEX,
    }

}
