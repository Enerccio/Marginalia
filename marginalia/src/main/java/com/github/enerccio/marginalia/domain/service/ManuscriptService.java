package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.List;

public interface ManuscriptService extends ExtendableService<Manuscript, ManuscriptRepository> {

    List<Long> searchManuscripts(Sorter... sorters) throws Exception;
    List<Long> searchManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception;

    /**
     * The same search over the books of the user only, the working copies of agents are left out. What the UI lists.
     */
    List<Long> searchRegularManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception;

    /**
     * The advanced search of the book list: the books of the user (not the working copies of agents) that pass all the
     * checks of the criteria, the text included.
     */
    List<Long> searchAdvanced(ManuscriptSearchCriteria criteria, Sorter... sorters) throws Exception;

    /**
     * The same search over the working copies of agents only.
     */
    List<Long> searchAgenticManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception;

    /**
     * @return the manuscripts of the current user that are not working copies of agents; what the UI lists
     */
    List<Manuscript> findAllRegularForUser() throws Exception;

    /**
     * @return the manuscripts of the current user that are working copies of agents
     */
    List<Manuscript> findAllAgenticForUser() throws Exception;

    /**
     * Finds manuscript by uuid if current user can view it (owns it or it is published), null otherwise.
     */
    Manuscript findViewable(String uuid) throws Exception;

    /**
     * A change of a book that can throw.
     */
    @FunctionalInterface
    interface BookChange {
        void apply(Manuscript book) throws Exception;
    }

    /**
     * Applies the change to a freshly loaded copy of the book and saves it. The way to change a book that the caller holds
     * from an earlier time (a generation, an open window): only what the change sets is written, not the old values of the
     * rest, which someone else may have changed since (the folder, the settings, the active leaf).
     *
     * @return the saved book
     * @throws IllegalStateException when the book no longer exists (nothing is applied)
     * @throws ManuscriptLockedException when an agent works on the book
     */
    Manuscript update(Manuscript book, BookChange change) throws Exception;

    /**
     * Puts the books on the folder (null is the top level). It is not a change of the content, so the books are not saved
     * and their {@code modification} stays.
     *
     * @throws IllegalArgumentException    when the folder or a book is not the user's
     * @throws ManuscriptLockedException when an agent works on a book
     */
    /**
     * The books (no agent working copies) of the current user in the folder (null is the top level), with
     * {@code includeSubfolders} also those in the folders below it (at the top level that is every book).
     */
    List<Manuscript> findRegularInFolder(Folder folder, boolean includeSubfolders) throws Exception;

    /**
     * The uuids of the books the user marked as favorites, in the order they were marked; a book that is gone is dropped
     * (and forgotten).
     */
    List<String> getFavorites() throws Exception;

    /**
     * Marks the book as a favorite or takes the mark off. Nothing of the book changes (not its modification either).
     */
    void setFavorite(Manuscript book, boolean favorite) throws Exception;

    void moveToFolder(Collection<Manuscript> books, Folder folder) throws Exception;

    /**
     * Marks manuscript as opened by its owner, used to order recently opened books.
     */
    void markOpened(Manuscript manuscript) throws Exception;

    /**
     * The value of a text setting of the book and where it comes from: the book, else the nearest folder above it that sets
     * one, else the user settings, else the built-in default. A blank text is not set. Works from any thread (a generation
     * asks from its own).
     */
    EffectiveSetting<String> resolve(Manuscript manuscript, PromptField field) throws Exception;

    /**
     * What a book filed in the folder (or a folder made in it) inherits for the field and where it comes from: the folder
     * itself if it sets one, else the nearest one above it, else the user settings, else the built-in default. The top
     * level (null) has the user settings only. What the settings forms show as the placeholder of an empty field.
     */
    EffectiveSetting<String> resolveInherited(Folder folder, PromptField field) throws Exception;

    /**
     * The language of the book and where it comes from: the book, else the nearest folder above it that sets one, else
     * English (the default).
     */
    EffectiveSetting<Language> resolveLanguage(Manuscript manuscript) throws Exception;

    /**
     * The language a book filed in the folder (or a folder made in it) inherits and where it comes from: the folder itself
     * if it sets one, else the nearest one above it, else English (the default). The top level (null) has English.
     */
    EffectiveSetting<Language> resolveInheritedLanguage(Folder folder) throws Exception;

    /**
     * How many versions of a part the history of the book keeps and where the limit comes from: the smallest of what the book,
     * the folders above it (all of them), the settings of its owner and the owner's {@code VERSION_HISTORY} permission set (an
     * owner without the permission has the default of {@link EffectiveHistoryLimit#DEFAULT_MAX_VERSIONS}, one whose permission is
     * not granted has none). A level that sets nothing, or a negative number, does not limit. Works from any thread.
     */
    EffectiveHistoryLimit resolveHistoryLimit(Manuscript manuscript) throws Exception;

