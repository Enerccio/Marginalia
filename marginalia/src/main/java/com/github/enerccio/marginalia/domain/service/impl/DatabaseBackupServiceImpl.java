package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class DatabaseBackupServiceImpl implements DatabaseBackupService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupServiceImpl.class);

    private static final String BACKUP_EXTENSION = ".sqlite";
    private static final byte[] SQLITE_HEADER = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);

    @Autowired
    private Configuration configuration;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @NoTx
    public synchronized DatabaseBackup createBackup() throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File target = uniqueFile("marginalia-" + timestamp);
        // VACUUM INTO produces consistent copy of live database, it can't run inside transaction
        jdbcTemplate.execute("VACUUM INTO '" + target.getAbsolutePath().replace("'", "''") + "'");
        log.info("Database backup created: {}", target.getAbsolutePath());
        return toBackup(target);
    }

    @Override
    @NoTx
    public List<DatabaseBackup> getBackups() throws Exception {
        File[] files = configuration.getDatabaseBackupFolder().listFiles(f -> f.isFile() && f.getName().endsWith(BACKUP_EXTENSION));
        List<DatabaseBackup> backups = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                backups.add(toBackup(file));
            }
        }
        backups.sort(Comparator.comparing(DatabaseBackup::getCreation).reversed());
        return backups;
    }

    @Override
    @NoTx
    public File getBackupFile(DatabaseBackup backup) throws Exception {
        File file = new File(configuration.getDatabaseBackupFolder(), FilenameUtils.getName(backup.getName()));
        if (!file.isFile()) {
            throw new IOException("Backup " + backup.getName() + " does not exist");
        }
        return file;
    }

    @Override
    @NoTx
    public synchronized DatabaseBackup importBackup(String fileName, File source) throws Exception {
        byte[] header = new byte[SQLITE_HEADER.length];
        int read;
        try (InputStream inputStream = new FileInputStream(source)) {
            read = inputStream.readNBytes(header, 0, header.length);
        }
        if (read != header.length || !Arrays.equals(header, SQLITE_HEADER)) {
            throw new IllegalArgumentException("Not a SQLite database");
        }
        String baseName = FilenameUtils.getBaseName(StringUtils.defaultIfBlank(fileName, "uploaded"));
        File target = uniqueFile(baseName.replaceAll("[^A-Za-z0-9._-]", "_"));
        Files.copy(source.toPath(), target.toPath());
        return toBackup(target);
    }

    @Override
    @NoTx
    public synchronized void deleteBackup(DatabaseBackup backup) throws Exception {
        File file = getBackupFile(backup);
        Files.deleteIfExists(file.toPath());
        Files.deleteIfExists(new File(file.getAbsolutePath() + "-wal").toPath());
        Files.deleteIfExists(new File(file.getAbsolutePath() + "-shm").toPath());
    }

    @Override
    @NoTx
    public synchronized void scheduleRestore(DatabaseBackup backup) throws Exception {
        File file = getBackupFile(backup);
        File pending = getPendingRestoreFile();
        File tmp = new File(pending.getAbsolutePath() + ".tmp");
        Files.copy(file.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.move(tmp.toPath(), pending.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        log.warn("Database restore from {} scheduled for next start", file.getAbsolutePath());
    }

    @Override
    @NoTx
    public synchronized void cancelRestore() throws Exception {
        Files.deleteIfExists(getPendingRestoreFile().toPath());
    }

    @Override
    @NoTx
    public boolean isRestorePending() throws Exception {
        return getPendingRestoreFile().exists();
    }

    @Override
    @NoTx
    public long getDatabaseSize() throws Exception {
        File db = configuration.getDatabaseFile();
        long size = db.length();
        File wal = new File(db.getAbsolutePath() + "-wal");
        if (wal.exists()) {
            size += wal.length();
        }
        return size;
    }

    private File getPendingRestoreFile() {
        return new File(configuration.getDatabaseFile().getAbsolutePath() + Configuration.PENDING_RESTORE_SUFFIX);
    }

    private File uniqueFile(String baseName) {
        File folder = configuration.getDatabaseBackupFolder();
        File file = new File(folder, baseName + BACKUP_EXTENSION);
        int counter = 1;
        while (file.exists()) {
            file = new File(folder, baseName + "-" + counter++ + BACKUP_EXTENSION);
        }
        return file;
    }

    private DatabaseBackup toBackup(File file) {
        DatabaseBackup backup = new DatabaseBackup();
        backup.setName(file.getName());
        backup.setCreation(new Date(file.lastModified()));
        backup.setSize(file.length());
        return backup;
    }
}
