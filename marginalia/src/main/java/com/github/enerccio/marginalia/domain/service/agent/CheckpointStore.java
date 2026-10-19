package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * The checkpoints of a run on the disk: one archive in {@code checkpoints/} for each, named by the code ({@code 0001.zip},
 * the next number), never by the agent. An archive holds the working copy as the JSON of a backup with its lorebooks
 * ({@code manuscript.json}), the pairs of the copy with the original and the handles ({@code pairs.json}), the files of
 * the workspace ({@code workspace/...}) and a description ({@code meta.json}). The size of the archive is what the quota
 * of the run counts.
 * <p>
 * This class only knows the files; the quota, the ring of the automatic checkpoints and the restore are in the
 * {@code CheckpointService}. It is not synchronized: the service takes a lock for the run around what it does.
 */
public final class CheckpointStore {

    private static final Logger log = LoggerFactory.getLogger(CheckpointStore.class);

    private static final Gson GSON = new Gson();
    private static final Pattern ID = Pattern.compile("\\d{1,9}");

    public static final String SUFFIX = ".zip";
    public static final String MANUSCRIPT = "manuscript.json";
    public static final String PAIRS = "pairs.json";
    public static final String META = "meta.json";
    public static final String WORKSPACE = "workspace/";
    public static final int MAX_LABEL_LENGTH = 80;
    private static final String TEMP_PREFIX = ".tmp-";

    /**
     * Who made a checkpoint: the start of the run (never rolled out), the runner before a round that changes something
     * (the last {@link #RING} are kept), or the agent or the user on purpose.
     */
    public enum Kind {
        START, AUTOMATIC, MANUAL
    }

    /**
     * How many automatic checkpoints are kept, besides the one of the start.
     */
    public static final int RING = 5;

    /**
     * A checkpoint.
     *
     * @param id             the number as the code wrote it, four digits or more
     * @param label          what the maker called it, cleaned, may be empty
     * @param created        the time it was made, in milliseconds
     * @param round          the round of the run when it was made
     * @param workspaceFiles how many files of the workspace are in it
     * @param workspaceBytes how much they take, unpacked
     * @param bytes          the size of the archive
     * @param after          the uuid of the last row of the transcript that was part of the state of the checkpoint, where a
     *                       rewind goes back to; null when it is not known (made before the rewind existed, or the transcript
     *                       was empty)
     */
    public record Info(String id, Kind kind, String label, long created, long round, int workspaceFiles, long workspaceBytes,
                       long bytes, String after) {
    }

    private final Path folder;

    /**
     * @param folder {@link RunFolder#checkpoints()} of the run, created with the first checkpoint
     */
    public CheckpointStore(Path folder) {
        this.folder = folder.toAbsolutePath().normalize();
    }

    /**
     * The number of a checkpoint as the model writes it ({@code 3}, {@code 0003}, {@code 0003.zip}), in the form of the
     * names; null when it is none.
     */
    public static String normalizeId(String text) {
        if (text == null) {
            return null;
        }
        String id = text.strip();
        if (id.endsWith(SUFFIX)) {
            id = id.substring(0, id.length() - SUFFIX.length());
        }
        if (!ID.matcher(id).matches()) {
            return null;
        }
        return String.format("%04d", Long.parseLong(id));
    }

    /**
     * A label for the list: control characters and line breaks become spaces, it is cut to {@link #MAX_LABEL_LENGTH}.
     */
    public static String cleanLabel(String label) {
        if (label == null) {
            return "";
        }
        String clean = label.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").strip();
        return clean.length() > MAX_LABEL_LENGTH ? clean.substring(0, MAX_LABEL_LENGTH - 3) + "..." : clean;
    }

