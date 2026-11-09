package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.util.HtmlUtils;

/**
 * Asked when saving the edit of a part would make summaries outdated: the author goes back to the editor, saves anyway (the
 * summaries become outdated) or saves and marks the summaries as valid because they still say the right thing.
 */
@Configurable
public class OutdatedSummariesDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final int count;
    private final Runnable saveAnyway;
    private final Runnable saveAndRevalidate;

    /**
     * Closing the dialog (the button, Esc) leaves the editor open with the text as it is.
     *
     * @param count             how many summaries the save makes outdated
     * @param saveAnyway        saves the text and leaves the summaries outdated
     * @param saveAndRevalidate saves the text and revalidates the summaries it made outdated
     */
    public static void open(int count, Runnable saveAnyway, Runnable saveAndRevalidate) {
        new OutdatedSummariesDialog(count, saveAnyway, saveAndRevalidate).create();
    }

    private OutdatedSummariesDialog(int count, Runnable saveAnyway, Runnable saveAndRevalidate) {
        this.count = count;
        this.saveAnyway = saveAnyway;
        this.saveAndRevalidate = saveAndRevalidate;
    }

    private void create() {
        setCloseOnOutsideClick(false);

        Button back = new Button(loc.getValue(L.LABEL_BACK_TO_EDIT), _ -> close());
        back.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button anyway = new Button(loc.getValue(L.LABEL_SAVE_ANYWAY), _ -> {
            close();
            saveAnyway.run();
        });
        Button revalidate = new Button(loc.getValue(L.LABEL_SAVE_AND_REVALIDATE_SUMMARIES), _ -> {
            close();
            saveAndRevalidate.run();
        });

        HorizontalLayout buttons = new HorizontalLayout(back, anyway, revalidate);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWrap(true);

        String text = HtmlUtils.htmlEscape(String.format(loc.getValue(L.MSG_SAVE_OUTDATED_SUMMARIES), count)).replace("\n", "<br/>");
        Html message = new Html("<h3 class='" + CssStyles.TEXT_CENTER + "'>" + text + "</h3>");

        VerticalLayout layout = new VerticalLayout(message, buttons);
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, message, buttons);
        add(layout);
        open();
    }

}
