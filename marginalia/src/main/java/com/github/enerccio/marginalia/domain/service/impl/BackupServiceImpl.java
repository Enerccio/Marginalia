package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookImportCandidate;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class BackupServiceImpl implements BackupService {
    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);

    // names of the entries of the archive with images, see exportBackup
    private static final String ARCHIVE_BACKUP = "backup.json";
    private static final String ARCHIVE_RESOURCES = "resources.json";
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
    private ResourceService resourceService;

    @Autowired
    private ChatMessageService chatMessageService;

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

        backup.setImageCount(BackupImages.collect(backupJson).size());
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
            Manuscript restored = manuscriptService.restoreBackup(manuscript, true, backup.getBackup());
            linkImages(restored);
            return restored;
        }
        Lorebook previousLorebook = manuscriptService.find(manuscript).getLorebook();
        Map<String, Lorebook> lorebooks = importLorebooks(backup.getBackup(), lorebookDecisions);
        Manuscript restored = manuscriptService.restoreBackup(manuscript, false, backup.getBackup());
        linkImages(restored);
        return linkLorebook(restored, backup.getBackup(), lorebooks, previousLorebook);
    }

    @Override
    public ManuscriptBackup importBackup(Manuscript manuscript, byte[] backupData) throws Exception {
        return withTempFile(backupData, file -> importBackup(manuscript, file));
    }

    @Override
    public ManuscriptBackup importBackup(Manuscript manuscript, File backupFile) throws Exception {
        File folder = getManuscriptBackupFolder(manuscript);

        try (BackupSource source = openSource(backupFile)) {
            JsonObject rootObj = source.root();
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

            if (source.isArchive()) {
                // the images of the archive become the user's resources, the backup then uses those
                BackupImages.remap(innerBackup, adopting(source));
            }
            backup.setImageCount(BackupImages.collect(innerBackup).size());

            backup.setLoaded(true);

            File storedFile = createBackupFileHandle(folder, backup.getBackupCreationDate());
            FileUtils.writeStringToFile(storedFile, gson.toJson(backup), StandardCharsets.UTF_8);
            backup.setFile(storedFile.getAbsolutePath());

            return backup;
        }
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
        return withTempFile(backupData, file -> restoreAsNewManuscript(file, newName, lorebookDecisions));
    }

    @Override
    public Manuscript restoreAsNewManuscript(File backupFile, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception {
        try (BackupSource source = openSource(backupFile)) {
            JsonObject backupObj = innerOf(source.root());

            // every use of an image gets a resource of its own, they are not shared by the books
            BackupImages.remap(backupObj, uuid -> {
                try {
                    BundledImage bundled = source.images().get(uuid);
                    if (bundled != null) {
                        return storeBundled(source, bundled);
                    }
                    return copyOwnImage(uuid);
                } catch (Exception e) {
                    log.warn("Image {} of the backup can't be restored, leaving it out", uuid, e);
                    return null;
                }
            });
            return createFromBackup(backupObj, newName, lorebookDecisions);
        }
    }

    @Override
    public Manuscript cloneBackup(ManuscriptBackup backup, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception {
        return cloneBackup(backup, newName, lorebookDecisions, false);
    }

    @Override
    public Manuscript cloneBackup(ManuscriptBackup backup, String newName, Map<String, LorebookDecision> lorebookDecisions, boolean withImages) throws Exception {
        if (backup == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_NULL));
        }

        ensureLoaded(backup);

        JsonObject backupObj = innerOf(backup.getBackup());
        if (backupObj != null && !BackupImages.collect(backupObj).isEmpty()) {
            // the loaded backup is kept by the caller, the images are changed in a copy
            backupObj = backupObj.deepCopy();
            BackupImages.remap(backupObj, uuid -> {
                if (!withImages) {
                    return null;
                }
                try {
                    return copyOwnImage(uuid);
                } catch (Exception e) {
                    log.warn("Image {} of the backup can't be cloned, leaving it out", uuid, e);
                    return null;
                }
            });
        }
        return createFromBackup(backupObj, newName, lorebookDecisions);
    }

    private Manuscript createFromBackup(JsonObject backupObj, String newName, Map<String, LorebookDecision> lorebookDecisions) throws Exception {
        Map<String, Lorebook> lorebooks = importLorebooks(backupObj, lorebookDecisions);
        Manuscript manuscript = manuscriptService.cloneFromBackup(backupObj);
        manuscript = linkLorebook(manuscript, backupObj, lorebooks, null);
        if (StringUtils.isNotBlank(newName)) {
            manuscript.setName(newName.trim());
            manuscript = manuscriptService.save(manuscript);
        }
        linkImages(manuscript);
        return manuscript;
    }

    @Override
    public List<LorebookImportCandidate> analyzeLorebooks(ManuscriptBackup backup) throws Exception {
        ensureLoaded(backup);
        return analyzeLorebooks(innerOf(backup.getBackup()));
    }

    @Override
    public List<LorebookImportCandidate> analyzeLorebooks(File backupFile) throws Exception {
        try (BackupSource source = openSource(backupFile)) {
            return analyzeLorebooks(innerOf(source.root()));
        }
    }

    @Override
    public List<LorebookImportCandidate> analyzeLorebooks(byte[] backupData) throws Exception {
        return withTempFile(backupData, this::analyzeLorebooks);
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

    // ------------------------------------------------------------------------------------------------------------
    // backup files: JSON, or ZIP with the images
    // ------------------------------------------------------------------------------------------------------------

    /**
     * An image in the archive.
     *
     * @param entry name of the entry of the archive with the file
     * @param hash  hash of the content, as the resource of the exporting user had it
     */
    private record BundledImage(String uuid, String entry, String name, String hash) {
    }

    /**
     * A backup file that is open, {@code zip} is null for a plain JSON.
     *
     * @param root   JSON of the backup, the whole stored file with the header or only the backup
     * @param images the images of the archive by the uuid of their resource when the backup was exported
     */
    private record BackupSource(JsonObject root, ZipFile zip, Map<String, BundledImage> images) implements Closeable {

        boolean isArchive() {
            return zip != null;
        }

        @Override
        public void close() throws IOException {
            if (zip != null) {
                zip.close();
            }
        }
    }

    private interface FileAction<T> {
        T run(File file) throws Exception;
    }

    private <T> T withTempFile(byte[] data, FileAction<T> action) throws Exception {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_DATA_EMPTY));
        }
        File file = Files.createTempFile("marginalia-backup-", ".json").toFile();
        try {
            Files.write(file.toPath(), data);
            return action.run(file);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    private static boolean isZip(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            byte[] head = in.readNBytes(4);
            return head.length == 4 && head[0] == 'P' && head[1] == 'K' && head[2] == 3 && head[3] == 4;
        }
    }

    private BackupSource openSource(File file) throws Exception {
        if (file == null || !file.isFile() || file.length() == 0) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_DATA_EMPTY));
        }
        if (!isZip(file)) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                return new BackupSource(parseObject(in), null, Map.of());
            }
        }

        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException e) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_INVALID_JSON), e);
        }
        try {
            ZipEntry entry = zip.getEntry(ARCHIVE_BACKUP);
            if (entry == null) {
                throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_MISSING_CONTENT));
            }
            JsonObject root;
            try (InputStream in = zip.getInputStream(entry)) {
                root = parseObject(in);
            }
            return new BackupSource(root, zip, readImages(zip));
        } catch (Exception e) {
            zip.close();
            throw e;
        }
    }

    private JsonObject parseObject(InputStream in) throws IOException {
        JsonElement element;
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            element = JsonParser.parseReader(reader);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_INVALID_JSON), e);
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_INVALID_ROOT_OBJECT));
        }
        return element.getAsJsonObject();
    }

    private Map<String, BundledImage> readImages(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry(ARCHIVE_RESOURCES);
        if (entry == null) {
            return Map.of();
        }
        Map<String, BundledImage> images = new HashMap<>();
        try (InputStream in = zip.getInputStream(entry)) {
            JsonObject manifest = parseObject(in);
            if (manifest.has("resources") && manifest.get("resources").isJsonArray()) {
                for (JsonElement element : manifest.getAsJsonArray("resources")) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject image = element.getAsJsonObject();
                    String uuid = string(image, "uuid");
                    String name = string(image, "entry");
                    // only the images of the archive are read, whatever the entry names say
                    if (uuid != null && name != null && name.startsWith("resources/") && zip.getEntry(name) != null) {
                        images.put(uuid, new BundledImage(uuid, name, string(image, "name"), string(image, "hash")));
                    }
                }
            }
        }
        return images;
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
    }

    /**
     * @return the backup itself, a stored backup file has it inside of the header
     */
    private static JsonObject innerOf(JsonObject root) {
        if (root != null && root.has("backup") && root.get("backup").isJsonObject()) {
            return root.getAsJsonObject("backup");
        }
        return root;
    }

    // ------------------------------------------------------------------------------------------------------------
    // images of the parts
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the images of an imported backup to the resources of the user: an image of the archive is added, unless
     * the user has the same one, an image that is not in the archive stays if the user has it. Anything else is left out.
     */
    private UnaryOperator<String> adopting(BackupSource source) {
        Map<String, String> done = new HashMap<>();
        return uuid -> {
            if (!done.containsKey(uuid)) {
                done.put(uuid, adopt(source, uuid));
            }
            return done.get(uuid);
        };
    }

    private String adopt(BackupSource source, String uuid) {
        try {
            Resource own = resourceService.findImage(uuid);
            BundledImage bundled = source.images().get(uuid);
            if (bundled == null) {
                return own != null ? uuid : null;
            }
            if (own != null && bundled.hash() != null && bundled.hash().equals(own.getHash())) {
                return uuid;
            }
            return storeBundled(source, bundled);
        } catch (Exception e) {
            log.warn("Image {} of the backup can't be imported, leaving it out", uuid, e);
            return null;
        }
    }

    /**
     * Stores an image of the archive as a new resource. The file is stored only if the user doesn't have one with the same
     * content, otherwise the resource uses that one.
     *
     * @return uuid of the resource, null if the image is not usable
     */
    private String storeBundled(BackupSource source, BundledImage bundled) throws Exception {
        ZipEntry entry = source.zip().getEntry(bundled.entry());
        if (entry == null) {
            return null;
        }
        byte[] data;
        try (InputStream in = source.zip().getInputStream(entry)) {
            // a bigger file is not an image of ours, and it is not read to the end
            data = in.readNBytes(ResourceService.MAX_IMAGE_BYTES + 1);
        }
        if (data.length > ResourceService.MAX_IMAGE_BYTES) {
            return null;
        }
        try {
            return resourceService.uploadImage(StringUtils.defaultIfBlank(bundled.name(), bundled.entry()), data).getUuid();
        } catch (IllegalArgumentException e) {
            log.warn("Image {} of the archive is not a usable image", bundled.uuid(), e);
            return null;
        }
    }

    /**
     * @return uuid of a new resource for the file of the user's resource, null if the user doesn't have the image (any more)
     */
    private String copyOwnImage(String uuid) throws Exception {
        Resource own = resourceService.findImage(uuid);
        if (own == null || !resourceService.getResourceFile(own).isFile()) {
            return null;
        }
        return resourceService.copy(own).getUuid();
    }

    /**
     * Notes in the resources of the images that the new parts of the book use them. A resource that is used by a part
     * that exists is left as it is.
     */
    private void linkImages(Manuscript manuscript) throws Exception {
        for (ChatMessage message : chatMessageService.getAllMessages(manuscript)) {
            for (ImageAttachment image : message.getImages()) {
                Resource resource = resourceService.findImage(image.resource());
                if (resource == null) {
                    continue;
                }
                ResourceService.ResourceLink link = resourceService.describeLink(resource);
                if (link == null || !link.present()) {
                    resourceService.link(resource, ChatMessage.class, message.getId());
                }
            }
        }
    }

    @Override
    public BackupExport exportBackup(ManuscriptBackup backup, boolean withImages, Runnable imagePacked) throws Exception {
        if (backup == null || backup.getFile() == null || !new File(backup.getFile()).isFile()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_BACKUP_NULL));
        }
        File stored = new File(backup.getFile());
        String name = StringUtils.isBlank(backup.getManuscriptName()) ? "manuscript" : backup.getManuscriptName();
        String base = name.replaceAll("[^a-zA-Z0-9.-]", "_") + "_backup";

        File file = Files.createTempFile("marginalia-backup-", withImages ? ".zip" : ".json").toFile();
        file.deleteOnExit();
        try {
            if (!withImages) {
                Files.copy(stored.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return new BackupExport(file, base + ".json", "application/json", 0);
            }

            ensureLoaded(backup);
            Set<String> uuids = BackupImages.collect(backup.getBackup());
            JsonArray manifest = new JsonArray();
            int missing = 0;
            try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(file.toPath())))) {
                // the stored file is copied as it is, the images are streamed one by one from their folder
                zip.putNextEntry(new ZipEntry(ARCHIVE_BACKUP));
                Files.copy(stored.toPath(), zip);
                zip.closeEntry();

                for (String uuid : uuids) {
                    Resource resource = resourceService.findImage(uuid);
                    File image = resource == null ? null : resourceService.getResourceFile(resource);
                    if (image == null || !image.isFile()) {
                        missing++;
                    } else {
                        String entry = "resources/" + (manifest.size() + 1) + "." + FilenameUtils.getExtension(resource.getPath());
                        zip.putNextEntry(new ZipEntry(entry));
                        Files.copy(image.toPath(), zip);
                        zip.closeEntry();

                        JsonObject item = new JsonObject();
                        item.addProperty("uuid", uuid);
                        item.addProperty("entry", entry);
                        item.addProperty("name", resource.getOriginalName());
                        item.addProperty("mimeType", resource.getMimeType());
                        item.addProperty("hash", resource.getHash());
                        item.addProperty("size", resource.getSize());
                        manifest.add(item);
                    }
                    if (imagePacked != null) {
                        imagePacked.run();
                    }
                }

                JsonObject resources = new JsonObject();
                resources.addProperty("version", 1);
                resources.add("resources", manifest);
                zip.putNextEntry(new ZipEntry(ARCHIVE_RESOURCES));
                zip.write(gson.toJson(resources).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            return new BackupExport(file, base + ".zip", "application/zip", missing);
        } catch (Exception e) {
            FileUtils.deleteQuietly(file);
            throw e;
        }
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
                    case "imageCount":
                        if (reader.peek() != JsonToken.NULL) {
                            backup.setImageCount(reader.nextInt());
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