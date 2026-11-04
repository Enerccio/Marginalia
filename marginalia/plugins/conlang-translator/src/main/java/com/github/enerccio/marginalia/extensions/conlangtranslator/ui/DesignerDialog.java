package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerPrompts;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Category;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RuleSection;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.DesignerService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The window of one action of the designer on a language: a form for what the action needs, then the request, then the proposal in the review
 * window. Nothing is written until the user picks and applies; what is applied goes into the language the window was given (the copy the
 * editor works on), and {@code changed} tells the editor to show it.
 */
public class DesignerDialog extends Dialog {

    /** What the designer is asked to do. */
    public enum Action {
        /** New words for a category. */
        WORDS,
        /** A new section of the rules, and the words it needs. */
        SECTION,
        /** A rewrite of a section. */
        REWRITE,
        /** A review of the rules and the lexicon: findings. */
        REVIEW
    }

    private final Action action;
    private final Language language;
    private final Runnable changed;
    private final DesignerService service = new DesignerService();

    private final ComboBox<String> category = new ComboBox<>(T.localized(CATEGORY));
    private final IntegerField count = new IntegerField(T.localized(HOW_MANY_WORDS));
    private final ComboBox<RuleSection> section = new ComboBox<>(T.localized(SECTION));
    private final TextArea note = new TextArea();

    /**
     * @param language  the language the designer works on and its proposals are written into
     * @param preselect the name of the category or the title of the section the action is about, null for none
     * @param changed   run after a proposal was applied
     */
    public DesignerDialog(Action action, Language language, String preselect, Runnable changed) {
        this.action = action;
        this.language = language;
        this.changed = changed;

        setHeaderTitle(title(action));
        setWidth("760px");
        setMaxWidth("96vw");
        setMaxHeight("94vh");
        setCloseOnOutsideClick(false);
        setResizable(true);
        setDraggable(true);

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        Span about = new Span(about(action));
        about.addClassName(ConlangStyles.HINT);
        form.add(about);

        note.setWidthFull();
        note.setMinHeight("8rem");
        note.setLabel(action == Action.WORDS ? T.localized(WHAT_THE_WORDS_ARE_FOR) : T.localized(WHAT_DO_YOU_WANT));
        if (action == Action.WORDS) {
            category.setWidthFull();
            category.setAllowCustomValue(true);
            category.setClearButtonVisible(true);
            category.setItems(language.getCategories().stream().map(Category::getName).toList());
            category.addCustomValueSetListener(event -> category.setValue(event.getDetail()));
            category.setHelperText(T.localized(A_NAME_THAT_IS_NOT_A_CATEGORY_MAKES_ONE));
            if (preselect != null) {
                category.setValue(preselect);
            }
            count.setWidthFull();
            count.setMin(1);
            count.setMax(200);
            count.setStepButtonsVisible(true);
            count.setValue(20);
            form.add(category, count);
        }
        if (action == Action.REWRITE) {
            section.setWidthFull();
            section.setItems(language.getRules());
            section.setItemLabelGenerator(RuleSection::getTitle);
            language.getRules().stream().filter(rule -> rule.getTitle().equals(preselect)).findFirst().ifPresentOrElse(section::setValue,
                    () -> language.getRules().stream().findFirst().ifPresent(section::setValue));
            form.add(section);
        }
        if (action != Action.REVIEW) {
            form.add(note);
        }
        add(form);

        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        Button run = new Button(action == Action.REVIEW ? T.localized(REVIEW_THE_LANGUAGE) : T.localized(PROPOSE), _ -> run());
        run.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, run);
    }

    private void run() {
        Language snapshot = ConlangStore.copy(language);
        String wanted = note.getValue() == null ? "" : note.getValue().strip();
        if ((action == Action.SECTION || action == Action.REWRITE) && wanted.isEmpty()) {
            Notification.error(T.localized(SAY_WHAT_YOU_WANT));
            return;
        }
        if (action == Action.REWRITE && section.getValue() == null) {
            Notification.error(T.localized(CHOOSE_A_SECTION));
            return;
        }
        RuleSection chosen = section.getValue() == null ? null : snapshot.findRule(section.getValue().getId());
        String categoryName = category.getValue();
        int words = count.getValue() == null ? 20 : count.getValue();
        close();
        if (action == Action.REVIEW) {
            Designing.run(T.localized(THE_DESIGNER_IS_REVIEWING), () -> service.answer(DesignerPrompts.review(snapshot)), this::showFindings);
            return;
        }
        Designing.run(T.localized(THE_DESIGNER_IS_WORKING), () -> {
            String lore = service.lore(snapshot);
            Request request = switch (action) {
                case WORDS -> DesignerPrompts.words(snapshot, categoryName, words, wanted, lore);
                case SECTION -> DesignerPrompts.section(snapshot, wanted, lore);
                default -> DesignerPrompts.rewrite(snapshot, chosen, wanted);
            };
            return service.propose(snapshot, request);
        }, proposal -> Proposals.show(title(action), language, proposal, changed));
    }

    private void showFindings(DesignerAnswer answer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(REVIEW_OF_S).formatted(language.getName()));
        dialog.setWidth("1100px");
        dialog.setMaxWidth("96vw");
        dialog.setHeight("70vh");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        Span message = new Span(answer.message().isBlank() && answer.findings().isEmpty() ? T.localized(NOTHING_FOUND) : answer.message());
        message.addClassName(ConlangStyles.DIFF_TEXT);
        Grid<DesignerAnswer.Finding> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassName(ConlangStyles.FILL);
        grid.setItems(answer.findings());
        // a finding is a card, which reads the same on a wide screen and on a phone
        grid.addComponentColumn(finding -> {
            Span heading = new Span((finding.severity() == null || finding.severity().isBlank() ? "" : finding.severity() + ": ") + (finding.where() == null ? "" : finding.where()));
            heading.addClassName(ConlangStyles.WORD);
            Div problem = new Div(new Span(finding.problem() == null ? "" : finding.problem()));
            problem.addClassName(ConlangStyles.DIFF_TEXT);
            Div fix = new Div(new Span(finding.fix() == null || finding.fix().isBlank() ? "" : T.localized(FIX) + ": " + finding.fix()));
            fix.addClassName(ConlangStyles.DIFF_TEXT);
            fix.addClassName(ConlangStyles.HINT);
            return new Div(heading, problem, fix);
        }).setHeader(T.localized(PROBLEM)).setFlexGrow(1);
        VerticalLayout content = new VerticalLayout(message, grid);
        content.setPadding(false);
        content.setSizeFull();
        content.setFlexGrow(1, grid);
        dialog.add(content);
        dialog.getFooter().add(new Button(T.localized(CLOSE), _ -> dialog.close()));
        dialog.open();
    }

    static String title(Action action) {
        return T.localized(switch (action) {
            case WORDS -> GENERATE_WORDS;
            case SECTION -> ADD_A_SECTION_WITH_AI;
            case REWRITE -> REWRITE_A_SECTION;
            case REVIEW -> REVIEW_THE_LANGUAGE;
        });
    }

    private static String about(Action action) {
        return T.localized(switch (action) {
            case WORDS -> THE_DESIGNER_INVENTS_WORDS;
            case SECTION -> THE_DESIGNER_WRITES_A_SECTION;
            case REWRITE -> THE_DESIGNER_REWRITES;
            case REVIEW -> THE_DESIGNER_REVIEWS;
        });
    }
}
