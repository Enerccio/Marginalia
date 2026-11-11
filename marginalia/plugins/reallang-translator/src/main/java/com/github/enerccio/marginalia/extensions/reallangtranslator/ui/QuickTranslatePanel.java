package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.BookReal;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.TranslationService.Quick;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.AsyncRuns;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PluginLog;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.WindowRun;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * Quick translation: the author writes a text, the model translates it into the language of the book. Nothing is written into the story; the author
 * takes the translation.
 */
public class QuickTranslatePanel extends VerticalLayout {

    private final Manuscript book;
    private final WindowRun request = WindowRun.of(this, "reallang-quick", () -> T.localized(QUICK_TRANSLATE));
    private final BookReal service;

    private final Select<LanguageRef> language = new Select<>();
    private final TextArea input = new TextArea(T.localized(TEXT_TO_TRANSLATE));
    private final TextField context = new TextField(T.localized(WHO_SAYS_IT_TO_WHOM));
    private final Button translate = new Button(T.localized(TRANSLATE), Solid.LANGUAGE.create(), _ -> translate());
    private final ProgressBar busy = new ProgressBar();
    private final TextArea output = new TextArea(T.localized(TRANSLATION));
    private final TextField reading = new TextField(T.localized(READING));
    private final Button copy = new Button(T.localized(COPY), Solid.COPY.create(), _ -> copy());
    private final Span choice = new Span();
    private final Span warning = new Span();

    /**
     * @param languages the languages of the book; with more than one the author chooses which one the text goes into
     */
    public QuickTranslatePanel(Manuscript book, BookReal service, List<LanguageRef> languages) {
        this.book = book;
        request.book(book);
        this.service = service;
        setPadding(false);
        setSizeFull();
        setMinWidth("0");

        language.setLabel(T.localized(LANGUAGE));
        language.setItems(languages);
        language.setItemLabelGenerator(LanguageRef::name);
        if (!languages.isEmpty()) {
            language.setValue(languages.getFirst());
        }
        language.setVisible(languages.size() > 1);
        Span hint = new Span(T.localized(THE_TEXT_IS_TRANSLATED_NOTHING_IS_WRITTEN));
        hint.addClassName(CommonStyles.HINT);
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
        reading.setWidthFull();
        reading.setReadOnly(true);
        choice.addClassName(CommonStyles.HINT);
        warning.addClassName(CommonStyles.CONFLICT);

        add(hint, language, input, context, new HorizontalLayout(translate), busy, output, reading, new HorizontalLayout(copy), choice, warning);
        showResult(null);
    }

    private void translate() {
        String text = input.getValue();
        if (text == null || text.isBlank()) {
            Notification.error(T.localized(THERE_IS_NO_TEXT_TO_TRANSLATE));
            return;
        }
        String who = context.getValue();
        String languageId = language.getValue() == null ? null : language.getValue().id();
        translate.setEnabled(false);
        busy.setVisible(true);
        showResult(null);
        // the request as a run: the panel of the runs shows it (no Stop: the translation can not be stopped)
        try (Runs.Binding _ = request.start(null)) {
            AsyncRuns.start("reallang-quick", PluginLog.get(), () -> service.quickTranslate(book, text, who, languageId), (result, failure) -> {
                request.finished();
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
    }

    private void showResult(Quick result) {
        boolean any = result != null;
        output.setValue(any ? result.text() : "");
        output.setVisible(any);
        reading.setValue(any ? result.reading() : "");
        reading.setVisible(any && !result.reading().isBlank());
        copy.setVisible(any);
        choice.setText(any ? result.choice() : "");
        choice.setVisible(any && !result.choice().isBlank());
        warning.setText(any && result.wrongScript() ? T.localized(THE_TRANSLATION_IS_NOT_IN_THE_SCRIPT_OF_THE_LANGUAGE) : "");
        warning.setVisible(any && result.wrongScript());
    }

    private void copy() {
        getUI().ifPresent(ui -> JavascriptLibrary.copyToClipboard(ui.getPage(), output.getValue()));
        Notification.success(T.localized(COPIED));
    }
}
