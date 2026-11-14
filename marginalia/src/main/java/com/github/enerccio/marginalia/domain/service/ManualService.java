package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.manual.ManualHit;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;

import java.util.List;
import java.util.Optional;

/**
 * The user manual packed in the WAR ({@code classpath:/manual/}, Markdown pages without pictures), kept in memory.
 * Global read-only data: no user, no transaction. Page paths are relative to the manual and end with {@code .md},
 * e.g. {@code books/story-editor.md}, the same form the pages use to link to each other.
 * <p>
 * The manual is also translated ({@code classpath:/manual-translations/<language>/}, made by {@code tools/manual_i18n.py}); the methods
 * with a language give a page in it, or in English when the language has no such page (a translation that does not exist yet, or a
 * language the application has no translation for), and the address of a page is always that of the language of its text.
 */
public interface ManualService {

    /** All pages ordered by path. */
    List<ManualPage> listPages();

    /**
     * @param path path of the page; a leading slash, an {@code #anchor} and a missing {@code .md} are tolerated
     */
    Optional<ManualPage> getPage(String path);

    /**
     * Full-text search over the sections of all pages (English text; a heading counts more than the body).
     *
     * @param query free text, e.g. a user's question
     * @param limit maximum number of hits
     * @return best hits first, empty when nothing matches
     */
    List<ManualHit> search(String query, int limit);

    /**
     * @param language code of a language ({@code es}); a language without translations gives the English pages
     * @return the pages in the language, an untranslated page in English, ordered by path
     */
    default List<ManualPage> listPages(String language) {
        return listPages();
    }

    /** As {@link #getPage(String)} in a language, the English page when it is not translated. */
    default Optional<ManualPage> getPage(String language, String path) {
        return getPage(path);
    }

    /**
     * As {@link #search(String, int)}: the search is over the English text (the model asks with the words of the application, and
     * the words of a translation can't be split the same way in every language), the sections found are those of the translation.
     */
    default List<ManualHit> search(String language, String query, int limit) {
        return search(query, limit);
    }
}
