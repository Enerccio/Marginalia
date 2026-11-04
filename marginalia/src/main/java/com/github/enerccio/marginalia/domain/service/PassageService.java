package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.PassageRepository;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The anchored passages of all kinds (see {@link AnchoredPassage}): the creation and the lists of the passages of the
 * extensions' kinds, and the anchoring for every kind, the editorial notes included, which is what {@link AnnotationService}'s
 * {@code shift}, {@code relocate} and the others call. Every method with a manuscript checks that the manuscript is the current
 * user's and the part (passage) is a part (passage) of it.
 * <p>
 * An extension makes its passages with {@link #create} / {@link #createFromQuote} (a kind, the range or the quote), keeps its own
 * data in {@link AnchoredPassage#getAttributes()} and saves it with {@link #save}, and registers the {@link PassageKind} that
 * shows them with {@link PassageKinds}.
 */
public interface PassageService extends ExtendableService<AnchoredPassage, PassageRepository> {

    /**
     * Makes a passage of the kind over [start, end) of the text of the part, or of the part as a whole when the range is null.
     * White space at the edges is left out.
     *
     * @param kind {@link PassageKind#id()}
     * @throws IllegalArgumentException when the range is not in the text, or the kind is blank
     */
    AnchoredPassage create(Manuscript manuscript, ChatMessage part, String kind, Integer start, Integer end) throws Exception;

    /**
     * Makes a passage of the kind over the place the quote is in the text of the part (see {@code QuoteFinder}); a quote that is
     * not found makes a passage of the part as a whole, marked {@link AnchoredPassage#isNotLocated() not located}.
     *
     * @param before the text before the quote when the caller knows it (picks the place when the quote is there more than once), or null
     * @param after  the text after the quote, or null
     */
    AnchoredPassage createFromQuote(Manuscript manuscript, ChatMessage part, String kind, String quote, String before, String after)
            throws Exception;

    /**
     * The passages of the kind of the part (not the editorial notes), in the order of their ranges.
     *
     * @param kind {@link PassageKind#id()}, null for the passages of all the extensions' kinds
     */
    List<AnchoredPassage> listForPart(Manuscript manuscript, ChatMessage part, String kind) throws Exception;

    /**
     * The passages of the kind of the book (of the parts that are not deleted; not the editorial notes), part after part in the
     * order they were made.
     *
     * @param kind {@link PassageKind#id()}, null for the passages of all the extensions' kinds
     */
    List<AnchoredPassage> listForManuscript(Manuscript manuscript, String kind) throws Exception;

    /**
     * Attaches the passage to another place of its part (an orphaned passage is in the text again), or to the part as a whole
     * when the range is null.
     *
     * @throws IllegalArgumentException when the range is not in the text
     */
    AnchoredPassage reattach(Manuscript manuscript, AnchoredPassage passage, Integer start, Integer end) throws Exception;

    /**
     * Deletes the passage (it can be restored from the trash).
     */
    void remove(Manuscript manuscript, AnchoredPassage passage) throws Exception;

    // ---- keeping the ranges in line with the text, for every kind

    /**
     * The text of the part was changed by the author: moves the ranges of its passages through the change, see
     * {@link AnnotationService#shift}.
     */
    void shift(Manuscript manuscript, ChatMessage part, String oldText, String newText) throws Exception;

    /**
     * The later part was merged into the previous one: its passages belong to the previous part now.
     *
     * @param offset where the text of the later part starts in the text of the previous one
     */
    void moveToPrevious(Manuscript manuscript, ChatMessage later, ChatMessage previous, int offset) throws Exception;

    /**
     * The text of the part was replaced as a whole: finds the passages again by their quotes and context; the ones that are not
     * found are orphaned.
     */
    void relocate(Manuscript manuscript, ChatMessage part) throws Exception;

    /**
     * Gives the part {@code to}, a branch (copy) of {@code from}, the passages {@code from} has.
     */
    void copyForBranch(ChatMessage from, ChatMessage to) throws Exception;

    // ---- deletes

    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    void softDeleteForParts(Collection<Long> partIds) throws Exception;

    void hardDeleteForParts(Collection<Long> partIds) throws Exception;

    // ---- backups

    /**
     * The passages of the book as the backup holds them, by the id of the part; a passage that is not an editorial note has
     * {@code kind} {@code PASSAGE} and its {@code passageKind}, a backup of an older version has no {@code kind} and means editorial.
     */
    Map<Long, JsonArray> marshalForManuscript(Manuscript manuscript) throws Exception;

    /**
     * Makes the passages of the backup of a part for the restored part; the uuid each had in the backup and the one it got are
     * added to {@code pairs} (the working copy of an agent), when it is not null.
     */
    void restoreForPart(ChatMessage part, JsonArray passages, Collection<UuidPair> pairs) throws Exception;

}
