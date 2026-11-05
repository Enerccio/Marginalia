package com.github.enerccio.marginalia.extensions.knowledgetracker.facts;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The facts the annotation gets from the plugin: what each tracked character of a part knew <b>when the part begins</b>, and what the part
 * itself changes, so that a model that reviews the part can flag a character who acts on what they cannot know.
 * <p>
 * The state of the plugin is the state of the end of the processed story, not a series of snapshots, so the state at a part is rebuilt from
 * the messages the items remember: an item learned in a later part (or on another branch) did not exist yet, an item that a later part
 * retracts was still true, the people met are the ones seen before. What the part itself brings is listed apart (what is learned, who is met,
 * what stops being true in it) so that neither the first half of the part is checked against what the second half teaches, nor the second half
 * against what is no longer true.
 * <p>
 * Items the user retracted by hand, the ones the tidy pass dropped and the ones declined in the review are not facts at any point, as for
 * the block of a generation. Pure logic over the state and the parts; the extension gives it the branch.
 */
public final class KnowledgeFactsBuilder {

    static final String HEADER = "What the tracked characters know when the part begins, from the author's Knowledge Tracker:";

    static final String UNREAD = "(The tracker has not read the story up to this part: what the characters learned in the parts it has not read is missing.)";

    private KnowledgeFactsBuilder() {
    }

    /**
     * @param state      the state of the book
     * @param branch     the parts from the start of the story to the reviewed part, which is the last one
     * @param separators the separators of the present characters field, {@code null} for the built-in ones
     * @return the facts, empty when no tracked character takes part in the reviewed part
     */
    public static String build(KnowledgeState state, List<Part> branch, Collection<String> separators) {
        if (branch == null || branch.isEmpty() || state.getCharacters().isEmpty()) {
            return "";
        }
        int target = branch.size() - 1;
        Part part = branch.get(target);
        Map<String, Integer> position = new HashMap<>();
        for (int i = 0; i < branch.size(); i++) {
            position.put(branch.get(i).uuid(), i);
        }

        Set<String> present = PresentCharacters.normalizedNames(part.presentCharacters(), separators);
        List<String> lines = new ArrayList<>();
        for (TrackedCharacter character : state.getCharacters()) {
            if (takesPart(character, part, present)) {
                lines.addAll(describe(character, position, target, state.getSettings().isClosedWorld()));
            }
        }
        if (lines.isEmpty()) {
            return "";
        }
        StringBuilder facts = new StringBuilder(String.join("\n", lines));
        if (!readUpTo(state, branch, target)) {
            facts.append("\n").append(UNREAD);
        }
        return facts.toString();
    }

    /** In the present characters field, the point of view of the part, or named in its text. */
    private static boolean takesPart(TrackedCharacter character, Part part, Set<String> present) {
        if (PresentCharacters.isPresent(present, character.getName(), character.getAliases())) {
            return true;
        }
        if (part.povCharacter() != null && character.isCalled(part.povCharacter().strip())) {
            return true;
        }
        if (part.text() == null || part.text().isBlank()) {
            return false;
        }
        if (isNamedIn(character.getName(), part.text())) {
            return true;
        }
        return character.getAliases().stream().anyMatch(alias -> isNamedIn(alias, part.text()));
    }

