package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StateAnswer.CharacterChanges;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange.Action;
import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateItem;
import com.github.enerccio.marginalia.extensions.statetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.storytracker.process.Evidence;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;

import java.util.*;

/**
 * Turns what the model answered into changes the user can review, and drops what is not usable: a character that was not asked
 * about, a category it does not track, an action the kind of the category does not have, a value that is not there (or not current),
 * a message that does not exist, a change without a quote, a value the character has already. Nothing is applied here.
 */
public final class StateChangeBuilder {

    /** The checked changes and, one line each, what was dropped and why. */
    public record Result(List<StateChange> changes, List<String> dropped) {
    }

    private StateChangeBuilder() {
    }

    /**
     * @param request the request the answer is to; its numbering says what the numbers of the answer stand for
     * @param answer  the answer, may be {@code null}
     */
    public static Result build(StateBook state, StatePrompt.Request request, StateAnswer answer) {
        List<StateChange> changes = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        if (answer == null || answer.characters() == null) {
            return new Result(changes, dropped);
        }
        for (CharacterChanges entry : answer.characters()) {
            if (entry == null || entry.name() == null) {
                continue;
            }
            TrackedCharacter character = state.findByName(entry.name());
            if (character == null || !request.included().contains(character)) {
                if (entry.changes() != null && !entry.changes().isEmpty()) {
                    dropped.add("Not asked about: " + entry.name());
                }
                continue;
            }
            // what the character holds once the earlier changes of the answer are taken into account, to drop repeats
            Set<String> seen = new HashSet<>();
            Set<String> ended = new HashSet<>();
            for (StateAnswer.Change change : orEmpty(entry.changes())) {
                check(state, request, character, change, seen, ended, changes, dropped);
            }
        }
        return new Result(changes, dropped);
    }

    private static void check(StateBook state, StatePrompt.Request request, TrackedCharacter character, StateAnswer.Change c,
                              Set<String> seen, Set<String> ended, List<StateChange> changes, List<String> dropped) {
        String who = character.getName();
        Category category = category(state, character, c.category());
        if (category == null) {
            dropped.add(who + ": no tracked category " + c.category());
            return;
        }
        Action action = action(c.action());
        if (action == null || !fits(category, action)) {
            dropped.add(who + ": " + category.getName() + " (" + StatePrompt.kind(category) + ") has no action " + c.action());
            return;
        }
        List<Part> parts = request.numbering().parts();
        if (c.message() < 1 || c.message() > parts.size()) {
            dropped.add(who + ": " + category.getName() + " names a message that does not exist (" + c.message() + ")");
            return;
        }
        if (blank(c.evidence())) {
            dropped.add(who + ": " + category.getName() + " has no evidence");
            return;
        }
        Part part = parts.get(c.message() - 1);
        boolean found = Evidence.found(part.text(), c.evidence());
        List<StateItem> current = character.active(category.getId());
        switch (action) {
            case SET -> {
                if (blank(c.value())) {
                    dropped.add(who + ": " + category.getName() + " set without a value");
                    return;
                }
                String value = c.value().strip();
                StateItem now = current.isEmpty() ? null : current.getFirst();
                if (now != null && Evidence.norm(now.getText()).equals(Evidence.norm(value))) {
                    dropped.add(who + ": " + category.getName() + " is " + value + " already");
                    return;
                }
                if (!seen.add(category.getId() + "\n" + Evidence.norm(value))) {
                    return;
                }
                changes.add(new StateChange(character.getId(), who, category.getId(), category.getName(), action, value,
                        now == null ? null : now.getId(), now == null ? null : now.getText(), c.evidence().strip(), c.message(), part.uuid(), found));
            }
            case CLEAR -> {
                if (current.isEmpty()) {
                    dropped.add(who + ": " + category.getName() + " has no value to clear");
                    return;
                }
                StateItem now = current.getFirst();
                if (!ended.add(now.getId())) {
                    return;
                }
                changes.add(new StateChange(character.getId(), who, category.getId(), category.getName(), action, null,
                        now.getId(), now.getText(), c.evidence().strip(), c.message(), part.uuid(), found));
            }
            case ADD -> {
                if (blank(c.value())) {
                    dropped.add(who + ": " + category.getName() + " add without a value");
                    return;
                }
                String value = c.value().strip();
                boolean there = current.stream().anyMatch(item -> Evidence.norm(item.getText()).equals(Evidence.norm(value)));
                if (there) {
                    dropped.add(who + ": " + category.getName() + " has " + value + " already");
                    return;
                }
                if (!seen.add(category.getId() + "\n" + Evidence.norm(value))) {
                    return;
                }
                changes.add(new StateChange(character.getId(), who, category.getId(), category.getName(), action, value,
                        null, null, c.evidence().strip(), c.message(), part.uuid(), found));
            }
            case REMOVE -> {
                List<StateItem> numbered = request.numbering().items();
                Integer number = c.item();
                StateItem item = number == null || number < 1 || number > numbered.size() ? null : numbered.get(number - 1);
                if (item == null || !character.getItems().contains(item) || !Objects.equals(item.getCategoryId(), category.getId())) {
                    dropped.add(who + ": there is no value " + number + " in " + category.getName());
                    return;
                }
                if (!item.isActive()) {
                    dropped.add(who + ": value " + number + " is not current");
                    return;
                }
                if (!ended.add(item.getId())) {
                    return;
                }
                changes.add(new StateChange(character.getId(), who, category.getId(), category.getName(), action, null,
                        item.getId(), item.getText(), c.evidence().strip(), c.message(), part.uuid(), found));
            }
        }
    }

    /** The category of the character called so (the case and the spacing ignored). */
    private static Category category(StateBook state, TrackedCharacter character, String name) {
        if (name == null) {
            return null;
        }
        String wanted = Evidence.norm(name);
        return state.categoriesOf(character).stream().filter(c -> Evidence.norm(c.getName()).equals(wanted)).findFirst().orElse(null);
    }

    static Action action(String text) {
        if (text == null) {
            return null;
        }
        return switch (text.strip().toLowerCase(Locale.ROOT)) {
            case "set", "rewrite", "replace" -> Action.SET;
            case "clear" -> Action.CLEAR;
            case "add" -> Action.ADD;
            case "remove", "delete" -> Action.REMOVE;
            default -> null;
        };
    }

    /** Which actions a kind of category has. */
    static boolean fits(Category category, Action action) {
        return switch (category.getKind()) {
            case SINGLE -> action == Action.SET || action == Action.CLEAR;
            case DESCRIPTION -> action == Action.SET || action == Action.CLEAR;
            case LIST -> action == Action.ADD || action == Action.REMOVE;
        };
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
