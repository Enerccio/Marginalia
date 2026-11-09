package com.github.enerccio.marginalia.workset;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * One line per call of the author in {@code usage.jsonl}: what was asked, what it cost, whether the cache answered. The table printed after a run is
 * made from it.
 */
public class Ledger {

    /** @param run the number of the run (1, 2...), 0 when unknown */
    public record Line(String time, int run, String part, String kind, String author, boolean cached, long requestChars, long answerChars,
                       long inputTokens, long outputTokens, long cachedTokens, double costUsd, long millis) {
    }

    private static final Gson GSON = new Gson();

    private final Path file;

    public Ledger(Path file) throws IOException {
        this.file = file;
        Files.createDirectories(file.getParent());
    }

    public synchronized void add(Line line) throws IOException {
        Files.writeString(file, GSON.toJson(line) + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<Line> read() throws IOException {
        List<Line> lines = new ArrayList<>();
        if (Files.exists(file)) {
            for (String l : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!l.isBlank()) {
                    lines.add(GSON.fromJson(l, Line.class));
                }
            }
        }
        return lines;
    }

    /** The number of the next run: one more than the highest in the ledger. */
    public int nextRun() throws IOException {
        return read().stream().mapToInt(Line::run).max().orElse(0) + 1;
    }
}
