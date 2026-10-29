package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
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

    /**
     * Makes a new book of a book of the user: its settings, attributes, tags, linked AI and protocol and its story, with
     * its own copies of the summaries and of the images of the parts. The lorebook is not copied, the clone is linked to
     * the same one. The clone is not published.
     *
     * @param onlyCurrentBranch copy only the parts of the branch the book is set to (the path from the first part to
     *                          the active leaf), otherwise all the branches
     * @throws IllegalArgumentException when the book is not a regular book of the user, or the name is blank
     */
    Manuscript cloneManuscript(Manuscript source, String newName, boolean onlyCurrentBranch) throws Exception;

    /**
     * Like {@link #cloneManuscript(Manuscript, String, boolean)}, but the clone is put into the given folder (null is the top
     * level) instead of next to the original.
     */
    Manuscript cloneManuscript(Manuscript source, String newName, boolean onlyCurrentBranch, Folder folder) throws Exception;

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
    default BackupExport exportBackup(ManuscriptBackup backup, boolean withImages, Runnable imagePacked) throws Exception {
        return exportBackup(backup, withImages, true, imagePacked);
    }

    /**
     * Like {@link #exportBackup(ManuscriptBackup, boolean, Runnable)}, with the choice of what the file says about the settings
     * the book inherits. The file never says which folder the book is in: where a book is filed is a local choice (the stored
     * backup keeps it, for the restore here).
     *
     * @param includeInherited write what the book inherits into the file as the book's own values, where it has none of its own
     *                         (from the folders it was in when the backup was taken, then from the user settings: language,
     *                         master template, point of view, tense, style and the prompts), so it reads the same where there
     *                         are no such folders; the built-in defaults are not written
     */
    BackupExport exportBackup(ManuscriptBackup backup, boolean withImages, boolean includeInherited, Runnable imagePacked) throws Exception;

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
