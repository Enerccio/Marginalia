package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.UpdateService;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.utils.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Properties;

public class UpdateServiceImpl implements UpdateService {

    private static final Logger log = LoggerFactory.getLogger(UpdateServiceImpl.class);

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private Configuration configuration;

    @Autowired
    private User currentUser;

    @Override
    @NoTx
    public UpdateStatus getStatus() throws Exception {
        adminGuard.requireAdmin();
        return readStatus(System.currentTimeMillis());
    }

    @Override
    @NoTx
    public void requestUpdate() throws Exception {
        adminGuard.requireAdmin();
        UpdateStatus status = readStatus(System.currentTimeMillis());
        if (!status.canRequest()) {
            throw new IllegalStateException(status.watching() ? "An update runs or is requested already"
                    : "The update watcher does not run on the server");
        }
        Files.createDirectories(folder());
        // the watcher prints the content to its log, nothing reads it
        String content = currentUser.getLogin() + " " + System.currentTimeMillis() / 1000 + "\n";
        try {
            Files.writeString(file(REQUEST_FILE), content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (java.nio.file.FileAlreadyExistsException e) {
            throw new IllegalStateException("An update runs or is requested already");
        }
        log.info("Update of the installation requested by {}", currentUser.getLogin());
    }

    @Override
    @NoTx
    public String readLog(int maxBytes) throws Exception {
        adminGuard.requireAdmin();
        Path logFile = file(LOG_FILE);
        if (!Files.isRegularFile(logFile, LinkOption.NOFOLLOW_LINKS)) {
            return "";
        }
        try (RandomAccessFile in = new RandomAccessFile(logFile.toFile(), "r")) {
            long length = in.length();
            long start = Math.max(0, length - Math.max(0, maxBytes));
            in.seek(start);
            byte[] bytes = new byte[(int) (length - start)];
            in.readFully(bytes);
            String text = new String(bytes, StandardCharsets.UTF_8);
            // cut in the middle of a line: start at the next one
            int newLine = start > 0 ? text.indexOf('\n') : -1;
            return newLine >= 0 ? text.substring(newLine + 1) : text;
        }
    }

    private UpdateStatus readStatus(long now) {
        BuildInfo build = BuildInfo.current();
        boolean requested = Files.exists(file(REQUEST_FILE), LinkOption.NOFOLLOW_LINKS);
        Properties properties = new Properties();
        Path statusFile = file(STATUS_FILE);
        if (Files.isRegularFile(statusFile, LinkOption.NOFOLLOW_LINKS)) {
            try (Reader reader = Files.newBufferedReader(statusFile, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                log.warn("Cannot read {}", statusFile, e);
            }
        }
        Date checked = date(properties, "checked");
        boolean running = "running".equals(value(properties, "state"));
        boolean watching = checked != null
                && now - checked.getTime() < (running ? RUNNING_TIMEOUT_MS : WATCHER_TIMEOUT_MS);
        return new UpdateStatus(watching, watching && running, requested, build.getShortText(), build.getCommitFull(),
                value(properties, "branch"), value(properties, "upstream"), value(properties, "remote"),
                "1".equals(value(properties, "fetchFailed")), checked, date(properties, "lastRun"),
                "ok".equals(value(properties, "lastResult")));
    }

    private Path folder() {
        return configuration.getFolder().toPath().resolve(FOLDER);
    }

    private Path file(String name) {
        return folder().resolve(name);
    }

    private static String value(Properties properties, String key) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * A time the watcher wrote, in seconds since 1970.
     */
    private static Date date(Properties properties, String key) {
        String value = value(properties, key);
        if (value == null) {
            return null;
        }
        try {
            return new Date(Long.parseLong(value) * 1000);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
