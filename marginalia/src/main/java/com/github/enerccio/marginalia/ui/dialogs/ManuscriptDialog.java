package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ManuscriptDialog extends Dialog {

    @Autowired
    protected Localization loc;

    private Manuscript manuscript;
    private Runnable onSave;

    public ManuscriptDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    public void create() {
        setHeaderTitle(manuscript.getName());
        setSizeFull();
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        add(mainLayout);

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button cancelButton = new Button(loc.getValue(L.LABEL_EXIT), event -> close());

        footerLayout.add(cancelButton);
        getFooter().add(footerLayout);
    }

    public Runnable getOnSave() {
        return onSave;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}