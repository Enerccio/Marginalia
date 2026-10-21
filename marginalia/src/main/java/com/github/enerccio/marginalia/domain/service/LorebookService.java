package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.repository.LorebookRepository;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;

import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LorebookService extends ExtendableService<Lorebook, LorebookRepository> {

    /**
     * @return the lorebooks of the current user that are not working copies of agents; what the UI lists
     */
    List<Lorebook> findAllRegularForUser() throws Exception;

    /**
     * @return the lorebooks of the current user that are working copies of agents
     */
    List<Lorebook> findAllAgenticForUser() throws Exception;

    List<Lorebook> getSubbooks(Lorebook lorebook) throws Exception;

    /**
     * The lorebook and its subbooks, however deep (not the deleted ones), the lorebook first, each lorebook once.
     */
    List<Lorebook> getLorebookTree(Lorebook lorebook) throws Exception;

    /**
     * Whether making {@code subbook} a subbook of {@code parent} would make a lorebook contain itself: it is the parent, or
     * the parent is one of its subbooks, however deep.
     */
    boolean wouldCycle(Lorebook parent, Lorebook subbook) throws Exception;

    /**
     * Makes the lorebook a subbook of the parent, if it is not one already.
     *
     * @return the saved parent
     * @throws LorebookCycleException   when the lorebook would contain itself
     * @throws IllegalArgumentException when a lorebook is not the user's, or one of them is a working copy of an agent and the
     *                                  other is not
     */
    Lorebook addSubbook(Lorebook parent, Lorebook subbook) throws Exception;

    /**
     * Takes the lorebook out of the subbooks of the parent (nothing happens when it is not one).
     *
     * @return the saved parent
     */
    Lorebook removeSubbook(Lorebook parent, Lorebook subbook) throws Exception;

    /**
     * Replaces the subbooks of the lorebook.
     *
     * @return the saved parent
     * @throws LorebookCycleException when one of them would make the lorebook contain itself; nothing is changed
     */
    Lorebook setSubbooks(Lorebook parent, Collection<Lorebook> subbooks) throws Exception;

    /**
     * Makes a new, enabled lorebook with the name and adds it as a subbook of the parent (a working copy of an agent when
     * the parent is one).
     *
     * @return the new lorebook
     */
    Lorebook createSubbook(Lorebook parent, String name) throws Exception;

    Lorebook fillEntries(Lorebook book) throws Exception;

    Lorebook importFromSillytavern(String json, String name) throws Exception;

    /**
     * Makes a new, enabled lorebook from the {@code .md} files of a zip archive: one entry for each file, in the order of
     * the file names (numbers inside names compare as numbers). The entries are disabled, as an entry without a filter
     * would be sent with every prompt. Other files, folders and hidden files of the archive are left out. See
     * {@link com.github.enerccio.marginalia.utils.MarkdownArchive}.
     *
     * @param archive the zip file
     * @param name    the name of the lorebook, usually the name of the uploaded file (a {@code .zip} ending is cut off)
     * @param names   the names of the entries in the order of the files, null for none; a missing or blank one is taken from
     *                the {@code names.txt} of the archive, else from the first heading of the file or its file name
     * @throws IllegalArgumentException when the data is not a zip archive or has no {@code .md} file
     */
    Lorebook importFromArchive(byte[] archive, String name, List<String> names) throws Exception;

    String exportLorebook(Lorebook lorebook) throws Exception;

    Lorebook importLorebook(String json, String name) throws Exception;

    /**
     * Serializes lorebook and all its (transitive) subbooks as flat list, root first. Subbooks are referenced by uuid.
     */
    JsonArray marshalLorebooks(Lorebook root) throws Exception;

    /**
     * As {@link #marshalLorebooks(Lorebook)}, the entries optionally with their {@code uuid}. The backup and export
     * format has none (the entries are matched by position when they are restored), a snapshot of the lorebooks to be
     * compared with the lorebooks later has to.
     */
    JsonArray marshalLorebooks(Lorebook root, boolean entryUuids) throws Exception;

    /**
     * Matches marshalled lorebooks against lorebooks of current user, first by uuid then by name.
     */
    List<LorebookImportCandidate> analyzeImport(JsonArray lorebooks, String rootUuid) throws Exception;

    /**
     * Imports marshalled lorebooks according to decisions (keyed by uuid, missing decision means skip unless
     * lorebook exists with same uuid). Returns resolved lorebooks keyed by marshalled uuid.
     */
    Map<String, Lorebook> importLorebooks(JsonArray lorebooks, Map<String, LorebookDecision> decisions) throws Exception;

    /**
     * Makes new working copies of marshalled lorebooks for an agent: every one of them is created again (nothing is
     * linked to the lorebooks of the user, not even by name), with a new uuid, hidden from the user's lists
     * ({@link Lorebook#isAgentic()}), the subbook structure and the entries kept. Returns the copies keyed by the
     * marshalled uuid.
     */
    Map<String, Lorebook> copyLorebooks(JsonArray lorebooks) throws Exception;

    /**
     * As {@link #copyLorebooks(JsonArray)}, reporting to {@code pairs} which lorebook and which entry of the copies was made
     * from which one of the marshalled ones (the uuids in it; an entry only has one when it was marshalled with
     * {@link #marshalLorebooks(Lorebook, boolean) entryUuids}).
     *
     * @param pairs where to add the pairs, null when nobody wants them
     */
    Map<String, Lorebook> copyLorebooks(JsonArray lorebooks, Collection<UuidPair> pairs) throws Exception;

    /**
     * Deletes for good the working copy of an agent, the lorebook and its subbooks with their entries and tags.
     *
     * @throws IllegalArgumentException when the lorebook, or one of its subbooks, is not a working copy of an agent
     */
    void deleteAgenticCopy(Lorebook root) throws Exception;

    enum LorebookMatch {
        EXISTING,
        SAME_NAME,
        NOT_FOUND,
    }

    enum LorebookDecision {
        LINK,
        CREATE,
        SKIP,
    }

    class LorebookImportCandidate {
        private String uuid;
        private String name;
        private int entryCount;
        private boolean root;
        private LorebookMatch match;
        private Lorebook matchedLorebook;
        private LorebookDecision decision;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getEntryCount() {
            return entryCount;
        }

        public void setEntryCount(int entryCount) {
            this.entryCount = entryCount;
        }

        public boolean isRoot() {
            return root;
        }

        public void setRoot(boolean root) {
            this.root = root;
        }

        public LorebookMatch getMatch() {
            return match;
        }

        public void setMatch(LorebookMatch match) {
            this.match = match;
        }

        public Lorebook getMatchedLorebook() {
            return matchedLorebook;
        }

        public void setMatchedLorebook(Lorebook matchedLorebook) {
            this.matchedLorebook = matchedLorebook;
        }

        public LorebookDecision getDecision() {
            return decision;
        }

        public void setDecision(LorebookDecision decision) {
            this.decision = decision;
        }
    }

}