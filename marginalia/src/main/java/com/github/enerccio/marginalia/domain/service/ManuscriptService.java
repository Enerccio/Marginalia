package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.google.gson.JsonObject;

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
     */
    Manuscript createAgenticCopy(Manuscript original) throws Exception;

    /**
     * Deletes for good the working copy of an agent, with its messages and its own lorebooks.
     *
     * @throws IllegalArgumentException when the manuscript is not a working copy of an agent
     */
    void deleteAgenticCopy(Manuscript copy) throws Exception;

    JsonObject createBackup(Manuscript manuscript) throws Exception;
    Manuscript cloneFromBackup(JsonObject backup) throws Exception;
    Manuscript restoreBackup(Manuscript manuscript, boolean onlyRestoreMessages, JsonObject backup) throws Exception;
}
