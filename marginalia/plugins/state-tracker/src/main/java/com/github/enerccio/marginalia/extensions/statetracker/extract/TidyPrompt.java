package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StatePrompt.Numbering;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StatePrompt.Request;
import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateItem;
import com.github.enerccio.marginalia.extensions.statetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.T;

/**
 * The request of the tidy: the current values of the tracked characters that are present in the last parts are read again with
 * those parts, and the model says which values faded (a mood, wet clothes, a cup long after the meal), which moved on and which
 * list entries are the same thing. Only the values the user did not lock are numbered, so only they can change; the locked ones
 * are shown (an entry can duplicate them) and stay. A character that is in none of the parts is not asked about: there is nothing
 * to judge its state from.
 * <p>
 * Pure, no state.
 */
public final class TidyPrompt {

    private TidyPrompt() {
    }

    /**
     * @param recent     the last processed parts, oldest first
     * @param separators the separators of the present characters field, {@code null} for the built-in ones
     * @param system     the system message, blank = the built-in one
     * @return a request without characters ({@link Request#isEmpty()}) when there is nothing to tidy
     */
    public static Request build(StateBook state, List<Part> recent, Collection<String> separators, String system) {
        String instructions = StringUtils.isBlank(system) ? T.defaults().tidySystem() : system.strip();
        List<Set<String>> present = new ArrayList<>();
        for (Part part : recent) {
            present.add(PresentCharacters.normalizedNames(part.presentCharacters(), separators));
        }

        Map<TrackedCharacter, List<Integer>> where = new LinkedHashMap<>();
        for (TrackedCharacter character : state.getCharacters()) {
            boolean candidates = state.categoriesOf(character).stream()
                    .anyMatch(category -> character.active(category.getId()).stream().anyMatch(item -> !item.isLocked()));
            if (!candidates) {
                continue;
            }
            List<Integer> in = new ArrayList<>();
            for (int i = 0; i < recent.size(); i++) {
                if (PresentCharacters.isPresent(present.get(i), character.getName(), character.getAliases())) {
                    in.add(i + 1);
                }
            }
            if (!in.isEmpty()) {
                where.put(character, in);
            }
        }

        Integer today = state.today();
        StringBuilder user = new StringBuilder();
        if (today != null) {
            user.append("[Clock]\n").append(state.getClock().now(state).forRequest(state.getClock().getAnchor())).append("\n\n");
        }
        user.append("[Tracked characters]\n");
        List<StateItem> numbered = new ArrayList<>();
        for (var entry : where.entrySet()) {
            TrackedCharacter character = entry.getKey();
            user.append(character.getName().strip()).append(" (present in the messages: ")
                    .append(String.join(", ", entry.getValue().stream().map(n -> "#" + n).toList())).append(")\n");
            for (Category category : state.categoriesOf(character)) {
                List<StateItem> current = character.active(category.getId());
                if (current.isEmpty()) {
                    continue;
                }
                user.append("  ").append(category.getName().strip()).append(" (").append(StatePrompt.kind(category)).append(")");
                if (!category.getInstruction().isBlank()) {
                    user.append(": ").append(oneLine(category.getInstruction()));
                }
                user.append('\n');
                for (StateItem item : current) {
                    if (item.isLocked()) {
                        user.append("    - (stays) ").append(oneLine(item.getText())).append(age(item, today)).append('\n');
                    } else {
                        numbered.add(item);
                        user.append("    ").append(numbered.size()).append(". ").append(oneLine(item.getText())).append(age(item, today)).append('\n');
                    }
                }
            }
        }

        user.append("\n[Messages]\n");
        for (int i = 0; i < recent.size(); i++) {
            Part part = recent.get(i);
            user.append('#').append(i + 1).append(" (POV ").append(StringUtils.defaultString(part.povCharacter()).strip())
                    .append("; present: ").append(StringUtils.defaultString(part.presentCharacters()).strip()).append(")\n")
                    .append(StringUtils.defaultString(part.text()).strip()).append("\n\n");
        }
        return new Request(instructions, user.toString().stripTrailing(), new Numbering(List.copyOf(numbered), List.copyOf(recent)),
                List.copyOf(where.keySet()));
    }

    /** " (set on day 4, 9 days ago)" when the book follows the time and the value knows its day. */
    static String age(StateItem item, Integer today) {
        if (today == null || item.getStoryDay() == null) {
            return "";
        }
        int ago = today - item.getStoryDay();
        return " (set on day " + item.getStoryDay() + ", " + (ago <= 0 ? "today" : ago == 1 ? "1 day ago" : ago + " days ago") + ")";
    }

    private static String oneLine(String text) {
        return text.strip().replaceAll("\\s*\\R\\s*", " ");
    }
}
