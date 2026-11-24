package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;

import java.util.*;
import java.util.function.Predicate;

/**
 * A character whose state the book tracks: its name and aliases (matched against the present characters field), the categories
 * tracked for it (ids of the book's categories, in the order the user gave) and its values, current and past.
 */
public class TrackedCharacter {

    private String id;
    private String name;
    private List<String> aliases = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<StateItem> items = new ArrayList<>();

    public TrackedCharacter() {
    }

    public TrackedCharacter(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    /** Whether the character is called {@code candidate}: its name or an alias, the case, accents and spaces ignored. */
    public boolean isCalled(String candidate) {
        String wanted = PresentCharacters.normalize(candidate);
        if (wanted.isEmpty()) {
            return false;
        }
        if (PresentCharacters.normalize(name).equals(wanted)) {
            return true;
        }
        return getAliases().stream().anyMatch(alias -> PresentCharacters.normalize(alias).equals(wanted));
    }

    /** The current values of a category, oldest first. */
    public List<StateItem> active(String categoryId) {
        return getItems().stream().filter(item -> item.isActive() && categoryId.equals(item.getCategoryId())).toList();
    }

    /** The past values of a category, the most recently ended first. */
    public List<StateItem> history(String categoryId) {
        return getItems().stream().filter(item -> !item.isActive() && categoryId.equals(item.getCategoryId()))
                .sorted(Comparator.comparingLong(StateItem::getEnded).reversed()).toList();
    }

    public StateItem findItem(String itemId) {
        return getItems().stream().filter(item -> item.getId().equals(itemId)).findFirst().orElse(null);
    }

    /**
     * Sets a value of the category: for a single or description category it replaces the current value (which goes to the
     * history), for a list it is one more entry. The category becomes tracked for the character if it was not.
     *
     * @return the new item
     */
    public StateItem set(Category category, String text, long now) {
        return set(category, text, now, null);
    }

    /**
     * As {@link #set(Category, String, long)}, read by the model in a part.
     *
     * @param part the part (uuid) the value was read in, {@code null} for a value the user sets
     */
    public StateItem set(Category category, String text, long now, String part) {
        return set(category, text, now, part, StateItem.EndReason.REPLACED);
    }

    /**
     * As {@link #set(Category, String, long, String)}; the value it replaces (single or description) ends for {@code reason}.
     */
    public StateItem set(Category category, String text, long now, String part, StateItem.EndReason reason) {
        track(category.getId());
        if (category.getKind().isSingleValued()) {
            for (StateItem current : active(category.getId())) {
                current.end(reason, now, part);
            }
        }
        StateItem item = new StateItem(category.getId(), text, now);
        item.setSourceMessage(part);
        getItems().add(item);
        return item;
    }

    /** Moves an item out of the current state into the history. */
    public void remove(StateItem item, long now) {
        remove(item, now, null);
    }

    /**
     * @param part the part (uuid) the removal was read in, {@code null} when the user removes it
     */
    public void remove(StateItem item, long now, String part) {
        end(item, StateItem.EndReason.REMOVED, now, part);
    }

    /** Moves an item out of the current state for {@code reason}; {@code part} as in {@link #remove(StateItem, long, String)}. */
    public void end(StateItem item, StateItem.EndReason reason, long now, String part) {
        if (item.isActive()) {
            item.end(reason, now, part);
        }
    }

    /**
     * Takes back what the model read in the parts that {@code dropped} accepts: the values it read there are deleted, the values it
     * ended there are current again. What the user wrote or ended stays.
     *
     * @return how many things changed
     */
    public int rewind(Predicate<String> dropped) {
        int changed = 0;
        Iterator<StateItem> it = getItems().iterator();
        while (it.hasNext()) {
            StateItem item = it.next();
            if (item.getSourceMessage() != null && dropped.test(item.getSourceMessage())) {
                it.remove();
                changed++;
            }
        }
        for (StateItem item : getItems()) {
            if (!item.isActive() && item.getEndedBy() != null && dropped.test(item.getEndedBy())) {
                item.reactivate();
                changed++;
            }
        }
        return changed;
    }

    /** Brings a past value back; the current value of a single or description category goes to the history in its place. */
    public void restore(StateItem item, Category category, long now) {
        if (item.isActive()) {
            return;
        }
        if (category != null && category.getKind().isSingleValued()) {
            for (StateItem current : active(item.getCategoryId())) {
                current.end(StateItem.EndReason.REPLACED, now);
            }
        }
        item.reactivate();
    }

    /** Deletes an item for good. */
    public boolean delete(StateItem item) {
        return getItems().remove(item);
    }

    /** Starts tracking the category for the character (at the end of its list), nothing when it is tracked already. */
    public void track(String categoryId) {
        if (!getCategories().contains(categoryId)) {
            getCategories().add(categoryId);
        }
    }

    /** Stops tracking the category; its values stay (history and current), and come back if it is tracked again. */
    public void untrack(String categoryId) {
        getCategories().remove(categoryId);
    }

    /** Deletes every value of the category and stops tracking it (the category is deleted from the book). */
    public void forget(String categoryId) {
        untrack(categoryId);
        getItems().removeIf(item -> categoryId.equals(item.getCategoryId()));
    }

    public boolean hasItemsOf(String categoryId) {
        return getItems().stream().anyMatch(item -> categoryId.equals(item.getCategoryId()));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAliases() {
        if (aliases == null) {
            aliases = new ArrayList<>();
        }
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = new ArrayList<>(aliases);
    }

    public List<String> getCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = new ArrayList<>(categories);
    }

    public List<StateItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
}
