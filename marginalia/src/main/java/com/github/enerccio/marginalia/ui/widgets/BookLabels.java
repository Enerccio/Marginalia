package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;

/**
 * How the combo boxes of books name a book: {@code Folder / Sub / Name} (just the name at the top level), which is what the
 * closed field shows and what typing filters on; the dropdown shows the folders in small letters above the name.
 */
@Configurable
public class BookLabels {

    private static final String SEPARATOR = " / ";

    @Autowired
    private FolderService folderService;

    @Autowired
    private Localization loc;

    /**
     * Gives the combo box the labels and the two line items. The folders are read now, so a combo box made earlier doesn't
     * show a folder that was renamed since.
     */
    public void apply(ComboBox<Manuscript> combo) {
        Map<Long, String> paths;
        try {
            paths = folderService.getPaths();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            paths = Map.of();
        }
        Map<Long, String> folders = paths;
        combo.setItemLabelGenerator(book -> label(book, folders));
        combo.setRenderer(new ComponentRenderer<>(book -> {
            Div item = new Div();
            String path = path(book, folders);
            if (!path.isEmpty()) {
                Div above = new Div(path);
                above.addClassNames(CssStyles.TEXT_XS, CssStyles.TEXT_SECONDARY);
                item.add(above);
            }
            item.add(new Div(StringUtils.defaultString(book.getName())));
            return item;
        }));
    }

    /**
     * {@code Folder / Sub / Name}, or the name of a book at the top level.
     */
    public static String label(Manuscript book, Map<Long, String> paths) {
        String path = path(book, paths);
        String name = StringUtils.defaultString(book.getName());
        return path.isEmpty() ? name : path + SEPARATOR + name;
    }

    private static String path(Manuscript book, Map<Long, String> paths) {
        if (book.getFolder() == null) {
            return "";
        }
        return paths.getOrDefault(book.getFolder().getId(), "").replace(String.valueOf(FolderService.SEPARATOR), SEPARATOR);
    }
}
