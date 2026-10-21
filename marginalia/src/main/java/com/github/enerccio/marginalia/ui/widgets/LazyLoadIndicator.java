package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * The "loading more" mark at the start or the end of a lazily loaded list. It is there as long as there is more to
 * load, so it is seen only when the user scrolls to the edge before the next chunk arrived.
 */
public class LazyLoadIndicator extends Div {

    public LazyLoadIndicator(String text) {
        addClassName(CssStyles.LAZY_LOAD_INDICATOR);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);

        add(progress, new Span(text));
        setVisible(false);
    }
}
