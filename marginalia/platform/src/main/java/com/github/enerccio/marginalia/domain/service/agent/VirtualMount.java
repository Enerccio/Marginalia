package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.file.Path;
import java.util.List;

/**
 * A part of the virtual file system backed by something: the workspace is a folder, later mounts can be backed by
 * services. A mount is given {@link VirtualPath}s (whole virtual paths, it knows where it is mounted) and answers with
 * them; a real path never leaves it.
 * <p>
 * Every method may throw {@link VirtualFileException} with a message for the model.
 */
public interface VirtualMount {

    /**
     * Whether {@link #write} and {@link #delete} work.
     */
    boolean isWritable();

    /**
     * @return the file or folder, null when there is none
     */
    VirtualEntry stat(VirtualPath path);

    /**
     * The entries of a folder: folders first, then by name.
     *
     * @throws VirtualFileException when the path is not a folder
     */
    List<VirtualEntry> list(VirtualPath folder);

    /**
     * The content of a file.
     *
     * @throws VirtualFileException when there is no such file or it is larger than the limit
     */
    byte[] read(VirtualPath file);

    /**
     * Creates the file, with the folders above it, or replaces it (or appends to it) in one step: readers see the old
     * content or the new one.
     */
    void write(VirtualPath file, byte[] content, boolean append);

    /**
     * As {@link #write} of a whole image file, which may be larger than a text file: the caller checked that it is an
     * image ({@link ImageFiles#check}).
     */
    default void writeImage(VirtualPath file, byte[] content) {
        throw new VirtualFileException(file + " can't hold images");
    }

    /**
     * As {@link #write} of a whole zip file, which may be larger than a text file: the caller checked that it starts
     * like one ({@link ArchiveFiles#isZip}).
     */
    default void writeArchive(VirtualPath file, byte[] content) {
        throw new VirtualFileException(file + " can't hold zip files");
    }

    /**
     * Unpacks a zip file of the mount into a folder that does not exist yet, all or nothing: the files are unpacked
     * next to the target first and moved into place when everything was checked and fits the limits. See {@link ZipExtractor}
     * for the checks.
     *
     * @return what was unpacked and what was left out
     * @throws VirtualFileException when the file is not a zip file, the archive is refused, or the target exists
     */
    default UnzipResult unzip(VirtualPath archive, VirtualPath target) {
        throw new VirtualFileException(archive + " can't be unpacked");
    }

    /**
     * Creates an empty folder, with the folders above it.
     *
     * @throws VirtualFileException when something of that name exists already
     */
    default void createFolder(VirtualPath folder) {
        throw new VirtualFileException(folder + " can't have folders");
    }

    /**
     * Deletes a file or a folder.
     *
     * @param recursive needed for a folder that is not empty
     */
    void delete(VirtualPath path, boolean recursive);

    /**
     * Moves or renames a file or a folder, to a name that is free; the folders above the target are created.
     */
    void move(VirtualPath from, VirtualPath to);

    /**
     * Copies a file or a folder with everything in it to a name that is free; the folders above the target are
     * created. All or nothing: a copy that does not fit the limits is not made.
     *
     * @return what was copied
     */
    default WorkspaceUsage copy(VirtualPath from, VirtualPath to) {
        throw new VirtualFileException(from + " can't be copied");
    }

    /**
     * The reverse of the mapping: the virtual path of a real one, null when the mount does not hold it.
     */
    VirtualPath toVirtual(Path real);

}
