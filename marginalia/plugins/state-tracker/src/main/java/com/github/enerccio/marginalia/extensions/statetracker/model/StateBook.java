package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.process.ProcessedPart;
import com.github.enerccio.marginalia.extensions.storytracker.process.TrackerProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Everything the plugin keeps for a book: the settings, the categories the book uses (copies of the user's, or its own) and
 * the tracked characters. Stored in the book's attributes under {@link #KEY}, so it travels with backups and exports.
 */
public class StateBook implements TrackerProgress {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.statetracker";

    private BookSettings settings = new BookSettings();
    private List<Category> categories = new ArrayList<>();
    private List<TrackedCharacter> characters = new ArrayList<>();
    /** Part (uuid) processing begins at; {@code null} = the start of the book. */
    private String startMessage;
    /** The last part (uuid) that was processed; {@code null} = none yet. */
    private String cursor;
    /** The parts that were processed, with the hash of their text; information for the user. */
    private List<ProcessedPart> processed = new ArrayList<>();
    /** How many parts the model read since the last tidy (or since the start); the automatic tidy is due when it reaches the setting. */
    private int partsSinceTidy;
    /** The time of the story, when the book follows it. */
    private StoryClock clock = new StoryClock();
    /** What runs applied without asking, oldest first; see {@code StateLog}. */
    private List<StateLogEntry> changeLog = new ArrayList<>();

    public Category findCategory(String id) {
        return getCategories().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    /** The book's copy of the user category {@code userCategoryId}, or {@code null}. */
    public Category copyOf(String userCategoryId) {
        return getCategories().stream().filter(c -> Objects.equals(c.getSourceId(), userCategoryId)).findFirst().orElse(null);
    }

    /** The category of the book called {@code name} (the case ignored), or {@code null}. */
    public Category findCategoryByName(String name) {
        String wanted = name == null ? "" : name.strip();
        return getCategories().stream().filter(c -> c.getName().strip().equalsIgnoreCase(wanted)).findFirst().orElse(null);
    }

    /**
     * The book's category for a user category: its copy, made when the book has none yet.
     */
    public Category adopt(Category userCategory) {
        Category copy = copyOf(userCategory.getId());
        if (copy == null) {
            copy = userCategory.copyForBook();
            getCategories().add(copy);
        }
        return copy;
    }

    /** The categories tracked for the character, in its order; ids the book does not have (any more) are skipped. */
    public List<Category> categoriesOf(TrackedCharacter character) {
        List<Category> result = new ArrayList<>();
        for (String id : character.getCategories()) {
            Category category = findCategory(id);
            if (category != null) {
                result.add(category);
            }
        }
        return result;
    }

    /** Whether any character has a value (current or past) of the category. */
    public boolean isUsed(String categoryId) {
        return getCharacters().stream().anyMatch(c -> c.hasItemsOf(categoryId));
    }

    /** How many characters track the category. */
    public int trackedBy(String categoryId) {
        return (int) getCharacters().stream().filter(c -> c.getCategories().contains(categoryId)).count();
    }

    /** Deletes the category from the book, and with it every value of it. */
    public boolean deleteCategory(String categoryId) {
        getCharacters().forEach(c -> c.forget(categoryId));
        return getCategories().removeIf(c -> c.getId().equals(categoryId));
    }

    public TrackedCharacter findCharacter(String id) {
        return getCharacters().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    /** The tracked character called {@code name} (its name or an alias), or {@code null}. */
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

    @Override
    public int rewind(Predicate<String> dropped) {
        return getCharacters().stream().mapToInt(c -> c.rewind(dropped)).sum() + getClock().rewind(dropped);
    }

    public StoryClock getClock() {
        if (clock == null) {
            clock = new StoryClock();
        }
        return clock;
    }

    /** The story day now, {@code null} when the book does not follow the time; what a value set now remembers. */
    public Integer today() {
        return getClock().isEnabled() ? getClock().now(this).day() : null;
    }

    @Override
    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    @Override
    public String getCursor() {
        return cursor;
    }

    @Override
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    @Override
    public List<ProcessedPart> getProcessed() {
        if (processed == null) {
            processed = new ArrayList<>();
        }
        return processed;
    }

    public int getPartsSinceTidy() {
        return partsSinceTidy;
    }

    /** The model read {@code parts} more parts. */
    public void partsRead(int parts) {
        partsSinceTidy += Math.max(0, parts);
    }

    /** A tidy was done or tried (even when it found nothing): the count starts again. */
    public void tidied() {
        partsSinceTidy = 0;
    }

    public List<StateLogEntry> getChangeLog() {
        if (changeLog == null) {
            changeLog = new ArrayList<>();
        }
        return changeLog;
    }

    /**
     * This state, read from a checkpoint, takes the place of {@code current}: what the author set up for the book (the settings, the
     * start point, the categories and the settings of the clock) stays as it is now, what the tracker followed (the characters with their
     * values, the cursor, the processed parts, the marks of the clock, the count for the tidy, the log) is the checkpoint's. A category
     * the book no longer has but the values of the checkpoint use comes back, so that those values are shown.
     */
    public void keepSetupOf(StateBook current) {
        List<Category> setup = new ArrayList<>(current.getCategories());
        for (Category category : getCategories()) {
            if (current.findCategory(category.getId()) == null) {
                setup.add(category);
            }
        }
        StoryClock clock = current.getClock();
        clock.takeMarksOf(getClock());
        this.categories = setup;
        this.clock = clock;
        this.settings = current.getSettings();
        this.startMessage = current.getStartMessage();
    }

    public void setSettings(BookSettings settings) {
        this.settings = settings;
    }

    public BookSettings getSettings() {
        if (settings == null) {
            settings = new BookSettings();
        }
        return settings;
    }

    public List<Category> getCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public List<TrackedCharacter> getCharacters() {
        if (characters == null) {
            characters = new ArrayList<>();
        }
        return characters;
    }
}
