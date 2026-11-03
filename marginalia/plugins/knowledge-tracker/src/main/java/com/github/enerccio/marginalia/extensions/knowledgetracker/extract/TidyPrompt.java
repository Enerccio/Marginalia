package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt.Numbering;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt.Request;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The request of the tidy pass: what the tracked characters know is read again, with the last parts of the story for context, and the
 * model says which items are not worth keeping and which belong together. Only the items the model learned and the user did not lock are
 * numbered, so only they can be changed; the others are shown (they are what an item can duplicate) and stay as they are.
 * <p>
 * Pure, no state.
 */
public final class TidyPrompt {

    public static final String SYSTEM = """
            You tidy what the characters of a story know. You get the TRACKED characters with the numbered items they know or do not know, the items that stay as they are (for comparison only) and the last parts of the story for context. The items are sent to a writing model so that it does not give a character knowledge it cannot have; every item costs room and must be worth it.

            Rules:
            1. drop an item that is not worth keeping: trivia (a detail of the scene, small talk, a joke, a mood, an object), an event that every character in the scene saw together (the story already holds it), something later events overtook, or something that says the same as another item or as an item that stays.
            2. merge two or more items about the same thing into one item that keeps every fact of them. Do not add anything that is not in the items.
            3. NEVER drop or merge a secret, an identity, a relationship, a feeling that was revealed, a plan, a promise, a discovery, or anything another character does not know, even when the last parts do not mention it: an item that has not come up lately is not a reason to drop it.
            4. When in doubt keep it. Report only what you are sure of; empty lists are normal.
            5. Every drop and every merge needs a short reason.

            Answer with JSON only, no other text: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}. A character with nothing to do has empty lists.""";

    private TidyPrompt() {
    }

    /**
     * @param recent   the last processed parts, oldest first, for context; may be empty
     * @param minItems a character with fewer items that may be changed is not asked about
     * @return a request without characters ({@link Request#isEmpty()}) when there is nothing to tidy
     */
    public static Request build(KnowledgeState state, List<Part> recent, int minItems) {
        StringBuilder user = new StringBuilder("[Tracked characters]\n");
        Map<String, List<String>> numbered = new LinkedHashMap<>();
        List<TrackedCharacter> included = new ArrayList<>();
        for (TrackedCharacter character : state.getCharacters()) {
            List<KnowledgeItem> candidates = character.activeItems().stream().filter(KnowledgeItem::isTidyCandidate).toList();
            if (candidates.isEmpty() || candidates.size() < minItems) {
                continue;
            }
            user.append(character.getName().strip()).append("\n  Items you may drop or merge:\n");
            List<String> ids = new ArrayList<>();
            for (KnowledgeItem item : candidates) {
                ids.add(item.getId());
                user.append("  ").append(ids.size()).append(". ").append(line(item)).append('\n');
            }
            List<KnowledgeItem> staying = character.activeItems().stream().filter(item -> !item.isTidyCandidate()).toList();
            if (!staying.isEmpty()) {
                user.append("  Items that stay as they are (comparison only):\n");
                staying.forEach(item -> user.append("  - ").append(line(item)).append('\n'));
            }
            numbered.put(character.getId(), ids);
            included.add(character);
        }
        if (!recent.isEmpty()) {
            user.append("\n[Last parts of the story]\n");
            for (Part part : recent) {
                user.append("(POV ").append(StringUtils.defaultString(part.povCharacter()).strip()).append("; present: ")
                        .append(StringUtils.defaultString(part.presentCharacters()).strip()).append(")\n")
                        .append(StringUtils.defaultString(part.text()).strip()).append("\n\n");
            }
        }
        return new Request(SYSTEM, user.toString().stripTrailing(), new Numbering(numbered, List.of()), List.copyOf(included));
    }

    private static String line(KnowledgeItem item) {
        return (item.getKind() == ItemKind.KNOWS ? "knows: " : "does not know: ") + item.getText().strip();
    }
}
