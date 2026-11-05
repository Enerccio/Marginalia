package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.ListEditor;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The window in which a language is edited: the name and the options, the rules, the categories and registers, and the lexicon.
 * It works on a copy: nothing the library holds changes until <i>Save</i>, which gives the edited copy to the caller.
 */
public class LanguageEditorDialog extends Dialog {

    private final Language language;
    private final Consumer<Language> onSave;
    private final Predicate<String> nameTaken;
    private final List<Choice> books;
    private final List<Choice> lorebooks;

    private final TextField name = new TextField(T.localized(NAME));
    private final TextArea description = new TextArea(T.localized(DESCRIPTION));
    private final Checkbox hyphen = new Checkbox(T.localized(A_HYPHEN_IS_PART_OF_A_WORD));
    private final ComboBox<Choice> exampleBook = new ComboBox<>(T.localized(EXAMPLE_BOOK));
    private final ComboBox<Choice> exampleLorebook = new ComboBox<>(T.localized(EXAMPLE_LOREBOOK));
    private final Button maximize = new Button(Solid.EXPAND.create());

    private LexiconTab lexicon;
    private CategoriesList categoriesList;
    private RegistersList registersList;
    private RulesList rulesList;
    private final MenuBar designer = new MenuBar();
    private boolean maximized;

