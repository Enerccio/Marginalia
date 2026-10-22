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
 * {@code /checkpoints}: the checkpoints of the run, which can be listed and nothing else. They are restored by name
 * with the checkpoint tools of the runner, they are not files to read or change.
 */
final class CheckpointsMount implements VirtualMount {

    private final VirtualPath mountPoint;
    private final Path folder;

    CheckpointsMount(VirtualPath mountPoint, Path folder) {
        this.mountPoint = mountPoint;
        this.folder = folder.toAbsolutePath().normalize();
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
                            // not a name of a checkpoint
                        }
                    }
                }
            } catch (IOException e) {
                throw new VirtualFileException("The checkpoints can't be listed", e);
            }
        }
        entries.sort(Comparator.comparing(e -> e.path().getName()));
        return entries;
    }

    @Override
    public byte[] read(VirtualPath file) {
        throw new VirtualFileException("Checkpoints can only be listed, they are restored with the checkpoint tools");
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
