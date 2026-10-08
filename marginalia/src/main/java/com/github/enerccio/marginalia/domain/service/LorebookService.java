package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.repository.LorebookRepository;

import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;

public interface LorebookService extends ExtendableService<Lorebook, LorebookRepository> {

    List<Lorebook> getSubbooks(Lorebook lorebook) throws Exception;

    Lorebook fillEntries(Lorebook book) throws Exception;

    Lorebook importFromSillytavern(String json, String name) throws Exception;

    String exportLorebook(Lorebook lorebook) throws Exception;

    Lorebook importLorebook(String json, String name) throws Exception;

    /**
     * Serializes lorebook and all its (transitive) subbooks as flat list, root first. Subbooks are referenced by uuid.
     */
    JsonArray marshalLorebooks(Lorebook root) throws Exception;

    /**
     * Matches marshalled lorebooks against lorebooks of current user, first by uuid then by name.
     */
    List<LorebookImportCandidate> analyzeImport(JsonArray lorebooks, String rootUuid) throws Exception;

    /**
     * Imports marshalled lorebooks according to decisions (keyed by uuid, missing decision means skip unless
     * lorebook exists with same uuid). Returns resolved lorebooks keyed by marshalled uuid.
     */
    Map<String, Lorebook> importLorebooks(JsonArray lorebooks, Map<String, LorebookDecision> decisions) throws Exception;

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