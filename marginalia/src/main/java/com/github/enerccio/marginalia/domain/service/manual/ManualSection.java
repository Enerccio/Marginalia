package com.github.enerccio.marginalia.domain.service.manual;

/**
 * One searchable part of a manual page: the text under a second or third level heading (the text before the first
 * heading is the introduction, its heading is the page title and its anchor empty).
 *
 * @param pagePath  path of the page in the manual, e.g. {@code books/story-editor.md}
 * @param pageTitle title of the page
 * @param heading   heading of the section, {@code Parent > Child} for a third level one
 * @param anchor    anchor of the heading in the page, empty for the introduction
 * @param text      Markdown of the section without its heading, pictures removed
 */
public record ManualSection(String pagePath, String pageTitle, String heading, String anchor, String text) {

    /** Link to the section as the pages themselves write it: {@code books/story-editor.md#images}. */
    public String link() {
        return anchor.isEmpty() ? pagePath : pagePath + "#" + anchor;
    }
}
