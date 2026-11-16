package com.github.enerccio.marginalia.extensions.statetracker.block;

import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateItem;
import com.github.enerccio.marginalia.extensions.statetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Builds the text that tells the model the current state of the characters of the scene. Only the tracked characters that are
 * in the present characters field get a part, and only their categories that have a value (or that say "none" when empty):
 *
 * <pre>
 * Fiora:
 * - Wearing: torn green cloak, leather boots
 * - Weapon: none
 * - Carrying: rope; lantern
 * </pre>
 *
 * When the book follows the time of the story, the block starts with it ({@code Story time: day 14 after the awakening, evening.}),
 * also when no tracked character is present.
 * <p>
 * The wording is the model-facing text of the plugin and is meant to be tuned against the models in use.
 */
public final class StateBlockBuilder {

    static final String HEADER = "The current state of the characters of this scene. Keep to it; it changes only when the story changes it:";
    static final String NONE = "none";

    private StateBlockBuilder() {
    }

    /**
     * The separators for the present characters field: the book's, else {@code null} (the built-in ones).
     */
    public static List<String> effectiveSeparators(StateBook state) {
        return state.getSettings().getSeparators().isEmpty() ? null : state.getSettings().getSeparators();
    }

    /**
     * @param presentField the text of the present characters field (or a hand-written list)
     * @return the block, empty when no tracked character with a state is present
     */
    public static String build(StateBook state, String presentField) {
        Collection<String> separators = effectiveSeparators(state);
        Set<String> present = PresentCharacters.normalizedNames(presentField, separators);
        List<String> parts = new ArrayList<>();
        for (TrackedCharacter character : state.getCharacters()) {
            if (PresentCharacters.isPresent(present, character.getName(), character.getAliases())) {
                String described = describe(state, character);
                if (!described.isEmpty()) {
                    parts.add(described);
                }
            }
        }
        String time = state.getClock().isEnabled() ? state.getClock().now(state).forBlock(state.getClock().getAnchor()) : "";
        if (parts.isEmpty()) {
            return time;
        }
        return (time.isEmpty() ? "" : time + "\n\n") + HEADER + "\n\n" + String.join("\n\n", parts);
    }

    /** The lines of one character, empty when it has nothing to say. */
    static String describe(StateBook state, TrackedCharacter character) {
        List<String> lines = new ArrayList<>();
        for (Category category : state.categoriesOf(character)) {
            List<String> values = character.active(category.getId()).stream()
                    .map(StateItem::getText)
                    .map(StateBlockBuilder::withoutFinalStop)
                    .filter(text -> !text.isEmpty())
                    .toList();
            if (values.isEmpty() && !category.isShowWhenEmpty()) {
                continue;
            }
            String value = values.isEmpty() ? NONE : String.join("; ", values);
            lines.add("- " + category.getName().strip() + ": " + value);
        }
        if (lines.isEmpty()) {
            return "";
        }
        return character.getName().strip() + ":\n" + String.join("\n", lines);
    }

    /** A value is followed by other values or ends a line, so its own full stop is dropped. */
    private static String withoutFinalStop(String text) {
        String stripped = text.strip().replaceAll("\\s*\\R\\s*", " ");
        while (!stripped.isEmpty() && ".。".indexOf(stripped.charAt(stripped.length() - 1)) >= 0) {
            stripped = stripped.substring(0, stripped.length() - 1).stripTrailing();
        }
        return stripped;
    }
}
