package com.github.enerccio.marginalia.ui.dialogs.phone;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.vaadin.flow.component.Component;

/**
 * A tab of the {@link PhoneManuscriptDialog}. Unlike the desktop parts they don't add themselves to a tab sheet, the
 * dialog owns the (scrolling) tab bar and only asks the part for its content.
 */
public interface PhoneManuscriptDialogPart {

    Component create() throws Exception;

    void setFrozen(boolean frozen);

    void load(Manuscript manuscript);

    void onTabLeave() throws Exception;

    void onTabEnter() throws Exception;

}
