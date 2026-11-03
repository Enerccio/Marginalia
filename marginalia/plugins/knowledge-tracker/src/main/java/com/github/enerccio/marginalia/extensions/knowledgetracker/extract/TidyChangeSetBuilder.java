package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt.Numbering;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyAnswer.CharacterTidy;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyAnswer.Drop;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyAnswer.Merge;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ProcessedPart;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Turns the answer of the tidy pass into changes the user can review: a drop is a retraction (made by the tidy pass: the item becomes old
 * knowledge and nothing re-reads it), a merge is the retraction of its items and the addition of the merged one. Nothing is applied here.
 * <p>
 * Only the numbers of the request can be changed, and those are only items the model learned and the user did not lock; an answer that
 * names anything else, an item twice, a merge of fewer than two items or without a text is dropped with a note.
 */
public final class TidyChangeSetBuilder {

    private TidyChangeSetBuilder() {
    }

    public static ChangeSet build(KnowledgeState state, Numbering numbering, TidyAnswer answer) {
        List<Change> changes = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        if (answer == null || answer.characters() == null) {
            return new ChangeSet(changes, dropped);
        }
        for (CharacterTidy entry : answer.characters()) {
            if (entry == null || entry.name() == null) {
                continue;
            }
            TrackedCharacter character = state.findByName(entry.name());
            List<String> itemIds = character == null ? null : numbering.items().get(character.getId());
            if (itemIds == null) {
                dropped.add("Not asked about: " + entry.name());
                continue;
            }
            Set<Integer> used = new HashSet<>();
            for (Drop drop : orEmpty(entry.drop())) {
                if (drop == null) {
                    continue;
                }
                KnowledgeItem item = item(character, itemIds, drop.item(), used, dropped);
                if (item == null) {
                    continue;
                }
                if (blank(drop.reason())) {
                    dropped.add(character.getName() + ": drop of item " + drop.item() + " has no reason");
                    continue;
                }
                changes.add(retract(character, item, drop.reason().strip()));
            }
            Set<String> known = new HashSet<>();
            character.activeItems().forEach(item -> known.add(norm(item.getText())));
            for (Merge merge : orEmpty(entry.merge())) {
                if (merge == null) {
                    continue;
                }
                merge(state, character, itemIds, merge, used, known, changes, dropped);
            }
        }
        return new ChangeSet(changes, dropped);
    }

    private static void merge(KnowledgeState state, TrackedCharacter character, List<String> itemIds, Merge merge, Set<Integer> used,
                              Set<String> known, List<Change> changes, List<String> dropped) {
        String who = character.getName();
        List<Integer> numbers = merge.items() == null ? List.of() : new ArrayList<>(new LinkedHashSet<>(merge.items()));
        if (numbers.size() < 2) {
            dropped.add(who + ": a merge needs two or more different items");
            return;
        }
        if (blank(merge.text()) || blank(merge.reason())) {
            dropped.add(who + ": merge of items " + numbers + " has no text or no reason");
            return;
        }
        String text = merge.text().strip();
        if (known.contains(norm(text)) && numbers.stream().noneMatch(n -> n >= 1 && n <= itemIds.size()
                && norm(character.findItem(itemIds.get(n - 1)).getText()).equals(norm(text)))) {
            dropped.add(who + ": the merged item already exists: " + text);
            return;
        }
        List<KnowledgeItem> sources = new ArrayList<>();
        Set<Integer> mine = new HashSet<>();
        for (Integer n : numbers) {
            KnowledgeItem item = item(character, itemIds, n == null ? 0 : n, used, mine, dropped);
            if (item == null) {
                return;
            }
            sources.add(item);
        }
        used.addAll(mine);
        ItemKind kind = "does-not-know".equalsIgnoreCase(String.valueOf(merge.kind()).strip()) ? ItemKind.DOES_NOT_KNOW : ItemKind.KNOWS;
        String earliest = earliestSource(state, sources);
        for (KnowledgeItem source : sources) {
            changes.add(retract(character, source, "merged: " + merge.reason().strip()));
        }
        changes.add(new Change(Change.Type.ADD, character.getId(), character.getName(), null, null, kind, text,
                merge.reason().strip(), true, earliest, 0).tidy());
        known.add(norm(text));
    }

    /** The source message of the merged item: the earliest of its items, so that a reprocess from there takes it back. */
    private static String earliestSource(KnowledgeState state, List<KnowledgeItem> sources) {
        List<ProcessedPart> processed = state.getProcessed();
        String best = null;
        int bestIndex = Integer.MAX_VALUE;
        for (KnowledgeItem source : sources) {
            String message = source.getSourceMessage();
            if (message == null) {
                continue;
            }
            int index = Integer.MAX_VALUE - 1;
            for (int i = 0; i < processed.size(); i++) {
                if (processed.get(i).getUuid().equals(message)) {
                    index = i;
                    break;
                }
            }
            if (best == null || index < bestIndex) {
                best = message;
                bestIndex = index;
            }
        }
        return best;
    }

    private static Change retract(TrackedCharacter character, KnowledgeItem item, String reason) {
        return new Change(Change.Type.RETRACT, character.getId(), character.getName(), item.getId(), item.getText(), item.getKind(), null,
                reason, true, null, 0).tidy();
    }

    private static KnowledgeItem item(TrackedCharacter character, List<String> itemIds, int number, Set<Integer> used, List<String> dropped) {
        Set<Integer> mine = new HashSet<>();
        KnowledgeItem item = item(character, itemIds, number, used, mine, dropped);
        if (item != null) {
            used.addAll(mine);
        }
        return item;
    }

    /** The item of that number, or {@code null} (noted): it must be one of the numbered items and not used by another change. */
    private static KnowledgeItem item(TrackedCharacter character, List<String> itemIds, int number, Set<Integer> used, Set<Integer> mine,
                                      List<String> dropped) {
        if (number < 1 || number > itemIds.size()) {
            dropped.add(character.getName() + ": there is no item " + number + " that may be changed");
            return null;
        }
        if (used.contains(number) || !mine.add(number)) {
            dropped.add(character.getName() + ": item " + number + " is named by more than one change");
            return null;
        }
        KnowledgeItem item = character.findItem(itemIds.get(number - 1));
        if (item == null || !item.isTidyCandidate()) {
            dropped.add(character.getName() + ": item " + number + " is not there any more");
            return null;
        }
        return item;
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Punct}]+", " ").strip();
    }
}
