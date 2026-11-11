package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.service.run.Runs;

import java.util.Set;

/**
 * What a generation of the book holds in its window now, by the {@link Runs.Stage stage} it is in. While it prepares the prompt (and from
 * the click until the run says more) it holds everything, as the freeze always did; from the inference on only the part it writes and the
 * branch above it: the lore, the settings of the book, the other tabs and the text of the earlier parts are free.
 *
 * @param stage  where the generation is
 * @param part   the id of the part it writes, null before it exists
 * @param branch the ids of the parts above it, which must stay where they are (no delete, merge, insert, switch of the branch)
 */
public record GenerationHold(Runs.Stage stage, Long part, Set<Long> branch) {

    /** Everything is held: the generation is started or prepares its prompt. */
    public static final GenerationHold WHOLE = new GenerationHold(Runs.Stage.PREPARE, null, Set.of());

    /**
     * The hold of a running generation, {@link #WHOLE} until it is in its inference.
     */
    public static GenerationHold of(Runs.Run generation) {
        if (generation == null || generation.getStage() != Runs.Stage.INFERENCE) {
            return WHOLE;
        }
        return new GenerationHold(Runs.Stage.INFERENCE, generation.getWrittenPart(), Set.copyOf(generation.getParts(Runs.Mode.STRUCTURE)));
    }

    /** Whether everything is held (the generation prepares). */
    public boolean isWhole() {
        return stage != Runs.Stage.INFERENCE;
    }

    /** Whether the part is the one being written: nothing is done to it. */
    public boolean writes(Long partId) {
        return isWhole() || partId != null && partId.equals(part);
    }

    /** Whether the part has to stay where it is: its text may change, but no delete, merge, regenerate, swipe or branch. */
    public boolean keepsInPlace(Long partId) {
        return writes(partId) || partId != null && branch.contains(partId);
    }
}
