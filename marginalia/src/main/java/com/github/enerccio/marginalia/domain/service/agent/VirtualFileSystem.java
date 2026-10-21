package com.github.enerccio.marginalia.domain.service.agent;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * The files an agent sees: one tree of {@link VirtualPath}s over a few {@link VirtualMount}s. The workspace of the
 * agent is the root, and {@code /checkpoints} and {@code /AGENTS.md} (when there is a run) are read only mounts next to it. This is the only
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

    /**
     * The read only file with the instructions of the user to the agent; the name is reserved at the root of the workspace.
     */
    public static final String INSTRUCTIONS = "AGENTS.md";

    /**
     * The folder of the workspace for the scratch files of the sub-agents, one folder for each of them.
     */
    public static final String SUBAGENTS = "tmp";

    private final WorkspaceMount workspace;
    private final Map<VirtualPath, VirtualMount> mounts = new LinkedHashMap<>();
    private final WorkspaceLimits limits;
    // when not null, the only folder that can be changed (the rest reads as usual)
    private final VirtualPath writeScope;

    private VirtualFileSystem(WorkspaceMount workspace, WorkspaceLimits limits) {
        this(workspace, limits, null);
    }

    private VirtualFileSystem(WorkspaceMount workspace, WorkspaceLimits limits, VirtualPath writeScope) {
        this.workspace = workspace;
        this.limits = limits;
        this.writeScope = writeScope;
    }

    /**
     * The same files with one change: only the folder (and what is below it) can be changed, everything else reads as before and is
     * read only. For a sub-agent, which writes in a folder of its own.
     */
    public VirtualFileSystem scopedTo(VirtualPath folder) {
        VirtualFileSystem scoped = new VirtualFileSystem(workspace, limits, folder);
        scoped.mounts.putAll(mounts);
        return scoped;
    }

    /**
     * The folder this file system can change, null when it can change the whole workspace.
     */
    public VirtualPath getWriteScope() {
        return writeScope;
    }

    /**
     * The workspace of an agent only, for the user to look at.
     *
     * @param folder the workspace, created when missing
     * @param lock   shared by everything that changes this workspace
     */
    public static VirtualFileSystem ofWorkspace(Path folder, WorkspaceLimits limits, Lock lock) {
        return new VirtualFileSystem(new WorkspaceMount(folder, limits, lock, java.util.Set.of(CHECKPOINTS, INSTRUCTIONS)), limits);
    }

    /**
     * The files of a run: the workspace of its agent and the list of its checkpoints.
     */
    public static VirtualFileSystem ofRun(RunFolder folder, WorkspaceLimits limits, Lock lock) {
        return ofRun(folder, limits, lock, null);
    }

    /**
     * The files of a run: the workspace of its agent, the list of its checkpoints and, when there are instructions,
     * {@code /AGENTS.md} with them.
     *
     * @param instructions the text of the file, null when the file is not there
     */
    public static VirtualFileSystem ofRun(RunFolder folder, WorkspaceLimits limits, Lock lock, String instructions) {
        VirtualFileSystem fs = ofWorkspace(folder.workspace(), limits, lock);
        VirtualPath mountPoint = VirtualPath.ROOT.resolve(CHECKPOINTS);
        fs.mounts.put(mountPoint, new CheckpointsMount(mountPoint, folder.checkpoints()));
        if (instructions != null) {
            VirtualPath file = VirtualPath.ROOT.resolve(INSTRUCTIONS);
            fs.mounts.put(file, new InstructionsMount(file, instructions));
        }
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
        if (writeScope != null && !path.startsWith(writeScope)) {
            throw new VirtualFileException(path + " is read only for you, you can change only " + writeScope + " and what is in it");
        }
        return mount;
    }

    /**
     * Whether the file can be written: not in a read only mount and inside the write scope.
     *
     * @throws VirtualFileException when it can't, the message says why
     */
    public void checkWritable(VirtualPath file) {
        writableMountFor(file);
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
            // a file of the workspace the name of a mount is hidden by it
            entries.removeIf(entry -> mounts.keySet().stream().anyMatch(point -> point.getName().equalsIgnoreCase(entry.path().getName())));
            // the folders of the other mounts come first, their files by name among the files
            List<VirtualEntry> mountEntries = mountEntries();
            entries.addAll(0, mountEntries.stream().filter(VirtualEntry::directory).toList());
            for (VirtualEntry file : mountEntries.stream().filter(e -> !e.directory()).toList()) {
                String name = file.path().getName().toLowerCase(Locale.ROOT);
                int at = 0;
                while (at < entries.size() && (entries.get(at).directory()
                        || entries.get(at).path().getName().toLowerCase(Locale.ROOT).compareTo(name) < 0)) {
                    at++;
                }
                entries.add(at, file);
            }
        }
        return entries;
    }

    /**
     * The other mounts as they appear at the root: {@code /checkpoints} a folder, {@code /AGENTS.md} a file.
     */
    public List<VirtualEntry> mountEntries() {
        List<VirtualEntry> entries = new ArrayList<>();
        mounts.forEach((point, mount) -> entries.add(mount.stat(point)));
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

    /**
     * The bytes of a file: a text file of at most {@link WorkspaceLimits#maxFileBytes()}, an image of at most
     * {@link WorkspaceLimits#maxImageBytes()}, a zip file of at most {@link WorkspaceLimits#maxArchiveBytes()}.
     */
    public byte[] read(VirtualPath file) {
        byte[] bytes = mountFor(file).read(file);
        if (bytes.length > limits.maxFileBytes() && !ImageFiles.isImage(bytes) && !ArchiveFiles.isZip(bytes)) {
            throw new VirtualFileException("The file " + file + " is larger than the limit of " + limits.maxFileBytes() + " bytes");
        }
        return bytes;
    }

    /**
     * The file as text.
     *
     * @throws VirtualFileException when it is not UTF-8 text (an image says so)
     */
    public String readText(VirtualPath file) {
        byte[] bytes = read(file);
        if (ImageFiles.isImage(bytes)) {
            throw new VirtualFileException(file + " is an image, not a text file; view_image shows it to you");
        }
        if (ArchiveFiles.isZip(bytes)) {
            throw new VirtualFileException(file + " is a zip file, not a text file; unzip_file unpacks it into a folder");
        }
        return decodeText(bytes, file);
    }

    /**
     * The image file, as what a model is sent of it ({@link ImageFiles#prepare}).
     *
     * @throws VirtualFileException when there is no such file or it is not an image
     */
    public ImageFiles.Prepared readImage(VirtualPath file) {
        return ImageFiles.prepare(read(file), file);
    }

    /**
     * Whether the path is a file that holds an image.
     */
    public boolean isImage(VirtualPath file) {
        VirtualEntry entry = stat(file);
        if (entry == null || entry.directory()) {
            return false;
        }
        try {
            return ImageFiles.isImage(read(file));
        } catch (VirtualFileException e) {
            return false;
        }
    }

    /**
     * The bytes as text, what the model can read and write: UTF-8 with no NUL character.
     *
     * @param file the name for the message
     * @throws VirtualFileException when it is not text
     */
    public static String decodeText(byte[] bytes, VirtualPath file) {
        try {
            String text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
            if (text.indexOf('\0') >= 0) {
                throw new CharacterCodingException();
            }
            return text;
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

    /**
     * Creates an image file or replaces it, the bytes as they are. It counts against the total and the number of files
     * like any file, the size it may have is {@link WorkspaceLimits#maxImageBytes()}.
     *
     * @throws VirtualFileException when the bytes are not a PNG, a JPEG, a GIF or a WebP, or have too many pixels
     */
    public void writeImage(VirtualPath file, byte[] content) {
        ImageFiles.check(content, file);
        writableMountFor(file).writeImage(file, content);
    }

    /**
     * Creates a zip file or replaces it, the bytes as they are. It counts against the total and the number of files
     * like any file, the size it may have is {@link WorkspaceLimits#maxArchiveBytes()}.
     *
     * @throws VirtualFileException when the bytes don't start like a zip file
     */
    public void writeArchive(VirtualPath file, byte[] content) {
        if (!ArchiveFiles.isZip(content)) {
            throw new VirtualFileException(file + " is not a zip file");
        }
        writableMountFor(file).writeArchive(file, content);
    }

    /**
     * Unpacks a zip file of the workspace into a folder that does not exist yet. All or nothing, and the archive is
     * not trusted: see {@link ZipExtractor} for what is checked, the limits of the workspace hold for what is unpacked.
     *
     * @return what was unpacked and what was left out
     * @throws VirtualFileException when the file is not a zip file, the archive is refused, the target exists, or the
     *                              paths are not on the same mount that can be changed
     */
    public UnzipResult unzip(VirtualPath archive, VirtualPath target) {
        VirtualMount source = mountFor(archive);
        VirtualMount destination = writableMountFor(target);
        if (source != destination) {
            throw new VirtualFileException("A zip file can't be unpacked between " + archive + " and " + target);
        }
        return destination.unzip(archive, target);
    }

    /**
     * Creates an empty folder (and the folders above it); folders don't count against the limits.
     *
     * @throws VirtualFileException when the name is taken or not allowed, or the path is on a mount that can't be changed
     */
    public void createFolder(VirtualPath folder) {
        writableMountFor(folder).createFolder(folder);
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
     * Copies a file or a folder to a name that is not taken.
     *
     * @return the files and bytes copied
     * @throws VirtualFileException when the source is missing, the target exists, a folder would go into itself, the
     *                              copy does not fit the limits, or the paths are not on the same mount that can be changed
     */
    public WorkspaceUsage copy(VirtualPath from, VirtualPath to) {
        VirtualMount source = mountFor(from);
        VirtualMount target = writableMountFor(to);
        if (source != target) {
            throw new VirtualFileException("A file can't be copied between " + from + " and " + to);
        }
        return target.copy(from, to);
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
        refuseWhenScoped();
        workspace.clear();
    }

    /**
     * Deletes the files and all the folders of the workspace except what is kept: a file or folder named by its path, or
     * the ones a pattern ({@link VirtualGlob}) matches. A kept folder keeps what is in it, a kept file keeps the folders
     * above it. Nothing is deleted when something to keep is not there.
     *
     * @param keep paths or patterns, may be empty
     * @throws VirtualFileException when something to keep is not in the workspace
     */
    public Cleared clearWorkspace(List<String> keep) {
        refuseWhenScoped();
        List<VirtualPath> kept = new ArrayList<>();
        List<VirtualEntry> everything = null;
        for (String item : keep) {
            if (StringUtils.containsAny(item, '*', '?', '[', '{')) {
                VirtualGlob glob = VirtualGlob.parse(item);
                if (everything == null) {
                    everything = walk(VirtualPath.ROOT, Integer.MAX_VALUE);
                }
                List<VirtualPath> matched = everything.stream().map(VirtualEntry::path)
                        .filter(path -> glob.matches(VirtualPath.ROOT, path)).toList();
                if (matched.isEmpty()) {
                    throw new VirtualFileException("Nothing in the workspace matches " + VirtualPath.printable(item));
                }
                kept.addAll(matched);
                continue;
            }
            VirtualPath path = parse(item);
            if (path.isRoot()) {
                throw new VirtualFileException("The workspace itself can't be kept, name what to keep in it");
            }
            if (mountFor(path) != workspace) {
                throw new VirtualFileException(path + " is not in the workspace");
            }
            if (stat(path) == null) {
                throw new VirtualFileException("There is nothing at " + path + " to keep");
            }
            kept.add(path);
        }
        int[] counts = new int[2];
        clear(VirtualPath.ROOT, kept, counts);
        return new Cleared(counts[0], counts[1], List.copyOf(kept));
    }

    private void clear(VirtualPath folder, List<VirtualPath> kept, int[] counts) {
        for (VirtualEntry entry : workspace.list(folder)) {
            VirtualPath path = entry.path();
            if (kept.stream().anyMatch(path::startsWith)) {
                continue;
            }
            if (entry.directory() && kept.stream().anyMatch(keep -> keep.startsWith(path))) {
                clear(path, kept, counts);
                continue;
            }
            if (entry.directory()) {
                for (VirtualEntry inside : walk(path, Integer.MAX_VALUE)) {
                    counts[inside.directory() ? 1 : 0]++;
                }
                counts[1]++;
            } else {
                counts[0]++;
            }
            workspace.delete(path, true);
        }
    }

    /**
     * What {@link #clearWorkspace(List)} deleted and what it left.
     *
     * @param kept the files and folders that were named to be kept, as paths
     */
    public record Cleared(int files, int folders, List<VirtualPath> kept) {
    }

    /**
     * Writes the workspace into the archive as entries below {@code prefix} (ending with a slash), for a checkpoint.
     */
    public WorkspaceUsage snapshotWorkspace(java.util.zip.ZipOutputStream zip, String prefix) {
        return workspace.snapshotTo(zip, prefix);
    }

    /**
     * Replaces everything in the workspace with what the archive holds below {@code prefix}; a failure changes nothing.
     *
     * @param maxBytes the most the files may take unpacked
     * @return the files that were in the workspace and are gone, as paths
     */
    public List<String> restoreWorkspace(java.util.zip.ZipFile zip, String prefix, long maxBytes) {
        refuseWhenScoped();
        return workspace.replaceFrom(zip, prefix, maxBytes);
    }

    /**
     * Copies the workspace of another file system into this one.
     */
    public void copyWorkspaceFrom(VirtualFileSystem other) {
        refuseWhenScoped();
        workspace.copyFrom(other.workspace);
    }

    private void refuseWhenScoped() {
        if (writeScope != null) {
            throw new VirtualFileException("The workspace can't be replaced from " + writeScope);
        }
    }

}
