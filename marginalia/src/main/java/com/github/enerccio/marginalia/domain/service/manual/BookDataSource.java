package com.github.enerccio.marginalia.domain.service.manual;

/**
 * Where the help assistant's model reads the books of the user, see {@link BookHelpTools}. Only used when the user
 * allowed it. What it returns is sent to the AI that answers: folder names, titles, descriptions and the settings of the
 * books, never the text of a story.
 */
public interface BookDataSource {

    /**
     * A folder as Markdown: its description, its subfolders (with the number of books beneath each) and its books.
     *
     * @param path names from the top, separated by {@code /}; blank for the top level
     * @throws IllegalArgumentException when there is no such folder; the message names the folders that are there
     */
    String listFolder(String path) throws Exception;

    /**
     * The books whose title or description, or the text of whose story, has all the words of the query, as Markdown.
     * The text found is not returned, only where it was found.
     *
     * @param folder where to look (with the folders below it), blank for everywhere
     * @throws IllegalArgumentException when there is no such folder
     */
    String searchBooks(String query, String folder) throws Exception;

    /**
     * The settings a book writes with and where each comes from (the book, a folder, the user settings, the built-in
     * default), as Markdown.
     *
     * @param book the id of the book as the other functions print it, or its title, or {@code Folder/Sub/Title}
     * @throws IllegalArgumentException when there is no such book, or the title names several; the message lists them
     */
    String bookSettings(String book) throws Exception;
}