    /**
     * The checkpoints, the oldest first.
     */
    public List<Info> list() {
        List<Info> infos = new ArrayList<>();
        if (!Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS)) {
            return infos;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                if (name.endsWith(SUFFIX) && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                        && normalizeId(name) != null && normalizeId(name).equals(name.substring(0, name.length() - SUFFIX.length()))) {
                    Info info = describe(file);
                    if (info != null) {
                        infos.add(info);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("The checkpoints in {} could not be listed: {}", folder.getFileName(), e.getMessage());
        }
        infos.sort(Comparator.comparing(Info::id));
        return infos;
    }

    /**
     * The checkpoint with the number; null when there is none.
     */
    public Info find(String id) {
        String normal = normalizeId(id);
        return normal == null ? null : list().stream().filter(info -> info.id().equals(normal)).findFirst().orElse(null);
    }

    /**
     * What all the archives take.
     */
    public long usedBytes() {
        return list().stream().mapToLong(Info::bytes).sum();
    }

    private Info describe(Path file) {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            ZipEntry entry = zip.getEntry(META);
            if (entry == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                JsonObject meta = GSON.fromJson(reader, JsonObject.class);
                String name = file.getFileName().toString();
                return new Info(name.substring(0, name.length() - SUFFIX.length()), Kind.valueOf(meta.get("kind").getAsString()),
                        meta.has("label") ? meta.get("label").getAsString() : "", meta.get("created").getAsLong(),
                        meta.get("round").getAsLong(), meta.get("workspaceFiles").getAsInt(),
                        meta.get("workspaceBytes").getAsLong(), Files.size(file),
                        meta.has("after") && !meta.get("after").isJsonNull() ? meta.get("after").getAsString() : null);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("The checkpoint {} could not be read and is left out: {}", file.getFileName(), e.getMessage());
            return null;
        }
    }

    /**
     * Starts a checkpoint: the archive is made as a temporary file and only {@link Pending#commit()} gives it its number,
     * so a refused or failed one leaves nothing.
     */
    public Pending begin(Kind kind, String label, long round, String after) throws IOException {
        Files.createDirectories(folder);
        long next = list().stream().mapToLong(info -> Long.parseLong(info.id())).max().orElse(0) + 1;
        return new Pending(String.format("%04d", next), kind, cleanLabel(label), round, after);
    }

    /**
     * Deletes a checkpoint.
     *
     * @return whether there was one
     */
    public boolean delete(String id) {
        String normal = normalizeId(id);
        if (normal == null) {
            return false;
        }
        try {
            return Files.deleteIfExists(folder.resolve(normal + SUFFIX));
        } catch (IOException e) {
            log.warn("The checkpoint {} could not be deleted: {}", normal, e.getMessage());
            return false;
        }
    }

    /**
     * The archive of a checkpoint, to download.
     *
     * @return null when there is none
     */
    public Path fileOf(String id) {
        String normal = normalizeId(id);
        Path file = normal == null ? null : folder.resolve(normal + SUFFIX);
        return file != null && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) ? file : null;
    }

    /**
     * Opens a checkpoint to read.
     *
     * @throws IOException when there is none or it can't be read
     */
    public Snapshot open(String id) throws IOException {
        Path file = fileOf(id);
        if (file == null) {
            throw new IOException("There is no checkpoint " + id);
        }
        return new Snapshot(new ZipFile(file.toFile()), describe(file));
    }

    /**
     * A checkpoint being written.
     */
    public final class Pending implements AutoCloseable {
        private final String id;
        private final Kind kind;
        private final String label;
        private final long round;
        private final String after;
        private final Path temp;
        private boolean done;
        private long size;
        private int workspaceFiles;
        private long workspaceBytes;

        private Pending(String id, Kind kind, String label, long round, String after) {
            this.id = id;
            this.kind = kind;
            this.label = label;
            this.round = round;
            this.after = after;
            this.temp = folder.resolve(TEMP_PREFIX + id);
        }

        public String getId() {
            return id;
        }

