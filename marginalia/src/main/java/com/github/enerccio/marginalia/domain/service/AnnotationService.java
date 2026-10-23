package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AnnotationRepository;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Notes anchored to a passage of a part (see {@link Annotation}). Every method with a manuscript checks that the manuscript is
 * the current user's and the part (annotation) is a part (annotation) of it.
 * <p>
 * The passage of an annotation is a range of offsets in the text of the part, so every change of the text has to move the
 * ranges: the paths of the author's edits end in {@link #shift}, the others have a method of their own
 * ({@link #moveToPrevious}, {@link #relocate}, {@link #copyForBranch}).
 */
public interface AnnotationService extends ExtendableService<Annotation, AnnotationRepository> {

    /**
     * How many characters before and after the passage are kept with the annotation.
     */
    int CONTEXT_LENGTH = 60;

    /**
     * Annotates the passage [start, end) of the text of the part, or the part as a whole when the range is null. White space
     * at the edges of the passage is left out.
     *
     * @param type  free word, null for the default
     * @param label who wrote it in particular (the extension, the agent), null for none
     * @throws IllegalArgumentException when the range is not in the text, or the note is blank
     */
    Annotation create(Manuscript manuscript, ChatMessage part, Integer start, Integer end, String text, AnnotationSource source,
                      String type, String label) throws Exception;

    /**
     * Annotates the passage the quote is in the text of the part (see {@code QuoteFinder}); a quote that is not found makes an
     * annotation of the part as a whole, marked {@link Annotation#isNotLocated() not located}.
     *
     * @param before the text before the quote when the writer knows it (picks the place when the quote is there more than once), or null
     * @param after  the text after the quote, or null
     */
    Annotation createFromQuote(Manuscript manuscript, ChatMessage part, String quote, String before, String after, String text,
                               AnnotationSource source, String type, String label) throws Exception;

    /**
     * All the annotations of the part, whatever their status, in the order of their passages.
     */
    List<Annotation> listForPart(Manuscript manuscript, ChatMessage part) throws Exception;

    /**
     * All the annotations of the book (of the parts that are not deleted), part after part in the order they were made.
     */
    List<Annotation> listForManuscript(Manuscript manuscript) throws Exception;

    /**
     * Changes what the author may change of the annotation; null leaves the value as it is (a blank type is no type).
     */
    Annotation update(Manuscript manuscript, Annotation annotation, String text, String type, Boolean sendToPrompt) throws Exception;

    Annotation setStatus(Manuscript manuscript, Annotation annotation, AnnotationStatus status) throws Exception;

    /**
     * Attaches the annotation to another passage of its part (an orphaned annotation becomes open again), or to the part as
     * a whole when the range is null.
     *
     * @throws IllegalArgumentException when the range is not in the text
     */
    Annotation reattach(Manuscript manuscript, Annotation annotation, Integer start, Integer end) throws Exception;

    /**
     * Deletes the annotation (it can be restored from the trash).
     */
    void remove(Manuscript manuscript, Annotation annotation) throws Exception;

    // ---- keeping the ranges in line with the text

    /**
     * The text of the part was changed by the author: moves the passages of its annotations through the change. An edit
     * inside a passage grows or shrinks it, an edit before it moves it, text typed exactly at its start or end is outside of
     * it; a passage that is gone completely makes the annotation orphaned. Texts so different that they can't be followed
     * word by word are handled by the quote and its context.
     */
    void shift(Manuscript manuscript, ChatMessage part, String oldText, String newText) throws Exception;

    /**
     * The later part was merged into the previous one: its annotations belong to the previous part now.
     *
     * @param offset where the text of the later part starts in the text of the previous one
     */
    void moveToPrevious(Manuscript manuscript, ChatMessage later, ChatMessage previous, int offset) throws Exception;

    /**
     * The text of the part was replaced as a whole (the part was generated again): finds the passages of the annotations
     * again by their quotes and context; the ones that are not found are orphaned.
     */
    void relocate(Manuscript manuscript, ChatMessage part) throws Exception;

    /**
     * Gives the part {@code to}, a branch (copy) of {@code from}, the annotations {@code from} has.
     */
    void copyForBranch(ChatMessage from, ChatMessage to) throws Exception;

    // ---- deletes

    /**
     * Soft deletes the annotations of all parts of the manuscript, for the delete of the manuscript.
     */
    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the annotations of the parts with the ids, for the delete of the parts.
     */
    void softDeleteForParts(Collection<Long> partIds) throws Exception;

    /**
     * Removes the annotations of the parts with the ids from the database (deleted ones too), before the parts are.
     */
    void hardDeleteForParts(Collection<Long> partIds) throws Exception;

    // ---- backups

    /**
     * The annotations of the book as the backup holds them, by the id of the part.
     */
    Map<Long, JsonArray> marshalForManuscript(Manuscript manuscript) throws Exception;

    /**
     * Makes the annotations of the backup of a part for the restored part.
     */
    void restoreForPart(ChatMessage part, JsonArray annotations) throws Exception;

}
