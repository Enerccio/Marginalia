package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.converter.LocalDateConverter;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

/**
 * How big a book was at the end of a day, the latest value of the day: one per book and day, and none for a day on which the
 * size did not change (it is the last one before it). The growth of a day is the size of the day minus the size before it.
 * Words are the main measure, tokens the second one; tokens are never recounted, a part keeps the count it was written with.
 * Not split by side, the size is a property of the book. The owner is the owner of the book; like {@link ManuscriptStatistic}
 * it holds the book as a plain uuid and outlives it. See {@code ManuscriptSizeService}.
 */
@Entity
@Table(name = "manuscript_sizes")
public class ManuscriptSize extends OwnedEntity {

    @Column(nullable = false, length = 36)
    private String bookUuid;

    @Column(length = 255)
    private String bookName;

    @Column(length = 2048)
    private String folderPath;

    @Convert(converter = LocalDateConverter.class)
    @Column(nullable = false, length = 10)
    private LocalDate day;

    /**
     * The whole book: every part that is not deleted, all branches and alternatives included.
     */
    @Column(nullable = false)
    private long bookWords;

    @Column(nullable = false)
    private long bookTokens;

    /**
     * The current branch: the story from the first part to the active leaf.
     */
    @Column(nullable = false)
    private long branchWords;

    @Column(nullable = false)
    private long branchTokens;

    /**
     * The uuid of the active leaf the branch values were measured on, so a day on which the author switched to another branch
     * can be told from a day on which the branch grew.
     */
    @Column(length = 36)
    private String leafUuid;

    /**
     * Only says how big the book was when tracking began; the change of that day is unknown.
     */
    @Column(nullable = false)
    private boolean baseline;

    /**
     * Rebuilt from the history of the parts, so an estimate.
     */
    @Column(nullable = false)
    private boolean reconstructed;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastChange;

    public String getBookUuid() {
        return bookUuid;
    }

    public void setBookUuid(String bookUuid) {
        this.bookUuid = bookUuid;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public long getBookWords() {
        return bookWords;
    }

    public void setBookWords(long bookWords) {
        this.bookWords = bookWords;
    }

    public long getBookTokens() {
        return bookTokens;
    }

    public void setBookTokens(long bookTokens) {
        this.bookTokens = bookTokens;
    }

    public long getBranchWords() {
        return branchWords;
    }

    public void setBranchWords(long branchWords) {
        this.branchWords = branchWords;
    }

    public long getBranchTokens() {
        return branchTokens;
    }

    public void setBranchTokens(long branchTokens) {
        this.branchTokens = branchTokens;
    }

    public String getLeafUuid() {
        return leafUuid;
    }

    public void setLeafUuid(String leafUuid) {
        this.leafUuid = leafUuid;
    }

    public boolean isBaseline() {
        return baseline;
    }

    public void setBaseline(boolean baseline) {
        this.baseline = baseline;
    }

    public boolean isReconstructed() {
        return reconstructed;
    }

    public void setReconstructed(boolean reconstructed) {
        this.reconstructed = reconstructed;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }
}
