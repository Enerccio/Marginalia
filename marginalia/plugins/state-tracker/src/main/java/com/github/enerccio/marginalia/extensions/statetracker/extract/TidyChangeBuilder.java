package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange.Action;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.CharacterTidy;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.Drop;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.Merge;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.Update;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.storytracker.process.Evidence;

import java.util.*;

/**
 * Turns the answer of the tidy into changes the user can review (all marked {@link StateChange#tidy()}, the reason in place of
 * the quote): a drop ends the value (clear, or remove for a list entry), an update replaces it (set, or replace for a list entry),
 * a merge replaces the first entry by the merged one and removes the others. Nothing is applied here.
 * <p>
 * Only the numbers of the request can change, and those are only current values the user did not lock; an answer that names
 * another character's value, a value twice, a merge of fewer than two entries or of entries of different categories, an update
 * to the same text or a change without a reason is dropped with a note.
 */
public final class TidyChangeBuilder {

    private TidyChangeBuilder() {
    }

    /**
     * @param request the request the answer is to
     * @param answer  the answer, may be {@code null}
     * @param part    the part (uuid) the changes are recorded on, the last processed one: a reprocess from it or earlier takes them back
     */
    public static StateChangeBuilder.Result build(StateBook state, StatePrompt.Request request, TidyAnswer answer, String part) {
        List<StateChange> changes = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        if (answer == null || answer.characters() == null) {
            return new StateChangeBuilder.Result(changes, dropped);
        }
        for (CharacterTidy entry : answer.characters()) {
            if (entry == null || entry.name() == null) {
                continue;
            }
            TrackedCharacter character = state.findByName(entry.name());
            if (character == null || !request.included().contains(character)) {
                if (!orEmpty(entry.drop()).isEmpty() || !orEmpty(entry.update()).isEmpty() || !orEmpty(entry.merge()).isEmpty()) {
                    dropped.add("Not asked about: " + entry.name());
                }
                continue;
            }
            Set<String> used = new HashSet<>();
            for (Drop drop : orEmpty(entry.drop())) {
                StateItem item = item(state, request, character, drop.item(), used, dropped);
                if (item == null) {
                    continue;
                }
                if (blank(drop.reason())) {
                    dropped.add(character.getName() + ": the drop of value " + drop.item() + " has no reason");
                    continue;
                }
                Category category = state.findCategory(item.getCategoryId());
                Action action = category.getKind() == CategoryKind.LIST ? Action.REMOVE : Action.CLEAR;
                changes.add(change(character, category, action, null, item, drop.reason(), part));
            }
            for (Update update : orEmpty(entry.update())) {
                StateItem item = item(state, request, character, update.item(), used, dropped);
                if (item == null) {
                    continue;
                }
                if (blank(update.value()) || blank(update.reason())) {
                    dropped.add(character.getName() + ": the update of value " + update.item() + " has no value or no reason");
                    continue;
                }
                if (Evidence.norm(update.value()).equals(Evidence.norm(item.getText()))) {
                    dropped.add(character.getName() + ": the update of value " + update.item() + " does not change it");
                    continue;
                }
                Category category = state.findCategory(item.getCategoryId());
                Action action = category.getKind() == CategoryKind.LIST ? Action.REPLACE : Action.SET;
                changes.add(change(character, category, action, update.value().strip(), item, update.reason(), part));
            }
            for (Merge merge : orEmpty(entry.merge())) {
                merge(state, request, character, merge, used, changes, dropped, part);
            }
        }
        return new StateChangeBuilder.Result(changes, dropped);
    }

    private static void merge(StateBook state, StatePrompt.Request request, TrackedCharacter character, Merge merge, Set<String> used,
                              List<StateChange> changes, List<String> dropped, String part) {
        String who = character.getName();
        List<Integer> numbers = merge.items() == null ? List.of()
                : new ArrayList<>(new LinkedHashSet<>(merge.items().stream().filter(Objects::nonNull).toList()));
        if (numbers.size() < 2) {
            dropped.add(who + ": a merge needs two or more different entries");
            return;
        }
        if (blank(merge.value()) || blank(merge.reason())) {
            dropped.add(who + ": the merge of " + numbers + " has no value or no reason");
            return;
        }
        Set<String> mine = new HashSet<>();
        List<StateItem> sources = new ArrayList<>();
        for (int number : numbers) {
            StateItem item = item(state, request, character, number, used, mine, dropped);
            if (item == null) {
                return;
            }
            sources.add(item);
        }
        Category category = state.findCategory(sources.getFirst().getCategoryId());
        if (sources.stream().anyMatch(item -> !item.getCategoryId().equals(category.getId())) || category.getKind() != CategoryKind.LIST) {
            dropped.add(who + ": the merge of " + numbers + " is not of entries of one list");
            return;
        }
        String value = merge.value().strip();
        boolean elsewhere = character.active(category.getId()).stream()
                .anyMatch(item -> !sources.contains(item) && Evidence.norm(item.getText()).equals(Evidence.norm(value)));
        if (elsewhere) {
            dropped.add(who + ": " + category.getName() + " has " + value + " already");
            return;
        }
        used.addAll(mine);
        String reason = "merged: " + merge.reason().strip();
        changes.add(change(character, category, Action.REPLACE, value, sources.getFirst(), reason, part));
        for (StateItem other : sources.subList(1, sources.size())) {
            changes.add(change(character, category, Action.REMOVE, null, other, reason, part));
        }
    }

    private static StateChange change(TrackedCharacter character, Category category, Action action, String value, StateItem item,
                                      String reason, String part) {
        return new StateChange(character.getId(), character.getName(), category.getId(), category.getName(), action, value,
                item.getId(), item.getText(), reason.strip(), 0, part, true).tidy();
    }

    private static StateItem item(StateBook state, StatePrompt.Request request, TrackedCharacter character, int number, Set<String> used,
                                  List<String> dropped) {
        Set<String> mine = new HashSet<>();
        StateItem item = item(state, request, character, number, used, mine, dropped);
        if (item != null) {
            used.addAll(mine);
        }
        return item;
    }

    /** The value of that number, or {@code null} (noted): numbered, the character's, current, unlocked and not used by another change. */
    private static StateItem item(StateBook state, StatePrompt.Request request, TrackedCharacter character, int number, Set<String> used,
                                  Set<String> mine, List<String> dropped) {
        List<StateItem> numbered = request.numbering().items();
        StateItem item = number < 1 || number > numbered.size() ? null : numbered.get(number - 1);
        if (item == null || !character.getItems().contains(item) || state.findCategory(item.getCategoryId()) == null) {
            dropped.add(character.getName() + ": there is no value " + number + " of this character");
            return null;
        }
        if (used.contains(item.getId()) || !mine.add(item.getId())) {
            dropped.add(character.getName() + ": value " + number + " is named by more than one change");
            return null;
        }
        if (!item.isActive() || item.isLocked()) {
            dropped.add(character.getName() + ": value " + number + " is not there any more");
            return null;
        }
        return item;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
