package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.utils.WordDiff;
import com.github.enerccio.marginalia.utils.WordDiff.Segment;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.List;

/**
 * The difference of two texts word by word, as one text: what only the first has struck through in red, what only the second
 * has in green, the rest as it is (see {@link WordDiff}).
 */
public class WordDiffView extends Div {

    private final String sameText;

    /**
     * @param sameText what is shown when the two texts are the same
     */
    public WordDiffView(String sameText) {
        this.sameText = sameText;
        addClassName(CssStyles.MERGE_DIFF);
    }

    /**
     * Shows what turns {@code before} into {@code after}.
     */
    public void show(String before, String after) {
        removeAll();
        List<Segment> segments = WordDiff.diff(before, after);
        if (WordDiff.same(segments)) {
            add(new Span(sameText));
            return;
        }
        for (Segment segment : segments) {
            Span span = new Span(segment.text());
            switch (segment.kind()) {
                case ADDED -> span.addClassName(CssStyles.MERGE_DIFF_ADDED);
                case REMOVED -> span.addClassName(CssStyles.MERGE_DIFF_REMOVED);
                default -> {
                }
            }
            add(span);
        }
    }
}