    /**
     * The limit a book filed in the folder (or a folder made in it) inherits, see {@link #resolveHistoryLimit}: the folder itself and
     * the ones above it, then the settings and the permission of the current user (the windows that ask are the user's own, and the folder
     * of a book they hold is a lazy proxy without a session: only its id is read). The top level (null) has the user and their permission only.
     */
    EffectiveHistoryLimit resolveInheritedHistoryLimit(Folder folder) throws Exception;

    /**
     * What the administrator's {@code VERSION_HISTORY} permission allows the current user, the level above the user settings: the
     * number of the permission, the default for a user without one, no history for one that is not granted, no limit for a
     * granted one without a number.
     */
    EffectiveHistoryLimit resolvePermissionHistoryLimit() throws Exception;

    /**
     * {@code resolveLanguage(manuscript).value()}.
     */
    Language getLanguage(Manuscript manuscript) throws Exception;

    String getMasterTemplate(Manuscript manuscript) throws Exception;
    String getPov(Manuscript manuscript) throws Exception;
    String getTense(Manuscript manuscript) throws Exception;
    String getStyle(Manuscript manuscript) throws Exception;
    String getUserPrompt(Manuscript manuscript) throws Exception;
    String getSummaryPrompt(Manuscript manuscript) throws Exception;

    String getMetaSummaryPrompt(Manuscript manuscript) throws Exception;

    /**
     * The system message of the redraft: the one of the book, else the one of the user, else the built in one. The story before the
     * part follows it as the earlier messages of the conversation and {@link #getRedraftPrompt} is the last message from the user.
     */
    String getRedraftSystemPrompt(Manuscript manuscript) throws Exception;

    /**
     * The last message of the redraft, from the user (the story after the part, the part, the instructions): the one of the book,
     * else the one of the user, else the built in one.
     */
    String getRedraftPrompt(Manuscript manuscript) throws Exception;

    /**
     * The system message of the annotation, see {@link #getRedraftSystemPrompt}.
     */
    String getAnnotateSystemPrompt(Manuscript manuscript) throws Exception;

    /**
     * The last message of the annotation, from the user: the one of the book, else the one of the user, else the built in one.
     */
    String getAnnotatePrompt(Manuscript manuscript) throws Exception;

    /**
     * The prompt that lets a model draft the instructions of the next turn: the one of the book, else the one of the user, else
     * the built in one.
     */
    String getImpersonatePrompt(Manuscript manuscript) throws Exception;
    BackupStrategy getBackupStrategy(Manuscript manuscript) throws Exception;
    String getBackupStrategyValue(Manuscript manuscript) throws Exception;

    /**
     * Makes the working copy of a manuscript for the run of an agent: a new hidden book ({@link Manuscript#isAgentic()})
     * with the messages, summaries, tags and settings of the original and its own copy of the lorebooks. Nothing of
     * the copy is shared with the original, so whatever happens to it, the original is not touched.
     * <p>
     * The result also says which item of the copy is which item of the original and what the original looked like
     * when it was copied, see {@link AgenticCopy}.
     */
    AgenticCopy createAgenticCopy(Manuscript original) throws Exception;

    /**
     * A working copy of an agent, as made.
     *
     * @param manuscript the copy
     * @param pairs      the messages, summaries, lorebooks and lorebook entries of the copy, each with the uuid of the item
     *                   of the original it was made from (the uuids of the copy are new)
     * @param base       the backup of the original the copy was made from, with the lorebooks of it and the uuids of their
     *                   entries ({@code lorebooks}); the snapshot a merge back is compared with
     */
    record AgenticCopy(Manuscript manuscript, List<UuidPair> pairs, JsonObject base) {
    }

    /**
     * A new working copy made of a snapshot of a working copy, the restore of a checkpoint: what
     * {@link #createAgenticCopy} makes of the original, made of the snapshot instead. The snapshot is the backup of the copy
     * with its lorebooks ({@code lorebooks}, with the uuids of their entries); the pairs report for each item of the new
     * copy the uuid it had in the snapshot, in place of an original.
     *
     * @throws IllegalArgumentException when the snapshot is not one
     */
    AgenticCopy createAgenticCopyFromSnapshot(JsonObject snapshot) throws Exception;

    /**
     * The backup of a working copy with its lorebooks and the uuids of their entries, the snapshot of a checkpoint and the
     * input of {@link #createAgenticCopyFromSnapshot}.
     *
     * @throws IllegalArgumentException when the manuscript is not a working copy of an agent
     */
    JsonObject snapshotAgenticCopy(Manuscript copy) throws Exception;

    /**
     * Deletes for good the working copy of an agent, with its messages and its own lorebooks.
     *
     * @throws IllegalArgumentException when the manuscript is not a working copy of an agent
     */
    void deleteAgenticCopy(Manuscript copy) throws Exception;

