package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.UserDefaults;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.LanguageLibrary;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.ListEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * The panel of the plugin in <i>Settings → Extension Settings</i>: the library of languages (new from a preset, edit, duplicate, delete, reorder)
 * and the user level of what books and folders inherit (the language, the automatic run, the model and the protocol).
 * <p>
 * Nothing is stored until the Settings screen is saved ({@link #save}); a language that is edited shows as changed in the summary field, which locks
 * the other sections of the workspace until the settings are saved or discarded, as any changed field does.
 */
public class LanguageSettingsForm extends VerticalLayout {

    private final LanguageLibrary library;

    private final TextField summary = new TextField(T.localized(LANGUAGES));
    private final ComboBox<RealLanguage> languageCombo = new ComboBox<>(T.localized(LANGUAGE_OF_THE_SPEECH));
    private final Checkbox auto = new Checkbox(T.localized(TRANSLATE_BY_ITSELF));
    private final ComboBox<AI> aiCombo = new ComboBox<>(T.localized(MODEL_OF_THE_TRANSLATION));
    private final ComboBox<Protocol> protocolCombo = new ComboBox<>(T.localized(PROTOCOL_OF_THE_TRANSLATION));

    private final List<RealLanguage> shown = new ArrayList<>();
    private boolean changed;
    private Languages languages;

    public LanguageSettingsForm(LanguageLibrary library) {
        this.library = library;
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        refresh();
    }

    /**
     * Loads the library and the defaults again, throwing away what was changed and not saved.
     */
    public final void refresh() {
        removeAll();
        try {
            shown.clear();
            shown.addAll(library.languages());
            changed = false;

            Span intro = new Span(T.localized(THE_REAL_LANGUAGES_YOU_TRANSLATE_INTO));
            intro.addClassName(CommonStyles.HINT);
            summary.setReadOnly(true);
            summary.setWidthFull();
            languages = new Languages();
            languages.setHeight("16rem");

            Span defaultsHint = new Span(T.localized(WHAT_BOOKS_AND_FOLDERS_INHERIT));
            defaultsHint.addClassName(CommonStyles.HINT);
            languageCombo.setWidthFull();
            languageCombo.setItemLabelGenerator(RealLanguage::getName);
            languageCombo.setClearButtonVisible(true);
            languageCombo.setHelperText(T.localized(EMPTY_NO_LANGUAGE_UNLESS_A_BOOK));
            aiCombo.setWidthFull();
            aiCombo.setItemLabelGenerator(AI::getName);
            aiCombo.setClearButtonVisible(true);
            aiCombo.setHelperText(T.localized(EMPTY_THE_MODEL_OF_THE_BOOK));
            protocolCombo.setWidthFull();
            protocolCombo.setItemLabelGenerator(Protocol::getName);
            protocolCombo.setClearButtonVisible(true);
            protocolCombo.setHelperText(T.localized(EMPTY_THE_PROTOCOL_OF_THE_BOOK));
            auto.setTooltipText(T.localized(A_NEW_PART_IS_TRANSLATED));
            FormLayout defaults = new FormLayout(languageCombo, aiCombo, protocolCombo, auto);
            defaults.setColspan(auto, 2);
            defaults.setWidthFull();
            defaults.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("36rem", 2));

            UserDefaults stored = library.defaults();
            List<AI> ais = library.availableAis();
            List<Protocol> protocols = library.availableProtocols();
            aiCombo.setItems(ais);
            aiCombo.setValue(ais.stream().filter(ai -> ai.getUuid().equals(stored.getAiUuid())).findFirst().orElse(null));
            protocolCombo.setItems(protocols);
            protocolCombo.setValue(protocols.stream().filter(protocol -> protocol.getUuid().equals(stored.getProtocolUuid())).findFirst().orElse(null));
            auto.setValue(stored.isAuto());
            showLibrary(stored.getLanguageId());
            add(intro, summary, languages, defaultsHint, defaults);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private void showLibrary(String chosenId) {
        languageCombo.setItems(shown);
        languageCombo.setValue(shown.stream().filter(language -> language.getId().equals(chosenId)).findFirst().orElse(null));
        summary.setValue(T.localized(LANGUAGES_D).formatted(shown.size()) + (changed ? " " + T.localized(EDITED) : ""));
    }

    private void libraryChanged() {
        changed = true;
        RealLanguage chosen = languageCombo.getValue();
        showLibrary(chosen == null ? null : chosen.getId());
    }

    private boolean nameTaken(RealLanguage editing, String name) {
        return shown.stream().anyMatch(other -> other != editing && other.getName().equalsIgnoreCase(name));
    }

    /**
     * Puts what the screen holds into the user settings it is about to store.
     */
    public void save(UserSetting into) {
        UserDefaults defaults = new UserDefaults();
        RealLanguage language = languageCombo.getValue();
        defaults.setLanguageId(language == null ? null : language.getId());
        defaults.setAuto(auto.getValue());
        AI ai = aiCombo.getValue();
        defaults.setAiUuid(ai == null ? null : ai.getUuid());
        Protocol protocol = protocolCombo.getValue();
        defaults.setProtocolUuid(protocol == null ? null : protocol.getUuid());
        library.put(shown, defaults, into);
    }

    /** The list of languages, with a button that duplicates the selected one. */
    private final class Languages extends ListEditor<RealLanguage> {

        Languages() {
            super(shown, LanguageSettingsForm.this::libraryChanged);
            Button duplicate = new Button(T.localized(DUPLICATE), Solid.COPY.create(), _ -> {
                RealLanguage selected = selected();
                if (selected != null) {
                    RealLanguage copy = RealStore.copy(selected);
                    copy.setId(java.util.UUID.randomUUID().toString());
                    String base = selected.getName() + " " + T.localized(COPY_SUFFIX);
                    String candidate = base;
                    for (int i = 2; nameTaken(null, candidate); i++) {
                        candidate = base + " " + i;
                    }
                    copy.setName(candidate);
                    shown.add(copy);
                    libraryChanged();
                    refresh();
                    grid().select(copy);
                }
            });
            addToolbarButton(duplicate);
            build();
        }

        @Override
        protected void addColumns(Grid<RealLanguage> grid) {
            grid.addColumn(RealLanguage::getName).setHeader(T.localized(NAME)).setFlexGrow(2);
            grid.addColumn(language -> LanguageEditor.label(language.getScript())).setHeader(T.localized(SCRIPT)).setFlexGrow(1);
            grid.addColumn(RealLanguage::getTongue).setHeader(T.localized(LANGUAGE)).setFlexGrow(1);
        }

        @Override
        protected RealLanguage create() {
            return new RealLanguage();
        }

        @Override
        protected void edit(RealLanguage item, boolean isNew, Runnable done) {
            new LanguageEditor(item, isNew, name -> nameTaken(item, name), done).open();
        }

        @Override
        protected String labelOf(RealLanguage item) {
            return item.getName();
        }
    }
}