        /**
         * Writes the archive.
         *
         * @param workspace the files whose workspace is saved, null for none
         */
        public Pending write(JsonObject manuscript, List<UuidPair> pairs, VirtualFileSystem workspace) throws IOException {
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temp))) {
                Writer writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
                zip.putNextEntry(new ZipEntry(MANUSCRIPT));
                GSON.toJson(manuscript, writer);
                writer.flush();
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry(PAIRS));
                GSON.toJson(pairsToJson(pairs), writer);
                writer.flush();
                zip.closeEntry();
                if (workspace != null) {
                    WorkspaceUsage usage = workspace.snapshotWorkspace(zip, WORKSPACE);
                    workspaceFiles = usage.files();
                    workspaceBytes = usage.bytes();
                }
                JsonObject meta = new JsonObject();
                meta.addProperty("id", id);
                meta.addProperty("kind", kind.name());
                meta.addProperty("label", label);
                meta.addProperty("created", System.currentTimeMillis());
                meta.addProperty("round", round);
                if (after != null) {
                    meta.addProperty("after", after);
                }
                meta.addProperty("workspaceFiles", workspaceFiles);
                meta.addProperty("workspaceBytes", workspaceBytes);
                zip.putNextEntry(new ZipEntry(META));
                GSON.toJson(meta, writer);
                writer.flush();
                zip.closeEntry();
            }
            size = Files.size(temp);
            return this;
        }

        /**
         * What the archive takes, known after {@link #write}.
         */
        public long size() {
            return size;
        }

        public int getWorkspaceFiles() {
            return workspaceFiles;
        }

        public long getWorkspaceBytes() {
            return workspaceBytes;
        }

        /**
         * Gives the archive its number.
         */
        public Info commit() throws IOException {
            Path target = folder.resolve(id + SUFFIX);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, target);
            }
            done = true;
            return find(id);
        }

        /**
         * Throws away what was not committed.
         */
        @Override
        public void close() {
            if (!done) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException e) {
                    log.warn("The unfinished checkpoint {} could not be deleted: {}", id, e.getMessage());
                }
            }
        }
    }

    /**
     * A checkpoint opened to read; close it.
     */
    public static final class Snapshot implements AutoCloseable {
        private final ZipFile zip;
        private final Info info;

        private Snapshot(ZipFile zip, Info info) {
            this.zip = zip;
            this.info = info;
        }

        public Info info() {
            return info;
        }

        public ZipFile zip() {
            return zip;
        }

        /**
         * The working copy as a backup with its lorebooks.
         */
        public JsonObject manuscript() throws IOException {
            return readJson(MANUSCRIPT, JsonObject.class);
        }

        /**
         * The pairs of the copy as they were.
         */
        public List<UuidPair> pairs() throws IOException {
            List<UuidPair> pairs = new ArrayList<>();
            JsonArray array = readJson(PAIRS, JsonArray.class);
            array.forEach(element -> {
                JsonObject pair = element.getAsJsonObject();
                pairs.add(new UuidPair(AgentRunPairKind.valueOf(pair.get("kind").getAsString()), pair.get("copy").getAsString(),
                        pair.has("original") && !pair.get("original").isJsonNull() ? pair.get("original").getAsString() : null,
                        pair.has("handle") && !pair.get("handle").isJsonNull() ? pair.get("handle").getAsString() : null));
            });
            return pairs;
        }

        private <T> T readJson(String name, Class<T> type) throws IOException {
            ZipEntry entry = zip.getEntry(name);
            if (entry == null) {
                throw new IOException("The checkpoint has no " + name);
            }
            try (InputStream in = zip.getInputStream(entry); Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, type);
            }
        }

        @Override
        public void close() throws IOException {
            zip.close();
        }
    }

    private static JsonArray pairsToJson(List<UuidPair> pairs) {
        JsonArray array = new JsonArray();
        for (UuidPair pair : pairs) {
            JsonObject object = new JsonObject();
            object.addProperty("kind", pair.kind().name());
            object.addProperty("copy", pair.copyUuid());
            object.addProperty("original", pair.originalUuid());
            object.addProperty("handle", pair.handle());
            array.add(object);
        }
        return array;
    }
}
