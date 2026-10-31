package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.SizeGrowth;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.repository.ManuscriptSizeRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * How big a book was at the end of each day ({@link ManuscriptSize}), in words and tokens, of the whole book and of the current
 * branch. A day on which the size did not change has no row, the size of that day is the last one before it. The writes work on
 * a thread without a session, the owner of a row is the owner of the book.
 */
public interface ManuscriptSizeService extends OwnedService<ManuscriptSize, ManuscriptSizeRepository> {

    /**
     * Sets the size of the book at the end of the day (today, as far as the caller knows): updates the row of the day or makes
     * it. Never throws: a failure to measure is logged and must not fail the work that changed the size.
     *
     * @param leafUuid the active leaf the branch values were measured on, null if the book has none
     * @param baseline whether the row only says how big the book was when tracking began
     */
    void set(Manuscript book, LocalDate day, long bookWords, long bookTokens, long branchWords, long branchTokens,
             String leafUuid, boolean baseline);

    /**
     * Says that the size of the book may have changed: a coalescing worker measures it a moment after the last call (a burst of
     * edits, or the end of a generation, is one measurement), loads the book by its id, adds up what its parts have saved (the word
     * and token counts of the parts: no tokenizer is called, so a change of the model leaves the stored numbers alone) and sets the row
     * of today. Cheap enough to call from a transaction, never throws, works on a thread without a session. A book an agent works on
     * (its hidden working copy) and a book in the trash are not measured.
     */
    void touch(Manuscript book);

    /**
     * The same for a book known by its id.
     */
    void touch(Long bookId);

    /**
     * @param born the book was just made (new, duplicated, branched, imported): when it has no row yet its first row is not a
     *             baseline, so the growth from nothing is the first day's
     */
    void touch(Manuscript book, boolean born);

    /**
     * Measures the book at once instead of in a moment: for a book that is being closed.
     */
    void touchNow(Manuscript book);

    /**
     * The same for a book known by its id.
     */
    void touchNow(Long bookId);

    /**
     * Measures everything that was touched and not measured yet. For the end of a work that needs the rows, the shutdown and tests.
     */
    void flush();

    /**
     * How much the book of the current user grew in the days from-to (both included): the size at the end of the range minus the
     * size before it, the latest row that is earlier. A book that has no row before the range grew from nothing; when its only
     * earlier knowledge is a baseline row there is nothing to show ({@link SizeGrowth#known()} is false). A range with no change is zero,
     * the carried size of a day without a row is the one before it.
     */
    SizeGrowth growth(String bookUuid, LocalDate from, LocalDate to) throws Exception;

    /**
     * @return the size of the book of the current user at the end of the day: the latest row that is not after it, null if the
     * book has none
     */
    ManuscriptSize sizeAt(String bookUuid, LocalDate day) throws Exception;

    /**
     * @return the size of the book of the current user before the day: the latest row that is earlier, null if there is none
     */
    ManuscriptSize sizeBefore(String bookUuid, LocalDate day) throws Exception;

    /**
     * The latest size of every book of the current user that is not after the day.
     */
    List<ManuscriptSize> sizesAt(LocalDate day) throws Exception;

    /**
     * The rows of the days from-to (both included) of the current user, the oldest first, of one book or (null) of all of them.
     */
    List<ManuscriptSize> findRange(String bookUuid, LocalDate from, LocalDate to) throws Exception;

    /**
     * Makes the name and the folder path of all the rows of the book the given ones; only the rows that differ are written.
     *
     * @param folderPath null leaves the path as it is
     */
    void refreshBook(User owner, String bookUuid, String bookName, String folderPath) throws Exception;

    void clearBook(String bookUuid) throws Exception;

    void clearAll() throws Exception;

}
