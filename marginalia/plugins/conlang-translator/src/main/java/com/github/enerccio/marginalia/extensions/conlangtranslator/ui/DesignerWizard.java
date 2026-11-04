package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerPrompts;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerStage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Audience;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Category;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RuleSection;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.DesignerService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Makes a new language with the designer, in stages: the brief (the author's), then the sound system, the writing, the grammar and the word
 * formation (each a section of the rules that the model proposes, the author edits or refines with a note and accepts), then the seed lexicon
 * (words by category, reviewed like any proposal) and a test (sentences translated with the new language). The author can go back to any
 * stage, skip one and write it by hand later in the editor. The language exists only inside this window until <i>Finish</i>, which gives it
 * to the caller.
 */
public class DesignerWizard extends Dialog {

    /** The categories a language usually wants; <i>Add the basic categories</i> puts the missing ones in. */
    private static final List<String> BASIC_CATEGORIES = List.of("Pronouns & particles", "People & social", "Body", "Nature & weather", "Animals", "Food & drink",
            "Things & tools", "Places", "Numbers", "Time", "Verbs of motion", "Verbs of action", "Qualities", "War & weapons");

    private final Language language = new Language();
    private final Consumer<Language> onFinish;
    private final Predicate<String> nameTaken;
    private final List<Choice> lorebooks;
    private final List<Choice> books;
    private final DesignerService service = new DesignerService();

    private final TextField name = new TextField(T.localized(NAME));
    private final TextArea purpose = new TextArea(T.localized(WHAT_THE_LANGUAGE_IS_FOR));
    private final TextArea inspirations = new TextArea(T.localized(INSPIRATIONS));
    private final TextArea constraints = new TextArea(T.localized(CONSTRAINTS_AND_WHAT_TO_AVOID));
    private final ComboBox<Choice> lorebook = new ComboBox<>(T.localized(EXAMPLE_LOREBOOK));
    private final ComboBox<Choice> book = new ComboBox<>(T.localized(EXAMPLE_BOOK));

    private final List<Component> steps = new ArrayList<>();
    private final List<String> stepNames = new ArrayList<>();
    private final VerticalLayout holder = new VerticalLayout();
    private final Span where = new Span();
    private final Button back = new Button(T.localized(BACK));
    private final Button next = new Button(T.localized(NEXT));
    private int step;

    /**
     * @param nameTaken whether a language of the library has the name
     * @param lorebooks the lorebooks of the user, for the world the language is for
     * @param books     the books of the user, for the example book
     * @param onFinish  given the new language after <i>Finish</i>
     */
    public DesignerWizard(Predicate<String> nameTaken, List<Choice> lorebooks, List<Choice> books, Consumer<Language> onFinish) {
        this.nameTaken = nameTaken;
        this.lorebooks = lorebooks;
        this.books = books;
        this.onFinish = onFinish;

        setHeaderTitle(T.localized(DESIGN_A_LANGUAGE));
        setWidth("1000px");
        setHeight("84vh");
        setMaxWidth("98vw");
        setMaxHeight("98vh");
        setCloseOnOutsideClick(false);
        setResizable(true);
        setDraggable(true);

        steps.add(briefStep());
        stepNames.add(T.localized(BRIEF));
        for (DesignerStage stage : DesignerStage.values()) {
            steps.add(new StagePanel(stage));
            stepNames.add(T.localized(stageName(stage)));
        }
        steps.add(lexiconStep());
        stepNames.add(T.localized(SEED_LEXICON));
        steps.add(testStep());
        stepNames.add(T.localized(TEST));

        where.addClassName(ConlangStyles.WORD);
        holder.setPadding(false);
        holder.setSizeFull();
        VerticalLayout content = new VerticalLayout(where, holder);
        content.setPadding(false);
        content.setSizeFull();
        content.setFlexGrow(1, holder);
        add(content);

        back.addClickListener(_ -> go(step - 1));
        next.addClickListener(_ -> {
            if (step == steps.size() - 1) {
                finish();
            } else {
                go(step + 1);
            }
        });
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> cancel()), back, next);
        go(0);
    }

    static String stageName(DesignerStage stage) {
        return switch (stage) {
            case SOUNDS -> STAGE_SOUNDS;
            case WRITING -> STAGE_WRITING;
            case GRAMMAR -> STAGE_GRAMMAR;
            case WORD_FORMATION -> STAGE_WORD_FORMATION;
        };
    }

    // ---- moving

    private void go(int to) {
        if (to < 0 || to >= steps.size()) {
            return;
        }
        if (step == 0 && to > 0 && !readBrief()) {
            return;
        }
        step = to;
        holder.removeAll();
        holder.add(steps.get(to));
        holder.setFlexGrow(1, steps.get(to));
        where.setText(T.localized(STEP_D_OF_D_S).formatted(to + 1, steps.size(), stepNames.get(to)));
        back.setEnabled(to > 0);
        next.setText(to == steps.size() - 1 ? T.localized(FINISH) : T.localized(NEXT));
        if (steps.get(to) instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
    }

    private void cancel() {
        if (language.getRules().isEmpty() && language.getLexicon().isEmpty()) {
            close();
        } else {
            ConfirmDialog.show(T.localized(DISCARD_THE_NEW_LANGUAGE), this::close);
        }
    }

    private void finish() {
        if (!readBrief()) {
            go(0);
            return;
        }
        close();
        onFinish.accept(language);
    }

    /** A step that shows the language and has to be told when it changed. */
    private interface Refreshable {
        void refresh();
    }

    // ---- the brief

    private Component briefStep() {
        name.setWidthFull();
        purpose.setWidthFull();
        purpose.setMinHeight("8rem");
        purpose.setHelperText(T.localized(THE_PEOPLE_THEIR_WORLD));
        inspirations.setWidthFull();
        inspirations.setMinHeight("5rem");
        inspirations.setHelperText(T.localized(HARSH_LIKE_OLD_NORSE));
        constraints.setWidthFull();
        constraints.setMinHeight("5rem");
        constraints.setHelperText(T.localized(NO_SOUNDS_AN_ENGLISH_READER_CANNOT_SAY));
        lorebook.setWidthFull();
        lorebook.setItems(lorebooks);
        lorebook.setItemLabelGenerator(Choice::label);
        lorebook.setClearButtonVisible(true);
        lorebook.setHelperText(T.localized(THE_DESIGNER_READS_THE_WORLD));
        book.setWidthFull();
        book.setItems(books);
        book.setItemLabelGenerator(Choice::label);
        book.setClearButtonVisible(true);
        book.setHelperText(T.localized(THE_DESIGNER_TESTS_ON_ITS_SPEECH));
        VerticalLayout layout = new VerticalLayout(name, purpose, inspirations, constraints, lorebook, book);
        layout.setPadding(false);
        layout.setWidthFull();
        VerticalLayout scroll = new VerticalLayout(layout);
        scroll.setPadding(false);
        scroll.setSizeFull();
        scroll.addClassName(ConlangStyles.SCROLL);
        return scroll;
    }

    /** Writes the brief into the language: its name, its links and a section for the designer only. */
    private boolean readBrief() {
        String chosen = name.getValue() == null ? "" : name.getValue().strip();
        if (chosen.isEmpty()) {
            Notification.error(T.localized(THE_NAME_IS_EMPTY));
            return false;
        }
        if (nameTaken.test(chosen)) {
            Notification.error(T.localized(ANOTHER_LANGUAGE_HAS_THE_NAME));
            return false;
        }
        language.setName(chosen);
        language.setExampleLorebook(lorebook.getValue() == null ? null : lorebook.getValue().id());
        language.setExampleBook(book.getValue() == null ? null : book.getValue().id());
        language.getRules().removeIf(section -> BRIEF_ID.equals(section.getId()));
        String text = briefText();
        if (!text.isBlank()) {
            RuleSection brief = new RuleSection(T.localized(BRIEF), text, Audience.DESIGNER);
            brief.setId(BRIEF_ID);
            language.getRules().addFirst(brief);
            language.setDescription(StringUtils.abbreviate(purpose.getValue() == null ? "" : purpose.getValue().strip().replaceAll("\\s+", " "), 300));
        }
        return true;
    }

    private static final String BRIEF_ID = "designer-brief";

    private String briefText() {
        StringBuilder text = new StringBuilder();
        append(text, "What the language is for", purpose.getValue());
        append(text, "Inspirations", inspirations.getValue());
        append(text, "Constraints and what to avoid", constraints.getValue());
        return text.toString().strip();
    }

    private static void append(StringBuilder text, String heading, String value) {
        if (value != null && !value.isBlank()) {
            text.append(heading).append(":\n").append(value.strip()).append("\n\n");
        }
    }

    // ---- a stage: the model proposes a section, the author refines and accepts

    private final class StagePanel extends VerticalLayout {

        private final DesignerStage stage;
        private final TextArea note = new TextArea(T.localized(TELL_THE_DESIGNER_WHAT_YOU_WANT));
        private final TextField title = new TextField(T.localized(TITLE));
        private final TextArea text = new TextArea(T.localized(TEXT));
        private final Span state = new Span();

        StagePanel(DesignerStage stage) {
            this.stage = stage;
            setPadding(false);
            setSizeFull();
            addClassName(ConlangStyles.SCROLL);

            Span about = new Span(T.localized(THE_DESIGNER_PROPOSES_THE_SECTION).formatted(T.localized(stageName(stage))));
            about.addClassName(ConlangStyles.HINT);
            note.setWidthFull();
            note.setMinHeight("4rem");
            note.setHelperText(T.localized(EMPTY_THE_FIRST_PROPOSAL_FROM_THE_BRIEF));
            title.setWidthFull();
            title.setValue(T.localized(stageName(stage)));
            text.setWidthFull();
            text.setMinHeight("18rem");
            text.setHelperText(T.localized(EDIT_THE_PROPOSAL_YOURSELF));
            state.addClassName(ConlangStyles.HINT);

            Button propose = new Button(T.localized(PROPOSE), _ -> propose());
            propose.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            Button accept = new Button(T.localized(ACCEPT), _ -> accept());
            com.vaadin.flow.component.orderedlayout.HorizontalLayout buttons = new com.vaadin.flow.component.orderedlayout.HorizontalLayout(propose, accept);
            buttons.setWrap(true);
            add(about, note, buttons, title, text, state);
        }

        private void propose() {
            if (!readBrief()) {
                return;
            }
            Language snapshot = ConlangStore.copy(language);
            // the section being written is not in the context as a rule: it is the proposal that is refined
            snapshot.getRules().removeIf(section -> ("designer-stage-" + stage.name()).equals(section.getId()));
            String previous = text.getValue() == null || text.getValue().isBlank() ? "" : title.getValue() + "\n" + text.getValue();
            String brief = briefText();
            String wanted = note.getValue() == null ? "" : note.getValue();
            Designing.run(T.localized(THE_DESIGNER_IS_WORKING), () -> service.answer(DesignerPrompts.stage(snapshot, stage, brief, previous, wanted, service.lore(snapshot))),
                    answer -> {
                        if (answer.sections().isEmpty()) {
                            Notification.warning(answer.message().isBlank() ? T.localized(THE_DESIGNER_PROPOSES_NOTHING) : answer.message());
                            return;
                        }
                        DesignerAnswer.Section section = answer.sections().getFirst();
                        title.setValue(StringUtils.defaultIfBlank(section.title(), stage.title()));
                        text.setValue(StringUtils.defaultString(section.text()));
                        state.setText(answer.message());
                    });
        }

        private void accept() {
            if (text.getValue() == null || text.getValue().isBlank()) {
                Notification.error(T.localized(THE_SECTION_IS_EMPTY));
                return;
            }
            String id = "designer-stage-" + stage.name();
            RuleSection section = language.getRules().stream().filter(rule -> id.equals(rule.getId())).findFirst().orElseGet(() -> {
                RuleSection created = new RuleSection();
                created.setId(id);
                language.getRules().add(created);
                return created;
            });
            section.setTitle(StringUtils.defaultIfBlank(title.getValue(), stage.title()).strip());
            section.setText(text.getValue().strip());
            section.setAudience(Audience.MODEL);
            state.setText(T.localized(ACCEPTED_IN_THE_RULES));
            Notification.success(T.localized(ACCEPTED_IN_THE_RULES));
        }
    }

    // ---- the seed lexicon

    private Component lexiconStep() {
        final class LexiconPanel extends VerticalLayout implements Refreshable {
            private final Grid<Category> grid = new Grid<>();
            private final ComboBox<String> category = new ComboBox<>(T.localized(CATEGORY));
            private final IntegerField count = new IntegerField(T.localized(HOW_MANY_WORDS));
            private final TextArea note = new TextArea(T.localized(WHAT_THE_WORDS_ARE_FOR));
            private final Span total = new Span();

            LexiconPanel() {
                setPadding(false);
                setSizeFull();
                addClassName(ConlangStyles.SCROLL);
                Span about = new Span(T.localized(THE_SEED_LEXICON_IS_MADE_BY_CATEGORY));
                about.addClassName(ConlangStyles.HINT);
                total.addClassName(ConlangStyles.HINT);
                Button basics = new Button(T.localized(ADD_THE_BASIC_CATEGORIES), _ -> {
                    BASIC_CATEGORIES.stream().filter(wanted -> language.getCategories().stream().noneMatch(existing -> existing.getName().equalsIgnoreCase(wanted)))
                            .forEach(wanted -> language.getCategories().add(new Category(wanted)));
                    refresh();
                });
                category.setWidthFull();
                category.setAllowCustomValue(true);
                category.addCustomValueSetListener(event -> category.setValue(event.getDetail()));
                category.setHelperText(T.localized(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE));
                count.setWidthFull();
                count.setMin(1);
                count.setMax(200);
                count.setStepButtonsVisible(true);
                count.setValue(30);
                note.setWidthFull();
                note.setMinHeight("4rem");
                Button generate = new Button(T.localized(GENERATE_WORDS), _ -> generate());
                generate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
                grid.setHeight("10rem");
                grid.addColumn(Category::getName).setHeader(T.localized(CATEGORY)).setFlexGrow(1);
                grid.addColumn(item -> language.entriesOf(item.getId()).size()).setHeader(T.localized(WORDS)).setFlexGrow(0).setAutoWidth(true);
                grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(item -> category.setValue(item.getName())));
                com.vaadin.flow.component.formlayout.FormLayout request = new com.vaadin.flow.component.formlayout.FormLayout(category, count);
                request.setWidthFull();
                request.setResponsiveSteps(new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1),
                        new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("36rem", 2));
                note.setMinHeight("3rem");
                add(about, total, basics, grid, request, note, generate);
            }

            private void generate() {
                String chosen = category.getValue();
                int words = count.getValue() == null ? 30 : count.getValue();
                String wanted = note.getValue() == null ? "" : note.getValue();
                if (!readBrief()) {
                    return;
                }
                Language snapshot = ConlangStore.copy(language);
                Designing.run(T.localized(THE_DESIGNER_IS_WORKING), () -> service.propose(snapshot, DesignerPrompts.words(snapshot, chosen, words, wanted, service.lore(snapshot))),
                        proposal -> Proposals.show(T.localized(GENERATE_WORDS), language, proposal, this::refresh));
            }

            @Override
            public void refresh() {
                grid.setItems(language.getCategories());
                category.setItems(language.getCategories().stream().map(Category::getName).toList());
                total.setText(T.localized(D_WORDS_D_NEW).formatted(language.getLexicon().size(), language.newEntries().size()));
            }
        }
        return new LexiconPanel();
    }

    // ---- the test

    private Component testStep() {
        final class TestPanel extends VerticalLayout {
            private final TextArea sentences = new TextArea(T.localized(SENTENCES_ONE_PER_LINE));
            private final Grid<DesignerAnswer.Sample> grid = new Grid<>();

            TestPanel() {
                setPadding(false);
                setSizeFull();
                addClassName(ConlangStyles.SCROLL);
                Span about = new Span(T.localized(THE_DESIGNER_TRANSLATES_SAMPLES));
                about.addClassName(ConlangStyles.HINT);
                sentences.setWidthFull();
                sentences.setMinHeight("8rem");
                sentences.setValue("The legion marches at dawn.\nHold the line, and do not look back.\nMy brother died at the gate.");
                Button translate = new Button(T.localized(TRANSLATE_THE_SENTENCES), _ -> translate());
                translate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
                grid.setHeight("16rem");
                grid.addColumn(DesignerAnswer.Sample::source).setHeader(T.localized(SENTENCE)).setFlexGrow(2);
                grid.addColumn(DesignerAnswer.Sample::text).setHeader(T.localized(IN_THE_LANGUAGE)).setFlexGrow(2);
                grid.addColumn(DesignerAnswer.Sample::note).setHeader(T.localized(NOTE)).setFlexGrow(3);
                grid.getColumns().forEach(column -> column.setResizable(true));
                add(about, sentences, translate, grid);
            }

            private void translate() {
                if (!readBrief()) {
                    return;
                }
                List<String> lines = new ArrayList<>();
                for (String line : (sentences.getValue() == null ? "" : sentences.getValue()).split("\n")) {
                    if (!line.isBlank()) {
                        lines.add(line.strip());
                    }
                }
                if (lines.isEmpty()) {
                    return;
                }
                Language snapshot = ConlangStore.copy(language);
                Designing.run(T.localized(THE_DESIGNER_IS_WORKING), () -> service.answer(DesignerPrompts.samples(snapshot, lines)), answer -> {
                    grid.setItems(answer.samples());
                    if (!answer.words().isEmpty()) {
                        Proposals.show(T.localized(NEW_WORDS_OF_THE_SAMPLES), language, service.proposal(ConlangStore.copy(language), answer), () -> {
                        });
                    }
                });
            }
        }
        return new TestPanel();
    }
}
