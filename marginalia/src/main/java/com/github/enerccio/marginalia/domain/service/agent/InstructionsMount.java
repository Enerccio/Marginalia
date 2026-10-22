package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * {@code /AGENTS.md}: the instructions of the user to the agent ("agents.md" of the agent dialog) as one read only file,
 * so the agent can read them again whenever it likes. The text is the one the run started with, the user changes it in
 * the agent dialog, never the agent.
 */
final class InstructionsMount implements VirtualMount {

    private final VirtualPath file;
    private final byte[] content;

    InstructionsMount(VirtualPath file, String instructions) {
        this.file = file;
        this.content = (instructions == null ? "" : instructions).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public VirtualEntry stat(VirtualPath path) {
        return path.equals(file) ? new VirtualEntry(file, false, content.length, null) : null;
    }

    @Override
    public List<VirtualEntry> list(VirtualPath folder) {
        throw new VirtualFileException(folder + (folder.equals(file) ? " is a file, not a folder" : " does not exist"));
    }

    @Override
    public byte[] read(VirtualPath path) {
        if (!path.equals(file)) {
            throw new VirtualFileException("There is no file " + path);
        }
        return content.clone();
    }

    @Override
    public void write(VirtualPath path, byte[] content, boolean append) {
        throw readOnly();
    }

    @Override
    public void delete(VirtualPath path, boolean recursive) {
        throw readOnly();
    }

    @Override
    public void move(VirtualPath from, VirtualPath to) {
        throw readOnly();
    }

    @Override
    public VirtualPath toVirtual(Path real) {
        return null;
    }

    private VirtualFileException readOnly() {
        return new VirtualFileException(file + " is read only, the instructions are changed by the user");
    }

}
