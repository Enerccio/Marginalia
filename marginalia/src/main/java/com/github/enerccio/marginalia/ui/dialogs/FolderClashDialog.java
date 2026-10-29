package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.service.FolderService.Choice;
import com.github.enerccio.marginalia.domain.service.FolderService.Resolution;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * Asks what to do with a folder whose name is taken in the target of a copy or a move: give it another name, leave it out or
 * merge it into the folder there. Closing the dialog without a choice cancels the whole transfer.
 */
@Configurable
public class FolderClashDialog extends Dialog {

    /**
     * The decision, and whether it is wanted for the other folders with a clash too.
     */
    public record Decision(Choice choice, boolean forAll) {
    }

    @Autowired
    private Localization loc;

    private final String name;
    private final String targetPath;
    private final String suggestedName;
    private final Consumer<Decision> decided;
    private final Runnable cancelled;
    private boolean answered;

    /**
     * @param name          the name of the folder that clashes
     * @param targetPath    the path of the folder it is put into, for the message
     * @param suggestedName a free name, offered for the rename
     * @param decided       called with the decision
     * @param cancelled     called when the user closes the dialog instead
     */
    public FolderClashDialog(String name, String targetPath, String suggestedName, Consumer<Decision> decided, Runnable cancelled) {
        this.name = name;
        this.targetPath = targetPath;
        this.suggestedName = suggestedName;
        this.decided = decided;
        this.cancelled = cancelled;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_FOLDER));
        setWidth("460px");
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, false);

        TextField newName = new TextField(loc.getValue(L.LABEL_FOLDER_NAME));
        newName.setValue(suggestedName);
        newName.setWidthFull();
        Checkbox forAll = new Checkbox(loc.getValue(L.LABEL_APPLY_TO_ALL));

        VerticalLayout layout = new VerticalLayout(new Span(String.format(loc.getValue(L.MSG_FOLDER_CLASH), name, targetPath)), newName, forAll);
        layout.setPadding(false);
        add(layout);

        Button skip = new Button(loc.getValue(L.LABEL_SKIP), _ -> answer(new Decision(new Choice(Resolution.SKIP, null), forAll.getValue())));
        Button merge = new Button(loc.getValue(L.LABEL_MERGE), _ -> answer(new Decision(new Choice(Resolution.MERGE, null), forAll.getValue())));
        Button rename = new Button(loc.getValue(L.LABEL_RENAME), _ -> {
            if (!newName.getValue().isBlank()) {
                answer(new Decision(new Choice(Resolution.RENAME, newName.getValue().trim()), forAll.getValue()));
            }
        });
        rename.setThemeName("primary");
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), _ -> close());
        HorizontalLayout footer = new HorizontalLayout(cancel, skip, merge, rename);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        addOpenedChangeListener(e -> {
            if (!e.isOpened() && !answered) {
                answered = true;
                cancelled.run();
            }
        });
        open();
    }

    private void answer(Decision decision) {
        answered = true;
        close();
        decided.accept(decision);
    }
}
