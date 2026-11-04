package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.AsyncRuns;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.BookConlang;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.BookConlang.Quick;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.PluginLog;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Word;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Quick translation: the author writes a text, the model translates it into the language of the book and says which words it had to invent.
 * Nothing is written into the story; the author takes the translation and imports the words he wants into the lexicon.
 */
public class QuickTranslatePanel extends VerticalLayout {

    private final Manuscript book;
    private final BookConlang service;
    private final Runnable wordsImported;

    private final TextArea input = new TextArea(T.localized(TEXT_TO_TRANSLATE));
    private final TextField context = new TextField(T.localized(WHO_SAYS_IT_TO_WHOM));
    private final Button translate = new Button(T.localized(TRANSLATE), Solid.LANGUAGE.create(), _ -> translate());
    private final ProgressBar busy = new ProgressBar();
    private final TextArea output = new TextArea(T.localized(TRANSLATION));
    private final Button copy = new Button(T.localized(COPY), Solid.COPY.create(), _ -> copy());
    private final Span choice = new Span();
    private final Span unknown = new Span();
    private final Span wordsHeader = new Span(T.localized(THE_WORDS_THE_TRANSLATION_NEEDED));
    private final Grid<Word> words = new Grid<>();
    private final Button importWords = new Button(T.localized(IMPORT_THE_SELECTED_WORDS), Solid.FILE_IMPORT.create(), _ -> importSelected());

    private Quick last;

    /**
     * @param wordsImported run after words were added to the lexicon, so the list of words shows them
     */
    public QuickTranslatePanel(Manuscript book, BookConlang service, Runnable wordsImported) {
        this.book = book;
        this.service = service;
        this.wordsImported = wordsImported;
        setPadding(false);
        setSizeFull();
        setMinWidth("0");

        Span hint = new Span(T.localized(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN));
        hint.addClassName(ConlangStyles.HINT);
        input.setWidthFull();
        input.setMinHeight("7rem");
        input.setMaxHeight("14rem");
        context.setWidthFull();
        context.setClearButtonVisible(true);
        context.setHelperText(T.localized(THE_NOTES_OF_THE_SPEAKERS_ARE_USED));
        translate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        busy.setIndeterminate(true);
        busy.setVisible(false);

        output.setWidthFull();
        output.setReadOnly(true);
        output.setMinHeight("6rem");
        output.setMaxHeight("14rem");
        choice.addClassName(ConlangStyles.HINT);
        unknown.addClassName(ConlangStyles.HINT);
        wordsHeader.addClassName(ConlangStyles.HINT);

        words.setSelectionMode(Grid.SelectionMode.MULTI);
        words.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        words.setWidthFull();
        words.setAllRowsVisible(false);
        words.setHeight("14rem");
        words.addComponentColumn(word -> {
            Span form = new Span(word.word().word());
            form.addClassName(ConlangStyles.WORD);
            return form;
        }).setHeader(T.localized(WORD)).setFlexGrow(0).setAutoWidth(true);
        words.addColumn(word -> word.word().gloss()).setHeader(T.localized(MEANING)).setFlexGrow(2);
        words.addColumn(word -> word.word().category()).setHeader(T.localized(CATEGORY)).setFlexGrow(1);
        words.addColumn(word -> word.word().rationale()).setHeader(T.localized(WHY_THE_WORD_IS_AS_IT_IS)).setFlexGrow(3);
        words.getColumns().forEach(column -> column.setResizable(true));
        words.addSelectionListener(_ -> updateImport());
        importWords.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout outputRow = new HorizontalLayout(copy);
        add(hint, input, context, new HorizontalLayout(translate), busy, output, outputRow, choice, unknown, wordsHeader, words, importWords);
        showResult(null);
    }

    private void translate() {
        String text = input.getValue();
        if (text == null || text.isBlank()) {
            Notification.error(T.localized(THERE_IS_NO_TEXT_TO_TRANSLATE));
            return;
        }
        String who = context.getValue();
        translate.setEnabled(false);
        busy.setVisible(true);
        showResult(null);
        AsyncRuns.start("conlang-quick", PluginLog.get(), () -> service.quickTranslate(book, text, who), (result, failure) -> {
            translate.setEnabled(true);
            busy.setVisible(false);
            if (failure != null) {
                Notification.error(String.format(T.localized(THE_TRANSLATION_FAILED), failure.getMessage()));
                return;
            }
            switch (result.status()) {
                case TRANSLATED -> showResult(result);
                case NO_LANGUAGE -> Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                case NO_MODEL -> Notification.error(T.localized(THE_BOOK_HAS_NO_MODEL));
                case BAD_ANSWER -> Notification.error(T.localized(THE_MODEL_ANSWERED_WRONG));
                default -> Notification.error(T.localized(THERE_IS_NO_TEXT_TO_TRANSLATE));
            }
        });
    }

    private void showResult(Quick result) {
        last = result;
        boolean any = result != null;
        output.setValue(any ? result.text() : "");
        output.setVisible(any);
        copy.setVisible(any);
        choice.setText(any ? result.choice() : "");
        choice.setVisible(any && !result.choice().isBlank());
        unknown.setText(any && !result.unknownWords().isEmpty()
                ? T.localized(WORDS_IN_NEITHER_THE_LEXICON_NOR_THE_NEW_WORDS).formatted(String.join(", ", result.unknownWords())) : "");
        unknown.setVisible(any && !result.unknownWords().isEmpty());
        boolean hasWords = any && !result.words().isEmpty();
        wordsHeader.setVisible(hasWords);
        words.setVisible(hasWords);
        importWords.setVisible(hasWords);
        words.setItems(hasWords ? result.words() : List.of());
        if (hasWords) {
            words.asMultiSelect().select(result.words());
        }
        updateImport();
    }

    private void updateImport() {
        int selected = words.getSelectedItems().size();
        importWords.setEnabled(selected > 0);
        importWords.setText(T.localized(IMPORT_THE_SELECTED_WORDS) + (selected > 0 ? " (" + selected + ")" : ""));
    }

    private void copy() {
        getUI().ifPresent(ui -> JavascriptLibrary.copyToClipboard(ui.getPage(), output.getValue()));
        Notification.success(T.localized(COPIED));
    }

    private void importSelected() {
        if (last == null || words.getSelectedItems().isEmpty()) {
            return;
        }
        List<Word> chosen = last.words().stream().filter(word -> words.getSelectedItems().contains(word)).toList();
        String model = last.model();
        importWords.setEnabled(false);
        AsyncRuns.start("conlang-import-words", PluginLog.get(), () -> service.importWords(book, chosen, model), (added, failure) -> {
            if (failure != null) {
                Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), failure.getMessage()));
                updateImport();
                return;
            }
            Notification.success(T.localized(IMPORTED_D_WORDS_AS_NEW).formatted(added));
            wordsImported.run();
            // the words are in the lexicon now: they are not offered again
            Quick shown = last;
            if (shown != null) {
                showResult(new Quick(shown.status(), shown.text(), shown.choice(),
                        shown.words().stream().filter(word -> !chosen.contains(word)).toList(), shown.leftOut(), shown.unknownWords(), shown.model()));
            }
        });
    }
}
