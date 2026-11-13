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

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.T;

/**
 * The request of the tidy pass: what the tracked characters know is read again, with the last parts of the story for context, and the
 * model says which items are not worth keeping and which belong together. Only the items the model learned and the user did not lock are
 * numbered, so only they can be changed; the others are shown (they are what an item can duplicate) and stay as they are.
 * <p>
 * Pure, no state.
 */
public final class TidyPrompt {

    private TidyPrompt() {
    }

    /**
     * @param recent   the last processed parts, oldest first, for context; may be empty
     * @param minItems a character with fewer items that may be changed is not asked about
     * @return a request without characters ({@link Request#isEmpty()}) when there is nothing to tidy
     */
    public static Request build(KnowledgeState state, List<Part> recent, int minItems) {
        return build(state, recent, minItems, T.defaults().tidySystem());
    }

    /**
     * @param system the system message, the one the book inherits; blank = the built-in {@link KnowledgeTrackerDefaults#tidySystem()}
     */
    public static Request build(KnowledgeState state, List<Part> recent, int minItems, String system) {
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
        return new Request(StringUtils.isBlank(system) ? T.defaults().tidySystem() : system.strip(), user.toString().stripTrailing(), new Numbering(numbered, List.of()), List.copyOf(included));
    }

    private static String line(KnowledgeItem item) {
        return (item.getKind() == ItemKind.KNOWS ? "knows: " : "does not know: ") + item.getText().strip();
    }
}
