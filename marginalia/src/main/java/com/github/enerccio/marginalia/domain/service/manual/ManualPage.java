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
 */
public record ManualPage(String path, String title, String description, String text, List<ManualSection> sections) {

    /** Where the published manual is: the user guide on the GitHub pages of the project (built from {@code manual/}). */
    public static final String WEB_BASE = "https://enerccio.github.io/Marginalia/user/";

    /** The address of the page in the published manual, e.g. {@code .../user/books/story-editor/}. */
    public String url() {
        return url(path, "");
    }

    /**
     * The address in the published manual of a page and, when given, a heading in it: the source path loses
     * {@code .md} (and {@code index}), as the site writes it.
     */
    public static String url(String path, String anchor) {
        String address = path.endsWith(".md") ? path.substring(0, path.length() - 3) : path;
        if (address.equals("index")) {
            address = "";
        } else if (address.endsWith("/index")) {
            address = address.substring(0, address.length() - "index".length());
        } else {
            address += "/";
        }
        return WEB_BASE + address + (anchor == null || anchor.isEmpty() ? "" : "#" + anchor);
    }
}
