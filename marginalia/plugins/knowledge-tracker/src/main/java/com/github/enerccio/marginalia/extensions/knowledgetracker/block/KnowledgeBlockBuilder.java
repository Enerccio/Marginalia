package com.github.enerccio.marginalia.extensions.knowledgetracker.block;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.MetPerson;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the text that tells the model what the characters of the scene know. Only the tracked characters that are in
 * the present characters field get a part, so nothing extra reaches the model.
 * <p>
 * The wording is the model-facing text of the plugin and is meant to be tuned against the models in use.
 */
public final class KnowledgeBlockBuilder {

    static final String HEADER = "What the characters of this scene know. A character acts only on the knowledge listed for it:";

    private KnowledgeBlockBuilder() {
    }

    /**
     * The separators for the present characters field: the book's, else the user's, else {@code null} (the built-in
     * ones).
     */
    public static List<String> effectiveSeparators(KnowledgeState state, Collection<String> userSeparators) {
        if (!state.getSettings().getSeparators().isEmpty()) {
            return state.getSettings().getSeparators();
        }
        return userSeparators == null || userSeparators.isEmpty() ? null : List.copyOf(userSeparators);
    }

    /**
     * @param presentField the text of the present characters field (or a hand-written list)
     * @param separators   see {@link #effectiveSeparators}
     * @return the block, empty when no tracked character is present
     */
    public static String build(KnowledgeState state, String presentField, Collection<String> separators) {
        List<String> shown = PresentCharacters.parse(presentField, separators);
        Set<String> present = PresentCharacters.normalizedNames(presentField, separators);
        List<String> parts = new ArrayList<>();
        for (TrackedCharacter character : state.getCharacters()) {
            if (PresentCharacters.isPresent(present, character.getName(), character.getAliases())) {
                parts.add(describe(character, shown, state.getSettings().isClosedWorld()));
            }
        }
        if (parts.isEmpty()) {
            return "";
        }
        return HEADER + "\n\n" + String.join("\n\n", parts);
    }

    private static String describe(TrackedCharacter character, List<String> shown, boolean closedWorld) {
        String name = character.getName().strip();
        List<String> lines = new ArrayList<>();
        addLine(lines, name + " knows: ", texts(character, ItemKind.KNOWS));
        addLine(lines, name + " does not know: ", texts(character, ItemKind.DOES_NOT_KNOW));
        addLine(lines, name + " has met: ", character.getMet().stream().map(MetPerson::getName).toList());
        addLine(lines, name + " is currently with: ", others(character, shown));
        if (closedWorld) {
            lines.add(name + " has never met or heard of anyone else in this story and knows nothing else of its "
                    + "secrets, plans or events.");
        }
        return String.join("\n", lines);
    }

    private static List<String> texts(TrackedCharacter character, ItemKind kind) {
        return character.activeItems().stream()
                .filter(item -> item.getKind() == kind)
                .map(KnowledgeItem::getText)
                .map(KnowledgeBlockBuilder::withoutFinalStop)
                .filter(text -> !text.isEmpty())
                .toList();
    }

    /** The other names of the field, once each, not the character itself. */
    private static List<String> others(TrackedCharacter character, List<String> shown) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String name : shown) {
            if (!character.isCalled(name)) {
                unique.putIfAbsent(PresentCharacters.normalize(name), name);
            }
        }
        return new ArrayList<>(unique.values());
    }

    /** An item is joined with "; " to the others, so its own full stop would read ".;". */
    private static String withoutFinalStop(String text) {
        String stripped = text.strip();
        while (!stripped.isEmpty() && ".。".indexOf(stripped.charAt(stripped.length() - 1)) >= 0) {
            stripped = stripped.substring(0, stripped.length() - 1).stripTrailing();
        }
        return stripped;
    }

    private static void addLine(List<String> lines, String prefix, List<String> values) {
        if (values.isEmpty()) {
            return;
        }
        boolean items = prefix.endsWith("knows: ") || prefix.endsWith("not know: ");
        String text = values.stream().collect(Collectors.joining(items ? "; " : ", "));
        lines.add(prefix + (items ? sentence(text) : text + "."));
    }

    private static String sentence(String text) {
        char last = text.charAt(text.length() - 1);
        return ".!?。！？".indexOf(last) >= 0 ? text : text + ".";
    }
}
