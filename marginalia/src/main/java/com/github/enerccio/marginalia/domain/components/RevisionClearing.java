package com.github.enerccio.marginalia.domain.components;

/**
 * What clearing the history of a part or a book removes (or would): how many versions, and the size of the texts they hold in
 * the database, in bytes (compressed).
 */
public record RevisionClearing(int versions, long bytes) {

    public static final RevisionClearing NOTHING = new RevisionClearing(0, 0);
}
