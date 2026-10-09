package com.github.enerccio.marginalia;

import com.github.enerccio.marginalia.domain.security.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class Configuration implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static final String PENDING_RESTORE_SUFFIX = ".restore";
    public static final String PRE_RESTORE_PREFIX = "pre-restore-";
    private static final String[] SQLITE_SIDE_FILES = {"-wal", "-shm"};
    public static final String SECRET_KEY_FILE = "secret.key";
    public static final String ENCRYPTED_PREFIX = "enc:";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecureRandom random = new SecureRandom();

    private Long persistentLoginInfoTTL = 60 * 60 * 24 * 30L;
    private boolean allowPersistentLogin = true;
    private File folder;
    private File dataFolder;
    private File databaseBackupFolder;
    private File databaseFile;
    private boolean saveAsyncStacks = false;
    private SecretKeySpec secretKey;

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

        secretKey = loadOrCreateSecretKey(new File(folder, SECRET_KEY_FILE));
    }

    /**
     * Key for secrets stored in the database (API keys). Generated on the first start and kept outside the database,
     * so database backups alone don't reveal the secrets.
     */
    private SecretKeySpec loadOrCreateSecretKey(File keyFile) throws IOException, GeneralSecurityException {
        if (!keyFile.exists()) {
            Files.writeString(keyFile.toPath(), UUID.randomUUID().toString(), StandardCharsets.UTF_8);
            log.info("Generated secret key {}", keyFile.getAbsolutePath());
        }
        String key = Files.readString(keyFile.toPath(), StandardCharsets.UTF_8).trim();
        if (key.isEmpty()) {
            throw new IllegalStateException("Secret key file " + keyFile.getAbsolutePath() + " is empty");
        }
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(hash, "AES");
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX);
    }

    /**
     * Encrypts a secret with the installation key, returns {@code enc:<base64 of iv + ciphertext>}.
     */
    public String encrypt(String value) {
        if (value == null || value.isEmpty() || isEncrypted(value)) {
            return value;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(result);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot encrypt secret", e);
        }
    }

    /**
     * Decrypts a value from {@link #encrypt}. A value without the prefix is returned as it is (not migrated yet), a
     * value encrypted with another key (database from another installation) is returned as {@code null}.
     */
    public String decrypt(String value) {
        if (!isEncrypted(value)) {
            return value;
        }
        try {
            byte[] data = Base64.getDecoder().decode(value.substring(ENCRYPTED_PREFIX.length()));
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, data, 0, IV_LENGTH));
            return new String(cipher.doFinal(data, IV_LENGTH, data.length - IV_LENGTH), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            log.warn("Cannot decrypt secret, was it encrypted with a different {}?", SECRET_KEY_FILE, e);
            return null;
        }
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

    public File getDataFolder() {
        return dataFolder;
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
