package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.ManualService;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

import java.util.List;

/**
 * {@link ManualTools} over a {@link ManualService}. The answers are short enough for a small model: a handful of
 * sections, a page cut at {@link #MAX_PAGE_CHARS}.
 */
public class ManualToolsImpl implements ManualTools {

    static final int SEARCH_HITS = 4;
    static final int MAX_SECTION_CHARS = 1800;
    static final int MAX_PAGE_CHARS = 12000;

    private final ManualService manual;

    public ManualToolsImpl(ManualService manual) {
        this.manual = manual;
    }

    @Override
    public void searchManual(FunctionContinuation<String> done, String query) {
        List<ManualHit> hits = manual.search(query, SEARCH_HITS);
        if (hits.isEmpty()) {
            done.complete("Nothing in the manual matches \"" + query + "\". Try other keywords or read a page from the index.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ManualHit hit : hits) {
            ManualSection section = hit.section();
            sb.append("## ").append(section.pageTitle()).append(" > ").append(section.heading())
                    .append("\nPage: ").append(section.link()).append("\n\n")
                    .append(cut(section.text(), MAX_SECTION_CHARS)).append("\n\n");
        }
        done.complete(sb.toString().strip());
    }

    @Override
    public void readManualPage(FunctionContinuation<String> done, String path) {
        manual.getPage(path).ifPresentOrElse(
                page -> done.complete("# " + page.title() + "\nPage: " + page.path() + "\n\n" + cut(page.text(), MAX_PAGE_CHARS)),
                () -> done.fail("There is no manual page \"" + path + "\". Use a path from the page index."));
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max).stripTrailing() + "\n[...cut, read the page for the rest]";
    }
}
