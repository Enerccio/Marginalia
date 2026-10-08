package com.github.enerccio.marginalia.domain.service;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface DatabaseBackupService {

    DatabaseBackup createBackup() throws Exception;

    List<DatabaseBackup> getBackups() throws Exception;

    File getBackupFile(DatabaseBackup backup) throws Exception;

    DatabaseBackup importBackup(String fileName, File source) throws Exception;

    void deleteBackup(DatabaseBackup backup) throws Exception;

    void scheduleRestore(DatabaseBackup backup) throws Exception;

    void cancelRestore() throws Exception;

    boolean isRestorePending() throws Exception;

    long getDatabaseSize() throws Exception;

    class DatabaseBackup {
        private String name;
        private Date creation;
        private long size;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getCreation() {
            return creation;
        }

        public void setCreation(Date creation) {
            this.creation = creation;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}