    /**
     * @param language  the language to edit, which is not changed: the dialog works on a copy
     * @param nameTaken whether another language of the library has the name
     * @param books     the books of the user, to choose the example book from
     * @param lorebooks the lorebooks of the user, to choose the example lorebook from
     * @param onSave    given the edited copy after <i>Save</i>
     */
    public LanguageEditorDialog(Language language, Predicate<String> nameTaken, List<Choice> books, List<Choice> lorebooks,
                                Consumer<Language> onSave) {
        this.language = com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore.copy(language);
        this.onSave = onSave;
        this.nameTaken = nameTaken;
        this.books = books;
        this.lorebooks = lorebooks;

        setHeaderTitle(T.localized(LANGUAGE_S).formatted(language.getName().isBlank() ? T.localized(NEW_LANGUAGE) : language.getName()));
        setWidth("1180px");
        setHeight("82vh");
        setMaxWidth("98vw");
        setMaxHeight("98vh");
        setCloseOnOutsideClick(false);
        setResizable(true);
        setDraggable(true);

        maximize.setTooltipText(T.localized(MAXIMIZE));
        maximize.setAriaLabel(T.localized(MAXIMIZE));
        maximize.addClickListener(_ -> toggleMaximized());
        getHeader().add(designerMenu(), maximize);

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add(T.localized(OVERVIEW), overview());
        tabs.add(T.localized(RULES), rules());
        tabs.add(T.localized(CATEGORIES), categories());
        tabs.add(T.localized(REGISTERS), registers());
        lexicon = new LexiconTab(this.language, () -> {
        });
        tabs.add(T.localized(LEXICON), lexicon);
        tabs.addSelectedChangeListener(_ -> {
            // the counts of the words and the choices of the lexicon follow what the other tabs changed
            categoriesList.refresh();
            registersList.refresh();
            lexicon.refresh();
        });
        VerticalLayout content = new VerticalLayout(tabs);
        content.setPadding(false);
        content.setSizeFull();
        add(content);

        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        Button save = new Button(T.localized(SAVE), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
    }

    // ---- the designer

    private MenuBar designerMenu() {
        designer.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        MenuItem root = designer.addItem(T.localized(DESIGN_WITH_AI));
        root.getSubMenu().addItem(T.localized(GENERATE_WORDS), _ -> design(DesignerDialog.Action.WORDS));
        root.getSubMenu().addItem(T.localized(ADD_A_SECTION_WITH_AI), _ -> design(DesignerDialog.Action.SECTION));
        root.getSubMenu().addItem(T.localized(REWRITE_A_SECTION), _ -> design(DesignerDialog.Action.REWRITE));
        root.getSubMenu().addItem(T.localized(REVIEW_THE_LANGUAGE), _ -> design(DesignerDialog.Action.REVIEW));
        root.getSubMenu().addItem(T.localized(ASK_THE_DESIGNER), _ -> ask());
        return designer;
    }

    /** Opens an action of the designer; what is selected on the tab (a category, a section) is what the action is about. */
    private void design(DesignerDialog.Action action) {
        String about = null;
        if (action == DesignerDialog.Action.WORDS && categoriesList.selected() != null) {
            about = categoriesList.selected().getName();
        } else if (action == DesignerDialog.Action.REWRITE && rulesList.selected() != null) {
            about = rulesList.selected().getTitle();
        }
        new DesignerDialog(action, language, about, this::afterDesigner).open();
    }

    private void ask() {
        new AskDialog(language, this::afterDesigner).open();
    }

    /** The designer wrote into the language: every tab shows it. */
    private void afterDesigner() {
        rulesList.refresh();
        categoriesList.refresh();
        registersList.refresh();
        lexicon.refresh();
    }

    private void toggleMaximized() {
        maximized = !maximized;
        setWidth(maximized ? "100vw" : "1180px");
        setHeight(maximized ? "100vh" : "82vh");
        maximize.setIcon(maximized ? Solid.COMPRESS.create() : Solid.EXPAND.create());
        maximize.setTooltipText(maximized ? T.localized(RESTORE_SIZE) : T.localized(MAXIMIZE));
    }

    private void save() {
        String chosen = name.getValue().strip();
        if (chosen.isEmpty()) {
            Notification.error(T.localized(THE_NAME_IS_EMPTY));
            return;
        }
        if (nameTaken.test(chosen)) {
            Notification.error(T.localized(ANOTHER_LANGUAGE_HAS_THE_NAME));
            return;
        }
        language.setName(chosen);
        language.setDescription(description.getValue().strip());
        language.setHyphenStructural(hyphen.getValue());
        language.setExampleBook(exampleBook.getValue() == null ? null : exampleBook.getValue().id());
        language.setExampleLorebook(exampleLorebook.getValue() == null ? null : exampleLorebook.getValue().id());
        close();
        onSave.accept(language);
    }

    // ---- the tabs

    private Component overview() {
        name.setWidthFull();
        name.setValue(language.getName());
        description.setWidthFull();
        description.setMinHeight("6rem");
        description.setValue(language.getDescription());
        description.setHelperText(T.localized(WHAT_THE_LANGUAGE_IS_AND_WHO));
        hyphen.setValue(language.isHyphenStructural());
        Span hyphenHint = new Span(T.localized(OFF_THE_SAME_COMPOUND_WITH_AND_WITHOUT));
        hyphenHint.addClassName(ConlangStyles.HINT);

        configureLink(exampleBook, books, language.getExampleBook(), T.localized(THE_DESIGNER_TESTS_ON_ITS_SPEECH));
        configureLink(exampleLorebook, lorebooks, language.getExampleLorebook(), T.localized(THE_DESIGNER_READS_THE_WORLD));

        FormLayout links = new FormLayout(exampleBook, exampleLorebook);
        links.setWidthFull();
        links.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("40rem", 2));
        VerticalLayout layout = new VerticalLayout(name, description, hyphen, hyphenHint, links);
        layout.setPadding(false);
        layout.setSizeFull();
        return layout;
    }

    private void configureLink(ComboBox<Choice> box, List<Choice> items, String id, String help) {
        box.setWidthFull();
        box.setItems(items);
        box.setItemLabelGenerator(Choice::label);
        box.setClearButtonVisible(true);
        Choice found = items.stream().filter(item -> item.id().equals(id)).findFirst().orElse(null);
        box.setValue(found);
        box.setHelperText(id != null && found == null ? T.localized(IT_DOES_NOT_EXIST_ANY_MORE) : help);
    }

