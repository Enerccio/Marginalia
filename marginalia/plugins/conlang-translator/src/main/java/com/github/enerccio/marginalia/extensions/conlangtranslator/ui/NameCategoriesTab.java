package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.NameGeneratorBridge;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The categories of names the language gives the Name generator: the author makes a category and checks the words of the lexicon it is made of
 * (a search, the lexicon's categories as a filter, <i>check all shown</i>), and a button shows names the generator makes of the checked words
 * right now, before anything is saved. Only the choice of words is kept in the language; a word that is gone or retired when the generator asks
 * is left out.
 */
public class NameCategoriesTab extends VerticalLayout {

    /** How many names a preview makes. */
    static final int PREVIEW_COUNT = 20;

    private final Language language;
    private final Runnable onChange;
    private final NameGeneratorBridge bridge;
    private final ListDataProvider<LexiconEntry> data;
    private final Grid<LexiconEntry> grid = new Grid<>();
    private final ComboBox<NameCategory> chooser = new ComboBox<>(T.localized(NAME_CATEGORY));
    private final TextField search = new TextField();
    private final ComboBox<Category> category = new ComboBox<>();
    private final Span count = new Span();
    private final TextArea names = new TextArea();

    private Button renameButton;
    private Button deleteButton;
    private Button checkAll;
    private Button uncheckAll;
    private Button preview;

    public NameCategoriesTab(Language language, NameGeneratorBridge bridge, Runnable onChange) {
        this.language = language;
        this.bridge = bridge;
        this.onChange = onChange;
        this.data = new ListDataProvider<>(language.getLexicon());

        setPadding(false);
        setSpacing(true);
        setSizeFull();
        setMinWidth("0");

        Span hint = new Span(T.localized(THE_NAME_GENERATOR_MAKES_ITS_NAMES));
        hint.addClassName(ConlangStyles.HINT);

        chooser.setItemLabelGenerator(NameCategory::getName);
        chooser.setAllowCustomValue(false);
        chooser.setWidth(UIUtils.isPhone() ? "100%" : "20rem");
        chooser.addValueChangeListener(_ -> chosen());
        Button add = new Button(T.localized(ADD), Solid.PLUS.create(), _ -> addCategory());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        renameButton = new Button(T.localized(RENAME), Solid.PEN.create(), _ -> renameCategory());
        deleteButton = new Button(T.localized(DELETE), Solid.TRASH.create(), _ -> deleteCategory());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        HorizontalLayout categoryRow = new HorizontalLayout(chooser, add, renameButton, deleteButton);
        categoryRow.setWidthFull();
        categoryRow.setWrap(true);
        categoryRow.setAlignItems(Alignment.END);

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
        if (UIUtils.isPhone()) {
            category.setWidth("100%");
        }
        count.addClassName(ConlangStyles.HINT);
        HorizontalLayout filters = new HorizontalLayout(search, category, count);
        filters.setWidthFull();
        filters.setWrap(true);
        filters.setAlignItems(Alignment.CENTER);

        boolean phone = UIUtils.isPhone();
        checkAll = tool(T.localized(CHECK_ALL_SHOWN), Solid.CHECK_DOUBLE, phone, () -> checkShown(true));
        uncheckAll = tool(T.localized(UNCHECK_ALL_SHOWN), Solid.SQUARE, phone, () -> checkShown(false));
        preview = tool(T.localized(PREVIEW_NAMES), Solid.WAND_MAGIC_SPARKLES, phone, this::preview);
        preview.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout actions = new HorizontalLayout(checkAll, uncheckAll, preview);
        actions.setWidthFull();
        actions.setWrap(true);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.addClassName(ConlangStyles.FILL);
        grid.setDataProvider(data);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addComponentColumn(this::checkbox).setHeader("").setFlexGrow(0).setWidth("4rem");
        grid.addComponentColumn(entry -> {
                    Span form = new Span(entry.getWord());
                    form.addClassName(ConlangStyles.WORD);
                    return form;
                }).setHeader(T.localized(WORD))
                .setSortable(true).setComparator((a, b) -> a.getWord().compareToIgnoreCase(b.getWord())).setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(EntryDialog::glosses).setHeader(T.localized(MEANING)).setFlexGrow(3);
        grid.addColumn(entry -> language.findCategory(entry.getCategory()) == null ? "" : language.findCategory(entry.getCategory()).getName())
                .setHeader(T.localized(CATEGORY)).setSortable(true).setFlexGrow(1);
        grid.getColumns().forEach(column -> column.setResizable(true));

        names.setReadOnly(true);
        names.setWidthFull();
        names.setMinHeight("5rem");
        names.setLabel(T.localized(PREVIEW_NAMES));
        names.setVisible(false);

        add(hint, categoryRow, filters, actions, grid, names);
        setFlexGrow(1, grid);
        refresh();
    }

    /** Shows the language again, after it changed elsewhere (a word added, deleted or retired; the categories of the lexicon). */
    public void refresh() {
        NameCategory current = chooser.getValue();
        chooser.setItems(language.getNameCategories());
        NameCategory still = current == null ? null : language.findNameCategory(current.getId());
        chooser.setValue(still != null ? still : language.getNameCategories().stream().findFirst().orElse(null));
        Category lexiconCategory = category.getValue();
        category.setItems(language.getCategories());
        category.setValue(lexiconCategory != null ? language.findCategory(lexiconCategory.getId()) : null);
        data.refreshAll();
        chosen();
    }

    private NameCategory current() {
        return chooser.getValue();
    }

    private void chosen() {
        boolean any = current() != null;
        renameButton.setEnabled(any);
        deleteButton.setEnabled(any);
        checkAll.setEnabled(any);
        uncheckAll.setEnabled(any);
        preview.setEnabled(any);
        grid.setEnabled(any);
        names.setVisible(false);
        names.clear();
        data.refreshAll();
        applyFilter();
    }

    private Checkbox checkbox(LexiconEntry entry) {
        Checkbox box = new Checkbox(current() != null && current().has(entry.getId()));
        box.setEnabled(current() != null);
        box.addValueChangeListener(event -> {
            NameCategory chosen = current();
            if (chosen != null && event.isFromClient() && chosen.set(entry.getId(), event.getValue())) {
                updateCount();
                onChange.run();
            }
        });
        return box;
    }

    /** The words the lexicon filters show: no retired word is offered (one that is chosen and retired later is left out by the generator). */
    private void applyFilter() {
        String text = StringUtils.defaultString(search.getValue()).strip().toLowerCase(Locale.ROOT);
        Category chosenCategory = category.getValue();
        data.setFilter(entry -> entry.getStatus() != EntryStatus.RETIRED
                && (chosenCategory == null || chosenCategory.getId().equals(entry.getCategory()))
                && (text.isEmpty() || entry.getWord().toLowerCase(Locale.ROOT).contains(text)
                || entry.getSenses().stream().anyMatch(sense -> sense.getGloss().toLowerCase(Locale.ROOT).contains(text))));
        updateCount();
    }

    private void updateCount() {
        NameCategory chosen = current();
        long shown = data.size(new Query<>());
        count.setText(chosen == null ? T.localized(ADD_A_NAME_CATEGORY_FIRST)
                : T.localized(D_WORDS_CHOSEN_D_SHOWN).formatted(language.nameWords(chosen).size(), shown));
    }

    /** Checks or unchecks the words that are shown (the search and the category filter apply), the ones that are not shown stay as they were. */
    private void checkShown(boolean checked) {
        NameCategory chosen = current();
        if (chosen == null) {
            return;
        }
        boolean changed = false;
        for (LexiconEntry entry : data.fetch(new Query<>()).toList()) {
            changed |= chosen.set(entry.getId(), checked);
        }
        data.refreshAll();
        updateCount();
        if (changed) {
            onChange.run();
        }
    }

    // ---- the categories

    private void addCategory() {
        nameDialog(T.localized(NEW_NAME_CATEGORY), null, name -> {
            NameCategory created = new NameCategory(name);
            language.getNameCategories().add(created);
            onChange.run();
            chooser.setItems(language.getNameCategories());
            chooser.setValue(created);
        });
    }

    private void renameCategory() {
        NameCategory chosen = current();
        if (chosen != null) {
            nameDialog(T.localized(RENAME_THE_NAME_CATEGORY), chosen, name -> {
                chosen.setName(name);
                onChange.run();
                chooser.setItems(language.getNameCategories());
                chooser.setValue(chosen);
            });
        }
    }

    private void deleteCategory() {
        NameCategory chosen = current();
        if (chosen != null) {
            ConfirmDialog.show(T.localized(DELETE_S).formatted(chosen.getName()), () -> {
                language.getNameCategories().remove(chosen);
                onChange.run();
                refresh();
            });
        }
    }

    /** Asks for the name of a category; {@code editing} is the category being renamed, null for a new one. */
    private void nameDialog(String title, NameCategory editing, java.util.function.Consumer<String> accept) {
        TextField field = new TextField(T.localized(NAME));
        field.setWidthFull();
        field.setValue(editing == null ? "" : editing.getName());
        new EditDialog(title, new VerticalLayout(field), () -> {
            String name = field.getValue().strip();
            if (name.isEmpty()) {
                return T.localized(THE_NAME_IS_EMPTY);
            }
            boolean taken = language.getNameCategories().stream().anyMatch(other -> other != editing && other.getName().equalsIgnoreCase(name));
            return taken ? T.localized(THE_LANGUAGE_HAS_A_NAME_CATEGORY_OF_THAT_NAME) : null;
        }, () -> accept.accept(field.getValue().strip())).open();
    }

    // ---- the preview

    /** Makes names of the words that are checked now (all of them, not only the shown ones), with what is in the window, saved or not. */
    private void preview() {
        NameCategory chosen = current();
        if (chosen == null) {
            return;
        }
        List<String> words = language.nameWords(chosen);
        if (words.isEmpty()) {
            Notification.error(T.localized(CHECK_SOME_WORDS_FIRST));
            return;
        }
        if (!bridge.available()) {
            Notification.error(T.localized(THE_NAME_GENERATOR_IS_NOT_INSTALLED));
            return;
        }
        try {
            List<String> made = bridge.preview(words, PREVIEW_COUNT);
            names.setValue(made.isEmpty() ? T.localized(TOO_FEW_WORDS_TO_MAKE_NAMES) : String.join(", ", made));
            names.setVisible(true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.error(e.getMessage());
        }
    }

    /** A button of a row of actions: icon and name, on a phone the icon with the name as its tooltip. */
    private static Button tool(String name, Solid icon, boolean iconOnly, Runnable action) {
        Button button = iconOnly ? new Button(icon.create(), _ -> action.run()) : new Button(name, icon.create(), _ -> action.run());
        if (iconOnly) {
            button.setTooltipText(name);
            button.setAriaLabel(name);
        }
        return button;
    }
}
