package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.BranchSize;

import java.util.List;
import java.util.Objects;

/**
 * A row of the list of books: a folder or a book, with what the list shows besides the entity (the tags, the total number
 * of books of a folder, the size of the active branch of a book, the path of its folder). The provider fills them for the
 * rows of a page.
 */
public final class FolderBookRow {

    private final Folder folder;
    private final Manuscript book;
    // the virtual folder of the favorite books, which has neither a folder nor a book
    private final String favoritesName;

    private List<String> tags = List.of();
    private long books;
    private BranchSize size;
    private String path = "";

    private boolean favorite;

    FolderBookRow(Folder folder, Manuscript book) {
        this(folder, book, null);
    }

    private FolderBookRow(Folder folder, Manuscript book, String favoritesName) {
        this.folder = folder;
        this.book = book;
        this.favoritesName = favoritesName;
    }

    /**
     * The row of the virtual folder with the favorite books ({@link #getBooks()} is how many there are).
     */
    static FolderBookRow favorites(String name, long books) {
        FolderBookRow row = new FolderBookRow(null, null, name);
        row.books = books;
        return row;
    }

    /**
     * Whether this is the row of the virtual folder with the favorite books.
     */
    public boolean isFavorites() {
        return favoritesName != null;
    }

    public boolean isFolder() {
        return folder != null;
    }

    /**
     * The folder of a folder row, null for a book.
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * The book of a book row, null for a folder.
     */
    public Manuscript getBook() {
        return book;
    }

    public String getName() {
        return favoritesName != null ? favoritesName : folder != null ? folder.getName() : book.getName();
    }

    /**
     * Whether the book of a book row is marked as a favorite.
     */
    public boolean isFavorite() {
        return favorite;
    }

    void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public List<String> getTags() {
        return tags;
    }

    void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * The number of books below the folder, all levels (folder rows).
     */
    public long getBooks() {
        return books;
    }

    void setBooks(long books) {
        this.books = books;
    }

    /**
     * The size of the active branch (book rows).
     */
    public BranchSize getSize() {
        return size;
    }

    void setSize(BranchSize size) {
        this.size = size;
    }

    /**
     * The path of the folder the row is in (the row itself for a folder is not part of it), empty at the top level.
     */
    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderBookRow that)) return false;
        return isFolder() == that.isFolder() && isFavorites() == that.isFavorites() && Objects.equals(key(), that.key());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFolder(), isFavorites(), key());
    }

    private Long key() {
        return folder != null ? folder.getId() : book != null ? book.getId() : null;
    }
}
