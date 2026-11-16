package com.github.enerccio.marginalia.extensions.storytracker.process;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Where a tracker that follows the story stands in a book: the start point, the cursor, the parts it read and what it
 * keeps from them. The state a plugin saves with the book implements it, and {@link BranchProcessor} moves it along the
 * active branch.
 */
public interface TrackerProgress {

    /** The part (uuid) processing begins at; {@code null} = the start of the book. */
    String getStartMessage();

    /** The last part (uuid) that was processed; {@code null} = none yet. */
    String getCursor();

    void setCursor(String cursor);

    /** The parts that were processed, with the hash of their text; information for the user, modifiable. */
    List<ProcessedPart> getProcessed();

    /**
     * Takes back what came from the parts (uuids) that {@code dropped} accepts. What the user made by hand stays. The
     * cursor is moved by the caller, which knows the order of the parts.
     *
     * @return how many things changed
     */
    int rewind(Predicate<String> dropped);

    /**
     * What the tracker notes, without a model, when the processor reads a part (the Knowledge Tracker: who has met whom).
     * Nothing by default.
     *
     * @param separators how the present characters field of the part is split
     */
    default void read(Part part, Collection<String> separators) {
    }
}
