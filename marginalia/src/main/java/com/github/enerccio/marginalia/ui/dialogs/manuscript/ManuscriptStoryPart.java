package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

@Configurable
public class ManuscriptStoryPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    private final ManuscriptDialog parent;

    public ManuscriptStoryPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        Div placeholder = new Div();
        placeholder.setSizeFull();
        container.add(loc.getValue(L.LABEL_STORY_PART), placeholder);
        return placeholder;
    }

    @Override
    public void setFrozen(boolean frozen) {

    }

    @Override
    public void load(Manuscript manuscript) {

    }
}
