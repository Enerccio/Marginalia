package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.vaadin.flow.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

@Configurable
public class ManuscriptInfoPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    private final ManuscriptDialog parent;

    public ManuscriptInfoPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {

    }

    @Override
    public void setFrozen(boolean frozen) {
        // ignored, since tab will be unselectable
    }

    @Override
    public void load(Manuscript manuscript) {

    }

}
