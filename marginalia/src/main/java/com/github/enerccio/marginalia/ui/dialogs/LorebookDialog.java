package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Extendable
public class LorebookDialog extends Dialog implements LLMInteract {

    @Autowired
    protected Localization loc;

    private Lorebook lorebook;
    private LorebookView lorebookView;
    private Runnable onClose;

    public LorebookDialog() {
        this(null);
    }

    public LorebookDialog(Lorebook lorebook) {
        this.lorebook = lorebook;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "footerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "closeButton", type = Button.class)})
    public void create() {
        boolean isEdit = lorebook != null && lorebook.getId() != null;
        setHeaderTitle(isEdit ? loc.getValue(L.LABEL_EDIT_LOREBOOK) : loc.getValue(L.LABEL_NEW_LOREBOOK));
        setWidth("1050px");
        setHeight("750px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        lorebookView = new LorebookView(lorebook);
        try {
            add(lorebookView.create());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button closeButton = new Button(loc.getValue(L.LABEL_OK), event -> {
            close();
            if (onClose != null) {
                onClose.run();
            }
        });
        closeButton.setThemeName("primary");

        footerLayout.add(closeButton);
        getFooter().add(footerLayout);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Runnable.class))
    public Runnable getOnClose() {
        return onClose;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onClose", type = Runnable.class)})
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

}