package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TrackerCheckpointsTest {

    private final MemoryCheckpointFiles files = new MemoryCheckpointFiles();
    private final TrackerCheckpoints checkpoints = new TrackerCheckpoints("knowledge-tracker", 3);

    private static JsonObject state(String cursor) {
        JsonObject state = new JsonObject();
        state.addProperty("cursor", cursor);
        return state;
    }

    private static List<Part> branch(String... uuids) {
        List<Part> parts = new ArrayList<>();
        for (String uuid : uuids) {
            parts.add(new Part(uuid, null, "text of " + uuid));
        }
        return parts;
    }

    @Test
    void aCheckpointIsListedWithWhatItSaysAboutItself() throws Exception {
        Checkpoint taken = checkpoints.take(files, state("p2"), "p2", Checkpoint.Kind.RUN, 3, 1);

        List<Checkpoint> listed = checkpoints.list(files);
        assertEquals(1, listed.size());
        Checkpoint checkpoint = listed.getFirst();
        assertEquals(taken.id(), checkpoint.id());
        assertEquals("p2", checkpoint.partUuid());
        assertEquals(Checkpoint.Kind.RUN, checkpoint.kind());
        assertEquals(3, checkpoint.added());
        assertEquals(1, checkpoint.retracted());
    }

    @Test
    void theStateComesBackAsItWasStored() throws Exception {
        JsonObject state = state("p2");
        state.addProperty("partsSinceTidy", 4);
        Checkpoint taken = checkpoints.take(files, state, "p2", Checkpoint.Kind.TIDY, 0, 2);

        assertEquals(state, checkpoints.read(files, taken));
    }

    @Test
    void aStateThatReadNothingIsTheStartOfTheBook() throws Exception {
        Checkpoint taken = checkpoints.take(files, state(null), null, Checkpoint.Kind.RUN, 2, 0);

        assertTrue(taken.isStart());
        assertTrue(taken.before());
        List<TrackerCheckpoints.Placed> placed = TrackerCheckpoints.onBranch(checkpoints.list(files), branch("p1", "p2"));
        assertEquals(1, placed.size());
        assertEquals(0, placed.getFirst().position());
        AtomicReference<JsonElement> restored = new AtomicReference<>();
        assertEquals(TrackerCheckpoints.Restore.RESTORED, checkpoints.restore(files, taken.id(), branch("x1"), restored::set),
                "the start is on every branch");
        assertEquals(state(null), restored.get());
    }

    @Test
    void aCheckpointOfAnOlderBuildIsTheStateAfterItsRun() throws Exception {
        files.writeOther("knowledge-tracker.checkpoint.RUN.2.1.p1.json.gz");

        Checkpoint old = checkpoints.list(files).getFirst();
        assertFalse(old.before());
        assertEquals("p1", old.partUuid());
        assertEquals(2, old.added());
    }

    @Test
    void aRestoreKeepsTheStateItReplaces() throws Exception {
        Checkpoint taken = checkpoints.take(files, state("p1"), "p1", Checkpoint.Kind.RUN, 1, 0);

        assertEquals(TrackerCheckpoints.Restore.RESTORED,
                checkpoints.restore(files, taken.id(), branch("p1", "p2", "p3"), state("p3"), "p3", _ -> { }));

        Checkpoint kept = checkpoints.list(files).stream().filter(c -> c.kind() == Checkpoint.Kind.RESTORE).findFirst().orElseThrow();
        assertEquals("p3", kept.partUuid());
        assertEquals(state("p3"), checkpoints.read(files, kept));
    }

    @Test
    void aFailedRestoreKeepsNothing() throws Exception {
        Checkpoint taken = checkpoints.take(files, state("x2"), "x2", Checkpoint.Kind.RUN, 1, 0);

        checkpoints.restore(files, taken.id(), branch("p1"), state("p1"), "p1", _ -> fail());
        assertEquals(1, checkpoints.list(files).size());
    }

    @Test
    void theOldestGoOverTheLimit() throws Exception {
        for (int n = 1; n <= 5; n++) {
            checkpoints.take(files, state("p" + n), "p" + n, Checkpoint.Kind.RUN, n, 0);
        }

        List<String> parts = checkpoints.list(files).stream().map(Checkpoint::partUuid).toList();
        assertEquals(List.of("p5", "p4", "p3"), parts);
        assertEquals(3, files.size());
    }

    @Test
    void otherFilesOfTheBookAreLeftAlone() throws Exception {
        files.writeOther("cover.png");
        files.writeOther("state-tracker.checkpoint.RUN.1.0.p1.json.gz");
        for (int n = 1; n <= 4; n++) {
            checkpoints.take(files, state("p" + n), "p" + n, Checkpoint.Kind.RUN, n, 0);
        }

        assertEquals(3, checkpoints.list(files).size());
        assertEquals(5, files.size());
    }

    @Test
    void onlyCheckpointsOnTheBranchAreOffered() throws Exception {
        checkpoints.take(files, state("p1"), "p1", Checkpoint.Kind.RUN, 1, 0);
        checkpoints.take(files, state("x2"), "x2", Checkpoint.Kind.RUN, 1, 0);
        checkpoints.take(files, state("p3"), "p3", Checkpoint.Kind.RUN, 1, 0);

        List<TrackerCheckpoints.Placed> placed = TrackerCheckpoints.onBranch(checkpoints.list(files), branch("p1", "p2", "p3"));
        assertEquals(List.of("p3", "p1"), placed.stream().map(p -> p.checkpoint().partUuid()).toList());
        assertEquals(List.of(3, 1), placed.stream().map(TrackerCheckpoints.Placed::position).toList());
    }

    @Test
    void aRestoreGivesTheStateToTheTracker() throws Exception {
        Checkpoint taken = checkpoints.take(files, state("p2"), "p2", Checkpoint.Kind.RUN, 1, 0);
        AtomicReference<JsonElement> restored = new AtomicReference<>();

        assertEquals(TrackerCheckpoints.Restore.RESTORED, checkpoints.restore(files, taken.id(), branch("p1", "p2", "p3"), restored::set));
        assertEquals(state("p2"), restored.get());
    }

    @Test
    void aCheckpointOffTheBranchIsNotRestored() throws Exception {
        Checkpoint taken = checkpoints.take(files, state("x2"), "x2", Checkpoint.Kind.RUN, 1, 0);

        assertEquals(TrackerCheckpoints.Restore.NOT_ON_BRANCH, checkpoints.restore(files, taken.id(), branch("p1", "p2"), _ -> fail()));
    }

    @Test
    void aMissingOrBrokenCheckpointIsGone() throws Exception {
        Checkpoint taken = checkpoints.take(files, state("p1"), "p1", Checkpoint.Kind.RUN, 1, 0);
        files.overwrite(taken.id(), "not gzip".getBytes());

        assertNull(checkpoints.read(files, taken));
        assertEquals(TrackerCheckpoints.Restore.GONE, checkpoints.restore(files, taken.id(), branch("p1"), _ -> fail()));
        assertEquals(TrackerCheckpoints.Restore.GONE, checkpoints.restore(files, "no-such-id", branch("p1"), _ -> fail()));
    }

    @Test
    void aFailingStoreDoesNotFailTheRun() {
        CheckpointFiles broken = new MemoryCheckpointFiles() {
            @Override
            public String write(String name, byte[] content) {
                throw new IllegalStateException("disk full");
            }
        };

        assertNull(checkpoints.take(broken, state("p1"), "p1", Checkpoint.Kind.RUN, 1, 0));
    }
}