    /** Whether the name stands in the text as a word, case does not matter. */
    static boolean isNamedIn(String name, String text) {
        String wanted = name == null ? "" : name.strip();
        if (wanted.isEmpty()) {
            return false;
        }
        String before = wordEdge(wanted.codePointAt(0)) ? "(?<![\\p{L}\\p{N}])" : "";
        String after = wordEdge(wanted.codePointBefore(wanted.length())) ? "(?![\\p{L}\\p{N}])" : "";
        return Pattern.compile(before + Pattern.quote(wanted) + after, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(text).find();
    }

    /**
     * Whether a name has a word edge there: a letter or digit of a script that puts spaces between words. A name in Chinese or Japanese
     * is found inside a run of text, there is no edge to look for.
     */
    private static boolean wordEdge(int codePoint) {
        if (!Character.isLetterOrDigit(codePoint)) {
            return false;
        }
        Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
        return script != Character.UnicodeScript.HAN && script != Character.UnicodeScript.HIRAGANA
                && script != Character.UnicodeScript.KATAKANA;
    }

    private static List<String> describe(TrackedCharacter character, Map<String, Integer> position, int target, boolean closedWorld) {
        String name = character.getName().strip();
        List<String> knows = new ArrayList<>();
        List<String> doesNotKnow = new ArrayList<>();
        List<String> learns = new ArrayList<>();
        List<String> stillUnknown = new ArrayList<>();
        List<String> stopsBeingTrue = new ArrayList<>();
        for (KnowledgeItem item : character.getItems()) {
            switch (when(item, position, target)) {
                case HELD -> (item.getKind() == ItemKind.KNOWS ? knows : doesNotKnow).add(item.getText());
                case RETRACTED_HERE -> stopsBeingTrue.add(item.getKind().equals(ItemKind.KNOWS)
                        ? "knows " + strip(item.getText()) : "does not know " + strip(item.getText()));
                case LEARNED_HERE -> (item.getKind() == ItemKind.KNOWS ? learns : stillUnknown).add(item.getText());
                case NOT_A_FACT -> {
                }
            }
        }
        List<String> metBefore = new ArrayList<>();
        List<String> metHere = new ArrayList<>();
        for (MetPerson person : character.getMet()) {
            Integer at = person.getSourceMessage() == null ? Integer.valueOf(-1) : position.get(person.getSourceMessage());
            if (at == null || at > target) {
                continue;
            }
            (at == target ? metHere : metBefore).add(person.getName());
        }

        List<String> lines = new ArrayList<>();
        add(lines, name + " knows: ", knows, "; ");
        add(lines, name + " does not know: ", doesNotKnow, "; ");
        if (!metBefore.isEmpty() || closedWorld) {
            lines.add(name + " has met: " + (metBefore.isEmpty() ? "nobody" : String.join(", ", metBefore))
                    + (closedWorld ? " (nobody else)." : "."));
        }
        add(lines, name + " learns in this part: ", learns, "; ");
        add(lines, name + " is told or shown in this part that they do not know: ", stillUnknown, "; ");
        add(lines, name + " meets in this part: ", metHere, ", ");
        add(lines, name + " until this part, which changes it: ", stopsBeingTrue, "; ");
        return lines;
    }

    private enum When { HELD, RETRACTED_HERE, LEARNED_HERE, NOT_A_FACT }

    /**
     * Where an item stands when the part at {@code target} begins: a fact already, brought by the part, true until the part retracts it,
     * or not a fact (not yet learned, or never a fact: retracted by hand, tidied away, declined).
     */
    private static When when(KnowledgeItem item, Map<String, Integer> position, int target) {
        if (item.getOrigin() == ItemOrigin.LEARNED) {
            Integer learned = item.getSourceMessage() == null ? null : position.get(item.getSourceMessage());
            if (learned == null || learned > target) {
                return When.NOT_A_FACT;
            }
            if (learned == target) {
                return item.isActive() || retractedLater(item, position, target) ? When.LEARNED_HERE : When.NOT_A_FACT;
            }
        }
        if (item.isActive()) {
            return When.HELD;
        }
        if (item.getOldReason() != OldReason.RETRACTED || item.getRetractedAt() == null) {
            return When.NOT_A_FACT;
        }
        Integer retracted = position.get(item.getRetractedAt());
        if (retracted == null || retracted > target) {
            // retracted by a part that comes later (or is not on this branch): true until then
            return When.HELD;
        }
        return retracted == target ? When.RETRACTED_HERE : When.NOT_A_FACT;
    }

    private static boolean retractedLater(KnowledgeItem item, Map<String, Integer> position, int target) {
        if (item.getOldReason() != OldReason.RETRACTED || item.getRetractedAt() == null) {
            return false;
        }
        Integer retracted = position.get(item.getRetractedAt());
        return retracted == null || retracted > target;
    }

    /** Whether every part from the start of the processing up to the one before the reviewed part was read. */
    private static boolean readUpTo(KnowledgeState state, List<Part> branch, int target) {
        Set<String> processed = new HashSet<>();
        state.getProcessed().forEach(p -> processed.add(p.getUuid()));
        int start = 0;
        for (int i = 0; i < branch.size() && state.getStartMessage() != null; i++) {
            if (branch.get(i).uuid().equals(state.getStartMessage())) {
                start = i;
                break;
            }
        }
        for (int i = start; i < target; i++) {
            if (!processed.contains(branch.get(i).uuid())) {
                return false;
            }
        }
        return true;
    }

    private static void add(List<String> lines, String prefix, List<String> values, String separator) {
        List<String> texts = values.stream().map(KnowledgeFactsBuilder::strip).filter(text -> !text.isEmpty()).toList();
        if (!texts.isEmpty()) {
            lines.add(prefix + texts.stream().collect(Collectors.joining(separator)) + ".");
        }
    }

    /** The items are joined with "; ", their own full stop would read ".;". */
    private static String strip(String text) {
        String stripped = text == null ? "" : text.strip();
        while (!stripped.isEmpty() && ".。".indexOf(stripped.charAt(stripped.length() - 1)) >= 0) {
            stripped = stripped.substring(0, stripped.length() - 1).stripTrailing();
        }
        return stripped;
    }
}
