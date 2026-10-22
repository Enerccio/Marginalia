package com.github.enerccio.marginalia.domain.service.agent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Unpacks a zip file into a folder that is empty and that nothing else writes to ({@link WorkspaceMount#unzip} makes
 * it and moves it into place when this is done). The archive is not trusted in any way:
 * <ul>
 * <li>an entry that names a place above the folder ({@code ..}) or starts with {@code /} is a zip slip attack and the
 * whole archive is refused; every other name goes through the rules of {@link VirtualPath} (an entry whose name is not
 * allowed is left out), and the path is built from the checked names only, so nothing but the folder can be written;
 * <li>nothing but files and folders is made: a link in the archive is an ordinary entry, whose bytes (the target as
 * text) are written to an ordinary file, and is judged like any file;
 * <li>the sizes in the archive are only read to leave out what is too large; what is written is what was inflated, at
 * most the size the entry declares (an entry that inflates to more is refused), at most the largest file the workspace
 * takes;
 * <li>zip bombs: the number of entries, all the bytes inflated (more than the workspace could take is refused, left out
 * entries count too), and the ratio of inflated to packed bytes of an entry and of the whole archive are limited. An
 * archive inside the archive is only stored as a file, never unpacked here, so there is no recursion;
 * <li>the files and bytes have to fit what the workspace has room for, or nothing is written;
 * <li>the same name twice, two names that differ only in case, and a name that is both a file and a folder are refused;
 * <li>the files kept are text (UTF-8 with no NUL character), images the workspace takes and zip files, as anywhere in the
 * workspace, the others are left out and reported.
 * </ul>
 */
final class ZipExtractor {

    /**
     * Most entries (files and folders) an archive may have.
     */
    static final int MAX_ENTRIES = 5_000;

    /**
     * How much larger than its packed size an entry, or the archive, may inflate. Text packs to a fifth or a tenth, the
     * bombs to a thousandth or less.
     */
    static final int MAX_RATIO = 100;

    /**
     * Below this many inflated bytes the ratio is not looked at, a small file of one repeated letter is not an attack.
     */
    static final long RATIO_FLOOR = 256 * 1024;

    /**
     * How many entries that were left out are named in the result.
     */
    static final int MAX_REPORTED = 20;

    private final WorkspaceLimits limits;
    private final Path staging;
    private final VirtualPath base;
    private final long archiveBytes;
    private final int roomFiles;
    private final long roomBytes;

    // lower case names of what the archive holds so far, to refuse a name twice and a file that is a folder
    private final Set<String> fileKeys = new HashSet<>();
    private final Set<String> folderKeys = new HashSet<>();
    private final Set<String> madeFolders = new HashSet<>();

    private int files;
    private long bytes;
    private long inflated;
    private int skipped;
    private final List<String> skippedNames = new ArrayList<>();

    private ZipExtractor(WorkspaceLimits limits, Path staging, VirtualPath base, long archiveBytes, int roomFiles, long roomBytes) {
        this.limits = limits;
        this.staging = staging;
        this.base = base;
        this.archiveBytes = archiveBytes;
        this.roomFiles = roomFiles;
        this.roomBytes = roomBytes;
    }

    /**
     * @param zip          the open archive
     * @param archiveBytes size of the archive file
     * @param staging      an existing empty folder to unpack into
     * @param base         the virtual path the folder will have, for the checks of the names
     * @param limits       the limits of the workspace
     * @param roomFiles    how many files the workspace has room for
     * @param roomBytes    how many bytes the workspace has room for
     * @throws VirtualFileException when the archive is refused; what was written to {@code staging} is then to be thrown away
     */
    static UnzipResult extract(ZipFile zip, long archiveBytes, Path staging, VirtualPath base, WorkspaceLimits limits,
                               int roomFiles, long roomBytes) throws IOException {
        return new ZipExtractor(limits, staging, base, archiveBytes, roomFiles, roomBytes).run(zip);
    }

    private UnzipResult run(ZipFile zip) throws IOException {
        if (zip.size() > MAX_ENTRIES) {
            throw new VirtualFileException("The archive has " + zip.size() + " entries, at most " + MAX_ENTRIES
                    + " are unpacked; it looks like an attack or it is too large for the workspace");
        }
        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = entries.nextElement();
            List<String> names = names(entry.getName());
            if (names.isEmpty()) {
                continue;
            }
            VirtualPath path;
            try {
                path = pathOf(names);
            } catch (VirtualFileException e) {
                skip(entry.getName(), "the name is not allowed here");
                continue;
            }
            if (entry.isDirectory()) {
                addFolder(names, true);
                continue;
            }
            addFile(zip, entry, names, path);
        }
        return new UnzipResult(files, madeFolders.size(), bytes, skipped, List.copyOf(skippedNames));
    }

    /**
     * The parts of the name of an entry: {@code .} and empty parts are dropped ({@code ./a//b}).
     *
     * @throws VirtualFileException for a name that goes above the folder or is absolute
     */
    private static List<String> names(String name) {
        if (name.startsWith("/")) {
            throw new VirtualFileException("The archive is refused: it has the entry \"" + VirtualPath.printable(name)
                    + "\" with an absolute path (an attack on the files around the folder)");
        }
        List<String> names = new ArrayList<>();
        for (String part : name.split("/", -1)) {
            if (part.equals("..")) {
                throw new VirtualFileException("The archive is refused: it has the entry \"" + VirtualPath.printable(name)
                        + "\" that leads above the folder (an attack on the files around it)");
            }
            if (!part.isEmpty() && !part.equals(".")) {
                names.add(Normalizer.normalize(part, Normalizer.Form.NFC));
            }
        }
        return names;
    }

    private VirtualPath pathOf(List<String> names) {
        VirtualPath path = base;
        for (String name : names) {
            if (name.startsWith(WorkspaceMount.TEMP_PREFIX)) {
                throw new VirtualFileException("The name is reserved");
            }
            path = path.resolve(name);
        }
        if (path.toString().length() > VirtualPath.MAX_PATH_LENGTH) {
            throw new VirtualFileException("The path is too long");
        }
        return path;
    }

    private static String key(List<String> names) {
        return String.join("/", names).toLowerCase(Locale.ROOT);
    }

    /**
     * Notes the folder and the ones above it; a name that is a file already is refused.
     */
    private void addFolder(List<String> names, boolean make) throws IOException {
        for (int i = 1; i <= names.size(); i++) {
            List<String> part = names.subList(0, i);
            String key = key(part);
            if (fileKeys.contains(key)) {
                throw new VirtualFileException("The archive is refused: \"" + VirtualPath.printable(String.join("/", part))
                        + "\" is both a file and a folder in it");
            }
            folderKeys.add(key);
        }
        if (make) {
            Path folder = resolve(names);
            Files.createDirectories(folder);
            for (int i = 1; i <= names.size(); i++) {
                madeFolders.add(key(names.subList(0, i)));
            }
        }
    }

    private Path resolve(List<String> names) {
        Path path = staging;
        for (String name : names) {
            path = path.resolve(name);
        }
        return path;
    }

    private void addFile(ZipFile zip, ZipEntry entry, List<String> names, VirtualPath path) throws IOException {
        String key = key(names);
        if (!fileKeys.add(key) || folderKeys.contains(key)) {
            throw new VirtualFileException("The archive is refused: the name \"" + VirtualPath.printable(String.join("/", names))
                    + "\" is in it twice (names that differ only in case are the same name) or is both a file and a folder");
        }
        if (names.size() > 1) {
            addFolder(names.subList(0, names.size() - 1), false);
        }
        long declared = entry.getSize();
        long most = limits.maxAnyFileBytes();
        if (declared > most) {
            skip(entry.getName(), "larger than any file of the workspace can be");
            return;
        }
        byte[] content = inflate(zip, entry, declared >= 0 ? declared : most);
        if (content == null) {
            skip(entry.getName(), "larger than any file of the workspace can be");
            return;
        }
        String reason = refusal(content, path);
        if (reason != null) {
            skip(entry.getName(), reason);
            return;
        }
        if (files + 1 > roomFiles) {
            throw new VirtualFileException("The archive has more files than the workspace has room for (" + roomFiles
                    + " more); delete files you don't need");
        }
        if (bytes + content.length > roomBytes) {
            throw new VirtualFileException("The archive unpacks to more than the workspace has room for (" + roomBytes
                    + " bytes more); delete files you don't need");
        }
        Path target = resolve(names);
        if (names.size() > 1) {
            Files.createDirectories(target.getParent());
            for (int i = 1; i < names.size(); i++) {
                madeFolders.add(key(names.subList(0, i)));
            }
        }
        Files.write(target, content, StandardOpenOption.CREATE_NEW);
        files++;
        bytes += content.length;
    }

    /**
     * The bytes of an entry, at most {@code limit} of them.
     *
     * @return null when the entry is larger than {@code limit} and the limit is not what the archive declares
     * @throws VirtualFileException when it inflates to more than it declares, to more than the archive may, or too much for its packed size
     */
    private byte[] inflate(ZipFile zip, ZipEntry entry, long limit) throws IOException {
        byte[] content;
        try (InputStream in = zip.getInputStream(entry)) {
            content = in.readNBytes((int) Math.min(limit + 1, Integer.MAX_VALUE - 8));
        } catch (ZipException e) {
            throw new VirtualFileException("The archive can't be unpacked: " + (e.getMessage() != null && e.getMessage().contains("encrypted")
                    ? "it is encrypted (a password is not supported)" : "it is damaged or uses something that is not supported"), e);
        } catch (IOException e) {
            throw new VirtualFileException("The archive can't be unpacked: it is damaged", e);
        }
        inflated += content.length;
        if (inflated > limits.maxTotalBytes()) {
            throw new VirtualFileException("The archive is refused: it unpacks to more than the whole workspace can hold ("
                    + limits.maxTotalBytes() + " bytes); it may be a zip bomb");
        }
        if (entry.getSize() >= 0 && content.length > entry.getSize()) {
            throw new VirtualFileException("The archive is refused: the entry \"" + VirtualPath.printable(entry.getName())
                    + "\" is larger than the size it declares; it may be a zip bomb");
        }
        long packed = Math.max(1, entry.getCompressedSize());
        if (content.length > RATIO_FLOOR && content.length / packed > MAX_RATIO) {
            throw new VirtualFileException("The archive is refused: the entry \"" + VirtualPath.printable(entry.getName())
                    + "\" packs to far less than a text does (more than " + MAX_RATIO + " times); it may be a zip bomb");
        }
        if (inflated > RATIO_FLOOR && inflated / Math.max(1, archiveBytes) > MAX_RATIO) {
            throw new VirtualFileException("The archive is refused: it unpacks to more than " + MAX_RATIO
                    + " times its own size; it may be a zip bomb");
        }
        return content.length > limit ? null : content;
    }

    /**
     * Why the bytes can't be kept in the workspace, null when they can.
     */
    private String refusal(byte[] content, VirtualPath path) {
        if (ImageFiles.isImage(content)) {
            if (content.length > limits.maxImageBytes()) {
                return "an image larger than " + limits.maxImageBytes() + " bytes";
            }
            try {
                ImageFiles.check(content, path);
            } catch (VirtualFileException e) {
                return "an image that can't be kept (too many pixels or not readable)";
            }
            return null;
        }
        if (ArchiveFiles.isZip(content)) {
            return content.length > limits.maxArchiveBytes() ? "a zip file larger than " + limits.maxArchiveBytes() + " bytes" : null;
        }
        if (content.length > limits.maxFileBytes()) {
            return "a file larger than " + limits.maxFileBytes() + " bytes";
        }
        try {
            VirtualFileSystem.decodeText(content, path);
        } catch (VirtualFileException e) {
            return "not a text file (UTF-8), an image or a zip file";
        }
        return null;
    }

    private void skip(String name, String reason) {
        skipped++;
        if (skippedNames.size() < MAX_REPORTED) {
            skippedNames.add(VirtualPath.printable(name) + " (" + reason + ")");
        }
    }

}
