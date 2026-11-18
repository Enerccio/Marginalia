package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgeProcessService;
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
 * A checkpoint after a run holds the whole state with the cursor; returning to it puts the characters and the cursor back, keeps the
 * settings, and the next run reads the parts after its part again.
 */
class KnowledgeCheckpointTest {

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
    private final TrackerCheckpoints checkpoints = new TrackerCheckpoints("knowledge-tracker");

    private static List<Part> branch() {
        List<Part> branch = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            branch.add(new Part("m" + i, "Fiora, Mara", "Text of part " + i + "."));
        }
        return branch;
    }

    /** A run over the parts up to {@code upTo}: the cursor moves, Fiora learns something in the last part. */
    private static void run(KnowledgeState state, List<Part> branch, int upTo) {
        BranchProcessor.process(state, branch, upTo, List.of(","), 0, Integer.MAX_VALUE);
        state.getCharacters().getFirst().getItems().add(KnowledgeItem.learned("learned in m" + upTo, ItemKind.KNOWS, "m" + upTo, "q"));
    }

    private Checkpoint take(KnowledgeState state) {
        return checkpoints.take(files, gson.toJsonTree(state), state.getCursor(), Checkpoint.Kind.RUN, 1, 0);
    }

    @Test
    void returningToACheckpointPutsBackTheCharactersAndTheCursor() throws Exception {
        List<Part> branch = branch();
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Fiora"));
        run(state, branch, 2);
        Checkpoint afterTwo = take(state);
        run(state, branch, 4);
        take(state);
        // the author edits by hand after the second run and changes a setting
        state.getCharacters().getFirst().getItems().add(KnowledgeItem.manual("a hand edit", ItemKind.KNOWS));
        state.getSettings().setDepth(7);
        assertEquals("m4", state.getCursor());

        KnowledgeState saved = gson.fromJson(checkpoints.read(files, afterTwo), KnowledgeState.class);
        KnowledgeState restored = KnowledgeProcessService.restore(state, saved, branch);

        assertEquals("m2", restored.getCursor());
        assertEquals(List.of("m1", "m2"), restored.getProcessed().stream().map(p -> p.getUuid()).toList());
        assertEquals(List.of("learned in m2"), restored.getCharacters().getFirst().getItems().stream().map(KnowledgeItem::getText).toList());
        assertEquals(7, restored.getSettings().getDepth(), "the settings stay as they are now");
        assertEquals(List.of("m3", "m4", "m5"), BranchProcessor.pending(restored, branch, 5, 10).stream().map(Part::uuid).toList(),
                "the next run reads again from the part after the checkpoint");
    }

    @Test
    void theStartPointStaysAsItIsNow() throws Exception {
        List<Part> branch = branch();
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Fiora"));
        run(state, branch, 3);
        Checkpoint checkpoint = take(state);
        state.setStartMessage("m2");

        KnowledgeState restored = KnowledgeProcessService.restore(state,
                gson.fromJson(checkpoints.read(files, checkpoint), KnowledgeState.class), branch);

        assertEquals("m2", restored.getStartMessage());
        assertEquals("m3", restored.getCursor());
    }

    @Test
    void aCheckpointWhosePartLeftTheBranchIsNotOffered() throws Exception {
        List<Part> branch = branch();
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Fiora"));
        run(state, branch, 3);
        Checkpoint checkpoint = take(state);
        // a swipe replaced the third part
        List<Part> swiped = new ArrayList<>(branch.subList(0, 2));
        swiped.add(new Part("m3b", "Fiora", "Another third part."));

        assertTrue(TrackerCheckpoints.onBranch(checkpoints.list(files), swiped).isEmpty());
        assertEquals(TrackerCheckpoints.Restore.NOT_ON_BRANCH, checkpoints.restore(files, checkpoint.id(), swiped, _ -> fail()));
    }
}
