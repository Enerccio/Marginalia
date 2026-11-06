package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionPrompt;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionPrompts;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.BookReal;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.ExtractionRuns;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeRuns;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;
import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.TC;

/**
 * The <i>Word extraction</i> tab of the book: the prompt of the library and the specification the book uses, the range of parts, and the run that asks the
 * model for the names and special words of each part and adds the new ones to the glossary of the book as proposals. The prompt and the specification
 * are remembered by the book; the range is chosen for every run.
 */
public class ExtractionPanel extends VerticalLayout {

    private final Manuscript book;
    private final BookReal service;
    private final ExtractionRuns runs;
    private final Supplier<List<RangePart>> loader;
    private final Runnable glossaryChanged;
    private final List<LanguageRef> languages;

    private final Select<LanguageRef> language = new Select<>();
    private final Select<ExtractionPrompt> prompt = new Select<>();
    private final TextArea specification = new TextArea(T.localized(SPECIFICATION));
    private final IntegerField from = new IntegerField(TC.localized(TranslatorCommonLocalization.FROM_PART));
    private final IntegerField to = new IntegerField(TC.localized(TranslatorCommonLocalization.TO_PART));
    private final Div count = new Div();
    private final ProgressBar progress = new ProgressBar();
    private final Div status = new Div();
    private final Button start = new Button(T.localized(EXTRACT_THE_WORDS));
    private final Button stop = new Button(TC.localized(TranslatorCommonLocalization.STOP));
    private RangeRuns.Handle handle;
    private List<RangePart> all = List.of();
    private int planned;

    /**
     * @param prompts         the prompts to choose from (the built-in ones, then the user's)
     * @param loader          the parts of the active branch that have text, in order, as they are now
     * @param glossaryChanged told on the page after a run that may have added entries, so the glossary tabs show them
     */
    public ExtractionPanel(Manuscript book, BookReal service, ExtractionRuns runs, List<LanguageRef> languages, List<ExtractionPrompt> prompts,
                           ExtractionSettings saved, Supplier<List<RangePart>> loader, Runnable glossaryChanged) {
        this.book = book;
        this.service = service;
        this.runs = runs;
        this.languages = languages;
        this.loader = loader;
        this.glossaryChanged = glossaryChanged;
        setPadding(false);
        setSpacing(true);
        setWidthFull();
        setMinWidth("0");

        Span hint = new Span(T.localized(THE_MODEL_READS_THE_PARTS_OF_THE_RANGE));
        hint.addClassName(CommonStyles.HINT);

        language.setLabel(T.localized(LANGUAGE));
        language.setItems(languages);
        language.setItemLabelGenerator(LanguageRef::name);
        language.setValue(languages.getFirst());
        language.setWidthFull();
        language.setVisible(languages.size() > 1);

        prompt.setLabel(T.localized(PROMPT));
        prompt.setEmptySelectionAllowed(true);
        prompt.setEmptySelectionCaption(T.localized(ONLY_MY_SPECIFICATION));
        prompt.setItems(prompts);
        prompt.setItemLabelGenerator(item -> item == null ? T.localized(ONLY_MY_SPECIFICATION)
                : ExtractionPrompts.isBuiltIn(item.getId()) ? T.localized(S_BUILT_IN).formatted(item.getName()) : item.getName());
        prompt.setValue(prompts.stream().filter(item -> item.getId().equals(saved.getPromptId())).findFirst().orElse(null));
        prompt.setWidthFull();
        prompt.addValueChangeListener(event -> remember());

        specification.setWidthFull();
        specification.setMinHeight("8rem");
        specification.setMaxHeight("24rem");
        specification.setHelperText(T.localized(ADDED_AFTER_THE_PROMPT));
        specification.setValue(saved.getSpecification());
        specification.addBlurListener(event -> remember());

        all = loader.get();
        int last = Math.max(1, all.size());
        for (IntegerField field : new IntegerField[]{from, to}) {
            field.setMin(1);
            field.setMax(last);
            field.setStepButtonsVisible(true);
            field.setWidthFull();
        }
        from.setValue(1);
        to.setValue(last);
        count.addClassName(CommonStyles.HINT);
        status.addClassName(CommonStyles.HINT);
        status.addClassName(CommonStyles.SOURCE_TEXT);
        from.addValueChangeListener(event -> refreshCount());
        to.addValueChangeListener(event -> refreshCount());
        progress.setVisible(false);
        progress.setWidthFull();
        start.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        start.addClickListener(event -> begin());
        stop.setEnabled(false);
        stop.addClickListener(event -> {
            if (handle != null) {
                handle.stop();
                status.setText(TC.localized(TranslatorCommonLocalization.STOPPING_AFTER_THIS_PART));
                stop.setEnabled(false);
            }
        });
        refreshCount();

        HorizontalLayout range = new HorizontalLayout(from, to);
        range.setWidthFull();
        HorizontalLayout buttons = new HorizontalLayout(start, stop);
        add(hint, language, prompt, specification, range, count, progress, status, buttons);
    }

