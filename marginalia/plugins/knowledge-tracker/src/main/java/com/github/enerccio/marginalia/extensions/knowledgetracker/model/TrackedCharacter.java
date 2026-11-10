package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;

import java.util.*;
import java.util.function.Predicate;

/**
 * A character the user chose to track: its name and aliases, what it knows and does not know (items) and the people it
 * has met.
 */
public class TrackedCharacter {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private List<String> aliases = new ArrayList<>();
    private List<KnowledgeItem> items = new ArrayList<>();
    private List<MetPerson> met = new ArrayList<>();

    public TrackedCharacter() {
    }

    public TrackedCharacter(String name) {
        this.name = name;
    }

    /**
     * Whether {@code candidate} is the name or an alias of this character, compared as the present characters are.
     */
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

    public List<KnowledgeItem> activeItems() {
        return getItems().stream().filter(KnowledgeItem::isActive).toList();
    }

    public List<KnowledgeItem> oldItems() {
        return getItems().stream().filter(item -> !item.isActive()).toList();
    }

    public KnowledgeItem findItem(String itemId) {
        return getItems().stream().filter(item -> item.getId().equals(itemId)).findFirst().orElse(null);
    }

    public boolean deleteItem(String itemId) {
        return getItems().removeIf(item -> item.getId().equals(itemId));
    }

    /**
     * Deletes the items with these ids for good.
     *
     * @return how many items were deleted
     */
    public int deleteItems(Collection<String> itemIds) {
        int before = getItems().size();
        Set<String> ids = new HashSet<>(itemIds);
        getItems().removeIf(item -> ids.contains(item.getId()));
        return before - getItems().size();
    }

    /**
     * Records that this character has met {@code person}, unless it is recorded already (the earlier record wins).
     * A character does not meet itself.
     */
    public void addMet(String person, String sourceMessage) {
        if (PresentCharacters.normalize(person).isEmpty() || isCalled(person)) {
            return;
        }
        String normalized = PresentCharacters.normalize(person);
        for (MetPerson known : getMet()) {
            if (PresentCharacters.normalize(known.getName()).equals(normalized)) {
                return;
            }
        }
        getMet().add(new MetPerson(person.strip(), sourceMessage));
    }

    /**
     * Sets the people this character has met to {@code names}, in that order: a name that is recorded already keeps its
     * record (and the message it was seen in), a new one is the user's own (no message, so a reprocessing keeps it), the
     * ones not named are forgotten. A name that is empty, this character's own or repeated is skipped.
     */
    public void setMetNames(List<String> names) {
        List<MetPerson> before = getMet();
        List<MetPerson> kept = new ArrayList<>();
        for (String name : names) {
            String normalized = PresentCharacters.normalize(name);
            if (normalized.isEmpty() || isCalled(name)
                    || kept.stream().anyMatch(k -> PresentCharacters.normalize(k.getName()).equals(normalized))) {
                continue;
            }
            MetPerson known = before.stream()
                    .filter(k -> PresentCharacters.normalize(k.getName()).equals(normalized)).findFirst().orElse(null);
            kept.add(known != null ? known : new MetPerson(name.strip(), null));
        }
        this.met = kept;
    }

    /**
     * Takes back what came from the messages that {@code dropped} accepts (the message being reprocessed and the later
     * ones, or messages of another branch): learned items and rejected additions go, an item that one of these messages
     * retracted is active again, the meetings seen in them are forgotten. Initial and manual items, and the retractions
     * by hand, stay.
     *
     * @return how many things changed
     */
    public int rewind(Predicate<String> dropped) {
        int changed = 0;
        int before = getItems().size();
        getItems().removeIf(item -> item.getOrigin() == ItemOrigin.LEARNED
                && item.getSourceMessage() != null && dropped.test(item.getSourceMessage()));
        changed += before - getItems().size();
        for (KnowledgeItem item : getItems()) {
            if (item.getOldReason() == OldReason.RETRACTED && item.getRetractedAt() != null
                    && dropped.test(item.getRetractedAt())) {
                item.restore();
                changed++;
            }
        }
        int metBefore = getMet().size();
        getMet().removeIf(person -> person.getSourceMessage() != null && dropped.test(person.getSourceMessage()));
        changed += metBefore - getMet().size();
        return changed;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public List<KnowledgeItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public List<MetPerson> getMet() {
        if (met == null) {
            met = new ArrayList<>();
        }
        return met;
    }
}
