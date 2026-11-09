package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Addition;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.CharacterChanges;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Retraction;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt.Numbering;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns what the model answered into a set of changes the user can review, and drops what is not usable: a character
 * that was not asked about, an item that is not there (or not active), a message that does not exist, a change without
 * a quote, something the character already has. Nothing is applied here.
 */
public final class ChangeSetBuilder {

    private static final Pattern ELLIPSIS = Pattern.compile("\\.{3}|…");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private ChangeSetBuilder() {
    }

    /**
     * @param request the request the answer is to; its numbering says what the numbers of the answer stand for
     * @param answer  the answer, may be {@code null}
     */
    public static ChangeSet build(KnowledgeState state, ExtractionPrompt.Request request, ExtractionAnswer answer) {
        List<Change> changes = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        if (answer == null || answer.characters() == null) {
            return new ChangeSet(changes, dropped);
        }
        Numbering numbering = request.numbering();
        for (CharacterChanges entry : answer.characters()) {
            if (entry == null || entry.name() == null) {
                continue;
            }
            TrackedCharacter character = state.findByName(entry.name());
            List<String> itemIds = character == null ? null : numbering.items().get(character.getId());
            if (itemIds == null) {
                dropped.add("Not asked about: " + entry.name());
                continue;
            }
            Set<Integer> retracted = new HashSet<>();
            Set<String> known = new HashSet<>();
            character.activeItems().forEach(item -> known.add(norm(item.getText())));
            for (Retraction retraction : orEmpty(entry.retract())) {
                retract(character, itemIds, numbering, retraction, retracted, changes, dropped);
            }
            for (Addition addition : orEmpty(entry.add())) {
                add(character, numbering, addition, known, changes, dropped);
            }
        }
        return new ChangeSet(changes, dropped);
    }

    private static void retract(TrackedCharacter character, List<String> itemIds, Numbering numbering, Retraction r,
                                Set<Integer> retracted, List<Change> changes, List<String> dropped) {
        String who = character.getName();
        Part part = part(numbering, r.message());
        if (part == null) {
            dropped.add(who + ": retract of item " + r.item() + " names a message that does not exist (" + r.message() + ")");
            return;
        }
        if (r.item() < 1 || r.item() > itemIds.size()) {
            dropped.add(who + ": there is no item " + r.item());
            return;
        }
        if (blank(r.evidence())) {
            dropped.add(who + ": retract of item " + r.item() + " has no evidence");
            return;
        }
        KnowledgeItem item = character.findItem(itemIds.get(r.item() - 1));
        if (item == null || !item.isActive()) {
            dropped.add(who + ": item " + r.item() + " is not active");
            return;
        }
        if (!retracted.add(r.item())) {
            dropped.add(who + ": item " + r.item() + " retracted twice");
            return;
        }
        changes.add(new Change(Change.Type.RETRACT, character.getId(), who, item.getId(), item.getText(), item.getKind(), null,
                r.evidence().strip(), found(part, r.evidence()), part.uuid(), r.message()));
    }

    private static void add(TrackedCharacter character, Numbering numbering, Addition a, Set<String> known,
                            List<Change> changes, List<String> dropped) {
        String who = character.getName();
        Part part = part(numbering, a.message());
        String text = a.text() == null ? "" : a.text().strip();
        if (part == null) {
            dropped.add(who + ": \"" + text + "\" names a message that does not exist (" + a.message() + ")");
            return;
        }
        if (text.isEmpty() || blank(a.evidence())) {
            dropped.add(who + ": an addition without text or evidence");
            return;
        }
        if (!known.add(norm(text))) {
            dropped.add(who + ": \"" + text + "\" is known already");
            return;
        }
        changes.add(new Change(Change.Type.ADD, character.getId(), who, null, null, kind(a.kind()), text, a.evidence().strip(),
                found(part, a.evidence()), part.uuid(), a.message()));
    }

    private static Part part(Numbering numbering, int number) {
        return number >= 1 && number <= numbering.parts().size() ? numbering.parts().get(number - 1) : null;
    }

    private static ItemKind kind(String text) {
        String k = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return k.contains("not") || k.contains("unknown") ? ItemKind.DOES_NOT_KNOW : ItemKind.KNOWS;
    }

    /**
     * Whether the quote is in the text of the message (case, spacing and the kind of quotation marks do not matter; a quote
     * with "..." has to have all its pieces in it).
     */
    static boolean found(Part part, String evidence) {
        String text = norm(part.text());
        for (String piece : ELLIPSIS.split(evidence == null ? "" : evidence)) {
            String wanted = norm(piece);
            if (!wanted.isEmpty() && !text.contains(wanted)) {
                return false;
            }
        }
        return !norm(evidence == null ? "" : evidence.replaceAll("\\.{3}|…", "")).isEmpty();
    }

    /** Lower case, one kind of quotes, spaces collapsed, no punctuation at the ends. */
    static String norm(String text) {
        if (text == null) {
            return "";
        }
        String s = text.replace('“', '"').replace('”', '"').replace('‘', '\'').replace('’', '\'').toLowerCase(Locale.ROOT);
        s = SPACES.matcher(s).replaceAll(" ").strip();
        return s.replaceAll("^[\\p{P}\\s]+|[\\p{P}\\s]+$", "");
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list.stream().filter(x -> x != null).toList();
    }
}
