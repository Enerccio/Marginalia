package com.github.enerccio.marginalia.domain.service.manual;

import java.util.List;

/**
 * One page of the user manual.
 *
 * @param path        path in the manual, e.g. {@code books/story-editor.md}, the same form the pages link to each other
 * @param title       the page's first level heading
 * @param description the first paragraph of the page, shortened
 * @param text        the whole page as Markdown, pictures removed
 * @param sections    the page split by its headings
 * @param language    the code of the language the text is in ({@code en} for the original, the others are translations)
 */
public record ManualPage(String path, String title, String description, String text, List<ManualSection> sections, String language) {

    /** The published manual, the user guide on the GitHub pages of the project (built from {@code manual/}), the English site. */
    public static final String WEB_BASE = "https://enerccio.github.io/Marginalia/user/";

    /** The root of the sites of the other languages: {@code <SITE_BASE><language>/user/}. */
    public static final String SITE_BASE = "https://enerccio.github.io/Marginalia/";

    public ManualPage {
        language = language == null || language.isBlank() ? ENGLISH : language;
    }

    /** An English page. */
    public ManualPage(String path, String title, String description, String text, List<ManualSection> sections) {
        this(path, title, description, text, sections, ENGLISH);
    }

    /** The code of the language of the manual as it is written. */
    public static final String ENGLISH = "en";

    /** The address of the page in the published manual, in the language of its text, e.g. {@code .../user/books/story-editor/}. */
    public String url() {
        return url(path, "", language);
    }

    /**
     * The address in the published manual of a page and, when given, a heading in it: the source path loses
     * {@code .md} (and {@code index}), as the site writes it.
     */
    public static String url(String path, String anchor) {
        return url(path, anchor, ENGLISH);
    }

    /** As {@link #url(String, String)} in the site of a language; the site of {@code en} is the original one. */
    public static String url(String path, String anchor, String language) {
        String address = path.endsWith(".md") ? path.substring(0, path.length() - 3) : path;
        if (address.equals("index")) {
            address = "";
        } else if (address.endsWith("/index")) {
            address = address.substring(0, address.length() - "index".length());
        } else {
            address += "/";
        }
        String base = language == null || language.equals(ENGLISH) ? WEB_BASE : SITE_BASE + language + "/user/";
        return base + address + (anchor == null || anchor.isEmpty() ? "" : "#" + anchor);
    }
}
