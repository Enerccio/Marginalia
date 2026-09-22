package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import org.vaadin.firitin.layouts.VTabSheet;

public interface ManuscriptDialogPart {

    void create(VTabSheet container) throws Exception;

    void setFrozen(boolean frozen);

    void load(Manuscript manuscript);

}
