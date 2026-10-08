package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookImportCandidate;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

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
    private LorebookService lorebookService;

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
        backup.setLoaded(true);

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
                ManuscriptBackup backup = parseMetadataOnly(file);
                if (backup != null) {
                    backups.add(backup);
                }
            }
        }

        // stored dates have second precision, the file name (creation time in millis) orders backups within a second
        backups.sort(Comparator.comparing(ManuscriptBackup::getBackupCreationDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(b -> new File(b.getFile()).getName(), Comparator.reverseOrder()));
        return backups;
    }

    @Override
    public Manuscript applyBackup(Manuscript manuscript, ManuscriptBackup backup, boolean messagesOnly,
                                  Map<String, LorebookDecision> lorebookDecisions) throws Exception {
        if (manuscript == null || backup == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_NULL));
        }

        ensureLoaded(backup);

        if (backup.getBackup() == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_NULL));
        }

        // Ensure target manuscript backup directory exists
        getManuscriptBackupFolder(manuscript);

        if (messagesOnly) {
            return manuscriptService.restoreBackup(manuscript, true, backup.getBackup());
        }
        Lorebook previousLorebook = manuscriptService.find(manuscript).getLorebook();
        Map<String, Lorebook> lorebooks = importLorebooks(backup.getBackup(), lorebookDecisions);
        Manuscript restored = manuscriptService.restoreBackup(manuscript, false, backup.getBackup());
        return linkLorebook(restored, backup.getBackup(), lorebooks, previousLorebook);
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
            } else if (manuscript.getName() != null) {
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
            } else if (manuscript.getOwner() != null) {
                backup.setOwnerName(manuscript.getOwner().getLogin());
            }
        }

        if (backup.getTotalMessagesCount() <= 0 && hasMessages) {
            backup.setTotalMessagesCount(countMessages(innerBackup.getAsJsonArray("messages")));
        }

        backup.setLoaded(true);

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

    @Override
    public Manuscript restoreAsNewManuscript(byte[] backupData, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception {
        JsonObject backupObj = parseBackupData(backupData);

        Map<String, Lorebook> lorebooks = importLorebooks(backupObj, lorebookDecisions);
        Manuscript manuscript = manuscriptService.cloneFromBackup(backupObj);
        manuscript = linkLorebook(manuscript, backupObj, lorebooks, null);
        if (StringUtils.isNotBlank(newName)) {
            manuscript.setName(newName.trim());
            manuscript = manuscriptService.save(manuscript);
        }
        return manuscript;
    }

    @Override
    public Manuscript cloneBackup(ManuscriptBackup backup, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception {
        if (backup == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_NULL));
        }

        ensureLoaded(backup);

        JsonObject backupObj = backup.getBackup();
        if (backupObj != null && backupObj.has("backup") && backupObj.get("backup").isJsonObject()) {
            backupObj = backupObj.getAsJsonObject("backup");
        }
        Map<String, Lorebook> lorebooks = importLorebooks(backupObj, lorebookDecisions);
        Manuscript manuscript = manuscriptService.cloneFromBackup(backupObj);
        manuscript = linkLorebook(manuscript, backupObj, lorebooks, null);
        if (StringUtils.isNotBlank(newName)) {
            manuscript.setName(newName.trim());
            manuscript = manuscriptService.save(manuscript);
        }
        return manuscript;
    }

    @Override
    public List<LorebookImportCandidate> analyzeLorebooks(ManuscriptBackup backup) throws Exception {
        ensureLoaded(backup);
        JsonObject backupObj = backup.getBackup();
        if (backupObj != null && backupObj.has("backup") && backupObj.get("backup").isJsonObject()) {
            backupObj = backupObj.getAsJsonObject("backup");
        }
        return analyzeLorebooks(backupObj);
    }

    @Override
    public List<LorebookImportCandidate> analyzeLorebooks(byte[] backupData) throws Exception {
        return analyzeLorebooks(parseBackupData(backupData));
    }

    private List<LorebookImportCandidate> analyzeLorebooks(JsonObject backupObj) throws Exception {
        if (backupObj == null || !backupObj.has("lorebooks") || !backupObj.get("lorebooks").isJsonArray()) {
            return Collections.emptyList();
        }
        return lorebookService.analyzeImport(backupObj.getAsJsonArray("lorebooks"), getRootLorebookUuid(backupObj));
    }

    /**
     * Imports or links lorebooks stored in backup, old backups without lorebooks or missing decisions import nothing.
     */
    private Map<String, Lorebook> importLorebooks(JsonObject backupObj, Map<String, LorebookDecision> decisions) throws Exception {
        if (decisions == null || backupObj == null || !backupObj.has("lorebooks") || !backupObj.get("lorebooks").isJsonArray()) {
            return null;
        }
        return lorebookService.importLorebooks(backupObj.getAsJsonArray("lorebooks"), decisions);
    }

    /**
     * Manuscript restore links lorebook only by uuid, when backup carries lorebooks the resolved root wins.
     */
    private Manuscript linkLorebook(Manuscript manuscript, JsonObject backupObj, Map<String, Lorebook> lorebooks, Lorebook fallback) throws Exception {
        if (lorebooks == null) {
            return manuscript;
        }
        Lorebook root = lorebooks.get(getRootLorebookUuid(backupObj));
        Manuscript m = manuscriptService.find(manuscript);
        m.setLorebook(root != null ? root : fallback);
        return manuscriptService.save(m);
    }

    private String getRootLorebookUuid(JsonObject backupObj) {
        if (backupObj.has("lorebook") && backupObj.get("lorebook").isJsonObject()) {
            JsonObject lbObj = backupObj.getAsJsonObject("lorebook");
            if (lbObj.has("uuid") && !lbObj.get("uuid").isJsonNull()) {
                return lbObj.get("uuid").getAsString();
            }
        }
        return null;
    }

    private JsonObject parseBackupData(byte[] backupData) {
        String jsonStr = new String(backupData, StandardCharsets.UTF_8);
        JsonElement element = JsonParser.parseString(jsonStr);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Invalid backup JSON payload");
        }
        JsonObject root = element.getAsJsonObject();
        if (root.has("backup") && root.get("backup").isJsonObject()) {
            return root.getAsJsonObject("backup");
        }
        return root;
    }

    @Override
    public String serializeBackup(ManuscriptBackup backup) throws Exception {
        if (backup != null) {
            ensureLoaded(backup);
        }
        return gson.toJson(backup);
    }

    @Override
    public ManuscriptBackup loadBackup(ManuscriptBackup b) throws Exception {
        ensureLoaded(b);
        return b;
    }

    /**
     * Lazy-loads the full backup JsonObject from disk if it hasn't been loaded yet.
     */
    private void ensureLoaded(ManuscriptBackup backup) throws Exception {
        if (backup == null || backup.isLoaded() || backup.getBackup() != null) {
            return;
        }

        if (backup.getFile() == null) {
            return;
        }

        File file = new File(backup.getFile());
        if (!file.exists()) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonObject rootObj = JsonParser.parseReader(reader).getAsJsonObject();
            if (rootObj.has("backup") && rootObj.get("backup").isJsonObject()) {
                backup.setBackup(rootObj.getAsJsonObject("backup"));
            } else {
                backup.setBackup(rootObj);
            }
            backup.setLoaded(true);
        }
    }

    /**
     * Streams only metadata fields from JSON file, skipping the heavy 'backup' tree using JsonReader.skipValue().
     */
    private ManuscriptBackup parseMetadataOnly(File file) {
        try (InputStream is = Files.newInputStream(file.toPath());
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             JsonReader reader = new JsonReader(isr)) {

            ManuscriptBackup backup = new ManuscriptBackup();
            backup.setFile(file.getAbsolutePath());
            backup.setLoaded(false);

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "manuscriptName":
                        if (reader.peek() != JsonToken.NULL) {
                            backup.setManuscriptName(reader.nextString());
                        } else {
                            reader.nextNull();
                        }
                        break;
                    case "ownerName":
                        if (reader.peek() != JsonToken.NULL) {
                            backup.setOwnerName(reader.nextString());
                        } else {
                            reader.nextNull();
                        }
                        break;
                    case "totalMessagesCount":
                        if (reader.peek() != JsonToken.NULL) {
                            backup.setTotalMessagesCount(reader.nextInt());
                        } else {
                            reader.nextNull();
                        }
                        break;
                    case "backupCreationDate":
                        if (reader.peek() != JsonToken.NULL) {
                            backup.setBackupCreationDate(gson.fromJson(reader, Date.class));
                        } else {
                            reader.nextNull();
                        }
                        break;
                    case "backup":
                        reader.skipValue(); // Fast-forwards stream past huge subtree with 0 allocations
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            return backup;
        } catch (Exception e) {
            // Skip corrupt or unparseable files
            return null;
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