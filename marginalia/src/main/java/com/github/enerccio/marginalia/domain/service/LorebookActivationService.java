package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.LoreConditions;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;

import java.util.List;
import java.util.Map;

/**
 * Which lorebook entries a generation uses, and why: the rules of the generation step that puts the lore into the
 * prompt, so the step and the commands that preview it (an agent asking "what would activate for this text") agree.
 * <p>
 * An entry is used when it is enabled, no tag of the book is one of its negative tags, it has no tags or the book has one
 * of them, and its filter (a text or a regular expression, case does not matter) is found in the text of the turn - or it
 * has no filter, which is a constant entry. The entries are used in the order of their {@code order}, the older first.
 * <p>
 * An entry can be limited to some of the parts of the story ({@code fromMessage}, {@code toMessage} of the entry, the
 * numbers of the outline, both included, the first part is 1): it is used only for the turn that creates a part inside
 * them, whatever its filter says ({@link Reason#OUT_OF_RANGE}). {@link #turnPosition} gives the number of the part the turn
 * creates; without it (null) the limits are not checked.
 * <p>
 * An entry can also have a condition ({@code activationCondition}): a template, written like the content of an entry
 * (Handlebars and SillyTavern macros, {@code {{command::...}}} for commands that only read included), that is rendered for
 * the turn with the POV, the present characters, the instructions and the variables as they are. The entry is used only
 * when the result is true (not empty, not {@code 0}, {@code false}, {@code no}, {@code off}, any case). It is checked after
 * the tags and before the filter, so a false condition wins over a filter that matches, also for the scan depth. A
 * condition that cannot be rendered (not a valid template, a name that does not exist, the result {@code Error}, the time
 * budget of {@link LoreConditions} spent) is false and says why ({@link Reason#CONDITION_ERROR}). It is rendered on a copy
 * of the context, so it changes no variable, and at most once per entry and activation.
 * <p>
 * The filter is looked for in the instructions of the turn, or - when the entry's activation context is
 * {@code INCLUDE_PARTS} - in the instructions and the newest N parts of the story (N is the entry's
 * {@code activationParts}). {@link #partsNeeded} says how many parts the enabled entries ask for at most, so only that many
 * are read; the entry then uses the newest of the ones it is given.
 * <p>
 * Entries can also be used because of other entries (the scan depth of the lorebook the book uses): the content of the
 * entries that were used is searched for the filters of the entries that were not, and the ones it matches are used too,
 * and their content is searched in turn, up to the scan depth times. Only the filters count, a constant entry is used in
 * the first pass already. An entry can opt out of being searched ({@code preventRecursion}).
 */
public interface LorebookActivationService {

    /**
     * The most the scan depth of a lorebook counts for, whatever it says.
     */
    int MAX_SCAN_DEPTH = 5;

    /**
     * Why an entry is or is not used.
     */
    enum Reason {
        /** Used, the entry has no filter. */
        CONSTANT,
        /** Used, its filter is found in the text. */
        FILTER_MATCHED,
        /** Not used, the entry is switched off. */
        DISABLED,
        /** Not used, one of the book's tags is a negative tag of the entry. */
        NEGATIVE_TAG,
        /** Not used, the entry has tags and the book has none of them. */
        MISSING_TAG,
        /** Not used, the part the turn creates is outside the entry's range of parts (the detail names the range). */
        OUT_OF_RANGE,
        /** Not used, its filter is not in the text (or is a regular expression that is not valid). */
        FILTER_NOT_MATCHED,
        /** Not used by the turn itself, the entry is used only through the content of other entries (the detail is its filter). */
        RECURSION_ONLY,
        /** Not used, its condition is false (the detail is the condition). */
        CONDITION_FALSE,
        /** Not used, its condition could not be decided, which counts as false (the detail says why). */
        CONDITION_ERROR,
        /** Used, its filter is found in the content of an entry that was used (the detail names the filter and the entry). */
        LORE_MATCHED,
        /** Used, an extension changed the verdict (the detail names why). */
        EXTENSION_ACTIVATED,
        /** Not used, an extension changed the verdict (the detail names why). */
        EXTENSION_SKIPPED;

        public boolean isActivated() {
            return this == CONSTANT || this == FILTER_MATCHED || this == LORE_MATCHED || this == EXTENSION_ACTIVATED;
        }

        /**
         * Whether the content of the entries that were used can still make the entry used: its filter was not found, or it is
         * used only that way.
         */
        public boolean isScannable() {
            return this == FILTER_NOT_MATCHED || this == RECURSION_ONLY;
        }
    }

    /**
     * The decision on one entry.
     *
     * @param detail the tag that decided ({@code NEGATIVE_TAG}) or the filter ({@code FILTER_MATCHED},
     *               {@code FILTER_NOT_MATCHED}, {@code LORE_MATCHED} - then with the name of the entry), the reason an extension gave ({@code EXTENSION_ACTIVATED},
     *               {@code EXTENSION_SKIPPED}), null otherwise
     */
    record Verdict(Reason reason, String detail) {

