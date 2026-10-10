package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookImportCandidate;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BackupService {

    ManuscriptBackup takeBackup(Manuscript manuscript) throws Exception;
    List<ManuscriptBackup> getBackups(Manuscript manuscript) throws Exception;
    Manuscript applyBackup(Manuscript manuscript, ManuscriptBackup backup, boolean messagesOnly,
                           Map<String, LorebookDecision> lorebookDecisions) throws Exception;
    /**
     * Adds a backup file to the backups of the book. The file is the JSON of a backup, or the archive made by
     * {@link #exportBackup} with the images: they are stored as resources of the user (the file of an image that is
     * already there is not stored again) and the stored backup uses them.
     */
    ManuscriptBackup importBackup(Manuscript manuscript, File backupFile) throws Exception;

    /**
     * Same as {@link #importBackup(Manuscript, File)} for a JSON backup that is in memory.
     */
    ManuscriptBackup importBackup(Manuscript manuscript, byte[] backupData) throws Exception;
    void deleteBackup(ManuscriptBackup backup) throws Exception;

    /**
     * Makes a new book of a backup file (JSON, or the archive made by {@link #exportBackup}). The images of the parts
     * are restored as resources of the new book: those in the archive are added to the resources of the user (without
     * storing a file that is already there), without an archive the images that the user still has are copied. Images
     * that can't be found are left out.
     */
    Manuscript restoreAsNewManuscript(File backupFile, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception;

    /**
     * Same as {@link #restoreAsNewManuscript(File, String, Map)} for a JSON backup that is in memory.
     */
    Manuscript restoreAsNewManuscript(byte[] backupData, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception;

    /**
     * Clones the backup without the images of the parts.
     */
    Manuscript cloneBackup(ManuscriptBackup backup, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception;

    /**
     * @param withImages the clone gets its own copies of the images the backup uses and the user still has, otherwise
     *                   the parts of the clone have no images
     */
    Manuscript cloneBackup(ManuscriptBackup backup, String newName, Map<String, LorebookDecision> lorebookDecisions, boolean withImages) throws Exception;
    List<LorebookImportCandidate> analyzeLorebooks(ManuscriptBackup backup) throws Exception;
    List<LorebookImportCandidate> analyzeLorebooks(File backupFile) throws Exception;
    List<LorebookImportCandidate> analyzeLorebooks(byte[] backupData) throws Exception;

    /**
     * Writes a backup to a temporary file to be downloaded, nothing of it is kept in memory. A backup is a JSON, the
     * files of its images are not in it. With images the file is a ZIP of the JSON ({@code backup.json}), a list of the
     * images ({@code resources.json}) and the images, import and restore accept both.
     *
     * @param imagePacked called after each image is dealt with, to show the progress ({@link ManuscriptBackup#getImageCount()}
     *                    times)
     * @return the file, the caller deletes it when it's done with it
     */
    BackupExport exportBackup(ManuscriptBackup backup, boolean withImages, Runnable imagePacked) throws Exception;

    /**
     * @param file          temporary file with the backup
     * @param missingImages how many images could not be packed because the user doesn't have them (any more) or their
     *                      file is missing
     */
    record BackupExport(File file, String fileName, String mimeType, int missingImages) {
    }
    String serializeBackup(ManuscriptBackup backup) throws Exception;
    ManuscriptBackup loadBackup(ManuscriptBackup b) throws Exception;

    class ManuscriptBackup {

        private transient String file;
        private transient boolean loaded = false;

        private String manuscriptName;
        private Date backupCreationDate;
        private String ownerName;
        private int totalMessagesCount;
        // images the parts use, the files are not in the backup; header only, so it can be listed without loading it
        private int imageCount;

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

        public int getImageCount() {
            return imageCount;
        }

        public void setImageCount(int imageCount) {
            this.imageCount = imageCount;
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
