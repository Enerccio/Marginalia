package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.T;

/**
 * The request that asks the model what the tracked characters learned in a few parts of the story. The wording is the
 * one that was tested against a real model (see TODO.md); change it together with that test.
 */
public final class ExtractionPrompt {


    /**
     * The story the model may read from: the parts of the active branch, of which the first {@code readable} (the ones before the
     * run) can be read. Part {@code n} is {@code branch.get(n - 1)}, the number the tab shows.
     */
    public record Readable(List<Part> branch, int readable) {

        /** {@code null} when nothing comes before the run, so that there is nothing to read. */
        public static Readable of(List<Part> branch, List<Part> run) {
            if (run.isEmpty()) {
                return null;
            }
            int first = position(branch, run.getFirst().uuid());
            return first > 1 ? new Readable(branch, first - 1) : null;
        }

        /** The number of the part, 0 when it is not in the branch. */
        public int positionOf(String uuid) {
            return position(branch, uuid);
        }

        private static int position(List<Part> branch, String uuid) {
            for (int i = 0; i < branch.size(); i++) {
                if (branch.get(i).uuid().equals(uuid)) {
                    return i + 1;
                }
            }
            return 0;
        }
    }

    /**
     * How the numbers of the request map back to the state: the items of every tracked character in the request, in the
     * order they were numbered (from 1), and the parts the messages were made of.
     *
     * @param items the id of the item of number {@code n + 1}, by the id of the tracked character
     * @param parts the parts of the run, message number {@code n + 1} is {@code parts.get(n)}
     */
    public record Numbering(Map<String, List<String>> items, List<Part> parts) {
    }

    /**
     * @param system   the system message
     * @param user     the user message
     * @param numbering what the numbers in them stand for
     * @param included the tracked characters that are in the request (present in at least one of the parts)
     */
    public record Request(String system, String user, Numbering numbering, List<TrackedCharacter> included) {

        /** Whether there is anything to ask: a tracked character is present in the parts. */
        public boolean isEmpty() {
            return included.isEmpty();
        }
    }

    private ExtractionPrompt() {
    }

    /**
     * @param lore       the world text, may be empty
     * @param parts      the parts to read, in order
     * @param separators the separators of the present characters field, {@code null} for the built-in ones
     */
    public static Request build(KnowledgeState state, String lore, List<Part> parts, Collection<String> separators) {
        return build(state, lore, parts, separators, null);
    }

    /**
     * @param readable what the model may read ({@code read_part}), {@code null} for nothing: the items that were learned in a part it
     *                 can read say so, the messages say their part and the system message tells about the function
     */
    public static Request build(KnowledgeState state, String lore, List<Part> parts, Collection<String> separators, Readable readable) {
        return build(state, lore, parts, separators, readable, T.defaults().extractionSystem());
    }

    /**
     * @param system the system message, the one the book inherits; blank = the built-in {@link KnowledgeTrackerDefaults#extractionSystem()}. The instructions about
     *               {@code read_part} are added to it when {@code readable} is given.
     */
    public static Request build(KnowledgeState state, String lore, List<Part> parts, Collection<String> separators, Readable readable,
                                String system) {
        String instructions = StringUtils.isBlank(system) ? T.defaults().extractionSystem() : system.strip();
        List<Set<String>> present = new ArrayList<>();
        for (Part part : parts) {
            present.add(PresentCharacters.normalizedNames(part.presentCharacters(), separators));
        }

        // a tracked character that is in none of the parts perceives nothing in them: it is not asked about
        Map<TrackedCharacter, List<Integer>> where = new LinkedHashMap<>();
        for (TrackedCharacter character : state.getCharacters()) {
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
        user.append("[Tracked characters]\n");
        Map<String, List<String>> numbered = new LinkedHashMap<>();
        for (var entry : where.entrySet()) {
            TrackedCharacter character = entry.getKey();
            user.append(character.getName().strip()).append(" (present in the messages: ")
                    .append(String.join(", ", entry.getValue().stream().map(n -> "#" + n).toList())).append(")\n");
            List<String> ids = new ArrayList<>();
            for (KnowledgeItem item : character.activeItems()) {
                ids.add(item.getId());
                user.append("  ").append(ids.size()).append(". ")
                        .append(item.getKind() == ItemKind.KNOWS ? "knows: " : "does not know: ")
                        .append(item.getText().strip());
                int learnedIn = readable != null && item.getSourceMessage() != null ? readable.positionOf(item.getSourceMessage()) : 0;
                if (learnedIn > 0 && learnedIn <= readable.readable()) {
                    user.append(" (learned in part ").append(learnedIn).append(')');
                }
                user.append('\n');
            }
            if (ids.isEmpty()) {
                user.append("  (nothing is written yet)\n");
            }
            numbered.put(character.getId(), ids);
        }

        user.append("\n[Messages]\n");
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            user.append('#').append(i + 1).append(readable != null ? " (part " + readable.positionOf(part.uuid()) + "; POV " : " (POV ").append(StringUtils.defaultString(part.povCharacter()).strip())
                    .append("; present: ").append(StringUtils.defaultString(part.presentCharacters()).strip()).append(")\n")
                    .append(StringUtils.defaultString(part.text()).strip()).append("\n\n");
        }
        return new Request(readable != null ? instructions + "\n\n" + T.defaults().readingNote() : instructions, user.toString().stripTrailing(), new Numbering(numbered, List.copyOf(parts)),
                List.copyOf(where.keySet()));
    }
}
