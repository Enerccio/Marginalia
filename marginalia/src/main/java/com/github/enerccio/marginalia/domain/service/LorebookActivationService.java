package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;

import java.util.List;

/**
 * Which lorebook entries a generation uses, and why: the rules of the generation step that puts the lore into the
 * prompt, so the step and the commands that preview it (an agent asking "what would activate for this text") agree.
 * <p>
 * An entry is used when it is enabled, no tag of the book is one of its negative tags, it has no tags or the book has one
 * of them, and its filter (a text or a regular expression, case does not matter) is found in the text of the turn - or it
 * has no filter, which is a constant entry. The entries are used in the order of their {@code order}, the older first.
 */
public interface LorebookActivationService {

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
        FILTER_NOT_MATCHED;

        public boolean isActivated() {
            return this == CONSTANT || this == FILTER_MATCHED;
        }
    }

    /**
     * The decision on one entry.
     *
     * @param detail the tag that decided ({@code NEGATIVE_TAG}) or the filter ({@code FILTER_MATCHED},
     *               {@code FILTER_NOT_MATCHED}), null otherwise
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
     * The text of the entry put into the prompt: its payload as a template. A payload that is not a valid template is
     * used as it is, a broken entry does not break the generation. Variables set in the payload stay in the data's
     * context, so a following entry sees them.
     */
    String renderPayload(LorebookEntry entry, LorebookTemplateData data);

}
