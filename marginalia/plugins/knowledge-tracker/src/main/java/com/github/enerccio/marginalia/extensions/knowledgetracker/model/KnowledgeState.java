package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Everything the plugin keeps for a book: the settings, the tracked characters and where the processing stands. Stored
 * in the book's attributes under {@link #KEY}, so it travels with backups.
 */
public class KnowledgeState {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.knowledgetracker";

    private BookSettings settings = new BookSettings();
    private List<TrackedCharacter> characters = new ArrayList<>();
    /** Message (uuid) processing begins at; {@code null} = the start of the book. */
    private String startMessage;
    /** The last message (uuid) that was processed; {@code null} = none yet. */
    private String cursor;
    /** The parts that were processed, in the order they were, with the hash of their text; information for the user. */
    private List<ProcessedPart> processed = new ArrayList<>();
    /** What automatic runs applied without asking, oldest first; see {@code ChangeLog}. */
    private List<ChangeLogEntry> changeLog = new ArrayList<>();

    public TrackedCharacter findCharacter(String id) {
        return getCharacters().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * The tracked character called {@code name} (its name or an alias), or {@code null}.
     */
    public TrackedCharacter findByName(String name) {
        return getCharacters().stream().filter(c -> c.isCalled(name)).findFirst().orElse(null);
    }

    /**
     * Another tracked character that is already called by {@code name} or one of {@code aliases}, or {@code null}.
     *
     * @param self the character being edited, which does not clash with itself; {@code null} for a new one
     */
    public TrackedCharacter findClash(TrackedCharacter self, String name, List<String> aliases) {
        for (TrackedCharacter other : getCharacters()) {
            if (other != self && (other.isCalled(name) || aliases.stream().anyMatch(other::isCalled))) {
                return other;
            }
        }
        return null;
    }

    public boolean deleteCharacter(String id) {
        return getCharacters().removeIf(c -> c.getId().equals(id));
    }

    /**
     * Takes back the state of the messages that {@code dropped} accepts, for every character, see
     * {@link TrackedCharacter#rewind}. The cursor is moved by the caller, which knows the order of the messages.
     *
     * @return how many things changed
     */
    public int rewind(Predicate<String> dropped) {
        return getCharacters().stream().mapToInt(c -> c.rewind(dropped)).sum();
    }

    public BookSettings getSettings() {
        if (settings == null) {
            settings = new BookSettings();
        }
        return settings;
    }

    public List<TrackedCharacter> getCharacters() {
        if (characters == null) {
            characters = new ArrayList<>();
        }
        return characters;
    }

    public List<ProcessedPart> getProcessed() {
        if (processed == null) {
            processed = new ArrayList<>();
        }
        return processed;
    }

    public List<ChangeLogEntry> getChangeLog() {
        if (changeLog == null) {
            changeLog = new ArrayList<>();
        }
        return changeLog;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
