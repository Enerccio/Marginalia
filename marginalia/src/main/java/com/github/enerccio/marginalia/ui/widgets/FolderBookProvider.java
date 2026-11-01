package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.BranchSize;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Stream;

/**
 * The rows of the list of books: the folders first (already in the order of the grid's sorters, they are few), then the
 * books, which are read page by page from their ids. What is dear to compute (tags, the size of the active branch of a book,
 * the number of books of a folder) is read for the rows of a page only, with one query for the page each.
 */
@Configurable
public class FolderBookProvider extends AbstractBackEndDataProvider<FolderBookRow, Void> {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private Localization loc;

    private final List<Folder> folders;
    private final List<Long> bookIds;
    private final Map<Long, Long> bookCounts;
    private final Map<Long, String> paths;
    private final Set<String> favorites;
    // the label of the row of the virtual folder of the favorites and how many there are, null when the row is not shown
    private final String favoritesLabel;

    /**
     * @param folders    the folder rows, in order
     * @param bookIds    the ids of the book rows, in order
     * @param bookCounts the total number of books of each folder (see {@code FolderService.countBooksByFolder})
     * @param paths      the path of each folder by id (see {@code FolderService.getPaths}), for the path of the row
     */
    public FolderBookProvider(List<Folder> folders, List<Long> bookIds, Map<Long, Long> bookCounts, Map<Long, String> paths) {
        this(folders, bookIds, bookCounts, paths, Set.of(), null);
    }

    /**
     * @param favorites      the uuids of the favorite books, their rows show a filled star
     * @param favoritesLabel the name of the virtual folder of the favorites, shown as the first row (with the number of
     *                       favorites as its books); null for no such row
     */
    public FolderBookProvider(List<Folder> folders, List<Long> bookIds, Map<Long, Long> bookCounts, Map<Long, String> paths,
                              Collection<String> favorites, String favoritesLabel) {
        this.folders = List.copyOf(folders);
        this.bookIds = List.copyOf(bookIds);
        this.bookCounts = bookCounts;
        this.paths = paths;
        this.favorites = Set.copyOf(favorites);
        this.favoritesLabel = favoritesLabel;
    }

    public int getFolderCount() {
        return folders.size();
    }

    public int getBookCount() {
        return bookIds.size();
    }

    @Override
    protected Stream<FolderBookRow> fetchFromBackEnd(Query<FolderBookRow, Void> query) {
        try {
            int leading = favoritesLabel == null ? 0 : 1;
            int to = Math.min(query.getOffset() + query.getLimit(), leading + folders.size() + bookIds.size());
            List<FolderBookRow> rows = new ArrayList<>();
            for (int i = query.getOffset(); i < to; i++) {
                if (i < leading) {
                    rows.add(FolderBookRow.favorites(favoritesLabel, favorites.size()));
                } else if (i - leading < folders.size()) {
                    Folder folder = folders.get(i - leading);
                    FolderBookRow row = new FolderBookRow(folder, null);
                    row.setBooks(bookCounts.getOrDefault(folder.getId(), 0L));
                    row.setPath(folder.getParent() == null ? "" : paths.getOrDefault(folder.getParent().getId(), ""));
                    rows.add(row);
                } else {
                    Manuscript book = manuscriptService.find(bookIds.get(i - leading - folders.size()));
                    FolderBookRow row = new FolderBookRow(null, book);
                    row.setFavorite(favorites.contains(book.getUuid()));
                    row.setPath(book.getFolder() == null ? "" : paths.getOrDefault(book.getFolder().getId(), ""));
                    rows.add(row);
                }
            }
            fill(rows);
            return rows.stream();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return Stream.empty();
        }
    }

    private void fill(List<FolderBookRow> rows) throws Exception {
        rows = rows.stream().filter(row -> !row.isFavorites()).toList();
        List<Long> pageBooks = rows.stream().filter(row -> !row.isFolder()).map(row -> row.getBook().getId()).toList();
        List<Long> pageFolders = rows.stream().filter(FolderBookRow::isFolder).map(row -> row.getFolder().getId()).toList();
        Map<Long, List<Tag>> bookTags = pageBooks.isEmpty() ? Map.of() : tagRelationService.getTagsForObjects(pageBooks, Manuscript.class);
        Map<Long, List<Tag>> folderTags = pageFolders.isEmpty() ? Map.of() : tagRelationService.getTagsForObjects(pageFolders, Folder.class);
        Map<Long, BranchSize> sizes = pageBooks.isEmpty() ? Map.of() : chatMessageService.getActiveBranchSizes(pageBooks);
        for (FolderBookRow row : rows) {
            Long id = row.isFolder() ? row.getFolder().getId() : row.getBook().getId();
            List<Tag> tags = (row.isFolder() ? folderTags : bookTags).getOrDefault(id, List.of());
            row.setTags(tags.stream().map(Tag::getValue).sorted().toList());
            if (!row.isFolder()) {
                row.setSize(sizes.get(id));
            }
        }
    }

    @Override
    protected int sizeInBackEnd(Query<FolderBookRow, Void> query) {
        return (favoritesLabel == null ? 0 : 1) + folders.size() + bookIds.size();
    }
}