    private Component rules() {
        rulesList = new RulesList(language.getRules());
        return tab(new Span(T.localized(THE_RULES_ARE_TEXT_THE_PLUGIN)), rulesList);
    }

    private Component categories() {
        categoriesList = new CategoriesList(language);
        return tab(new Span(T.localized(THE_LEXICON_IS_SHOWN_AND_SENT)), categoriesList);
    }

    private Component registers() {
        registersList = new RegistersList(language);
        return tab(new Span(T.localized(THE_MODEL_CHOOSES_THE_FORM)), registersList);
    }

    /** A tab: a hint line on top, the rest takes the height. */
    private static VerticalLayout tab(Span hint, Component... rest) {
        hint.addClassName(ConlangStyles.HINT);
        VerticalLayout layout = new VerticalLayout(hint);
        layout.add(rest);
        layout.setPadding(false);
        layout.setSizeFull();
        layout.setMinWidth("0");
        layout.getChildren().reduce((a, b) -> b).ifPresent(last -> layout.setFlexGrow(1, last));
        return layout;
    }

    // ---- the lists

    private final class RulesList extends ListEditor<RuleSection> {

        RulesList(List<RuleSection> sections) {
            super(sections, () -> {
            });
            Button rewrite = new Button(T.localized(REWRITE_WITH_AI), Solid.WAND_MAGIC_SPARKLES.create(), _ -> design(DesignerDialog.Action.REWRITE));
            addToolbarButton(rewrite);
            build();
        }

        @Override
        protected void addColumns(Grid<RuleSection> grid) {
            grid.addColumn(RuleSection::getTitle).setHeader(T.localized(TITLE)).setFlexGrow(1);
            grid.addColumn(section -> T.localized(audienceLabel(section.getAudience()))).setHeader(T.localized(READ_BY)).setFlexGrow(0).setAutoWidth(true);
            grid.addColumn(section -> summary(section.getText())).setHeader(T.localized(TEXT)).setFlexGrow(4);
        }

        @Override
        protected RuleSection create() {
            return new RuleSection();
        }

        @Override
        protected void edit(RuleSection section, boolean isNew, Runnable done) {
            TextField title = new TextField(T.localized(TITLE));
            title.setWidthFull();
            title.setValue(section.getTitle());
            ComboBox<Audience> audience = new ComboBox<>(T.localized(READ_BY));
            audience.setWidthFull();
            audience.setItems(Audience.values());
            audience.setItemLabelGenerator(value -> T.localized(audienceLabel(value)));
            audience.setAllowCustomValue(false);
            audience.setValue(section.getAudience());
            audience.setHelperText(T.localized(THE_TRANSLATOR_GETS_MODEL_SECTIONS));
            TextArea text = new TextArea(T.localized(TEXT));
            text.setWidthFull();
            text.setHeight("45vh");
            text.setValue(section.getText());
            EditDialog dialog = new EditDialog(isNew ? T.localized(NEW_SECTION) : T.localized(EDIT_SECTION), new VerticalLayout(title, audience, text), () -> {
                if (StringUtils.isBlank(title.getValue())) {
                    return T.localized(THE_TITLE_IS_EMPTY);
                }
                section.setTitle(title.getValue().strip());
                section.setAudience(audience.getValue() == null ? Audience.MODEL : audience.getValue());
                section.setText(text.getValue());
                return null;
            }, done);
            dialog.setWidth("900px");
            dialog.open();
        }

        @Override
        protected String labelOf(RuleSection section) {
            return section.getTitle();
        }
    }

    private final class CategoriesList extends ListEditor<Category> {

        private final Language owner;

        CategoriesList(Language language) {
            super(language.getCategories(), () -> {
            });
            this.owner = language;
            Button generate = new Button(T.localized(GENERATE_WORDS), Solid.WAND_MAGIC_SPARKLES.create(), _ -> design(DesignerDialog.Action.WORDS));
            addToolbarButton(generate);
            build();
        }