        public boolean activated() {
            return reason.isActivated();
        }
    }

    /**
     * What the filters of the entries are looked for in besides the instructions of the turn: the text of the newest parts of
     * the story and, for the entries that ask for it ({@code activationSummaries}), the summaries of the story so far.
     *
     * @param parts     the text of the parts of the story the turn continues, the oldest first
     * @param summaries the summaries in use for the story so far, the oldest first, one text; empty when none is needed or there is none
     */
    record StoryText(List<String> parts, String summaries) {

        public static final StoryText NONE = new StoryText(List.of(), "");

        public StoryText {
            parts = parts == null ? List.of() : parts;
            summaries = summaries == null ? "" : summaries;
        }

        public static StoryText ofParts(List<String> parts) {
            return new StoryText(parts, "");
        }
    }

    /**
     * The lorebooks of a tree - the lorebook and its subbooks, each once, the ones that are deleted or switched off
     * (with their subbooks) left out - with their entries filled in ({@code getCachedEntries}, the tags of the entries).
     */
    List<Lorebook> collectLorebooks(Lorebook root) throws Exception;

    /**
     * The entries of the lorebooks in the order they are used: by {@code order}, the older first inside one.
     */
    List<LorebookEntry> orderedEntries(List<Lorebook> lorebooks);

