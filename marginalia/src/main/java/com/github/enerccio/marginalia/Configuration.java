package com.github.enerccio.marginalia;

import com.github.enerccio.marginalia.domain.security.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Configuration implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static final String PENDING_RESTORE_SUFFIX = ".restore";
    public static final String PRE_RESTORE_PREFIX = "pre-restore-";
    private static final String[] SQLITE_SIDE_FILES = {"-wal", "-shm"};

    private Long persistentLoginInfoTTL = 60 * 60 * 24 * 30L;
    private boolean allowPersistentLogin = true;
    private File folder;
    private File dataFolder;
    private File databaseBackupFolder;
    private File databaseFile;
    private boolean saveAsyncStacks = false;

    public Long getPersistentLoginInfoTTL() {
        return persistentLoginInfoTTL;
    }

    public void setPersistentLoginInfoTTL(Long persistentLoginInfoTTL) {
        this.persistentLoginInfoTTL = persistentLoginInfoTTL;
    }

    public boolean isAllowPersistentLogin() {
        return allowPersistentLogin;
    }

    public void setAllowPersistentLogin(boolean allowPersistentLogin) {
        this.allowPersistentLogin = allowPersistentLogin;
    }

    public File getFolder() {
        return folder;
    }

    /**
     * Overrides the application folder (default {@code ~/.marginalia}). Used by tests to keep data in a temp folder.
     */
    public void setFolder(File folder) {
        this.folder = folder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (folder == null) {
            folder = new File(System.getProperty("user.home"), ".marginalia");
        }
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Cannot create folder " + folder.getAbsolutePath());
        }

        dataFolder = new File(folder, "data");
        if (!dataFolder.exists())
            dataFolder.mkdirs();

        databaseBackupFolder = new File(folder, "db-backups");
        if (!databaseBackupFolder.exists())
            databaseBackupFolder.mkdirs();
    }

    public String resolveDb(String db) throws IOException {
        databaseFile = new File(folder, db);
        applyPendingRestore(databaseFile);
        return "jdbc:sqlite:" + databaseFile.getAbsolutePath() + "?busy_timeout=10000&journal_mode=WAL";
    }

    /**
     * Restore can't be done on live database, so it is staged next to database file and swapped in before
     * datasource is created. Current database (with its WAL files) is kept in backup folder.
     */
    private void applyPendingRestore(File db) throws IOException {
        File pending = new File(db.getAbsolutePath() + PENDING_RESTORE_SUFFIX);
        if (!pending.exists()) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File preRestore = new File(databaseBackupFolder, PRE_RESTORE_PREFIX + timestamp + "-" + db.getName());
        if (db.exists()) {
            Files.move(db.toPath(), preRestore.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        for (String suffix : SQLITE_SIDE_FILES) {
            File sideFile = new File(db.getAbsolutePath() + suffix);
            if (sideFile.exists()) {
                Files.move(sideFile.toPath(), new File(preRestore.getAbsolutePath() + suffix).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Files.move(pending.toPath(), db.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.warn("Database restored from pending backup, previous database moved to {}", preRestore.getAbsolutePath());
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public File getDatabaseBackupFolder() {
        return databaseBackupFolder;
    }

    public File getUserDataFolder(User user) {
        File file = new File(dataFolder, user.getLogin());
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public File getImagesFolder(User user) {
        File file = new File(getUserDataFolder(user), "images");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public File getResourcesFolder(User user) {
        File file = new File(getUserDataFolder(user), "resources");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public boolean isSaveAsyncStacks() {
        return saveAsyncStacks;
    }

    public void setSaveAsyncStacks(boolean saveAsyncStacks) {
        this.saveAsyncStacks = saveAsyncStacks;
    }
}
