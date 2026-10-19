package com.github.enerccio.marginalia.domain.service.agent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * The workspace of an agent: a folder of the disk, the only one the virtual root maps to. This class is where the
 * cage is made of real checks, after {@link VirtualPath} cleaned the text:
 * <ul>
 * <li>a path is walked name by name from the real root; a symbolic link or anything but a file and a folder on the
 * way is refused, so a link planted in the folder leads nowhere, and so is a link as the folder itself;
 * <li>names that differ only in case are one name (also on a file system that tells them apart), and a name that
 * exists in another spelling is reported;
 * <li>files are written whole to a temporary file next to the target and moved over it, so a reader sees the old
 * content or the new one; writes and deletes of the workspace take its lock, which also keeps the quotas true;
 * <li>errors name virtual paths only.
 * </ul>
 */
final class WorkspaceMount implements VirtualMount {

    /**
     * Start of the names of the files being written; such files are not listed and the model can't create them.
     */
    static final String TEMP_PREFIX = ".marginalia-tmp-";

    private final Path root;
    private final Path realRoot;
    private final WorkspaceLimits limits;
    private final Lock lock;
    // names (lower case) the model can't create at the root, because they are mounts
    private final Set<String> reservedAtRoot;

    /**
     * @param root     the folder of the workspace, created when missing; must not be a symbolic link
     * @param lock     taken by every change; the same lock for every mount of the same workspace
     * @param reserved names of the other mounts at the root
     * @throws IllegalStateException when the folder is a link or not a folder
     */
    WorkspaceMount(Path root, WorkspaceLimits limits, Lock lock, Set<String> reserved) {
        this.root = root.toAbsolutePath().normalize();
        this.limits = limits;
        this.lock = lock;
        this.reservedAtRoot = Set.copyOf(reserved.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList());
        try {
            if (Files.isSymbolicLink(this.root)) {
                throw new IllegalStateException("The workspace folder is a symbolic link");
            }
            Files.createDirectories(this.root);
            if (!Files.isDirectory(this.root, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalStateException("The workspace is not a folder");
            }
            this.realRoot = this.root.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("The workspace folder can't be opened", e);
        }
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public VirtualEntry stat(VirtualPath path) {
        Path real = resolve(path, false);
        BasicFileAttributes attributes = attributes(real);
        return attributes == null ? null : entry(path, attributes);
    }

    @Override
    public List<VirtualEntry> list(VirtualPath folder) {
        Path real = resolve(folder, false);
        BasicFileAttributes attributes = attributes(real);
        if (attributes == null) {
            throw new VirtualFileException("There is no folder " + folder);
        }
        if (!attributes.isDirectory()) {
            throw new VirtualFileException(folder + " is a file, not a folder");
        }
        List<VirtualEntry> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(real)) {
            for (Path child : stream) {
                String name = child.getFileName().toString();
                BasicFileAttributes childAttributes = attributes(child);
                if (name.startsWith(TEMP_PREFIX) || childAttributes == null || childAttributes.isSymbolicLink()
                        || !(childAttributes.isDirectory() || childAttributes.isRegularFile())) {
                    continue;
                }
                try {
                    entries.add(entry(folder.resolve(Normalizer.normalize(name, Normalizer.Form.NFC)), childAttributes));
                } catch (VirtualFileException e) {
                    // a name put there by hand that the model could not write: not shown
                }
            }
        } catch (IOException e) {
            throw new VirtualFileException("The folder " + folder + " can't be listed", e);
        }
        entries.sort(Comparator.comparing(VirtualEntry::directory).reversed()
                .thenComparing(e -> e.path().getName().toLowerCase(Locale.ROOT)).thenComparing(e -> e.path().getName()));
        return entries;
    }

    @Override
    public byte[] read(VirtualPath file) {
        Path real = resolve(file, false);
        BasicFileAttributes attributes = attributes(real);
        if (attributes == null) {
            throw new VirtualFileException("There is no file " + file);
        }
        if (attributes.isDirectory()) {
            throw new VirtualFileException(file + " is a folder, not a file");
        }
        if (attributes.size() > limits.maxFileBytes()) {
            throw new VirtualFileException("The file " + file + " is larger than the limit of " + limits.maxFileBytes() + " bytes");
        }
        try (InputStream in = Files.newInputStream(real, LinkOption.NOFOLLOW_LINKS)) {
            return in.readNBytes((int) limits.maxFileBytes() + 1);
        } catch (IOException e) {
            throw new VirtualFileException("The file " + file + " can't be read", e);
        }
    }

    @Override
    public void write(VirtualPath file, byte[] content, boolean append) {
        if (file.isRoot()) {
            throw new VirtualFileException("/ is the folder of the workspace, not a file");
        }
        lock.lock();
        try {
            Path target = resolve(file, true);
            BasicFileAttributes attributes = attributes(target);
            long existing = 0;
            byte[] data = content;
            if (attributes == null) {
                checkNewName(target.getParent(), file.getName(), file.getDepth() == 1);
            } else {
                if (attributes.isDirectory()) {
                    throw new VirtualFileException(file + " is a folder, not a file");
                }
                existing = attributes.size();
                if (append) {
                    if (existing > limits.maxFileBytes()) {
                        throw new VirtualFileException("The file " + file + " is larger than the limit of "
                                + limits.maxFileBytes() + " bytes");
                    }
                    byte[] old = Files.readAllBytes(target);
                    data = new byte[old.length + content.length];
                    System.arraycopy(old, 0, data, 0, old.length);
                    System.arraycopy(content, 0, data, old.length, content.length);
                }
            }
            if (data.length > limits.maxFileBytes()) {
                throw new VirtualFileException("The file would be " + data.length + " bytes, the limit of a file is "
                        + limits.maxFileBytes() + " bytes");
            }
            WorkspaceUsage usage = usage();
            if (attributes == null && usage.files() >= limits.maxFiles()) {
                throw new VirtualFileException("The workspace has the most files it can have (" + limits.maxFiles()
                        + "); delete files you don't need");
            }
            if (usage.bytes() - existing + data.length > limits.maxTotalBytes() && data.length > existing) {
                throw new VirtualFileException("The workspace would be larger than its limit of " + limits.maxTotalBytes()
                        + " bytes; delete files you don't need");
            }
            writeAtomically(target, data);
        } catch (IOException e) {
            throw new VirtualFileException("The file " + file + " can't be written", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void createFolder(VirtualPath folder) {
        if (folder.isRoot()) {
            throw new VirtualFileException("/ is the folder of the workspace, it exists");
        }
        lock.lock();
        try {
            Path target = resolve(folder, true);
            if (attributes(target) != null) {
                throw new VirtualFileException(folder + " exists already");
            }
            checkNewName(target.getParent(), folder.getName(), folder.getDepth() == 1);
            Files.createDirectory(target);
        } catch (IOException e) {
            throw new VirtualFileException("The folder " + folder + " can't be created", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(VirtualPath path, boolean recursive) {
        if (path.isRoot()) {
            throw new VirtualFileException("The workspace itself can't be deleted");
        }
        lock.lock();
        try {
            Path real = resolve(path, false);
            BasicFileAttributes attributes = attributes(real);
            if (attributes == null) {
                throw new VirtualFileException("There is nothing at " + path);
            }
            if (attributes.isDirectory() && !isEmpty(real) && !recursive) {
                throw new VirtualFileException("The folder " + path + " is not empty; delete with recursive true to remove it "
                        + "with everything in it");
            }
            deleteTree(real);
        } catch (IOException e) {
            throw new VirtualFileException(path + " can't be deleted", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void move(VirtualPath from, VirtualPath to) {
        if (from.isRoot() || to.isRoot()) {
            throw new VirtualFileException("The workspace itself can't be moved");
        }
        if (to.startsWith(from)) {
            throw new VirtualFileException(from.equals(to) ? "The source and the target are the same"
                    : "A folder can't be moved into itself");
        }
        lock.lock();
        try {
            Path source = resolve(from, false);
            if (attributes(source) == null) {
                throw new VirtualFileException("There is nothing at " + from);
            }
            Path target = resolve(to, true);
            if (attributes(target) != null) {
                throw new VirtualFileException(to + " exists already; delete it first or choose another name");
            }
            checkNewName(target.getParent(), to.getName(), to.getDepth() == 1);
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(source, target);
            }
        } catch (IOException e) {
            throw new VirtualFileException(from + " can't be moved", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public WorkspaceUsage copy(VirtualPath from, VirtualPath to) {
        if (from.isRoot() || to.isRoot()) {
            throw new VirtualFileException("The workspace itself can't be copied");
        }
        if (to.startsWith(from)) {
            throw new VirtualFileException(from.equals(to) ? "The source and the target are the same"
                    : "A folder can't be copied into itself");
        }
        lock.lock();
        try {
            Path source = resolve(from, false);
            BasicFileAttributes attributes = attributes(source);
            if (attributes == null) {
                throw new VirtualFileException("There is nothing at " + from);
            }
            if (attributes(resolve(to, false)) != null) {
                throw new VirtualFileException(to + " exists already; delete it first or choose another name");
            }
            // everything is checked before a folder is created or a byte is copied
            WorkspaceUsage size = measure(source, to);
            WorkspaceUsage usage = usage();
            if (usage.files() + size.files() > limits.maxFiles()) {
                throw new VirtualFileException("The copy would make " + (usage.files() + size.files()) + " files, the workspace "
                        + "has room for " + limits.maxFiles() + "; delete files you don't need");
            }
            if (usage.bytes() + size.bytes() > limits.maxTotalBytes()) {
                throw new VirtualFileException("The workspace would be larger than its limit of " + limits.maxTotalBytes()
                        + " bytes; delete files you don't need");
            }
            Path target = resolve(to, true);
            checkNewName(target.getParent(), to.getName(), to.getDepth() == 1);
            Path temp = attributes.isDirectory() ? Files.createTempDirectory(target.getParent(), TEMP_PREFIX)
                    : Files.createTempFile(target.getParent(), TEMP_PREFIX, null);
            try {
                copyTree(source, temp);
                try {
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, target);
                }
            } finally {
                if (Files.exists(temp, LinkOption.NOFOLLOW_LINKS)) {
                    deleteTree(temp);
                }
            }
            return size;
        } catch (IOException e) {
            throw new VirtualFileException(from + " can't be copied", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * The files and bytes of a file or a folder; refuses a copy to {@code to} that would be too deep or have too long
     * a path, since the paths below the target are made by the copy and not by {@link VirtualPath}.
     */
    private static WorkspaceUsage measure(Path source, VirtualPath to) throws IOException {
        int[] files = {0};
        long[] bytes = {0};
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                check(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && !file.getFileName().toString().startsWith(TEMP_PREFIX)) {
                    check(file);
                    files[0]++;
                    bytes[0] += attrs.size();
                }
                return FileVisitResult.CONTINUE;
            }

            private void check(Path path) {
                Path relative = source.relativize(path);
                int depth = to.getDepth() + relative.getNameCount() - (relative.toString().isEmpty() ? 1 : 0);
                int length = to.toString().length() + relative.toString().length() + 1;
                if (depth > VirtualPath.MAX_DEPTH) {
                    throw new VirtualFileException("The copy would be deeper than " + VirtualPath.MAX_DEPTH + " folders");
                }
                if (length > VirtualPath.MAX_PATH_LENGTH) {
                    throw new VirtualFileException("The copy would have paths longer than " + VirtualPath.MAX_PATH_LENGTH
                            + " characters");
                }
            }
        });
        return new WorkspaceUsage(files[0], bytes[0]);
    }

    /**
     * Copies a file over {@code target} (an empty temporary file), or the files and folders of a folder into
     * {@code target} (an empty temporary folder); links and anything else are left out.
     */
    private static void copyTree(Path source, Path target) throws IOException {
        if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path copy = target.resolve(source.relativize(dir).toString());
                if (!Files.isDirectory(copy, LinkOption.NOFOLLOW_LINKS)) {
                    Files.createDirectory(copy);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile() && !file.getFileName().toString().startsWith(TEMP_PREFIX)) {
                    Files.copy(file, target.resolve(source.relativize(file).toString()));
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public VirtualPath toVirtual(Path real) {
        Path path = real.toAbsolutePath().normalize();
        Path base = path.startsWith(realRoot) ? realRoot : path.startsWith(root) ? root : null;
        if (base == null) {
            return null;
        }
        VirtualPath result = VirtualPath.ROOT;
        try {
            for (Path name : base.relativize(path)) {
                if (!name.toString().isEmpty()) {
                    result = result.resolve(Normalizer.normalize(name.toString(), Normalizer.Form.NFC));
                }
            }
        } catch (VirtualFileException e) {
            return null;
        }
        return result;
    }

    /**
     * What the workspace holds now.
     */
    WorkspaceUsage usage() {
        int[] files = {0};
        long[] bytes = {0};
        try {
            Files.walkFileTree(realRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        files[0]++;
                        bytes[0] += attrs.size();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new VirtualFileException("The workspace can't be measured", e);
        }
        return new WorkspaceUsage(files[0], bytes[0]);
    }

    /**
     * Deletes everything in the workspace, the folder stays.
     */
    void clear() {
        lock.lock();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(realRoot)) {
            for (Path child : stream) {
                deleteTree(child);
            }
        } catch (IOException e) {
            throw new VirtualFileException("The workspace can't be cleared", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Copies the files and folders of another workspace into this one (links and anything else are left out).
     */
    void copyFrom(WorkspaceMount source) {
        lock.lock();
        try {
            Files.walkFileTree(source.realRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(realRoot.resolve(source.realRoot.relativize(dir).toString()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile() && !file.getFileName().toString().startsWith(TEMP_PREFIX)) {
                        Files.copy(file, realRoot.resolve(source.realRoot.relativize(file).toString()),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new VirtualFileException("The workspace can't be copied", e);
        } finally {
            lock.unlock();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the cage
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * The real path of a virtual one. Every name on the way that exists is checked (not a link, a file or a folder, in
     * the spelling asked for); the names that don't exist yet are only appended, or created when the path is for a write.
     */
    private Path resolve(VirtualPath path, boolean createFolders) {
        Path current = realRoot;
        List<String> names = path.getSegments();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            boolean last = i == names.size() - 1;
            Path next = current.resolve(name);
            BasicFileAttributes attributes = attributes(next);
            if (attributes == null) {
                if (last || !createFolders) {
                    for (int j = i + 1; j < names.size(); j++) {
                        next = next.resolve(names.get(j));
                    }
                    return checkInside(next);
                }
                checkNewName(current, name, i == 0);
                try {
                    Files.createDirectory(next);
                } catch (IOException e) {
                    throw new VirtualFileException("The folder " + prefix(names, i) + " can't be created", e);
                }
            } else {
                if (attributes.isSymbolicLink() || !(attributes.isDirectory() || attributes.isRegularFile())) {
                    throw new VirtualFileException(prefix(names, i) + " is not a file or a folder");
                }
                checkSpelling(next, name);
                if (!last && !attributes.isDirectory()) {
                    throw new VirtualFileException(prefix(names, i) + " is a file, not a folder");
                }
            }
            current = next;
        }
        return checkInside(current);
    }

    private Path checkInside(Path path) {
        if (!path.startsWith(realRoot)) {
            // VirtualPath makes this impossible; kept as the last line of the defence
            throw new VirtualFileException("The path is not in the workspace");
        }
        return path;
    }

    private static VirtualPath prefix(List<String> names, int last) {
        VirtualPath path = VirtualPath.ROOT;
        for (int i = 0; i <= last; i++) {
            path = path.resolve(names.get(i));
        }
        return path;
    }

    private static BasicFileAttributes attributes(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new VirtualFileException("The file system could not be read", e);
        }
    }

    /**
     * An existing name must be the one on the disk; a file system that ignores the case finds "Notes.md" for "notes.md".
     */
    private static void checkSpelling(Path path, String name) {
        try {
            String actual = Normalizer.normalize(path.toRealPath(LinkOption.NOFOLLOW_LINKS).getFileName().toString(),
                    Normalizer.Form.NFC);
            if (!actual.equals(name)) {
                throw new VirtualFileException("There is already \"" + actual + "\" here; names that differ only in case "
                        + "are the same name");
            }
        } catch (IOException e) {
            throw new VirtualFileException("The file system could not be read", e);
        }
    }

    /**
     * A name that is to be created: not reserved and with no other spelling of it in the folder.
     */
    private void checkNewName(Path folder, String name, boolean atRoot) {
        if (name.startsWith(TEMP_PREFIX)) {
            throw new VirtualFileException("Names starting with " + TEMP_PREFIX + " are reserved");
        }
        if (atRoot && reservedAtRoot.contains(name.toLowerCase(Locale.ROOT))) {
            throw new VirtualFileException("/" + name + " is not part of the workspace, choose another name");
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path child : stream) {
                String other = Normalizer.normalize(child.getFileName().toString(), Normalizer.Form.NFC);
                if (!other.equals(name) && other.equalsIgnoreCase(name)) {
                    throw new VirtualFileException("There is already \"" + other + "\" here; names that differ only in "
                            + "case are the same name");
                }
            }
        } catch (IOException e) {
            throw new VirtualFileException("The file system could not be read", e);
        }
    }

    private static VirtualEntry entry(VirtualPath path, BasicFileAttributes attributes) {
        return new VirtualEntry(path, attributes.isDirectory(), attributes.isDirectory() ? 0 : attributes.size(),
                attributes.lastModifiedTime().toInstant());
    }

    private static boolean isEmpty(Path folder) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            return !stream.iterator().hasNext();
        }
    }

    private static void deleteTree(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e != null) {
                    throw e;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void writeAtomically(Path target, byte[] data) throws IOException {
        Path temp = Files.createTempFile(target.getParent(), TEMP_PREFIX, null);
        try {
            Files.write(temp, data);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

}
