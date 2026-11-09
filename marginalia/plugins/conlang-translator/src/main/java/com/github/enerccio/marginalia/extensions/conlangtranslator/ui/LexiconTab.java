package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Category;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The lexicon of a language: the words under their categories with a search and filters, and the actions on a word (edit, accept, lock,
 * retire, delete). The words the model invented are <i>new</i> until the author looks at them; <i>Accept all new</i> is for the author who
 * trusts the model.
 */
public class LexiconTab extends VerticalLayout {

    private final Language language;
    private final Runnable onChange;
    private final ListDataProvider<LexiconEntry> data;
    private final Grid<LexiconEntry> grid = new Grid<>();
    private final TextField search = new TextField();
    private final ComboBox<Category> category = new ComboBox<>();
    private final ComboBox<EntryStatus> status = new ComboBox<>();
    private final Span count = new Span();

    private Button editButton;
    private Button acceptButton;
    private Button lockButton;
    private Button retireButton;
    private Button deleteButton;

    public LexiconTab(Language language, Runnable onChange) {
        this.language = language;
        this.onChange = onChange;
        this.data = new ListDataProvider<>(language.getLexicon());

        setPadding(false);
        setSpacing(true);
        setSizeFull();
        setMinWidth("0");

        search.setPlaceholder(T.localized(SEARCH_BY_WORD_OR_MEANING));
        search.setClearButtonVisible(true);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(_ -> applyFilter());
        search.setWidth(UIUtils.isPhone() ? "100%" : "18rem");
        category.setPlaceholder(T.localized(ALL_CATEGORIES));
        category.setClearButtonVisible(true);
        category.setItemLabelGenerator(Category::getName);
        category.addValueChangeListener(_ -> applyFilter());
        status.setPlaceholder(T.localized(ALL_STATUSES));
        status.setClearButtonVisible(true);
        status.setItems(EntryStatus.values());
        status.setItemLabelGenerator(value -> T.localized(EntryDialog.statusLabel(value)));
        status.addValueChangeListener(_ -> applyFilter());
        count.addClassName(ConlangStyles.HINT);

        if (UIUtils.isPhone()) {
            category.setWidth("48%");
            status.setWidth("48%");
        }
        HorizontalLayout filters = new HorizontalLayout(search, category, status, count);
        filters.setWidthFull();
        filters.setWrap(true);
        filters.setAlignItems(Alignment.CENTER);

        // on a phone the buttons are their icons (the name is the tooltip): the list needs the height
        boolean phone = UIUtils.isPhone();
        Button add = tool(T.localized(ADD), Solid.PLUS, phone, () -> add());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton = tool(T.localized(EDIT), Solid.PEN, phone, () -> selected(this::edit));
        acceptButton = tool(T.localized(ACCEPT), Solid.CHECK, phone, () -> selected(entry -> setStatus(entry, EntryStatus.ACCEPTED)));
        lockButton = tool(T.localized(LOCK), Solid.LOCK, phone, () -> selected(this::toggleLock));
        retireButton = tool(T.localized(RETIRE), Solid.BAN, phone, () -> selected(this::toggleRetired));
        deleteButton = tool(T.localized(DELETE), Solid.TRASH, phone, () -> selected(this::delete));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button acceptAll = tool(T.localized(ACCEPT_ALL_NEW), Solid.CHECK_DOUBLE, phone, this::acceptAllNew);
        HorizontalLayout actions = new HorizontalLayout(add, editButton, acceptButton, lockButton, retireButton, deleteButton, acceptAll);
        actions.setWidthFull();
        actions.setWrap(true);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.addClassName(ConlangStyles.FILL);
        grid.setDataProvider(data);
        grid.addComponentColumn(entry -> {
                    Span form = new Span(entry.getWord());
                    form.addClassName(ConlangStyles.WORD);
                    return form;
                }).setHeader(T.localized(WORD))
                .setSortable(true).setComparator((a, b) -> a.getWord().compareToIgnoreCase(b.getWord())).setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(LexiconEntry::getScript).setHeader(T.localized(IN_THE_SCRIPT)).setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(EntryDialog::glosses).setHeader(T.localized(MEANING)).setFlexGrow(3);
        grid.addColumn(entry -> language.findCategory(entry.getCategory()) == null ? "" : language.findCategory(entry.getCategory()).getName())
                .setHeader(T.localized(CATEGORY)).setSortable(true).setFlexGrow(1);
        grid.addColumn(entry -> T.localized(EntryDialog.kindLabel(entry.getKind()))).setHeader(T.localized(KIND)).setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(entry -> language.findRegister(entry.getRegister()) == null ? "" : language.findRegister(entry.getRegister()).getName())
                .setHeader(T.localized(REGISTER)).setFlexGrow(0).setAutoWidth(true);
        grid.addComponentColumn(entry -> {
            Span badge = new Span(T.localized(EntryDialog.statusLabel(entry.getStatus())));
            badge.addClassName(ConlangStyles.BADGE);
            badge.getElement().setAttribute("data-status", entry.getStatus().name());
            return badge;
        }).setHeader(T.localized(STATUS)).setFlexGrow(0).setWidth("7rem");
        grid.getColumns().forEach(column -> column.setResizable(true));
        grid.addSelectionListener(_ -> updateButtons());
        grid.addItemDoubleClickListener(event -> edit(event.getItem()));

        add(filters, actions, grid);
        setFlexGrow(1, grid);
        refresh();
    }

