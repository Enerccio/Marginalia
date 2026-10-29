package com.github.enerccio.marginalia.domain.service;

/**
 * A change of a folder that would change what a book in it (or below it) inherits, and an agent works on that book: the
 * lock of the book holds ({@link ManuscriptLockedException}), and this one names the book as well.
 */
public class FolderBookLockedException extends ManuscriptLockedException {

    private final String bookName;

    public FolderBookLockedException(String agentName, String bookName) {
        super("The book " + bookName + " is locked by the agent " + agentName + ", pause or stop its run to change the folder",
                agentName);
        this.bookName = bookName;
    }

    /**
     * Name of the book the agent works on, for the message shown to the user.
     */
    public String getBookName() {
        return bookName;
    }
}
