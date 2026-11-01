package com.github.enerccio.marginalia.ui.styles;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem.Choice;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Puts a {@link PartStyle} on the element that holds the text of the parts (the story, the reader, the preview of the style editor) and
 * builds the "Change styles" menu of the story.
 * <p>
 * The stylesheet of a style is a {@code <style>} element of the page with an id derived from the style, written each time the style
 * is applied (so a style that was edited shows the new look at once) and the element carries the class of the style. A style
 * without content (the plain look) adds neither: it only takes the class of the previous one away.
 */
public final class PartStyleApplier {

    private static final String CLASS_PREFIX = "part-style-";

    private PartStyleApplier() {
    }

    /**
     * Makes the style the only text style of the host.
     */
    public static <C extends Component & HasStyle> void apply(C host, PartStyle style) {
        for (String className : new ArrayList<>(host.getClassNames())) {
            if (className.startsWith(CLASS_PREFIX)) {
                host.removeClassName(className);
            }
        }
        if (style == null || !style.hasContent()) {
            return;
        }
        UI ui = host.getUI().orElse(UI.getCurrent());
        if (ui != null) {
            JavascriptLibrary.setPageStyle(ui.getPage(), style.getStyleElementId(), style.toCss());
        }
        host.addClassName(style.getCssClass());
    }

    /**
     * The styles sorted by the name the user sees.
     */
    public static List<PartStyle> sorted(List<PartStyle> styles, Localization loc) {
        List<PartStyle> sorted = new ArrayList<>(styles);
        sorted.sort(Comparator.comparing((PartStyle style) -> style.getDisplayName(loc), String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    /**
     * The entries of the "Change styles" menu: the entry that follows the default of the user, then every style by name. The style the
     * book has chosen is ticked (the default entry when it has chosen none).
     *
     * @param manuscript the book as it is stored now
     * @param choose     called with the id of the chosen style, null for the default entry
     */
    public static List<Choice> choices(Localization loc, PartStyleService service, Manuscript manuscript, Consumer<String> choose) throws Exception {
        PartStyle chosen = service.findChosen(manuscript);
        List<Choice> choices = new ArrayList<>();
        choices.add(new Choice(String.format(loc.getValue(L.LABEL_STYLE_DEFAULT_ENTRY), service.getDefault().getDisplayName(loc)),
                chosen == null, () -> choose.accept(null)));
        for (PartStyle style : sorted(service.listForBooks(), loc)) {
            choices.add(new Choice(style.getDisplayName(loc), chosen != null && style.getId().equals(chosen.getId()),
                    () -> choose.accept(style.getId())));
        }
        return choices;
    }
}
