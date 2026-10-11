package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.PhoneLorebookView;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.List;

/**
 * The lorebook tab of the {@link PhoneManuscriptDialog}: the lorebook of the book and the reduced
 * {@link PhoneLorebookView} of it.
 */
@Configurable
@Extendable
public class PhoneManuscriptLorebookPart implements PhoneManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    private final PhoneManuscriptDialog parent;

    private ComboBox<Lorebook> activeLorebookCombo;
    private PhoneLorebookView lorebookView;
    private boolean loading = false;

    public PhoneManuscriptLorebookPart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        activeLorebookCombo = new ComboBox<>(loc.getValue(L.LABEL_LOREBOOK));
        activeLorebookCombo.setItemLabelGenerator(Lorebook::getName);
        activeLorebookCombo.setClearButtonVisible(true);
        activeLorebookCombo.setWidthFull();
        activeLorebookCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                autosave();
            }
        });

        // pinned: the lorebook is the one of the book, chosen in the combo box above
        lorebookView = new PhoneLorebookView(true);
        Component lorebookViewComponent = lorebookView.create();

        mainLayout.add(activeLorebookCombo, lorebookViewComponent);
        mainLayout.setFlexGrow(1, lorebookViewComponent);
        return mainLayout;
    }

    private void autosave() {
        if (loading) {
            return;
        }

        try {
            Manuscript manuscript = parent.refreshManuscript();
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
