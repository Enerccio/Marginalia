package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.Comparator;
import java.util.List;

@Configurable
public class ManuscriptLorebookPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    private final ManuscriptDialog parent;

    private ComboBox<Lorebook> activeLorebookCombo;
    private LorebookView lorebookView;
    private boolean loading = false;

    public ManuscriptLorebookPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        activeLorebookCombo = new ComboBox<>(loc.getValue(L.LABEL_LOREBOOK));
        activeLorebookCombo.setItemLabelGenerator(Lorebook::getName);
        activeLorebookCombo.setClearButtonVisible(true);
        activeLorebookCombo.setWidthFull();
        activeLorebookCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        headerLayout.add(activeLorebookCombo);

        // Instantiate pinned LorebookView (hides internal lorebook dropdown and create button)
        lorebookView = new LorebookView(null);
        Component lorebookViewComponent = lorebookView.create();

        mainLayout.add(headerLayout, lorebookViewComponent);
        mainLayout.setFlexGrow(1, lorebookViewComponent);

        container.add(loc.getValue(L.LABEL_LOREBOOK), mainLayout);
        return mainLayout;
    }

    private Manuscript refreshModel() throws Exception {
        return parent.refreshManuscript();
    }

    private void autosave() {
        if (loading) {
            return;
        }

        try {
            Manuscript manuscript = refreshModel();
            if (manuscript == null) {
                return;
            }

            Lorebook selectedLorebook = activeLorebookCombo.getValue();
            manuscript.setLorebook(selectedLorebook);

            parent.save();

            if (lorebookView != null) {
                lorebookView.setLorebook(selectedLorebook);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void setFrozen(boolean frozen) {
        // ignored, since tab will be unselectable
    }

    @Override
    public void load(Manuscript manuscript) {
        if (manuscript == null) {
            return;
        }

        loading = true;
        try {
            List<Lorebook> lorebooks = lorebookService.findAllForUser();
            lorebooks.sort(Comparator.comparing(Lorebook::getName));
            activeLorebookCombo.setItems(lorebooks);

            Lorebook manuscriptLorebook = manuscript.getLorebook();
            if (manuscriptLorebook != null && manuscriptLorebook.getId() != null) {
                Lorebook matched = lorebooks.stream()
                        .filter(l -> l.getId().equals(manuscriptLorebook.getId()))
                        .findFirst()
                        .orElse(manuscriptLorebook);
                activeLorebookCombo.setValue(matched);
            } else {
                activeLorebookCombo.setValue(null);
            }

            if (lorebookView != null) {
                lorebookView.setLorebook(activeLorebookCombo.getValue());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    @Override
    public void onTabLeave() throws Exception {

    }

    @Override
    public void onTabEnter() throws Exception {

    }
}