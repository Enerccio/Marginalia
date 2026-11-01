package com.github.enerccio.marginalia.ui.styles;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The button of the reader (the view of a book) that chooses the text style of what is read. Only the styles of the reader are offered
 * (the built in ones and the ones of the logged in user, see {@link PartStyleService}), also when the book is a published one of another
 * user: the style of the author is never seen. The choice is kept in a cookie of the browser, not in the book or the settings, so it
 * is the same for every book read there; without a cookie, or when the style of the cookie is not among the reader's styles (deleted, or
 * of another user of the same browser), the reader shows the Book style.
 */
public class ReaderStyleChooser extends Button {

    private static final Logger log = LoggerFactory.getLogger(ReaderStyleChooser.class);

    /** The cookie that holds the id of the chosen style. */
    public static final String COOKIE = "marginalia_reader_style";
    private static final int COOKIE_MAX_AGE_SECONDS = 365 * 24 * 60 * 60;

    /**
     * Where the choice is kept.
     */
    public interface ChoiceStore {
        /** The id that was stored, null when none. */
        String read();

        void write(String styleId);
    }

    private final Localization loc;
    private final PartStyleService service;
    private final Consumer<PartStyle> onChosen;
    private final ChoiceStore store;
    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private PartStyle current;

    /**
     * A chooser that keeps its choice in the cookie {@link #COOKIE}.
     *
     * @param onChosen gets the style to show now (called once at the start, and when the reader chooses another)
     */
    public ReaderStyleChooser(Localization loc, PartStyleService service, Consumer<PartStyle> onChosen) {
        this(loc, service, onChosen, new ChoiceStore() {
            @Override
            public String read() {
                try {
                    return UIUtils.getCookieValue(COOKIE);
                } catch (RuntimeException e) {
                    // no request, or no cookies at all
                    return null;
                }
            }

            @Override
            public void write(String styleId) {
                try {
                    UIUtils.setCookieValue(COOKIE, styleId, COOKIE_MAX_AGE_SECONDS);
                } catch (RuntimeException e) {
                    log.debug("The choice of the style could not be stored in a cookie: {}", e.getMessage());
                }
            }
        });
    }

    public ReaderStyleChooser(Localization loc, PartStyleService service, Consumer<PartStyle> onChosen, ChoiceStore store) {
        super(Solid.PEN_FANCY.create());
        this.loc = loc;
        this.service = service;
        this.onChosen = onChosen;
        this.store = store;
        setThemeName("primary icon");
        addClassName(CssStyles.FLOATING_STYLE_BUTTON);
        UIUtils.addTooltip(this, loc.getValue(L.LABEL_CHANGE_STYLES));

        try {
            List<PartStyle> styles = PartStyleApplier.sorted(service.listForBooks(), loc);
            String stored = store.read();
            current = styles.stream().filter(style -> style.getId().equals(stored)).findFirst()
                    .orElseGet(() -> styles.stream().filter(style -> PartStyleRegistry.BOOK.equals(style.getId())).findFirst().orElse(null));

            ContextMenu menu = new ContextMenu(this);
            menu.setOpenOnClick(true);
            for (PartStyle style : styles) {
                MenuItem item = menu.addItem(style.getDisplayName(loc), event -> choose(style));
                item.setCheckable(true);
                item.setChecked(current != null && style.getId().equals(current.getId()));
                items.put(style.getId(), item);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        onChosen.accept(current);
    }

    /**
     * The style the reader shows now.
     */
    public PartStyle getCurrent() {
        return current;
    }

    /**
     * The menu item of the style, null when the reader has no such style.
     */
    public MenuItem getItem(String styleId) {
        return items.get(styleId);
    }

    private void choose(PartStyle style) {
        current = style;
        items.forEach((id, item) -> item.setChecked(id.equals(style.getId())));
        store.write(style.getId());
        onChosen.accept(style);
    }
}
