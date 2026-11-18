package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.statetracker.service.StateProcessService;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.Checkpoint;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.CheckpointFiles;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.TrackerCheckpoints;
import com.github.enerccio.marginalia.extensions.storytracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A checkpoint after a run holds the whole state with the cursor; returning to it puts the values, the marks of the clock and the
 * cursor back, keeps what the author set up, and the next run reads the parts after its part again.
 */
class StateCheckpointTest {

    /** The files of one book in memory. */
    private static class Files implements CheckpointFiles {
        private final Map<String, StoredFile> files = new LinkedHashMap<>();
        private final Map<String, byte[]> contents = new HashMap<>();
        private long clock = 1;

        @Override
        public String write(String name, byte[] content) {
            String id = UUID.randomUUID().toString();
            files.put(id, new StoredFile(id, name, clock++));
            contents.put(id, content);
            return id;
        }

        @Override
        public List<StoredFile> list() {
            return files.values().stream().sorted(Comparator.comparingLong(StoredFile::time).reversed()).toList();
        }

        @Override
        public byte[] read(String id) {
            return contents.get(id);
        }

        @Override
        public void delete(Collection<String> ids) {
            ids.forEach(files::remove);
        }
    }

    private final Gson gson = new Gson();
    private final Files files = new Files();
    private final TrackerCheckpoints checkpoints = new TrackerCheckpoints("state-tracker", 3);

    private static List<Part> branch() {
        List<Part> branch = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            branch.add(new Part("m" + i, "Fiora", "Text of part " + i + "."));
        }
        return branch;
    }

    private static StateBook book() {
        StateBook state = new StateBook();
        state.getCategories().add(new Category("carrying", "Carrying", CategoryKind.LIST, ""));
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.track("carrying");
        state.getCharacters().add(fiora);
        state.getClock().setEnabled(true);
        return state;
    }

    /** A run up to part {@code upTo}: the cursor moves, Fiora picks something up in the last part, the part marks the clock. */
    private static void run(StateBook state, List<Part> branch, int upTo) {
        BranchProcessor.process(state, branch, upTo, List.of(","), 0, Integer.MAX_VALUE);
        state.getCharacters().getFirst().set(state.findCategory("carrying"), "item of m" + upTo, upTo, "m" + upTo);
        state.getClock().getMarks().put("m" + upTo, new ClockMark("m" + upTo, ClockMark.Kind.ADVANCE));
    }

    private Checkpoint take(StateBook state) {
        return checkpoints.take(files, gson.toJsonTree(state), state.getCursor(), Checkpoint.Kind.RUN, 1, 0);
    }

    private static List<String> carried(StateBook state) {
        return state.getCharacters().getFirst().active("carrying").stream().map(StateItem::getText).toList();
    }

    @Test
    void returningToACheckpointPutsBackTheValuesTheClockAndTheCursor() throws Exception {
        List<Part> branch = branch();
        StateBook state = book();
        run(state, branch, 2);
        Checkpoint afterTwo = take(state);
        run(state, branch, 4);
        take(state);
        state.getSettings().setPartsPerRun(9);
        state.getClock().setAnchor("the awakening");

        StateBook restored = StateProcessService.restore(state, gson.fromJson(checkpoints.read(files, afterTwo), StateBook.class), branch);

        assertEquals("m2", restored.getCursor());
        assertEquals(List.of("item of m2"), carried(restored));
        assertEquals(Set.of("m2"), restored.getClock().getMarks().keySet());
        assertEquals(9, restored.getSettings().getPartsPerRun(), "the settings stay as they are now");
        assertEquals("the awakening", restored.getClock().getAnchor(), "the settings of the clock stay");
        assertTrue(restored.getClock().isEnabled());
        assertEquals(List.of("m3", "m4"), BranchProcessor.pending(restored, branch, 4, 10).stream().map(Part::uuid).toList(),
                "the next run reads again from the part after the checkpoint");
    }

    @Test
    void aCategoryTheValuesNeedComesBackTheOthersStayAsTheyAre() throws Exception {
        List<Part> branch = branch();
        StateBook state = book();
        run(state, branch, 2);
        Checkpoint checkpoint = take(state);
        // the author deletes the category and adds another one
        state.deleteCategory("carrying");
        state.getCategories().add(new Category("mood", "Mood", CategoryKind.DESCRIPTION, ""));

        StateBook restored = StateProcessService.restore(state, gson.fromJson(checkpoints.read(files, checkpoint), StateBook.class), branch);

        assertNotNull(restored.findCategory("mood"));
        assertNotNull(restored.findCategory("carrying"));
        assertEquals(List.of("item of m2"), carried(restored));
    }

    @Test
    void theTrackerKeepsTheLastCheckpoints() throws Exception {
        List<Part> branch = branch();
        StateBook state = book();
        for (int n = 1; n <= 5; n++) {
            run(state, branch, n);
            take(state);
        }

        assertEquals(List.of("m5", "m4", "m3"), checkpoints.list(files).stream().map(Checkpoint::partUuid).toList());
    }
}
