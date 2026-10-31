package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.time.LocalDate;
import java.util.List;

public interface ManuscriptSizeRepository extends OwnedRepository<ManuscriptSize> {

    /**
     * @return the row of the book and day of the user, null if there is none
     */
    ManuscriptSize find(User owner, String bookUuid, LocalDate day) throws Exception;

    /**
     * @return the size row of the book at the end of the day: the latest one that is not after it, null if there is none
     */
    ManuscriptSize findAtOrBefore(User owner, String bookUuid, LocalDate day) throws Exception;

    /**
     * @return the size row of the book before the day: the latest one that is earlier, null if there is none
     */
    ManuscriptSize findBefore(User owner, String bookUuid, LocalDate day) throws Exception;

    /**
     * The latest row of every book of the user that is not after the day.
     */
    List<ManuscriptSize> findAllAtOrBefore(User owner, LocalDate day) throws Exception;

    /**
     * The rows of the days from-to (both included), the oldest first, of one book or (null) of all of them.
     */
    List<ManuscriptSize> findRange(User owner, String bookUuid, LocalDate from, LocalDate to) throws Exception;

    int refreshBook(User owner, String bookUuid, String bookName, String folderPath) throws Exception;

    int deleteByBook(User owner, String bookUuid) throws Exception;

    int deleteAll(User owner) throws Exception;

}
