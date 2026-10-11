package com.github.enerccio.marginalia.ui.workspace.phone.parts;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.parts.UserPart;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The settings tab for phones. It is the desktop {@link UserPart} (so the extension settings panels, which decorate
 * {@code UserPart.refresh} and {@code UserPart.save}, work unchanged) with a header that fits a narrow screen: the
 * buttons are stacked below the title instead of sharing its row, and the book backup import is left out.
 */
@Configurable
@Extendable
public class PhoneSettingsPart extends UserPart {

    public PhoneSettingsPart(Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Component createHeader() {
        unsavedChangesSpan = createUnsavedChangesSpan();
        discardButton = createDiscardButton();
        Button saveButton = createSaveButton();

        HorizontalLayout buttons = new HorizontalLayout(discardButton, saveButton);
        buttons.setWidthFull();
        buttons.setSpacing(true);
        buttons.setFlexGrow(1, discardButton, saveButton);

        Span titleSpan = createTitle();

        VerticalLayout headerLayout = new VerticalLayout(titleSpan, unsavedChangesSpan, buttons);
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setSpacing(false);
        headerLayout.addClassName(CssStyles.GAP_S);
        return headerLayout;
    }

}
