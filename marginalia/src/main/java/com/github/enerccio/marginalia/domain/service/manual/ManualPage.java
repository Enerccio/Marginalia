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
}
