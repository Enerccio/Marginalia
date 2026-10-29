package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.PromptSettingsForm;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;


/**
 * The prompts tab of the {@link ManuscriptDialog}: the {@link PromptSettingsForm} of the book. An empty field is
 * inherited from the folders of the book, the user settings or the defaults, the form says which.
 */
@Configurable
@Extendable
public class ManuscriptPromptPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    private final ManuscriptDialog parent;

    private PromptSettingsForm form;

    public ManuscriptPromptPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "container", type = VTabSheet.class)},
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class)})
    @Override
    public Component create(VTabSheet container) throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        form = new PromptSettingsForm("240px", this::autosave).create();
        mainLayout.add(form.formLayout());

        container.add(loc.getValue(L.LABEL_PROMPT_PART), mainLayout);
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class))
    private Manuscript refreshModel() throws Exception {
        return parent.refreshManuscript();
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void autosave() {
        try {
            Manuscript manuscript = refreshModel();
            if (manuscript == null) {
                return;
            }

            form.values().forEach((field, value) -> field.setOfBook(manuscript, value));

            parent.save();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "frozen", type = boolean.class)})
    @Override
    public void setFrozen(boolean frozen) {
        // ignored, since tab will be unselectable
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    @Override
    public void load(Manuscript manuscript) {
        if (manuscript == null) {
            return;
        }
        form.load(field -> field.ofBook(manuscript), manuscript.getFolder());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabLeave() throws Exception {

    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabEnter() throws Exception {

    }
}
