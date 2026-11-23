package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * One read only text file held in memory, for example the instructions of the user to an agent, so the model can read
 * them again whenever it likes while only the user changes them.
 */
public final class TextFileMount implements VirtualMount {

    private final VirtualPath file;
    private final byte[] content;
    private final String readOnly;

    /**
     * @param file     where the file is in the virtual tree
     * @param text     its content, null is empty
     * @param readOnly the message (to the model) when the file is changed
     */
    public TextFileMount(VirtualPath file, String text, String readOnly) {
        this.file = file;
        this.content = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
        this.readOnly = readOnly;
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
        return new VirtualFileException(readOnly);
    }

}
