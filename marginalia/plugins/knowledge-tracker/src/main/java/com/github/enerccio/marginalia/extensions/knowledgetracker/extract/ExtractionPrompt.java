package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The request that asks the model what the tracked characters learned in a few parts of the story. The wording is the
 * one that was tested against a real model (see TODO.md); change it together with that test.
 */
public final class ExtractionPrompt {

    /** The system message. */
    public static final String SYSTEM = """
            You keep track of what the characters of a story know. You get the world (lorebook, when given), the TRACKED characters with the numbered items of what each knows or does not know now, and the new story MESSAGES (numbered). Report, for each tracked character, only what changed in these messages.

            Rules:
            1. add: knowledge the character GAINS in these messages: what it saw, heard, read, was told or worked out itself. Not what the reader knows, not what only the narrator says, not what other characters know.
            2. Do NOT add global knowledge that a member of that world has anyway (who the king is for a subject of the kingdom, common customs, the language, the geography of their own home), and nothing that follows from the character itself as the world text describes it (its species, body, abilities, history, family, trade).
            3. Do NOT add anything the items of the character already say.
            4. retract: the number of an item that the messages clearly contradict (for example "has never met X" after they meet). Only when the text shows the contradiction. An item saying a character does not know Y is retracted only if the character learns Y in these messages. An item that says more than one thing is retracted only when the messages contradict all of it; when only a part is contradicted, leave the item and add what was learned. Judge an item by how the messages END: when the character is told, reminded of or works out again what the item says later in these messages (it asks who someone is, and is then told), the item is true at the end, so do not retract it and do not add it again.
            5. Never infer. When in doubt, report nothing. A character that is not present, or does not see or hear an event (private thoughts, whispers, a closed letter), learns nothing from it.
            6. Every change needs an evidence quote copied from the messages and the number of the message.
            7. Only record knowledge that could matter later in the story: secrets, identities, relationships and feelings that were revealed, plans, promises, discoveries, and facts that another character does not know. Do NOT record events that every character present saw or heard together (the story text itself holds them), small talk, jokes, moods, weather, objects and details of the setting, what a character did itself, or the same fact for several characters. At most 3 additions per character, usually 0 or 1. When nothing matters, report nothing.

            Answer with JSON only, no other text: {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. A character with no changes has empty lists.""";

    /** Added to the system message when the model may read earlier parts ({@code read_part}). */
    public static final String READING = """
            You may also call read_part with the number of an earlier part of the story. The items that were learned in an earlier part say so ("learned in part N") and the messages say which part they are. When an item came from hearsay, a rumour, a plan or an introduction and these messages seem to confirm, repeat or contradict it, or the item alone does not tell you what the character knew at that point, read that part first and then decide, instead of guessing. Do not read parts for any other reason (at most %d). Your final answer is still the JSON only.""".formatted(PartReader.MAX_READS);

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
        return build(state, lore, parts, separators, readable, SYSTEM);
    }

    /**
     * @param system the system message, the one the book inherits; blank = the built-in {@link #SYSTEM}. The instructions about
     *               {@code read_part} are added to it when {@code readable} is given.
     */
    public static Request build(KnowledgeState state, String lore, List<Part> parts, Collection<String> separators, Readable readable,
                                String system) {
        String instructions = StringUtils.isBlank(system) ? SYSTEM : system.strip();
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
        return new Request(readable != null ? instructions + "\n\n" + READING : instructions, user.toString().stripTrailing(), new Numbering(numbered, List.copyOf(parts)),
                List.copyOf(where.keySet()));
    }
}
