package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.List;

public interface BackupService {

    ManuscriptBackup takeBackup(Manuscript manuscript) throws Exception;
    List<ManuscriptBackup> getBackups(Manuscript manuscript) throws Exception;
    Manuscript applyBackup(Manuscript manuscript, ManuscriptBackup backup, boolean messagesOnly) throws Exception;
    ManuscriptBackup importBackup(Manuscript manuscript, byte[] backupData) throws Exception;
    void deleteBackup(ManuscriptBackup backup) throws Exception;
    Manuscript restoreAsNewManuscript(byte[] backupData, String newName) throws Exception;
    Manuscript cloneBackup(ManuscriptBackup backup, String newName) throws Exception;
    String serializeBackup(ManuscriptBackup backup) throws Exception;
    ManuscriptBackup loadBackup(ManuscriptBackup b) throws Exception;

    class ManuscriptBackup {

        private transient String file;
        private transient boolean loaded = false;

        private String manuscriptName;
        private Date backupCreationDate;
        private String ownerName;
        private int totalMessagesCount;

        private JsonObject backup;

        public String getManuscriptName() {
            return manuscriptName;
        }

        public void setManuscriptName(String manuscriptName) {
            this.manuscriptName = manuscriptName;
        }

        public Date getBackupCreationDate() {
            return backupCreationDate;
        }

        public void setBackupCreationDate(Date backupCreationDate) {
            this.backupCreationDate = backupCreationDate;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public int getTotalMessagesCount() {
            return totalMessagesCount;
        }

        public void setTotalMessagesCount(int totalMessagesCount) {
            this.totalMessagesCount = totalMessagesCount;
        }

        public JsonObject getBackup() {
            return backup;
        }

        public void setBackup(JsonObject backup) {
            this.backup = backup;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public boolean isLoaded() {
            return loaded;
        }

        public void setLoaded(boolean loaded) {
            this.loaded = loaded;
        }
    }

    enum BackupStrategy {

        DISABLED,
        AFTER_N_MESSAGES,
        AFTER_N_MINUTES

    }

}
