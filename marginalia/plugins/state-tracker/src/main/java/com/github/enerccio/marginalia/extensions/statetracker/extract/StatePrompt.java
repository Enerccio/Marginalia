package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateItem;
import com.github.enerccio.marginalia.extensions.statetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The request that asks the model what changed in the state of the tracked characters in a few parts of the story. The wording is
 * the one that was tested against a real model (see TODO.md); change it together with that test.
 */
public final class StatePrompt {

    /**
     * How the numbers of the request map back to the state.
     *
     * @param items the value of number {@code n + 1} (numbered through the whole request)
     * @param parts the parts of the run, message number {@code n + 1} is {@code parts.get(n)}
     */
    public record Numbering(List<StateItem> items, List<Part> parts) {
    }

    /**
     * @param system    the system message
     * @param user      the user message
     * @param numbering what the numbers in them stand for
     * @param included  the tracked characters that are in the request (present in at least one of the parts, with a category)
     * @param clock     whether the model is also asked about the time of the story
     */
    public record Request(String system, String user, Numbering numbering, List<TrackedCharacter> included, boolean clock) {

        public Request(String system, String user, Numbering numbering, List<TrackedCharacter> included) {
            this(system, user, numbering, included, false);
        }

        /**
         * Whether there is anything to ask: a tracked character with a category is present in the parts, or the book follows the time
         * (every part can move the clock).
         */
        public boolean isEmpty() {
            return included.isEmpty() && !clock;
        }
    }

    private StatePrompt() {
    }

    /**
     * @param lore       the world text, may be empty
     * @param parts      the parts to read, in order
     * @param separators the separators of the present characters field, {@code null} for the built-in ones
     * @param system     the system message, blank = the built-in one
     */
    public static Request build(StateBook state, String lore, List<Part> parts, Collection<String> separators, String system) {
        boolean clock = state.getClock().isEnabled();
        String instructions = StringUtils.isBlank(system) ? StateDefaults.extractionSystem(clock) : system.strip();
        List<Set<String>> present = new ArrayList<>();
        for (Part part : parts) {
            present.add(PresentCharacters.normalizedNames(part.presentCharacters(), separators));
        }

        // a tracked character that is in none of the parts sees nothing change: it is not asked about
        Map<TrackedCharacter, List<Integer>> where = new LinkedHashMap<>();
        for (TrackedCharacter character : state.getCharacters()) {
            if (state.categoriesOf(character).isEmpty()) {
                continue;
            }
            List<Integer> in = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                if (PresentCharacters.isPresent(present.get(i), character.getName(), character.getAliases())) {
                    in.add(i + 1);
                }
            }
            if (!in.isEmpty()) {
                where.put(character, in);
            }
        }

        StringBuilder user = new StringBuilder();
        if (StringUtils.isNotBlank(lore)) {
            user.append("[World]\n").append(lore.strip()).append("\n\n");
        }
        if (clock) {
            // the time before the parts of the run: they come right after the processed ones
            user.append("[Clock]\n").append(state.getClock().now(state).forRequest(state.getClock().getAnchor())).append("\n\n");
        }
        user.append("[Tracked characters]\n");
        if (clock && where.isEmpty()) {
            user.append("(none of them is in these messages)\n");
        }
        List<StateItem> numbered = new ArrayList<>();
        for (var entry : where.entrySet()) {
            TrackedCharacter character = entry.getKey();
            user.append(character.getName().strip()).append(" (present in the messages: ")
                    .append(String.join(", ", entry.getValue().stream().map(n -> "#" + n).toList())).append(")\n");
            for (Category category : state.categoriesOf(character)) {
                user.append("  ").append(category.getName().strip()).append(" (").append(kind(category)).append(", ")
                        .append(category.isStrict() ? "strict" : "not strict").append(")");
                if (!category.getInstruction().isBlank()) {
                    user.append(": ").append(category.getInstruction().strip().replaceAll("\\s*\\R\\s*", " "));
                }
                user.append('\n');
                List<StateItem> current = character.active(category.getId());
                if (current.isEmpty()) {
                    user.append("    (nothing)\n");
                }
                for (StateItem item : current) {
                    numbered.add(item);
                    user.append("    ").append(numbered.size()).append(". ").append(item.getText().strip().replaceAll("\\s*\\R\\s*", " ")).append('\n');
                }
            }
        }

        user.append("\n[Messages]\n");
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            user.append('#').append(i + 1).append(" (POV ").append(StringUtils.defaultString(part.povCharacter()).strip())
                    .append("; present: ").append(StringUtils.defaultString(part.presentCharacters()).strip()).append(")\n")
                    .append(StringUtils.defaultString(part.text()).strip()).append("\n\n");
        }
        return new Request(instructions, user.toString().stripTrailing(), new Numbering(List.copyOf(numbered), List.copyOf(parts)),
                List.copyOf(where.keySet()), clock);
    }

    /** The kind of the category as the request names it. */
    static String kind(Category category) {
        return switch (category.getKind()) {
            case SINGLE -> "single";
            case LIST -> "list";
            case DESCRIPTION -> "description";
        };
    }
}
