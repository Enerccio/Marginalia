package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.UserDefaults;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.LanguageLibrary;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The panel of the plugin in <i>Settings → Extension Settings</i>: the library of languages (new, edit, duplicate, delete, export and
 * import) and the user level of what books and folders inherit (the language, the automatic run, the model and the protocol).
 * <p>
 * Nothing is stored until the Settings screen is saved ({@link #save}); a language that is edited in its window shows as changed in the
 * summary field, which locks the other sections of the workspace until the settings are saved or discarded, as any changed field does. The
 * languages the user did not touch are taken from the library as it is when the screen is saved, so the words a run added meanwhile stay.
 */
public class LanguageSettingsForm extends VerticalLayout {

    private final LanguageLibrary library;

    private final TextField summary = new TextField(T.localized(LANGUAGES));
    private final Grid<Language> grid = new Grid<>();
    private final ComboBox<Language> languageCombo = new ComboBox<>(T.localized(LANGUAGE_OF_THE_SPEECH));
    private final Checkbox auto = new Checkbox(T.localized(TRANSLATE_BY_ITSELF));
    private final ComboBox<AI> aiCombo = new ComboBox<>(T.localized(MODEL_OF_THE_TRANSLATION));
    private final ComboBox<Protocol> protocolCombo = new ComboBox<>(T.localized(PROTOCOL_OF_THE_TRANSLATION));

    private final List<Language> shown = new ArrayList<>();
    private final Set<String> loaded = new HashSet<>();
    private final Set<String> edited = new HashSet<>();
    private boolean changed;

    private Button editButton;
    private Button duplicateButton;
    private Button deleteButton;
    private Button exportButton;

    public LanguageSettingsForm(LanguageLibrary library) {
        this.library = library;
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        build();
        refresh();
    }

    private void build() {
        Span intro = new Span(T.localized(THE_LANGUAGES_YOU_TRANSLATE_INTO));
        intro.addClassName(ConlangStyles.HINT);

        summary.setReadOnly(true);
        summary.setWidthFull();

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("16rem");
        grid.addColumn(Language::getName).setHeader(T.localized(NAME)).setFlexGrow(2).setResizable(true);
        grid.addColumn(language -> language.getLexicon().size()).setHeader(T.localized(WORDS)).setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(language -> language.newEntries().size()).setHeader(T.localized(NEW)).setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(language -> LanguageEditorDialog.summary(language.getDescription())).setHeader(T.localized(DESCRIPTION))
                .setFlexGrow(4).setResizable(true);
        grid.addSelectionListener(_ -> updateButtons());
        grid.addItemDoubleClickListener(event -> edit(event.getItem()));

        Button add = new Button(T.localized(NEW_LANGUAGE), Solid.PLUS.create(), _ -> add());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton = new Button(T.localized(EDIT), Solid.PEN.create(), _ -> selected(this::edit));
        duplicateButton = new Button(T.localized(DUPLICATE), Solid.COPY.create(), _ -> selected(this::duplicate));
        deleteButton = new Button(T.localized(DELETE), Solid.TRASH.create(), _ -> selected(this::delete));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        exportButton = new Button(T.localized(EXPORT), Solid.FILE_EXPORT.create(), _ -> selected(this::export));
        Button importButton = new Button(T.localized(IMPORT), Solid.FILE_IMPORT.create(), _ -> importLanguage());
        HorizontalLayout buttons = new HorizontalLayout(add, editButton, duplicateButton, exportButton, importButton, deleteButton);
        buttons.setWidthFull();
        buttons.setWrap(true);

        Span defaultsHint = new Span(T.localized(WHAT_BOOKS_AND_FOLDERS_INHERIT));
        defaultsHint.addClassName(ConlangStyles.HINT);
        languageCombo.setWidthFull();
        languageCombo.setItemLabelGenerator(Language::getName);
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

        add(intro, summary, buttons, grid, defaultsHint, defaults);
    }

    /**
     * Loads the library and the defaults again, throwing away what was changed and not saved.
     */
    public void refresh() {
        try {
            shown.clear();
            shown.addAll(library.languages());
            loaded.clear();
            shown.forEach(language -> loaded.add(language.getId()));
            edited.clear();
            changed = false;

            UserDefaults defaults = library.defaults();
            List<AI> ais = library.availableAis();
            List<Protocol> protocols = library.availableProtocols();
            aiCombo.setItems(ais);
            aiCombo.setValue(ais.stream().filter(ai -> ai.getUuid().equals(defaults.getAiUuid())).findFirst().orElse(null));
            protocolCombo.setItems(protocols);
            protocolCombo.setValue(protocols.stream().filter(protocol -> protocol.getUuid().equals(defaults.getProtocolUuid())).findFirst().orElse(null));
            auto.setValue(defaults.isAuto());
            showLibrary(defaults.getLanguageId());
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private void showLibrary(String chosenId) {
        grid.setItems(shown);
        languageCombo.setItems(shown);
        languageCombo.setValue(shown.stream().filter(language -> language.getId().equals(chosenId)).findFirst().orElse(null));
        summary.setValue(T.localized(LANGUAGES_D).formatted(shown.size()) + (changed ? " " + T.localized(EDITED) : ""));
        updateButtons();
    }

    private void libraryChanged() {
        changed = true;
        Language chosen = languageCombo.getValue();
        showLibrary(chosen == null ? null : chosen.getId());
    }

    // ---- the actions

    private void add() {
        Language language = new Language();
        open(language, true);
    }

    private void edit(Language language) {
        open(language, false);
    }

    private void open(Language language, boolean isNew) {
        List<Choice> books;
        List<Choice> lorebooks;
        try {
            books = library.bookChoices();
            lorebooks = library.lorebookChoices();
        } catch (Exception e) {
            books = List.of();
            lorebooks = List.of();
        }
        new LanguageEditorDialog(language, name -> nameTaken(language, name), books, lorebooks, saved -> {
            if (isNew) {
                shown.add(saved);
            } else {
                shown.replaceAll(other -> other.getId().equals(saved.getId()) ? saved : other);
            }
            edited.add(saved.getId());
            libraryChanged();
            grid.select(saved);
        }).open();
    }

    private void duplicate(Language language) {
        String name = language.getName();
        int number = 2;
        while (nameTaken(null, name + " " + number)) {
            number++;
        }
        Language copy = language.duplicate(name + " " + number);
        shown.add(shown.indexOf(language) + 1, copy);
        edited.add(copy.getId());
        libraryChanged();
        grid.select(copy);
    }

    private void delete(Language language) {
        ConfirmDialog.show(T.localized(DELETE_THE_LANGUAGE_S).formatted(language.getName()), () -> {
            shown.remove(language);
            edited.remove(language.getId());
            libraryChanged();
        });
    }

    private boolean nameTaken(Language except, String name) {
        String key = StringUtils.defaultString(name).strip();
        return shown.stream().anyMatch(other -> other != except && (except == null || !other.getId().equals(except.getId()))
                && other.getName().strip().equalsIgnoreCase(key));
    }

    private void export(Language language) {
        String document = ConlangStore.export(language);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(EXPORT_THE_LANGUAGE_S).formatted(language.getName()));
        dialog.setWidth("720px");
        dialog.setMaxWidth("96vw");
        TextArea text = new TextArea();
        text.setWidthFull();
        text.setHeight("50vh");
        text.setReadOnly(true);
        text.setValue(document);
        byte[] bytes = document.getBytes(StandardCharsets.UTF_8);
        StreamResource resource = new StreamResource(fileName(language), () -> new ByteArrayInputStream(bytes));
        resource.setContentType("application/json");
        resource.setCacheTime(0);
        Anchor download = new Anchor(resource, T.localized(DOWNLOAD));
        download.getElement().setAttribute("download", true);
        Span hint = new Span(T.localized(THE_DOCUMENT_HAS_THE_RULES));
        hint.addClassName(ConlangStyles.HINT);
        dialog.add(new VerticalLayout(hint, text, download));
        dialog.getFooter().add(new Button(T.localized(CLOSE), _ -> dialog.close()));
        dialog.open();
    }

    private static String fileName(Language language) {
        String base = language.getName().replaceAll("[^\\p{L}\\p{N}_-]+", "-").replaceAll("^-+|-+$", "");
        return (base.isEmpty() ? "language" : base) + ".json";
    }

    private void importLanguage() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(IMPORT_A_LANGUAGE));
        dialog.setWidth("720px");
        dialog.setMaxWidth("96vw");
        TextArea text = new TextArea();
        text.setWidthFull();
        text.setHeight("50vh");
        text.setPlaceholder(T.localized(PASTE_THE_DOCUMENT_OR_CHOOSE_A_FILE));
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/json", ".json");
        upload.setMaxFiles(1);
        upload.addSucceededListener(_ -> {
            try {
                text.setValue(new String(buffer.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                Notification.error(e.getMessage());
            }
        });
        Button run = new Button(T.localized(IMPORT), _ -> {
            try {
                Language language = ConlangStore.importDocument(text.getValue());
                String name = StringUtils.defaultIfBlank(language.getName(), T.localized(IMPORTED_LANGUAGE));
                String candidate = name;
                int number = 2;
                while (nameTaken(null, candidate)) {
                    candidate = name + " " + number++;
                }
                language.setName(candidate);
                shown.add(language);
                edited.add(language.getId());
                libraryChanged();
                grid.select(language);
                dialog.close();
            } catch (IllegalArgumentException e) {
                Notification.error(e.getMessage());
            }
        });
        run.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(upload, text));
        dialog.getFooter().add(new Button(T.localized(CANCEL), _ -> dialog.close()), run);
        dialog.open();
    }

    private void selected(java.util.function.Consumer<Language> action) {
        grid.getSelectedItems().stream().findFirst().ifPresent(action);
    }

    private void updateButtons() {
        boolean any = !grid.getSelectedItems().isEmpty();
        editButton.setEnabled(any);
        duplicateButton.setEnabled(any);
        deleteButton.setEnabled(any);
        exportButton.setEnabled(any);
    }

    /**
     * Puts what the screen holds into the user settings it is about to store.
     */
    public void save(com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting into) throws Exception {
        UserDefaults defaults = new UserDefaults();
        Language language = languageCombo.getValue();
        defaults.setLanguageId(language == null ? null : language.getId());
        defaults.setAuto(auto.getValue());
        AI ai = aiCombo.getValue();
        defaults.setAiUuid(ai == null ? null : ai.getUuid());
        Protocol protocol = protocolCombo.getValue();
        defaults.setProtocolUuid(protocol == null ? null : protocol.getUuid());
        library.put(shown, loaded, edited, defaults, into);
    }
}
