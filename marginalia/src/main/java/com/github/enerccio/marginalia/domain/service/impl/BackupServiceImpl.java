package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BackupServiceImpl implements BackupService {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

    @Autowired
    private Configuration configuration;

    @Autowired
    private User currentUser;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    @Override
    public ManuscriptBackup takeBackup(Manuscript manuscript) throws Exception {
        if (manuscript == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_MANUSCRIPT_NULL));
        }

        File folder = getManuscriptBackupFolder(manuscript);

        JsonObject backupJson = manuscriptService.createBackup(manuscript);
        if (backupJson == null) {
            return null;
        }

        ManuscriptBackup backup = new ManuscriptBackup();
        backup.setManuscriptName(manuscript.getName());
        backup.setBackupCreationDate(new Date());

        if (currentUser != null && currentUser.getLogin() != null) {
            backup.setOwnerName(currentUser.getLogin());
        } else if (manuscript.getOwner() != null) {
            backup.setOwnerName(manuscript.getOwner().getLogin());
        }

        if (backupJson.has("messages") && backupJson.get("messages").isJsonArray()) {
            backup.setTotalMessagesCount(countMessages(backupJson.getAsJsonArray("messages")));
        } else {
            backup.setTotalMessagesCount(0);
        }

        backup.setBackup(backupJson);

        File backupFile = createBackupFileHandle(folder, backup.getBackupCreationDate());
        FileUtils.writeStringToFile(backupFile, gson.toJson(backup), StandardCharsets.UTF_8);
        backup.setFile(backupFile.getAbsolutePath());

        return backup;
    }

    @Override
    public List<ManuscriptBackup> getBackups(Manuscript manuscript) throws Exception {
        File folder = getManuscriptBackupFolder(manuscript);
        List<ManuscriptBackup> backups = new ArrayList<>();

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                    ManuscriptBackup backup = gson.fromJson(content, ManuscriptBackup.class);
                    if (backup != null) {
                        backup.setFile(file.getAbsolutePath());
                        backups.add(backup);
                    }
                } catch (Exception ignored) {
                    // Skip corrupt or unparseable backup files during directory scan
                }
            }
        }

        backups.sort(Comparator.comparing(ManuscriptBackup::getBackupCreationDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return backups;
    }

    @Override
    public Manuscript applyBackup(Manuscript manuscript, ManuscriptBackup backup, boolean messagesOnly) throws Exception {
        if (manuscript == null || backup == null || backup.getBackup() == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_NULL));
        }

        // Ensure target manuscript backup directory exists
        getManuscriptBackupFolder(manuscript);

        return manuscriptService.restoreBackup(manuscript, messagesOnly, backup.getBackup());
    }

    @Override
    public ManuscriptBackup importBackup(Manuscript manuscript, byte[] backupData) throws Exception {
        if (backupData == null || backupData.length == 0) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_DATA_EMPTY));
        }

        File folder = getManuscriptBackupFolder(manuscript);

        String jsonStr = new String(backupData, StandardCharsets.UTF_8);
        JsonElement parsedElement;
        try {
            parsedElement = JsonParser.parseString(jsonStr);
        } catch (Exception e) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_INVALID_JSON), e);
        }

        if (!parsedElement.isJsonObject()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_INVALID_ROOT_OBJECT));
        }

        JsonObject rootObj = parsedElement.getAsJsonObject();
        ManuscriptBackup backup;
        JsonObject innerBackup;

        if (rootObj.has("backup") && rootObj.get("backup").isJsonObject()) {
            backup = gson.fromJson(rootObj, ManuscriptBackup.class);
            innerBackup = backup.getBackup();
        } else {
            innerBackup = rootObj;
            backup = new ManuscriptBackup();
            backup.setBackup(innerBackup);
        }

        // Validate metadata before creating backup file
        if (innerBackup == null || !innerBackup.isJsonObject()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_MISSING_CONTENT));
        }

        boolean hasName = innerBackup.has("name") && !innerBackup.get("name").isJsonNull();
        boolean hasUuid = innerBackup.has("uuid") && !innerBackup.get("uuid").isJsonNull();
        boolean hasMessages = innerBackup.has("messages") && innerBackup.get("messages").isJsonArray();

        if (!hasName && !hasUuid && !hasMessages) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_MISSING_METADATA));
        }

        // Ensure metadata fields are populated on ManuscriptBackup object
        if (backup.getManuscriptName() == null) {
            if (hasName) {
                backup.setManuscriptName(innerBackup.get("name").getAsString());
            } else if (manuscript != null && manuscript.getName() != null) {
                backup.setManuscriptName(manuscript.getName());
            } else {
                backup.setManuscriptName("Imported Backup");
            }
        }

        if (backup.getBackupCreationDate() == null) {
            backup.setBackupCreationDate(new Date());
        }

        if (backup.getOwnerName() == null) {
            if (currentUser != null && currentUser.getLogin() != null) {
                backup.setOwnerName(currentUser.getLogin());
            } else if (manuscript != null && manuscript.getOwner() != null) {
                backup.setOwnerName(manuscript.getOwner().getLogin());
            }
        }

        if (backup.getTotalMessagesCount() <= 0 && hasMessages) {
            backup.setTotalMessagesCount(countMessages(innerBackup.getAsJsonArray("messages")));
        }

        File backupFile = createBackupFileHandle(folder, backup.getBackupCreationDate());
        FileUtils.writeStringToFile(backupFile, gson.toJson(backup), StandardCharsets.UTF_8);
        backup.setFile(backupFile.getAbsolutePath());

        return backup;
    }

    @Override
    public void deleteBackup(ManuscriptBackup backup) throws Exception {
        if (backup == null || backup.getFile() == null) {
            return;
        }

        File file = new File(backup.getFile());
        if (file.exists()) {
            file.delete();
        }
    }

    private synchronized File getManuscriptBackupFolder(Manuscript manuscript) {
        if (manuscript == null || manuscript.getUuid() == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_MANUSCRIPT_UUID_NULL));
        }

        File dataFolder = configuration.getUserDataFolder(currentUser);
        File backupFolder = new File(dataFolder, "backups");
        File manuscriptBackupFolder = new File(backupFolder, "manuscripts");
        File specificManuscriptFolder = new File(manuscriptBackupFolder, manuscript.getUuid());

        if (!specificManuscriptFolder.exists()) {
            specificManuscriptFolder.mkdirs();
        }

        return specificManuscriptFolder;
    }

    private File createBackupFileHandle(File folder, Date creationDate) {
        long timestamp = creationDate != null ? creationDate.getTime() : System.currentTimeMillis();
        File backupFile = new File(folder, timestamp + ".json");
        if (backupFile.exists()) {
            backupFile = new File(folder, timestamp + "_" + System.nanoTime() + ".json");
        }
        return backupFile;
    }

    private int countMessages(JsonArray messagesArray) {
        if (messagesArray == null) {
            return 0;
        }
        int count = 0;
        for (JsonElement elem : messagesArray) {
            if (elem.isJsonObject()) {
                count++;
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("children") && obj.get("children").isJsonArray()) {
                    count += countMessages(obj.getAsJsonArray("children"));
                }
            }
        }
        return count;
    }
}