        @Override
        protected void addColumns(Grid<Category> grid) {
            grid.addColumn(Category::getName).setHeader(T.localized(NAME)).setFlexGrow(1);
            grid.addColumn(category -> owner.entriesOf(category.getId()).size()).setHeader(T.localized(WORDS)).setFlexGrow(0).setAutoWidth(true);
            grid.addColumn(category -> category.isAlwaysSend() ? T.localized(YES_TEXT) : "").setHeader(T.localized(ALWAYS_SENT))
                    .setFlexGrow(0).setAutoWidth(true);
            grid.addColumn(Category::getNote).setHeader(T.localized(NOTE)).setFlexGrow(3);
        }

        @Override
        protected Category create() {
            return new Category();
        }

        @Override
        protected void edit(Category category, boolean isNew, Runnable done) {
            TextField title = new TextField(T.localized(NAME));
            title.setWidthFull();
            title.setValue(category.getName());
            TextArea note = new TextArea(T.localized(NOTE));
            note.setWidthFull();
            note.setValue(category.getNote());
            Checkbox always = new Checkbox(T.localized(ALWAYS_SEND_THE_WHOLE_CATEGORY));
            always.setValue(category.isAlwaysSend());
            Span hint = new Span(T.localized(PARTICLES_NUMBERS_KINSHIP));
            hint.addClassName(ConlangStyles.HINT);
            new EditDialog(isNew ? T.localized(NEW_CATEGORY) : T.localized(EDIT_CATEGORY), new VerticalLayout(title, note, always, hint), () -> {
                if (StringUtils.isBlank(title.getValue())) {
                    return T.localized(THE_NAME_IS_EMPTY);
                }
                category.setName(title.getValue().strip());
                category.setNote(note.getValue().strip());
                category.setAlwaysSend(always.getValue());
                return null;
            }, done).open();
        }

        @Override
        protected void remove(Category category) {
            owner.deleteCategory(category.getId());
        }

        @Override
        protected String labelOf(Category category) {
            return category.getName();
        }
    }

    private final class RegistersList extends ListEditor<Register> {

        private final Language owner;

        RegistersList(Language language) {
            super(language.getRegisters(), () -> {
            });
            this.owner = language;
            build();
        }

        @Override
        protected void addColumns(Grid<Register> grid) {
            grid.addColumn(Register::getName).setHeader(T.localized(NAME)).setFlexGrow(1);
            grid.addColumn(Register::getNote).setHeader(T.localized(WHEN_IT_IS_USED)).setFlexGrow(4);
        }

        @Override
        protected Register create() {
            return new Register();
        }

        @Override
        protected void edit(Register register, boolean isNew, Runnable done) {
            TextField title = new TextField(T.localized(NAME));
            title.setWidthFull();
            title.setValue(register.getName());
            TextArea note = new TextArea(T.localized(WHEN_IT_IS_USED));
            note.setWidthFull();
            note.setValue(register.getNote());
            new EditDialog(isNew ? T.localized(NEW_REGISTER) : T.localized(EDIT_REGISTER), new VerticalLayout(title, note), () -> {
                if (StringUtils.isBlank(title.getValue())) {
                    return T.localized(THE_NAME_IS_EMPTY);
                }
                register.setName(title.getValue().strip());
                register.setNote(note.getValue().strip());
                return null;
            }, done).open();
        }

        @Override
        protected void remove(Register register) {
            owner.deleteRegister(register.getId());
        }

        @Override
        protected String labelOf(Register register) {
            return register.getName();
        }
    }

    static String audienceLabel(Audience audience) {
        return switch (audience) {
            case MODEL -> AUDIENCE_TRANSLATOR;
            case DESIGNER -> AUDIENCE_DESIGNER;
            case NONE -> AUDIENCE_NOBODY;
        };
    }

    static String summary(String text) {
        if (text == null) {
            return "";
        }
        String line = text.replaceAll("\\s+", " ").strip();
        return line.length() > 200 ? line.substring(0, 197) + "..." : line;
    }
}
