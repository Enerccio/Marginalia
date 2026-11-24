package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

import java.util.*;

/** The files of one book in memory, for the tests; each write is one millisecond later than the one before. */
public class MemoryCheckpointFiles implements CheckpointFiles {

    private final Map<String, StoredFile> files = new LinkedHashMap<>();
    private final Map<String, byte[]> contents = new HashMap<>();
    private long clock = 1_000_000;

    @Override
    public String write(String name, byte[] content) {
        String id = UUID.randomUUID().toString();
        files.put(id, new StoredFile(id, name, clock++));
        contents.put(id, content.clone());
        return id;
    }

    @Override
    public List<StoredFile> list() {
        List<StoredFile> list = new ArrayList<>(files.values());
        list.sort(Comparator.comparingLong(StoredFile::time).reversed());
        return list;
    }

    @Override
    public byte[] read(String id) {
        return contents.get(id);
    }

    @Override
    public void delete(Collection<String> ids) {
        ids.forEach(id -> {
            files.remove(id);
            contents.remove(id);
        });
    }

    /** Replaces the content of a file, as if it was changed by hand. */
    public void overwrite(String id, byte[] content) {
        contents.put(id, content);
    }

    /** Adds a file another user of the book wrote (an image, another tracker). */
    public void writeOther(String name) {
        write(name, new byte[]{1, 2, 3});
    }

    public int size() {
        return files.size();
    }
}
