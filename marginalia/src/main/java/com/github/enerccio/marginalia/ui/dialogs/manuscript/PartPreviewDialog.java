package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.PartView;
import com.github.enerccio.marginalia.ui.styles.PartStyleApplier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Shows a part with its images as the viewer shows them (the editor of the story lists the images after the text). Nothing is
 * saved: the text and the images are the ones the author has now, also when they are not saved yet.
 */
@Configurable(preConstruction = true)
public class PartPreviewDialog extends Dialog {

    @Autowired
    private Localization loc;

    /**
     * @param style the text style of the book, null for none
     */
    public PartPreviewDialog(String markdown, List<ImageAttachment> images, PartStyle style) {
        setHeaderTitle(loc.getValue(L.LABEL_STYLE_PREVIEW));
        setWidth("min(900px, 95vw)");
        setMaxHeight("90vh");

        Div frame = new Div(new PartView(markdown, images));
        frame.addClassName(CssStyles.PART_PREVIEW_FRAME);
        if (style != null) {
            PartStyleApplier.apply(frame, style);
        }

        RadioButtonGroup<Boolean> width = new RadioButtonGroup<>();
        width.setItems(false, true);
        width.setItemLabelGenerator(phone -> loc.getValue(phone ? L.LABEL_PREVIEW_NARROW : L.LABEL_PREVIEW_WIDE));
        width.setValue(false);
        width.addValueChangeListener(e -> {
            if (e.getValue()) {
                frame.addClassName(CssStyles.PART_PREVIEW_PHONE);
            } else {
                frame.removeClassName(CssStyles.PART_PREVIEW_PHONE);
            }
        });

        add(width, frame);
        getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), _ -> close()));
    }
}