    /**
     * Whether the entry is used for the text.
     *
     * @param manuscriptTags the values of the tags of the book
     * @param text           what the filters are looked for in: the user prompt of the turn
     */
    Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String text);

    /**
     * Whether the entry is used for the instructions and the parts of the story. The filter is looked for in the
     * instructions alone, or, for an entry with {@code INCLUDE_PARTS}, in the newest {@code activationParts} of the parts
     * and the instructions.
     *
     * @param manuscriptTags the values of the tags of the book
     * @param instructions   the user prompt of the turn
     * @param parts          the text of the parts of the story the turn continues, the oldest first (at least
     *                       {@link #partsNeeded} of the newest ones, fewer when the story is shorter)
     */
    Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, List<String> parts);

    /**
     * Whether the entry is used for the instructions and the parts of the story, and for the turn that creates the part
     * with the given number: an entry whose range of parts ({@code fromMessage}, {@code toMessage}) does not include it
     * is {@link Reason#OUT_OF_RANGE}, whatever its filter and tags say.
     *
     * @param position the number of the part the turn creates ({@link #turnPosition}), null when it is not known: the
     *                 range of parts is not checked then
     */
    Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, List<String> parts, Integer position);

    /**
     * {@link #evaluate(LorebookEntry, List, String, List, Integer)} with the context the condition of the entry is
     * rendered against.
     *
     * @param conditions the turn and the time budget for the conditions of the entries; null renders the conditions with
     *                   nothing known about the turn (no POV, no instructions...)
     */
    Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, List<String> parts, Integer position, LoreConditions conditions);

    /**
     * {@link #evaluate(LorebookEntry, List, String, List, Integer, LoreConditions)} with the summaries of the story as well: an
     * entry with {@code activationSummaries} looks for its filter in them too.
     */
    Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, StoryText story, Integer position, LoreConditions conditions);

    /**
     * What the entries look into besides the instructions, for the story that ends in the leaf: the newest parts as far back as
     * {@link #partsNeeded} says and, when an enabled entry has {@code activationSummaries}, the summaries in use (the ones that
     * still match the story; nothing is changed or asked).
     *
     * @param excludeLeaf see {@link #recentParts}
     */
    StoryText storyText(List<LorebookEntry> entries, ChatMessage leaf, boolean excludeLeaf) throws Exception;

    /**
     * Whether an enabled entry looks for its filter in the summaries.
     */
    boolean summariesNeeded(List<LorebookEntry> entries);

    /**
     * Whether an enabled entry has a condition, so {@link LoreConditions} has to be made for the activation.
     */
    boolean conditionsNeeded(List<LorebookEntry> entries);

    /**
     * The number of the part a turn creates, the first part of the branch being 1: one more than the parts of the branch
     * that ends in the leaf, or - with {@code excludeLeaf}, for a swipe, a regeneration or the preview of the turn of a
     * part - the number of the leaf itself. 1 without a leaf.
     */
    int turnPosition(ChatMessage leaf, boolean excludeLeaf) throws Exception;

    /**
     * Whether the position of the turn matters: an enabled entry has a range of parts. When it does not, there is no need
     * to count the parts of the story ({@link #turnPosition}).
     */
    boolean positionNeeded(List<LorebookEntry> entries);

    /**
     * How many of the newest parts of the story the entries look into: the most {@code activationParts} of an enabled
     * entry with {@code INCLUDE_PARTS}, 0 when there is none (no part has to be read then). Disabled entries do not count.
     */
    int partsNeeded(List<LorebookEntry> entries);

    /**
     * The text of the newest {@code count} parts of the story that end in the leaf (the leaf included unless
     * {@code excludeLeaf}), the oldest first; parts without a text are skipped. Empty for a count of 0 (nothing is
     * read) or without a leaf. Use {@link #partsNeeded} for the count.
     *
     * @param excludeLeaf the leaf is the part being replaced (a swipe, a regeneration), or the part whose turn is
     *                    previewed: it is not the story so far
     */
    List<String> recentParts(ChatMessage leaf, int count, boolean excludeLeaf) throws Exception;

    /**
     * How many times the content of used entries is searched for the filters of the others: the scan depth of the lorebook
     * the book uses, from 0 (never) to {@link #MAX_SCAN_DEPTH}.
     */
    int scanDepth(Lorebook root);

    /**
     * The scan depth for the entries of one lorebook of the tree: the scan depth of the root for the root itself and for a
     * sub lorebook without an own one, else the {@code ownScanDepth} of the sub lorebook (0 to {@link #MAX_SCAN_DEPTH}).
     */
    int scanDepth(Lorebook root, Lorebook book);

    /**
     * The scan depth of every lorebook of the tree, by the id of the lorebook.
     */
    Map<Long, Integer> scanDepths(Lorebook root, List<Lorebook> lorebooks);

    /**
     * The most times the content of used entries is searched: the greatest of the scan depths, how many passes are needed.
     */
    default int deepestScan(Map<Long, Integer> depths) {
        return depths.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /**
     * The scan depth that counts for the entry (its lorebook's), 0 when the lorebook is not known.
     */
    default int scanDepthOf(Map<Long, Integer> depths, LorebookEntry entry) {
        return entry.getLorebook() == null ? 0 : depths.getOrDefault(entry.getLorebook().getId(), 0);
    }

    /**
     * The entries whose content is searched for filters after a pass: the ones that were used, that may be searched
     * ({@code preventRecursion} is off) and that have a content.
     */
    List<LorebookEntry> scannable(List<LorebookEntry> used);

    /**
     * Whether an entry that was not used for the prompt is used because of the content of the entries that were. Does
     * the checks of {@link #evaluate}; an entry that matches is {@link Reason#LORE_MATCHED}, naming the first of the
     * entries that has the filter (several together can match too, then without a name). The text is the raw content of
     * the entries, templates are not rendered.
     *
     * @param sources the entries to search, {@link #scannable} of the last pass
     */
    Verdict evaluateInContent(LorebookEntry entry, List<String> manuscriptTags, List<LorebookEntry> sources);

    /**
     * {@link #evaluateInContent(LorebookEntry, List, List)} with the context for the conditions: the condition of the entry
     * counts here too (the answer given in the first pass is used, it is not rendered again).
     */
    Verdict evaluateInContent(LorebookEntry entry, List<String> manuscriptTags, List<LorebookEntry> sources, LoreConditions conditions);

    /**
     * The decision on every entry of the lorebooks for the text, scan depth included, without the events of a generation
     * (extensions do not get to change it), in the order the entries are used.
     *
     * @param root           the lorebook the book uses, its scan depth is used
     * @param lorebooks      {@link #collectLorebooks} of it
     * @param manuscriptTags the values of the tags of the book
     * @param text           the user prompt of the turn
     */
    List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text);

    /**
     * {@link #evaluateAll(Lorebook, List, List, String)} with the parts of the story for the entries that look into them.
     *
     * @param text  the user prompt of the turn
     * @param parts the text of the parts of the story, the oldest first (at least {@link #partsNeeded} of the newest)
     */
    List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, List<String> parts);

    /**
     * {@link #evaluateAll(Lorebook, List, List, String, List)} for the turn that creates the part with the given number.
     *
     * @param position the number of the part the turn creates ({@link #turnPosition}), null when it is not known
     */
    List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, List<String> parts, Integer position);

    /**
     * {@link #evaluateAll(Lorebook, List, List, String, List, Integer)} with the context for the conditions of the entries.
     */
    List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, List<String> parts, Integer position, LoreConditions conditions);

    /**
     * {@link #evaluateAll(Lorebook, List, List, String, List, Integer, LoreConditions)} with the summaries of the story as well.
     */
    List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, StoryText story, Integer position, LoreConditions conditions);

    /**
     * The text of the entry put into the prompt: its payload as a template. A payload that is not a valid template is
     * used as it is, a broken entry does not break the generation. Variables set in the payload stay in the data's
     * context, so a following entry sees them.
     */
    String renderPayload(LorebookEntry entry, LorebookTemplateData data);

    /**
     * The lore block ({@code IN_LORE_BLOCK} entries) the turn that wrote the part would get from the lorebooks as they are
     * now: the entries are activated for the turn stored with the part (its instructions, scene, point of view, who is
     * present, the story in front of it) and rendered, without the events of a generation. What the part was written with is
     * kept in its prompt and does not follow later changes of the lore; this does.
     *
     * @return the lore, empty when no entry is used; null when the book has no lorebook (nothing to refresh from)
     */
    String currentBackgroundLore(Manuscript manuscript, ChatMessage part) throws Exception;

}
