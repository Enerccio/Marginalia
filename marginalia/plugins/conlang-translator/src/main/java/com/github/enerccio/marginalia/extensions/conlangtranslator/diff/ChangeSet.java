package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Category;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Register;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The difference between two languages as a list of typed operations that a person reviews and picks from ({@link LanguageDiff#apply} writes the
 * picked ones). The same set is what an import against an existing language, a comparison of two languages, the proposals of the designer and
 * the review of an agent's work are.
 */
public final class ChangeSet {

    private final Language incoming;
    private final Language current;
    private final List<Change> changes = new ArrayList<>();
    private boolean twoWay;

    ChangeSet(Language incoming, Language current) {
        this.incoming = incoming;
        this.current = current;
    }

    /** A comparison with no base: what the other language lacks is not a wish to delete it. */
    void setTwoWay(boolean twoWay) {
        this.twoWay = twoWay;
    }

    public boolean isTwoWay() {
        return twoWay;
    }

    void add(Change change) {
        changes.add(change);
    }

    public List<Change> changes() {
        return changes;
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public long conflicts() {
        return changes.stream().filter(Change::hasConflict).count();
    }

    public long pickedCount() {
        return changes.stream().filter(Change::isPicked).count();
    }

    public Optional<Change> find(String key) {
        return changes.stream().filter(change -> change.key().equals(key)).findFirst();
    }

    /**
     * Picks or unpicks a change. Picking brings along what it depends on (the category of a new word); unpicking takes away what depends on it.
     */
    public void pick(Change change, boolean picked) {
        change.setPicked(picked);
        if (picked) {
            for (String thing : change.dependsOn()) {
                changes.stream().filter(other -> other.thing().equals(thing) && other.kind() == Change.Kind.ADD).forEach(other -> other.setPicked(true));
            }
        } else {
            changes.stream().filter(other -> other.dependsOn().contains(change.thing())).forEach(other -> other.setPicked(false));
        }
    }

    public void pickAll() {
        changes.forEach(change -> change.setPicked(true));
    }

    public void pickNone() {
        changes.forEach(change -> change.setPicked(false));
    }

    /** Picks what is not in conflict; in a comparison with no base also nothing that deletes. */
    public void pickClean() {
        if (twoWay) {
            pickAdditions();
            return;
        }
        changes.forEach(change -> change.setPicked(change.conflict() == null));
    }

    /** Picks what is not in conflict and does not delete anything: the default of a comparison with no base, where what is missing there is not a wish to delete. */
    public void pickAdditions() {
        changes.forEach(change -> change.setPicked(change.conflict() == null && change.kind() != Change.Kind.DELETE));
    }

    /**
     * What the user reads for a value of a field: a category or a register is its name (looked up in the other language, else here), the rest is the value.
     */
    public String display(String field, String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        if ("category".equals(field)) {
            Category found = Optional.ofNullable(incoming.findCategory(raw)).orElse(current.findCategory(raw));
            return found == null ? raw : found.getName();
        }
        if ("register".equals(field)) {
            Register found = Optional.ofNullable(incoming.findRegister(raw)).orElse(current.findRegister(raw));
            return found == null ? raw : found.getName();
        }
        if ("variants".equals(field)) {
            return java.util.Arrays.stream(raw.split("\n")).map(id -> {
                var entry = Optional.ofNullable(incoming.findEntry(id)).orElse(current.findEntry(id));
                return entry == null ? id : entry.getWord();
            }).collect(java.util.stream.Collectors.joining(", "));
        }
        return raw.replace(Items.SEP, '|');
    }
}