    private String languageId() {
        return language.getValue() == null ? null : language.getValue().id();
    }

    /** Stores the prompt and the specification in the book. */
    private void remember() {
        try {
            ExtractionSettings settings = new ExtractionSettings();
            settings.setPromptId(prompt.getValue() == null ? null : prompt.getValue().getId());
            settings.setSpecification(specification.getValue());
            service.saveExtraction(book, settings);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    private List<RangePart> selected() {
        int first = from.getValue() == null ? 1 : from.getValue();
        int last = to.getValue() == null ? all.size() : to.getValue();
        return all.stream().filter(part -> part.number() >= first && part.number() <= last).toList();
    }

    private void refreshCount() {
        count.setText(String.format(T.localized(PARTS_IN_THE_RANGE_D), selected().size()));
    }

    private void begin() {
        if (prompt.getValue() == null && StringUtils.isBlank(specification.getValue())) {
            Notification.error(T.localized(CHOOSE_A_PROMPT_OR_WRITE_A_SPECIFICATION));
            return;
        }
        remember();
        try {
            all = loader.get();
        } catch (RuntimeException e) {
            Notification.error(String.format(T.localized(THE_EXTRACTION_FAILED), e.getMessage()));
            return;
        }
        List<RangePart> parts = selected();
        if (parts.isEmpty()) {
            Notification.error(TC.localized(TranslatorCommonLocalization.NOTHING_TO_DO_IN_THE_RANGE));
            return;
        }
        planned = parts.size();
        RangeRuns.Handle started = runs.start(book, languageId(), parts, this::showProgress, this::finished);
        if (started == null) {
            Notification.error(T.localized(THE_WORDS_OF_THIS_BOOK_ARE_BEING_EXTRACTED));
            return;
        }
        handle = started;
        setRunning(true);
        showProgress(0);
    }

    private void setRunning(boolean running) {
        start.setEnabled(!running);
        stop.setEnabled(running);
        for (var field : new com.vaadin.flow.component.Component[]{language, prompt, specification, from, to}) {
            ((com.vaadin.flow.component.HasEnabled) field).setEnabled(!running);
        }
        progress.setVisible(running);
    }

    private void showProgress(int done) {
        progress.setMax(Math.max(1, planned));
        progress.setValue(done);
        status.setText(String.format(TC.localized(TranslatorCommonLocalization.PART_D_OF_D), Math.min(done + 1, planned), planned));
    }

    private void finished(Summary summary, Throwable failure) {
        handle = null;
        setRunning(false);
        if (failure != null) {
            status.setText(String.format(T.localized(THE_EXTRACTION_FAILED), failure.getMessage()));
            Notification.error(status.getText());
            return;
        }
        StringBuilder text = new StringBuilder(String.format(T.localized(D_PARTS_READ_D_TERMS_D_NEW), summary.processed(), summary.speeches(), summary.newWords()));
        if (summary.failed() > 0) {
            text.append(' ').append(String.format(TC.localized(TranslatorCommonLocalization.D_PARTS_FAILED_S), summary.failed(), summary.firstFailure()));
        }
        if (summary.aborted()) {
            text.append(' ').append(StringUtils.defaultString(summary.firstFailure()));
        }
        if (summary.stopped()) {
            text.append(' ').append(String.format(TC.localized(TranslatorCommonLocalization.STOPPED_AFTER_D_OF_D_PARTS), summary.processed(), summary.planned()));
        }
        status.setText(text.toString());
        if (summary.aborted() || summary.failed() > 0) {
            Notification.error(status.getText());
        } else {
            Notification.success(status.getText());
        }
        if (summary.newWords() > 0) {
            glossaryChanged.run();
        }
    }
}
