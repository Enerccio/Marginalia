package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Checkpoints of a tracker: after every committed run the whole state of the tracker in the book (with its cursor) is stored as a
 * gzipped JSON file, and the author can return the tracker to one of them and run again from its part. A tracker keeps the last
 * {@link #KEEP} of a book, the oldest go first.
 * <p>
 * What a checkpoint says about itself (the part, the kind of run, the counts) is in the name of its file, so the list is read without
 * opening the files: {@code <tracker>.checkpoint.<kind>.<added>.<retracted>.<part uuid>.json.gz}. The content is
 * {@code {"part": uuid, "state": the state as the tracker stores it}}. How a state is serialized and put back is the tracker's.
 */
public final class TrackerCheckpoints {

    private static final Logger log = LoggerFactory.getLogger(TrackerCheckpoints.class);

    /** How many checkpoints of a book a tracker keeps. */
    public static final int KEEP = 20;

    private static final String INFIX = ".checkpoint.";
    private static final String SUFFIX = ".json.gz";

    /** What came of a restore. */
    public enum Restore {
        RESTORED,
        /** The part of the checkpoint is not on the active branch (a swipe, another branch, a deleted part). */
        NOT_ON_BRANCH,
        /** The checkpoint (or its file) is gone or can't be read. */
        GONE
    }

    /** A checkpoint whose part is on the active branch, with the position of the part (the first part is 1). */
    public record Placed(Checkpoint checkpoint, int position) {
    }

    private final String prefix;
    private final int keep;

    /** @param tracker the name of the tracker, the start of the names of its files ({@code knowledge-tracker}) */
    public TrackerCheckpoints(String tracker) {
        this(tracker, KEEP);
    }

    public TrackerCheckpoints(String tracker, int keep) {
        if (tracker.contains(".")) {
            throw new IllegalArgumentException("The name of the tracker must not contain a dot");
        }
        this.prefix = tracker + INFIX;
        this.keep = Math.max(1, keep);
    }

    /**
     * Stores a checkpoint and drops the oldest over {@link #KEEP}. A checkpoint is a convenience: when it can't be stored the run that
     * was committed stays as it is, so the failure is logged, not thrown.
     *
     * @param state    the state as the tracker stores it
     * @param partUuid the cursor of the state; nothing is stored without one (nothing was read yet)
     * @return the checkpoint, {@code null} when none was stored
     */
    public Checkpoint take(CheckpointFiles files, JsonElement state, String partUuid, Checkpoint.Kind kind, int added, int retracted) {
        if (partUuid == null || partUuid.isBlank() || partUuid.contains(".")) {
            return null;
        }
        try {
            JsonObject content = new JsonObject();
            content.addProperty("part", partUuid);
            content.add("state", state);
            int a = Math.max(0, added);
            int r = Math.max(0, retracted);
            String id = files.write(prefix + kind.name() + "." + a + "." + r + "." + partUuid + SUFFIX, gzip(content.toString()));
            retain(files);
            return new Checkpoint(id, partUuid, System.currentTimeMillis(), kind, a, r);
        } catch (Exception e) {
            log.warn("A checkpoint of {} can't be stored", prefix, e);
            return null;
        }
    }

    /** The checkpoints of the book, newest first. */
    public List<Checkpoint> list(CheckpointFiles files) throws Exception {
        List<Checkpoint> checkpoints = new ArrayList<>();
        for (CheckpointFiles.StoredFile file : files.list()) {
            Checkpoint checkpoint = parse(file);
            if (checkpoint != null) {
                checkpoints.add(checkpoint);
            }
        }
        checkpoints.sort(Comparator.comparingLong(Checkpoint::time).reversed());
        return checkpoints;
    }

    /** The checkpoint with the id, {@code null} when there is none. */
    public Checkpoint find(CheckpointFiles files, String id) throws Exception {
        return list(files).stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
    }

    /**
     * The state stored in the checkpoint.
     *
     * @return {@code null} when the file is gone or can't be read (changed by hand)
     */
    public JsonElement read(CheckpointFiles files, Checkpoint checkpoint) throws Exception {
        byte[] content = files.read(checkpoint.id());
        if (content == null) {
            return null;
        }
        try {
            JsonObject object = JsonParser.parseString(gunzip(content)).getAsJsonObject();
            if (!object.has("state") || !object.has("part") || !checkpoint.partUuid().equals(object.get("part").getAsString())) {
                return null;
            }
            return object.get("state");
        } catch (IOException | RuntimeException e) {
            log.warn("The checkpoint {} can't be read", checkpoint.id(), e);
            return null;
        }
    }

    /** Puts a state read from a checkpoint in the place of the tracker's state, and stores it. */
    @FunctionalInterface
    public interface Restorer {
        void restore(JsonElement state) throws Exception;
    }

    /**
     * The restore flow: finds the checkpoint, checks that its part is on the active branch, reads its state and lets the tracker put
     * it in place. The tracker holds the lock of the book around it.
     *
     * @param branch the active branch
     */
    public Restore restore(CheckpointFiles files, String checkpointId, List<Part> branch, Restorer restorer) throws Exception {
        Checkpoint checkpoint = find(files, checkpointId);
        if (checkpoint == null) {
            return Restore.GONE;
        }
        if (!isOnBranch(checkpoint, branch)) {
            return Restore.NOT_ON_BRANCH;
        }
        JsonElement state = read(files, checkpoint);
        if (state == null) {
            return Restore.GONE;
        }
        restorer.restore(state);
        return Restore.RESTORED;
    }

    /** The checkpoints whose part is on the branch, in the order given, with the position of the part. */
    public static List<Placed> onBranch(List<Checkpoint> checkpoints, List<Part> branch) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < branch.size(); i++) {
            index.put(branch.get(i).uuid(), i + 1);
        }
        List<Placed> placed = new ArrayList<>();
        for (Checkpoint checkpoint : checkpoints) {
            Integer position = index.get(checkpoint.partUuid());
            if (position != null) {
                placed.add(new Placed(checkpoint, position));
            }
        }
        return placed;
    }

    /** Whether the part of the checkpoint is on the branch. */
    public static boolean isOnBranch(Checkpoint checkpoint, List<Part> branch) {
        return branch.stream().anyMatch(p -> p.uuid().equals(checkpoint.partUuid()));
    }

    // --- internals ---

    private void retain(CheckpointFiles files) throws Exception {
        List<Checkpoint> all = list(files);
        if (all.size() > keep) {
            files.delete(all.subList(keep, all.size()).stream().map(Checkpoint::id).toList());
        }
    }

    /** The checkpoint a file of this tracker holds, {@code null} for any other file. */
    Checkpoint parse(CheckpointFiles.StoredFile file) {
        String name = file.name();
        if (name == null || !name.startsWith(prefix) || !name.endsWith(SUFFIX)) {
            return null;
        }
        String[] fields = name.substring(prefix.length(), name.length() - SUFFIX.length()).split("\\.");
        if (fields.length != 4) {
            return null;
        }
        try {
            return new Checkpoint(file.id(), fields[3], file.time(), Checkpoint.Kind.valueOf(fields[0]), Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2]));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static byte[] gzip(String text) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(out)) {
            zip.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return out.toByteArray();
    }

    private static String gunzip(byte[] content) throws IOException {
        try (GZIPInputStream zip = new GZIPInputStream(new ByteArrayInputStream(content))) {
            return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
