package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourceGarbageServiceImpl implements ResourceGarbageService {

    private static final Logger log = LoggerFactory.getLogger(ResourceGarbageServiceImpl.class);

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private Configuration configuration;

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    @NoTx
    public List<String> userFolders() throws Exception {
        adminGuard.requireAdmin();
        File[] folders = configuration.getDataFolder().listFiles(f -> Files.isDirectory(f.toPath(), LinkOption.NOFOLLOW_LINKS));
        List<String> names = new ArrayList<>();
        if (folders != null) {
            for (File folder : folders) {
                names.add(folder.getName());
            }
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    @Override
    @NoTx
    public List<StoredFile> listFiles(String userFolder) throws Exception {
        adminGuard.requireAdmin();
        List<StoredFile> files = new ArrayList<>();
        for (String kind : List.of(IMAGES, RESOURCES)) {
            Path folder = folderOf(userFolder, kind);
            if (!Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            try (Stream<Path> children = Files.list(folder)) {
                for (Path child : (Iterable<Path>) children::iterator) {
                    BasicFileAttributes attributes;
                    try {
                        attributes = Files.readAttributes(child, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    } catch (IOException e) {
                        // gone while listing
                        continue;
                    }
                    if (attributes.isRegularFile()) {
                        files.add(new StoredFile(userFolder, kind, child.getFileName().toString(), attributes.size(),
                                attributes.lastModifiedTime().toMillis()));
                    }
                }
            }
        }
        files.sort(Comparator.comparing(StoredFile::path, String.CASE_INSENSITIVE_ORDER));
        return files;
    }

    @Override
    @CommonTxReadOnly
    public List<StoredFile> findUnused(Collection<StoredFile> files) throws Exception {
        adminGuard.requireAdmin();
        Set<String> used = resourceRepository.findAllPaths();
        List<StoredFile> unused = new ArrayList<>();
        for (StoredFile file : files) {
            if (!used.contains(file.name())) {
                unused.add(file);
            }
        }
        return unused;
    }

    @Override
    @NoTx
    public File getFile(StoredFile file) throws Exception {
        adminGuard.requireAdmin();
        return resolve(file).toFile();
    }

    @Override
    @NoTx
    public void writeZip(Collection<StoredFile> files, OutputStream out) throws Exception {
        adminGuard.requireAdmin();
        ZipOutputStream zip = new ZipOutputStream(out);
        for (StoredFile file : files) {
            Path path = resolve(file);
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                continue;
            }
            ZipEntry entry = new ZipEntry(file.path());
            entry.setTime(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis());
            zip.putNextEntry(entry);
            try (InputStream in = Files.newInputStream(path)) {
                in.transferTo(zip);
            }
            zip.closeEntry();
        }
        zip.finish();
    }

    @Override
    @CommonTxReadOnly
    public DeleteResult delete(Collection<StoredFile> files) throws Exception {
        adminGuard.requireAdmin();
        // read again: a resource may have been uploaded or restored since the scan
        Set<String> used = resourceRepository.findAllPaths();
        int deleted = 0;
        int skipped = 0;
        long bytes = 0;
        for (StoredFile file : files) {
            Path path = resolve(file);
            if (used.contains(file.name()) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                skipped++;
                continue;
            }
            try {
                long size = Files.size(path);
                if (Files.deleteIfExists(path)) {
                    deleted++;
                    bytes += size;
                    continue;
                }
            } catch (IOException e) {
                log.warn("Failed to delete unused resource file {}", path, e);
            }
            skipped++;
        }
        log.info("Resource garbage collection deleted {} files ({} bytes), skipped {}", deleted, bytes, skipped);
        return new DeleteResult(deleted, skipped, bytes);
    }

    private Path folderOf(String userFolder, String kind) {
        if (!isPlainName(userFolder) || !(IMAGES.equals(kind) || RESOURCES.equals(kind))) {
            throw new IllegalArgumentException("Not a resource folder: " + userFolder + "/" + kind);
        }
        return configuration.getDataFolder().toPath().toAbsolutePath().normalize().resolve(userFolder).resolve(kind);
    }

    /**
     * The file is named by the client side (the table keeps what the scan returned), so it is checked to be in one of the
     * resource folders and not to leave them.
     */
    private Path resolve(StoredFile file) {
        if (file == null || !isPlainName(file.name())) {
            throw new IllegalArgumentException("Not a resource file");
        }
        return folderOf(file.userFolder(), file.kind()).resolve(file.name());
    }

    private static boolean isPlainName(String name) {
        return name != null && !name.isEmpty() && !name.equals(".") && !name.equals("..")
                && name.indexOf('/') < 0 && name.indexOf('\\') < 0 && name.indexOf('\0') < 0;
    }
}