    /**
     * Shows the lexicon again, after the language changed (the categories are offered for the filter).
     */
    public void refresh() {
        Category chosen = category.getValue();
        category.setItems(language.getCategories());
        category.setValue(chosen != null && language.findCategory(chosen.getId()) != null ? language.findCategory(chosen.getId()) : null);
        data.refreshAll();
        applyFilter();
    }

    private void applyFilter() {
        String text = StringUtils.defaultString(search.getValue()).strip().toLowerCase(Locale.ROOT);
        Category chosenCategory = category.getValue();
        EntryStatus chosenStatus = status.getValue();
        data.setFilter(entry -> (chosenCategory == null || chosenCategory.getId().equals(entry.getCategory()))
                && (chosenStatus == null || chosenStatus == entry.getStatus())
                && (text.isEmpty() || entry.getWord().toLowerCase(Locale.ROOT).contains(text)
                || entry.getSenses().stream().anyMatch(sense -> sense.getGloss().toLowerCase(Locale.ROOT).contains(text))));
        long shown = data.size(new com.vaadin.flow.data.provider.Query<>());
        count.setText(T.localized(WORDS_D_OF_D_D_NEW).formatted(shown, language.getLexicon().size(), language.newEntries().size()));
        updateButtons();
    }

    private void add() {
        LexiconEntry entry = new LexiconEntry();
        entry.setStatus(EntryStatus.ACCEPTED);
        if (category.getValue() != null) {
            entry.setCategory(category.getValue().getId());
        }
        new EntryDialog(entry, true, language, () -> {
            language.addEntry(entry);
            changed();
            grid.select(entry);
        }).open();
    }

    private void edit(LexiconEntry entry) {
        new EntryDialog(entry, false, language, () -> {
            changed();
            grid.select(entry);
        }).open();
    }

    private void setStatus(LexiconEntry entry, EntryStatus status) {
        entry.setStatus(status);
        changed();
    }

    private void toggleLock(LexiconEntry entry) {
        setStatus(entry, entry.isLocked() ? EntryStatus.ACCEPTED : EntryStatus.LOCKED);
    }

    private void toggleRetired(LexiconEntry entry) {
        setStatus(entry, entry.getStatus() == EntryStatus.RETIRED ? EntryStatus.ACCEPTED : EntryStatus.RETIRED);
    }

    private void delete(LexiconEntry entry) {
        ConfirmDialog.show(T.localized(DELETE_S).formatted(entry.getWord()), () -> {
            language.deleteEntry(entry.getId());
            changed();
        });
    }

    private void acceptAllNew() {
        List<LexiconEntry> fresh = language.newEntries();
        if (fresh.isEmpty()) {
            return;
        }
        ConfirmDialog.show(T.localized(ACCEPT_D_NEW_WORDS).formatted(fresh.size()), () -> {
            fresh.forEach(entry -> entry.setStatus(EntryStatus.ACCEPTED));
            changed();
        });
    }

    private void changed() {
        data.refreshAll();
        applyFilter();
        onChange.run();
    }

    private void selected(java.util.function.Consumer<LexiconEntry> action) {
        grid.getSelectedItems().stream().findFirst().ifPresent(action);
    }

    private void updateButtons() {
        LexiconEntry entry = grid.getSelectedItems().stream().findFirst().orElse(null);
        boolean any = entry != null;
        editButton.setEnabled(any);
        acceptButton.setEnabled(any && entry.getStatus() == EntryStatus.NEW);
        lockButton.setEnabled(any);
        retext(lockButton, any && entry.isLocked() ? T.localized(UNLOCK) : T.localized(LOCK));
        retireButton.setEnabled(any);
        retext(retireButton, any && entry.getStatus() == EntryStatus.RETIRED ? T.localized(RESTORE) : T.localized(RETIRE));
        deleteButton.setEnabled(any);
    }

    /** A button of the row of actions: icon and name, on a phone the icon with the name as its tooltip. */
    private static Button tool(String name, Solid icon, boolean iconOnly, Runnable action) {
        Button button = iconOnly ? new Button(icon.create(), _ -> action.run()) : new Button(name, icon.create(), _ -> action.run());
        if (iconOnly) {
            button.setTooltipText(name);
            button.setAriaLabel(name);
        }
        return button;
    }

    /** A new name of a button made by {@link #tool}: its text, or its tooltip when it has no text. */
    private static void retext(Button button, String name) {
        if (button.getText().isEmpty()) {
            button.setTooltipText(name);
            button.setAriaLabel(name);
        } else {
            button.setText(name);
        }
    }

    /** The component, for the tab sheet. */
    public Component component() {
        return this;
    }
}