    /**
     * Sets the AI and the protocol of the working copy of an agent (null clears), which is what the copy writes with when no model
     * profile is named. The AI and the protocol are not compared when a run is merged back, so the original keeps its own.
     *
     * @throws IllegalArgumentException when the manuscript is not a working copy of an agent
     */
    void setAgenticCopyModel(Manuscript copy, AI ai, Protocol protocol) throws Exception;

    /**
     * Puts the working copy of an agent into the folder of the book it is a copy of (null: the top level), which is what the
     * copy resolves its settings through (language, template, prompts), so it writes as the book does. The copy is a working
     * copy and not a book of the list, so this changes neither the folder's content nor its modification. Called when the copy
     * is made and when the run is synced or its copy is replaced, as the book may have been moved while the run was paused.
     *
     * @throws IllegalArgumentException when the manuscript is not a working copy of an agent
     */
    void setAgenticCopyFolder(Manuscript copy, Folder folder) throws Exception;

    /**
     * Changes the settings of the book that are named in {@code changes} (the others are left as they are) and saves it. The
     * prompt templates are checked first: an invalid one refuses the whole change, a template that uses names that do not
     * exist is saved and reported in the result.
     *
     * @throws IllegalArgumentException when the manuscript is not the user's, the name would be empty or a template is not valid
     */
    SettingsSaved updateSettings(Manuscript manuscript, BookSettings changes) throws Exception;

    /**
     * @param warnings what is wrong with a saved template without making it unusable, one text for each
     */
    record SettingsSaved(Manuscript manuscript, List<String> warnings) {
    }

    /**
     * The settings of a book that can be changed together; a field that is not set stays as it is. The AI, the protocol, the
     * lorebook and publishing are not among them, those are the user's decision.
     */
    class BookSettings {
        private String name;
        private String description;
        private Language language;
        private String pov;
        private String tense;
        private String style;
        private String masterTemplate;
        private String userPrompt;
        private String summaryPrompt;
        private String metaSummaryPrompt;

        public String name() {
            return name;
        }

        public BookSettings name(String name) {
            this.name = name;
            return this;
        }

        public String description() {
            return description;
        }

        public BookSettings description(String description) {
            this.description = description;
            return this;
        }

        public Language language() {
            return language;
        }

        public BookSettings language(Language language) {
            this.language = language;
            return this;
        }

        public String pov() {
            return pov;
        }

        public BookSettings pov(String pov) {
            this.pov = pov;
            return this;
        }

        public String tense() {
            return tense;
        }

        public BookSettings tense(String tense) {
            this.tense = tense;
            return this;
        }

        public String style() {
            return style;
        }

        public BookSettings style(String style) {
            this.style = style;
            return this;
        }

        public String masterTemplate() {
            return masterTemplate;
        }

        public BookSettings masterTemplate(String masterTemplate) {
            this.masterTemplate = masterTemplate;
            return this;
        }

        public String userPrompt() {
            return userPrompt;
        }

        public BookSettings userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }

        public String summaryPrompt() {
            return summaryPrompt;
        }

        public BookSettings summaryPrompt(String summaryPrompt) {
            this.summaryPrompt = summaryPrompt;
            return this;
        }

        public String metaSummaryPrompt() {
            return metaSummaryPrompt;
        }

        public BookSettings metaSummaryPrompt(String metaSummaryPrompt) {
            this.metaSummaryPrompt = metaSummaryPrompt;
            return this;
        }
    }

    /**
     * What a part of a restored or copied book gets when the backup has no history of it.
     */
    enum MissingHistory {
        /** Nothing: the working copy of an agent, whose parts get their first version when they are changed. */
        NONE,
        /** A first version made as the migration of existing parts did, see {@code MessageRevisionService#recordFirst}. */
        FIRST_VERSION,
        /** A {@code DUPLICATE} version of the text: the book was duplicated. */
        DUPLICATE
    }

    /**
     * The backup of the book without the history of its parts.
     */
    JsonObject createBackup(Manuscript manuscript) throws Exception;

    /**
     * The backup of the book; with {@code includeHistory} each part has its versions ({@code revisions}, the ones in the trash too).
     */
    JsonObject createBackup(Manuscript manuscript, boolean includeHistory) throws Exception;
    Manuscript cloneFromBackup(JsonObject backup) throws Exception;

    /**
     * As {@link #cloneFromBackup(JsonObject)}, reporting which message and summary of the clone was made from which one of
     * the backup (the uuids in it) to {@code pairs}.
     *
     * @param pairs where to add the pairs, null when nobody wants them
     */
    Manuscript cloneFromBackup(JsonObject backup, Collection<UuidPair> pairs) throws Exception;

    /**
     * As {@link #cloneFromBackup(JsonObject, Collection)}; the parts the backup has versions of get them, the others what {@code missing} says.
     */
    Manuscript cloneFromBackup(JsonObject backup, Collection<UuidPair> pairs, MissingHistory missing) throws Exception;
    Manuscript restoreBackup(Manuscript manuscript, boolean onlyRestoreMessages, JsonObject backup) throws Exception;
}
