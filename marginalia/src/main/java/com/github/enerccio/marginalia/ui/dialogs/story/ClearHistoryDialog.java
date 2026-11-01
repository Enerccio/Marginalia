package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * <i>Clear history</i> of a part or of a book: asks first, and tells how many versions go and how much room they take. The current
 * version always stays; the saved versions (the ones with a label) and the first generated version stay too while the box is ticked.
 */
@Configurable(preConstruction = true)
public class ClearHistoryDialog extends Dialog {

    /**
     * What clearing does, for a part or a book.
     */
    public interface Clearing {
        /**
         * What clearing would remove.
         */
        RevisionClearing preview(boolean keepSnapshots) throws Exception;

        /**
         * Clears the history.
         */
        RevisionClearing clear(boolean keepSnapshots) throws Exception;
    }

    @Autowired
    private Localization loc;

    private final Clearing clearing;
    private final Runnable done;

    private final Checkbox keepSnapshots = new Checkbox();
    private final Span question = new Span();
    private final Button clear = new Button();

    /**
     * @param title the title of the dialog
     * @param done  called after the history was cleared
     */
    public ClearHistoryDialog(String title, Clearing clearing, Runnable done) {
        this.clearing = clearing;
        this.done = done;
        setHeaderTitle(title);
        setWidth("min(520px, 95vw)");

        keepSnapshots.setLabel(loc.getValue(L.LABEL_KEEP_SAVED_VERSIONS));
        keepSnapshots.setValue(true);
        keepSnapshots.addValueChangeListener(_ -> refresh());

        VerticalLayout content = new VerticalLayout(question, keepSnapshots);
        content.setPadding(false);
        content.addClassName(CssStyles.GAP_S);
        add(content);

        clear.setText(loc.getValue(L.LABEL_CLEAR_HISTORY));
        clear.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        clear.addClickListener(_ -> run());
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), clear);
        UIUtils.fitPhone(this, false);
        refresh();
    }

    private void refresh() {
        try {
            RevisionClearing preview = clearing.preview(keepSnapshots.getValue());
            if (preview.versions() == 0) {
                question.setText(loc.getValue(L.MSG_HISTORY_NOTHING_TO_CLEAR));
                clear.setEnabled(false);
            } else {
                question.setText(String.format(loc.getValue(L.MSG_HISTORY_CONFIRM_CLEAR), preview.versions(),
                        FileUtils.byteCountToDisplaySize(preview.bytes())));
                clear.setEnabled(true);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void run() {
        try {
            RevisionClearing cleared = clearing.clear(keepSnapshots.getValue());
            close();
            Notification.success(String.format(loc.getValue(L.MSG_HISTORY_CLEARED), cleared.versions()));
            done.run();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
