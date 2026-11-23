package com.github.enerccio.marginalia.domain.service.agent;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A read only folder whose files can be listed and nothing else: the files of a folder of the disk (not its
 * subfolders), for things the tools name but do not read, such as the checkpoints of an agent run, which are restored
 * by name with the checkpoint tools. The folder need not exist, it then lists nothing.
 */
public final class ListOnlyMount implements VirtualMount {

    private final VirtualPath mountPoint;
    private final Path folder;
    private final String notReadable;

    /**
     * @param mountPoint  where the folder is in the virtual tree
     * @param folder      the folder of the disk whose files are listed
     * @param notReadable the message (to the model) when a file is read
     */
    public ListOnlyMount(VirtualPath mountPoint, Path folder, String notReadable) {
        this.mountPoint = mountPoint;
        this.folder = folder.toAbsolutePath().normalize();
        this.notReadable = notReadable;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public VirtualEntry stat(VirtualPath path) {
        if (path.equals(mountPoint)) {
            return new VirtualEntry(path, true, 0, null);
        }
        if (path.getDepth() != mountPoint.getDepth() + 1 || !path.startsWith(mountPoint)) {
            return null;
        }
        return list(mountPoint).stream().filter(e -> e.path().equals(path)).findFirst().orElse(null);
    }

    @Override
    public List<VirtualEntry> list(VirtualPath path) {
        if (!path.equals(mountPoint)) {
            throw new VirtualFileException("There is no folder " + path);
        }
        List<VirtualEntry> entries = new ArrayList<>();
        if (Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                for (Path child : stream) {
                    BasicFileAttributes attributes = Files.readAttributes(child, BasicFileAttributes.class,
                            LinkOption.NOFOLLOW_LINKS);
                    if (attributes.isRegularFile()) {
                        try {
                            entries.add(new VirtualEntry(mountPoint.resolve(child.getFileName().toString()), false,
                                    attributes.size(), attributes.lastModifiedTime().toInstant()));
                        } catch (VirtualFileException e) {
                            // not a valid virtual name
                        }
                    }
                }
            } catch (IOException e) {
                throw new VirtualFileException(mountPoint + " can't be listed", e);
            }
        }
        entries.sort(Comparator.comparing(e -> e.path().getName()));
        return entries;
    }

    @Override
    public byte[] read(VirtualPath file) {
        throw new VirtualFileException(notReadable);
    }

    @Override
    public void write(VirtualPath file, byte[] content, boolean append) {
        throw new VirtualFileException(mountPoint + " is read only");
    }

    @Override
    public void delete(VirtualPath path, boolean recursive) {
        throw new VirtualFileException(mountPoint + " is read only");
    }

    @Override
    public void move(VirtualPath from, VirtualPath to) {
        throw new VirtualFileException(mountPoint + " is read only");
    }

    @Override
    public VirtualPath toVirtual(Path real) {
        Path path = real.toAbsolutePath().normalize();
        if (!path.startsWith(folder)) {
            return null;
        }
        VirtualPath result = mountPoint;
        try {
            for (Path name : folder.relativize(path)) {
                if (!name.toString().isEmpty()) {
                    result = result.resolve(name.toString());
                }
            }
        } catch (VirtualFileException e) {
            return null;
        }
        return result;
    }

}
