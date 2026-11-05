package com.github.enerccio.marginalia.workset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Every answer of the author, stored by the hash of the request it answered (the author id and the messages). A rerun reads them and calls no model,
 * so the same book can be built again offline; a request that changed (a prompt, the lorebook) is the only one that misses.
 */
public class AnswerCache {

    /** What is stored: the request is kept too, it is the only record of the prompt that was sent. */
    public record Entry(String author, List<Author.Message> messages, Author.Answer answer) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path dir;

    public AnswerCache(Path dir) throws IOException {
        this.dir = dir;
        Files.createDirectories(dir);
    }

    public static String key(String authorId, List<Author.Message> messages) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(authorId.getBytes(StandardCharsets.UTF_8));
            for (Author.Message m : messages) {
                digest.update((byte) 0);
                digest.update(m.role().getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(m.content().getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<Author.Answer> find(String key) throws IOException {
        Path file = dir.resolve(key + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        return Optional.of(GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Entry.class).answer());
    }

    public void store(String key, String authorId, List<Author.Message> messages, Author.Answer answer) throws IOException {
        Path tmp = dir.resolve(key + ".json.tmp");
        Files.writeString(tmp, GSON.toJson(new Entry(authorId, messages, answer)), StandardCharsets.UTF_8);
        Files.move(tmp, dir.resolve(key + ".json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }
}
