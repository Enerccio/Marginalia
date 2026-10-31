package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.repository.ManuscriptSizeRepository;

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

    void clearBook(String bookUuid) throws Exception;

    void clearAll() throws Exception;

}
