package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BookHelpTools} over a {@link BookDataSource}.
 */
public final class BookHelpToolsImpl implements BookHelpTools {
    private static final Logger log = LoggerFactory.getLogger(BookHelpToolsImpl.class);

    private final BookDataSource source;

    public BookHelpToolsImpl(BookDataSource source) {
        this.source = source;
    }

    @Override
    public void listFolder(FunctionContinuation<String> done, String path) {
        try {
            done.complete(source.listFolder(path == null ? "" : path));
        } catch (IllegalArgumentException e) {
            done.fail(e.getMessage());
        } catch (Exception e) {
            log.warn("The help assistant could not list the folder {}", path, e);
            done.fail("The folder could not be read.");
        }
    }

    @Override
    public void searchBooks(FunctionContinuation<String> done, String query, String folder) {
        if (query == null || query.isBlank()) {
            done.fail("Give the words to look for.");
            return;
        }
        try {
            done.complete(source.searchBooks(query.strip(), folder == null ? "" : folder));
        } catch (IllegalArgumentException e) {
            done.fail(e.getMessage());
        } catch (Exception e) {
            log.warn("The help assistant could not search the books", e);
            done.fail("The books could not be searched.");
        }
    }

    @Override
    public void getBookSettings(FunctionContinuation<String> done, String book) {
        if (book == null || book.isBlank()) {
            done.fail("Name the book: its id, its title or Folder/Sub/Title.");
            return;
        }
        try {
            done.complete(source.bookSettings(book.strip()));
        } catch (IllegalArgumentException e) {
            done.fail(e.getMessage());
        } catch (Exception e) {
            log.warn("The help assistant could not read the settings of a book", e);
            done.fail("The settings could not be read.");
        }
    }
}
