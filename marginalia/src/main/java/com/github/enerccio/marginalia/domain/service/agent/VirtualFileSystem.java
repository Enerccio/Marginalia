package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * The files an agent sees: one tree of {@link VirtualPath}s over a few {@link VirtualMount}s. The workspace of the
 * agent is the root, and {@code /checkpoints} (when there is a run) is a read only mount next to it. This is the only
 * thing the file tools of the model hold; there is no way from here to a {@code File} or a {@code Path} that is
 * returned, and nothing outside the mounts can be named.
 * <pre>
 * model's text -> VirtualPath.parse -> mount (the cage: names checked on the disk) -> real path (never returned)
 * </pre>
 * Created per run (or per look of the user at the workspace) by the {@code AgentVirtualFileSystemService}; cheap, and
 * the instances of one workspace share its lock.
 */
public final class VirtualFileSystem {

    public static final String CHECKPOINTS = "checkpoints";

    private final WorkspaceMount workspace;
    private final Map<VirtualPath, VirtualMount> mounts = new LinkedHashMap<>();
    private final WorkspaceLimits limits;

    private VirtualFileSystem(WorkspaceMount workspace, WorkspaceLimits limits) {
        this.workspace = workspace;
        this.limits = limits;
    }

    /**
     * The workspace of an agent only, for the user to look at.
     *
     * @param folder the workspace, created when missing
     * @param lock   shared by everything that changes this workspace
     */
    public static VirtualFileSystem ofWorkspace(Path folder, WorkspaceLimits limits, Lock lock) {
        return new VirtualFileSystem(new WorkspaceMount(folder, limits, lock, java.util.Set.of(CHECKPOINTS)), limits);
    }

    /**
     * The files of a run: the workspace of its agent and the list of its checkpoints.
     */
    public static VirtualFileSystem ofRun(RunFolder folder, WorkspaceLimits limits, Lock lock) {
        VirtualFileSystem fs = ofWorkspace(folder.workspace(), limits, lock);
        VirtualPath mountPoint = VirtualPath.ROOT.resolve(CHECKPOINTS);
        fs.mounts.put(mountPoint, new CheckpointsMount(mountPoint, folder.checkpoints()));
        return fs;
    }

    public WorkspaceLimits getLimits() {
        return limits;
    }

    /**
     * @throws VirtualFileException when the text is not a valid path
     */
    public VirtualPath parse(String text) {
        return VirtualPath.parse(text);
    }

    private VirtualMount mountFor(VirtualPath path) {
        for (Map.Entry<VirtualPath, VirtualMount> mount : mounts.entrySet()) {
            if (path.startsWith(mount.getKey())) {
                return mount.getValue();
            }
        }
        return workspace;
    }

    private VirtualMount writableMountFor(VirtualPath path) {
        VirtualMount mount = mountFor(path);
        if (!mount.isWritable()) {
            throw new VirtualFileException(path + " is read only");
        }
        return mount;
    }

    /**
     * @return the file or folder, null when there is none
     */
    public VirtualEntry stat(VirtualPath path) {
        if (path.isRoot()) {
            return new VirtualEntry(path, true, 0, null);
        }
        return mountFor(path).stat(path);
    }

    /**
     * The entries of a folder, folders first then by name; the root lists the other mounts as well.
     */
    public List<VirtualEntry> list(VirtualPath folder) {
        List<VirtualEntry> entries = new ArrayList<>(mountFor(folder).list(folder));
        if (folder.isRoot()) {
            List<VirtualEntry> mountPoints = new ArrayList<>();
            mounts.keySet().forEach(point -> mountPoints.add(new VirtualEntry(point, true, 0, null)));
            entries.addAll(0, mountPoints);
        }
        return entries;
    }

    /**
     * Everything below a folder, folders before what is in them, at most {@code limit} entries; ask for one more than
     * needed to know whether there is more. Other mounts are not entered from the root.
     */
    public List<VirtualEntry> walk(VirtualPath folder, int limit) {
        List<VirtualEntry> found = new ArrayList<>();
        walk(mountFor(folder), folder, limit, found);
        return found;
    }

    private void walk(VirtualMount mount, VirtualPath folder, int limit, List<VirtualEntry> found) {
        for (VirtualEntry entry : mount.list(folder)) {
            if (found.size() >= limit) {
                return;
            }
            found.add(entry);
            if (entry.directory()) {
                walk(mount, entry.path(), limit, found);
            }
        }
    }

    public byte[] read(VirtualPath file) {
        return mountFor(file).read(file);
    }

    /**
     * The file as text.
     *
     * @throws VirtualFileException when it is not UTF-8 text
     */
    public String readText(VirtualPath file) {
        byte[] bytes = read(file);
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new VirtualFileException(file + " is not a text file");
        }
    }

    /**
     * Creates or replaces a file (or appends to it) with the text, in UTF-8.
     */
    public void writeText(VirtualPath file, String text, boolean append) {
        if (text.indexOf('\0') >= 0) {
            throw new VirtualFileException("Only text can be written, the content has a NUL character");
        }
        writableMountFor(file).write(file, text.getBytes(StandardCharsets.UTF_8), append);
    }

    public void delete(VirtualPath path, boolean recursive) {
        writableMountFor(path).delete(path, recursive);
    }

    /**
     * Moves or renames a file or a folder to a name that is not taken.
     *
     * @throws VirtualFileException when the source is missing, the target exists, a folder would go into itself, or a
     *                              path is on a mount that can't be changed (the two must be on the same mount)
     */
    public void move(VirtualPath from, VirtualPath to) {
        VirtualMount source = writableMountFor(from);
        if (writableMountFor(to) != source) {
            throw new VirtualFileException("A file can't be moved between " + from + " and " + to);
        }
        source.move(from, to);
    }

    /**
     * The virtual path of a real one, what every output to the model goes through.
     *
     * @throws VirtualFileException when no mount holds the path, which is also how a stray real path is kept from
     *                              leaving
     */
    public VirtualPath toVirtual(Path real) {
        for (VirtualMount mount : mounts.values()) {
            VirtualPath path = mount.toVirtual(real);
            if (path != null) {
                return path;
            }
        }
        VirtualPath path = workspace.toVirtual(real);
        if (path == null) {
            throw new VirtualFileException("The path is not in the workspace");
        }
        return path;
    }

    /**
     * What the workspace holds.
     */
    public WorkspaceUsage getUsage() {
        return workspace.usage();
    }

    /**
     * Deletes everything in the workspace.
     */
    public void clearWorkspace() {
        workspace.clear();
    }

    /**
     * Copies the workspace of another file system into this one.
     */
    public void copyWorkspaceFrom(VirtualFileSystem other) {
        workspace.copyFrom(other.workspace);
    }

}
