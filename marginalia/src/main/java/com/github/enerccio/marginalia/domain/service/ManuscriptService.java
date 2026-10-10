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

    JsonObject createBackup(Manuscript manuscript) throws Exception;
    Manuscript cloneFromBackup(JsonObject backup) throws Exception;
    Manuscript restoreBackup(Manuscript manuscript, boolean onlyRestoreMessages, JsonObject backup) throws Exception;
}
