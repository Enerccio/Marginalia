package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.components.PhoneLorebookView;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The lorebook tab of the {@link PhoneManuscriptDialog}: the lorebook of the book and the reduced
 * {@link PhoneLorebookView} of it, or of one of its subbooks.
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
    // which lorebook of the one of the book and its subbooks the view shows, the book keeps the lorebook of the first box
    private ComboBox<Lorebook> shownLorebookCombo;
    private PhoneLorebookView lorebookView;
    private boolean loading = false;

    public PhoneManuscriptLorebookPart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "lorebookViewComponent", type = Component.class)})
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

        shownLorebookCombo = new ComboBox<>(loc.getValue(L.LABEL_SHOWN_LOREBOOK));
        shownLorebookCombo.setItemLabelGenerator(Lorebook::getName);
        shownLorebookCombo.setWidthFull();
        shownLorebookCombo.setVisible(false);
        shownLorebookCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                showLorebook(e.getValue());
            }
        });

        // pinned: the lorebook is the one of the book, chosen in the combo box above, or one of its subbooks
        lorebookView = new PhoneLorebookView(true);
        lorebookView.setOnStateChange(parent::stateChanged);
        Component lorebookViewComponent = lorebookView.create();

        mainLayout.add(activeLorebookCombo, shownLorebookCombo, lorebookViewComponent);
        mainLayout.setFlexGrow(1, lorebookViewComponent);
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "selectedLorebook", type = Lorebook.class),
                    @APILocals(name = "shown", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
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

            // another lorebook of the book shows its own subbooks
            shownLorebookCombo.clear();
            Lorebook shown = refreshShown();
            if (lorebookView != null) {
                lorebookView.setLorebook(shown);
            }
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "lorebooks", type = List.class),
                    @APILocals(name = "manuscriptLorebook", type = Lorebook.class),
                    @APILocals(name = "matched", type = Lorebook.class),
                    @APILocals(name = "shown", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void load(Manuscript manuscript) {
        if (manuscript == null) {
            return;
        }

        loading = true;
        try {
            List<Lorebook> lorebooks = lorebookService.findAllRegularForUser();
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

            Lorebook shown = refreshShown();
            if (lorebookView != null) {
                lorebookView.setLorebook(shown);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    /**
     * Lists the lorebook of the book and its subbooks, however deep, in the box of the shown lorebook, which is shown only
     * when there is a subbook to switch to. The shown lorebook stays when it is still among them, otherwise the lorebook
     * of the book is shown.
     *
     * @return the lorebook the view has to show, null when the book has none
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Lorebook.class),
            locals = {@APILocals(name = "bound", type = Lorebook.class),
                    @APILocals(name = "tree", type = List.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "shown", type = Lorebook.class),
                    @APILocals(name = "shownId", type = Long.class)})
    private Lorebook refreshShown() {
        Lorebook bound = activeLorebookCombo.getValue();
        List<Lorebook> tree = List.of();
        try {
            tree = bound == null ? List.of() : lorebookService.getLorebookTree(bound);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Lorebook shown = shownLorebookCombo.getValue();
        Long shownId = shown == null ? null : shown.getId();
        shown = tree.stream().filter(l -> Objects.equals(l.getId(), shownId)).findFirst().orElse(bound);
        shownLorebookCombo.setItems(tree);
        shownLorebookCombo.setValue(shown);
        shownLorebookCombo.setVisible(tree.size() > 1);
        return shown;
    }

    /**
     * Shows the lorebook, the book keeps its own.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lorebook", type = Lorebook.class)})
    private void showLorebook(Lorebook lorebook) {
        shownLorebookCombo.setValue(lorebook);
        if (lorebookView != null) {
            lorebookView.setLorebook(lorebook);
        }
        parent.stateChanged();
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "shown", type = Lorebook.class),
                    @APILocals(name = "bound", type = Lorebook.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = lorebookView == null ? new JsonObject() : lorebookView.storeState();
        Lorebook shown = shownLorebookCombo == null ? null : shownLorebookCombo.getValue();
        Lorebook bound = activeLorebookCombo == null ? null : activeLorebookCombo.getValue();
        if (shown != null && shown.getUuid() != null && (bound == null || !Objects.equals(shown.getId(), bound.getId()))) {
            state.addProperty("shown", shown.getUuid());
        }
        return state;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "shownUuid", type = String.class)})
    @Override
    public void restoreState(JsonObject self) {
        // the shown lorebook first, the open entries are the ones of it
        String shownUuid = RestorableState.string(self, "shown");
        if (shownUuid != null && shownLorebookCombo != null && lorebookView != null) {
            shownLorebookCombo.getListDataView().getItems().filter(l -> shownUuid.equals(l.getUuid())).findFirst().ifPresent(shown -> {
                shownLorebookCombo.setValue(shown);
                lorebookView.setLorebook(shown);
            });
        }
        if (lorebookView != null) {
            lorebookView.restoreState(self);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabLeave() throws Exception {

    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "before", type = Lorebook.class),
                    @APILocals(name = "shown", type = Lorebook.class)})
    @Override
    public void onTabEnter() throws Exception {
        // the subbooks may have been changed in the view
        Lorebook before = shownLorebookCombo.getValue();
        Lorebook shown = refreshShown();
        if (lorebookView != null && !Objects.equals(before == null ? null : before.getId(), shown == null ? null : shown.getId())) {
            lorebookView.setLorebook(shown);
        }
    }
}
