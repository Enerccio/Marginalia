package com.github.enerccio.marginalia.extensions.mangagenerator.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.T;

/**
 * Edits a text that may replace an inherited one (a prompt of a section replaces the prompt of the manga, which replaces the
 * built-in one): an empty text, or the inherited one, is saved as null, meaning "use the inherited".
 */
public class TextEditDialog extends Dialog {

    /**
     * @param current   the text that is set, null when the inherited text is used
     * @param inherited what null means; shown when nothing is set, null or empty when null means no text at all
     * @param onSave    gets the new text, null for the inherited one
     */
    public TextEditDialog(String title, String hint, String current, String inherited, Consumer<String> onSave) {
        setHeaderTitle(title);
        setWidth("70vw");
        setHeight("70vh");

        TextArea area = new TextArea();
        area.setWidthFull();
        // the text area fills what the helper text leaves and scrolls itself; the dialog body must not scroll too
        area.addClassName(CssStyles.MIN_HEIGHT_0);
        area.setHelperText(hint);
        area.setValue(StringUtils.isNotBlank(current) ? current : StringUtils.defaultString(inherited));
        DialogFocus.onOpen(this, area);
        VerticalLayout content = new VerticalLayout(area);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        content.expand(area);
        add(content);

        Button save = new Button(T.localized("Save"), e -> {
            String value = area.getValue();
            boolean useInherited = StringUtils.isBlank(value) || value.equals(inherited);
            onSave.accept(useInherited ? null : value);
            close();
        });
        save.setThemeName("primary");
        Button cancel = new Button(T.localized("Cancel"), e -> close());
        if (StringUtils.isNotEmpty(inherited)) {
            Button reset = new Button(T.localized("Reset to default"), e -> area.setValue(inherited));
            reset.setThemeName("tertiary");
            getFooter().add(reset);
        }
        getFooter().add(cancel, save);
    }
}
