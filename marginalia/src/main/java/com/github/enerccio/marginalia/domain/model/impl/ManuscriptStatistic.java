package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.converter.LocalDateConverter;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

/**
 * What happened to a book on a day: the tokens spent on it, the words written, generated and removed, the parts made and
 * deleted, the edits. At most two per book and day, the author's and the agents' ({@link #isAgent()}). The owner is the owner
 * of the book.
 * <p>
 * The statistics are the author's record of their work, not a part of the book: the book is a plain {@code bookUuid}, not a
 * reference, so the rows outlive it (the trash and the purge of the book leave them) and are cleared only by the user.
 * Counters are real columns because they are added up in SQL ({@code ManuscriptStatisticRepository#add}), not read and
 * saved, so parallel generations and an agent don't lose counts. The time is not here but in {@link ActivityInterval}.
 */
@Entity
@Table(name = "manuscript_statistics")
public class ManuscriptStatistic extends OwnedEntity {

    /**
     * The uuid of the book, not a foreign key.
     */
    @Column(nullable = false, length = 36)
    private String bookUuid;

    /**
     * The name of the book, a copy kept current while the book exists, the last one after it is gone.
     */
    @Column(length = 255)
    private String bookName;

    /**
     * The path of the folder of the book, empty for a book in no folder; kept current like {@link #bookName}.
     */
    @Column(length = 2048)
    private String folderPath;

    @Convert(converter = LocalDateConverter.class)
    @Column(nullable = false, length = 10)
    private LocalDate day;

    /**
     * False: what the author did (with the models they asked). True: what the agents did on the book.
     */
    @Column(name = "is_agent", nullable = false)
    private boolean agent;

    @Column(nullable = false)
    private long prefillTokens;

    @Column(nullable = false)
    private long generatedTokens;

    @Column(nullable = false)
    private long requests;

    @Column(nullable = false)
    private long wordsWritten;

    @Column(nullable = false)
    private long wordsGenerated;

    @Column(nullable = false)
    private long wordsRemoved;

    @Column(nullable = false)
    private long partsCreated;

    @Column(nullable = false)
    private long partsDeleted;

    @Column(nullable = false)
    private long edits;

    /**
     * Rebuilt from the history of the parts instead of counted live, so an estimate.
     */
    @Column(nullable = false)
    private boolean reconstructed;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastChange;

    public StatisticCounts getCounts() {
        return new StatisticCounts(prefillTokens, generatedTokens, requests, wordsWritten, wordsGenerated, wordsRemoved,
                partsCreated, partsDeleted, edits);
    }

    public void setCounts(StatisticCounts counts) {
        prefillTokens = counts.prefillTokens();
        generatedTokens = counts.generatedTokens();
        requests = counts.requests();
        wordsWritten = counts.wordsWritten();
        wordsGenerated = counts.wordsGenerated();
        wordsRemoved = counts.wordsRemoved();
        partsCreated = counts.partsCreated();
        partsDeleted = counts.partsDeleted();
        edits = counts.edits();
    }

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

    public boolean isAgent() {
        return agent;
    }

    public void setAgent(boolean agent) {
        this.agent = agent;
    }

    public long getPrefillTokens() {
        return prefillTokens;
    }

    public long getGeneratedTokens() {
        return generatedTokens;
    }

    public long getRequests() {
        return requests;
    }

    public long getWordsWritten() {
        return wordsWritten;
    }

    public long getWordsGenerated() {
        return wordsGenerated;
    }

    public long getWordsRemoved() {
        return wordsRemoved;
    }

    public long getPartsCreated() {
        return partsCreated;
    }

    public long getPartsDeleted() {
        return partsDeleted;
    }

    public long getEdits() {
        return edits;
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
