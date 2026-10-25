package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;

import java.util.List;

/**
 * Which lorebook entries a generation uses, and why: the rules of the generation step that puts the lore into the
 * prompt, so the step and the commands that preview it (an agent asking "what would activate for this text") agree.
 * <p>
 * An entry is used when it is enabled, no tag of the book is one of its negative tags, it has no tags or the book has one
 * of them, and its filter (a text or a regular expression, case does not matter) is found in the text of the turn - or it
 * has no filter, which is a constant entry. The entries are used in the order of their {@code order}, the older first.
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
        /** Not used, its filter is not in the text (or is a regular expression that is not valid). */
        FILTER_NOT_MATCHED,
        /** Used, its filter is found in the content of an entry that was used (the detail names the filter and the entry). */
        LORE_MATCHED,
        /** Used, an extension changed the verdict (the detail names why). */
        EXTENSION_ACTIVATED,
        /** Not used, an extension changed the verdict (the detail names why). */
        EXTENSION_SKIPPED;

        public boolean isActivated() {
            return this == CONSTANT || this == FILTER_MATCHED || this == LORE_MATCHED || this == EXTENSION_ACTIVATED;
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
     * How many times the content of used entries is searched for the filters of the others: the scan depth of the lorebook
     * the book uses, from 0 (never) to {@link #MAX_SCAN_DEPTH}.
     */
    int scanDepth(Lorebook root);

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
     * The text of the entry put into the prompt: its payload as a template. A payload that is not a valid template is
     * used as it is, a broken entry does not break the generation. Variables set in the payload stay in the data's
     * context, so a following entry sees them.
     */
    String renderPayload(LorebookEntry entry, LorebookTemplateData data);

}
