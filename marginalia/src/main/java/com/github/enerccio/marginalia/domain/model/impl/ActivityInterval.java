package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.converter.LocalDateConverter;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

/**
 * A stretch of time during which a kind of activity went on in a book. The time is recorded as it happened, per book, and
 * combined only when it is shown ({@code TimeUnion}): five agents that run for 24 hours on five books are 24 hours on each
 * book and 24 hours in total, which a number per book and day could not tell. A stretch is cut at midnight, so a row is inside
 * one day; the length is {@code endTime - startTime}, and {@code endTime} is moved forward while the stretch is open. The
 * owner is the owner of the book; like {@link ManuscriptStatistic} it holds the book as a plain uuid and outlives it.
 * See {@code ActivityIntervalService}.
 */
@Entity
@Table(name = "activity_intervals")
public class ActivityInterval extends OwnedEntity {

    @Column(nullable = false, length = 36)
    private String bookUuid;

    @Column(length = 255)
    private String bookName;

    @Column(length = 2048)
    private String folderPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ActivityKind kind;

    /**
     * False for {@link ActivityKind#OPEN} and {@link ActivityKind#EDIT}; for the others whether the agents or the author
     * caused it.
     */
    @Column(name = "is_agent", nullable = false)
    private boolean agent;

    @Convert(converter = LocalDateConverter.class)
    @Column(nullable = false, length = 10)
    private LocalDate day;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date endTime;

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

    public ActivityKind getKind() {
        return kind;
    }

    public void setKind(ActivityKind kind) {
        this.kind = kind;
    }

    public boolean isAgent() {
        return agent;
    }

    public void setAgent(boolean agent) {
        this.agent = agent;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
