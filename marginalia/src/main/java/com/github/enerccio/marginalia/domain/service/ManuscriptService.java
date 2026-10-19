package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
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
     * Marks manuscript as opened by its owner, used to order recently opened books.
     */
    void markOpened(Manuscript manuscript) throws Exception;

    String getMasterTemplate(Manuscript manuscript) throws Exception;
    String getPov(Manuscript manuscript) throws Exception;
    String getTense(Manuscript manuscript) throws Exception;
    String getStyle(Manuscript manuscript) throws Exception;
    String getUserPrompt(Manuscript manuscript) throws Exception;
    String getSummaryPrompt(Manuscript manuscript) throws Exception;

    String getMetaSummaryPrompt(Manuscript manuscript) throws Exception;
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

    JsonObject createBackup(Manuscript manuscript) throws Exception;
    Manuscript cloneFromBackup(JsonObject backup) throws Exception;

    /**
     * As {@link #cloneFromBackup(JsonObject)}, reporting which message and summary of the clone was made from which one of
     * the backup (the uuids in it) to {@code pairs}.
     *
     * @param pairs where to add the pairs, null when nobody wants them
     */
    Manuscript cloneFromBackup(JsonObject backup, Collection<UuidPair> pairs) throws Exception;
    Manuscript restoreBackup(Manuscript manuscript, boolean onlyRestoreMessages, JsonObject backup) throws Exception;
}
