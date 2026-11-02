package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.LoreUsageReport;
import com.github.enerccio.marginalia.domain.components.PartLoreUsage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

/**
 * Reads the lore usage that the generations recorded with the prompts of the parts ({@code MessagePrompt#getLoreUsage()})
 * and tells which entries of the lorebooks were used and which never were. Only the small record is read, never the text
 * of the prompts.
 */
public interface LoreUsageService {

    /**
     * The entries the prompt of the part used.
     *
     * @param part a part of a book of the current user; any other has no record
     */
    PartLoreUsage forPart(ChatMessage part) throws Exception;

    /**
     * Counts the entries over the parts of the book.
     *
     * @param manuscript a book of the current user
     * @param scope      the active branch or every part of the book
     * @param from       the first part number to count (1 is the first part of a branch), null or less than 1 for the start
     * @param to         the last part number to count, null or less than 1 for the end
     * @throws IllegalArgumentException when the book is not the user's
     */
    LoreUsageReport forManuscript(Manuscript manuscript, LoreUsageReport.Scope scope, Integer from, Integer to) throws Exception;
}
