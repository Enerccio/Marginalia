package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.utils.WordDiff;
import com.github.enerccio.marginalia.utils.WordDiff.Segment;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.util.List;

/**
 * The difference of two texts word by word, as one text: what only the first has struck through in red, what only the second
 * has in green, the rest as it is (see {@link WordDiff}).
 */
@Extendable
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "before", type = String.class),
                    @APIParameter(name = "after", type = String.class)},
            locals = {@APILocals(name = "segments", type = List.class),
                    @APILocals(name = "segment", type = WordDiff.Segment.class),
                    @APILocals(name = "span", type = Span.class)})
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
