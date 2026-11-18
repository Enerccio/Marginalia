package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

import java.util.Collection;
import java.util.List;

/**
 * Where the checkpoints of one book are kept: files of the user linked to the book ({@link ResourceCheckpointFiles}), never in the
 * attributes of the book, so a save of the book stays small and a backup of the book does not carry them.
 */
public interface CheckpointFiles {

    /** A stored file: its id, its name and when it was written (millis). */
    record StoredFile(String id, String name, long time) {
    }

    /** Stores a new file. @return its id */
    String write(String name, byte[] content) throws Exception;

    /** The files of the book, newest first (also those of other trackers and of other uses: filter by name). */
    List<StoredFile> list() throws Exception;

    /** @return the content, {@code null} when the file is gone */
    byte[] read(String id) throws Exception;

    void delete(Collection<String> ids) throws Exception;
}
