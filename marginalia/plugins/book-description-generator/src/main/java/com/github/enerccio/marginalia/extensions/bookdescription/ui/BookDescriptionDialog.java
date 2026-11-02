package com.github.enerccio.marginalia.extensions.bookdescription.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.bookdescription.model.BookDescriptionSettings;
import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;
import com.github.enerccio.marginalia.extensions.bookdescription.service.AnswerParser;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService.Branch;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService.ModelSelection;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService.Progress;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService.Result;
import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner;
import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner.Piece;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.CancellationException;

import static com.github.enerccio.marginalia.extensions.bookdescription.BookDescriptionLocalization.*;

/**
 * Asks the model for the name and the description of the book from a range of the messages of its active branch. A counter shows how
 * big the range is for the model: when it is more than the model takes, notes are made of its parts first and the description is
 * written from the notes. The name and the description are shown on the right, each with a button that copies it to the book.
 */
@Configurable(preConstruction = true)
public class BookDescriptionDialog extends Dialog {

    /** Where the name and the description go when the author copies them, the book dialog that opened this one. */
    public interface BookTarget {

        void setName(String name);

        void setDescription(String description);
    }

    /** How much of a message is shown to tell which one it is. */
    private static final int PREVIEW_LENGTH = 90;

    @Autowired
    private Localization loc;

    private final BookDescriptionService service;
    private final Manuscript manuscript;
    private final BookTarget target;

    private Branch branch = new Branch(List.of(), List.of());

    private ModelProtocolPicker modelPicker;
    private IntegerField fromField;
    private IntegerField toField;
    private Checkbox useSummaries;
    private Select<OutputFormat> formatSelect;
    private Span tokenInfo;
    private Button generateButton;
    private Button closeButton;
    private Span status;
    private Details notesDetails;
    private Markdown notesMarkdown;
    private TextField nameField;
    private Button copyName;
    private TextArea descriptionField;
    private Button copyDescription;
    private Span problem;
    private TextArea rawAnswer;

    /** How many messages the range of the last {@link #pieces()} has. */
    private int rangeCount;

    private final StringBuilder notesText = new StringBuilder();
    private boolean generating;
    private Thread worker;

    public BookDescriptionDialog(BookDescriptionService service, Manuscript manuscript, BookTarget target) {
        this.service = service;
        this.manuscript = manuscript;
        this.target = target;

        setHeaderTitle(T.localized(GENERATE_NAME_AND_DESCRIPTION));
        setWidth("90vw");
        setHeight("90vh");
        // closing while the model runs would leave it running without anyone to read it
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                stop();
            }
        });

        try {
            branch = service.activeBranch(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        build();
        updateTokenInfo();
    }

    // --- construction ---

    private void build() {
        BookDescriptionSettings settings = loadSettings();

        addThemeName("no-padding");

        // starts with the model of the book, can be changed for this generation
        modelPicker = new ModelProtocolPicker();
        try {
            ModelSelection initial = service.resolveModel(manuscript);
            if (initial != null) {
                modelPicker.setSelection(initial.ai(), initial.protocol());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        modelPicker.addSelectionListener(this::updateTokenInfo);

        int last = branch.messages().size();
        fromField = rangeField(T.localized(FROM_MESSAGE), last == 0 ? null : 1, last);
        toField = rangeField(T.localized(TO_MESSAGE), last == 0 ? null : last, last);
        fromField.addValueChangeListener(_ -> rangeChanged());
        toField.addValueChangeListener(_ -> rangeChanged());
        HorizontalLayout range = new HorizontalLayout(fromField, toField);
        range.setWidthFull();
        range.setPadding(false);
        range.getThemeList().add("spacing-xs");

        Span rangeHint = new Span(T.localized(THE_MESSAGES_WITH_TEXT).formatted(last));
        rangeHint.addClassNames(CssStyles.TEXT_XS, CssStyles.TEXT_SECONDARY);

        useSummaries = new Checkbox(T.localized(USE_SUMMARIES), settings.isUseSummaries());
        Tooltip.forComponent(useSummaries).setText(T.localized(A_SUMMARY_STANDS_IN));
        useSummaries.addValueChangeListener(_ -> updateTokenInfo());

        formatSelect = new Select<>();
        formatSelect.setLabel(T.localized(ANSWER_FORMAT));
        formatSelect.setItems(OutputFormat.AUTO, OutputFormat.JSON, OutputFormat.JSON_NO_SCHEMA, OutputFormat.MARKDOWN);
        formatSelect.setItemLabelGenerator(BookDescriptionDialog::label);
        formatSelect.setValue(settings.format());
        formatSelect.setWidthFull();
        formatSelect.setHelperText(T.localized(AUTOMATIC_ASKS_FOR_JSON) + ". " + T.localized(JSON_WITHOUT_A_SCHEMA_HELP));
        formatSelect.addValueChangeListener(_ -> updateTokenInfo());

        tokenInfo = new Span();
        tokenInfo.setWidthFull();
        tokenInfo.addClassNames(CssStyles.TEXT_S);

        Span promptsHint = new Span(T.localized(THE_PROMPTS_ARE_SET_IN));
        promptsHint.addClassNames(CssStyles.TEXT_XS, CssStyles.TEXT_SECONDARY);

        generateButton = new Button(T.localized(GENERATE), Solid.WAND_MAGIC_SPARKLES.create(), _ -> {
            if (generating) {
                stop();
            } else {
                generate();
            }
        });
        generateButton.setWidthFull();

        VerticalLayout left = new VerticalLayout(modelPicker, range, rangeHint, useSummaries, formatSelect, tokenInfo, promptsHint, generateButton);
        left.setPadding(true);
        left.getThemeList().add("spacing-xs");
        left.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        status = new Span();
        status.setWidthFull();
        status.addClassNames(CssStyles.TEXT_S, CssStyles.TEXT_SECONDARY);

        notesMarkdown = new Markdown();
        notesMarkdown.setWidthFull();
        notesDetails = new Details(T.localized(NOTES), notesMarkdown);
        notesDetails.setWidthFull();
        notesDetails.setVisible(false);

        nameField = new TextField(T.localized(NAME));
        nameField.setReadOnly(true);
        nameField.setWidthFull();
        copyName = new Button(T.localized(COPY_TO_BOOK), Solid.COPY.create(), _ -> copy(true));
        copyName.setEnabled(false);

        descriptionField = new TextArea(T.localized(DESCRIPTION));
        descriptionField.setReadOnly(true);
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("240px");
        copyDescription = new Button(T.localized(COPY_TO_BOOK), Solid.COPY.create(), _ -> copy(false));
        copyDescription.setEnabled(false);

        problem = new Span();
        problem.addClassName(CssStyles.NOTICE_WARNING);
        problem.setWidthFull();
        problem.setVisible(false);
        rawAnswer = new TextArea();
        rawAnswer.setReadOnly(true);
        rawAnswer.setWidthFull();
        rawAnswer.setVisible(false);

        VerticalLayout right = new VerticalLayout(status, notesDetails, nameField, copyName, descriptionField, copyDescription, problem, rawAnswer);
        right.setPadding(true);
        right.getThemeList().add("spacing-xs");
        right.setAlignItems(FlexComponent.Alignment.START);
        right.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        if (UIUtils.isPhone()) {
            // no room for two columns: the options above the result, the whole dialog scrolls
            left.setWidthFull();
            right.setWidthFull();
            VerticalLayout stacked = new VerticalLayout(left, right);
            stacked.setSizeFull();
            stacked.setPadding(false);
            stacked.setSpacing(false);
            stacked.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);
            setWidth("100vw");
            setHeight("100vh");
            add(stacked);
        } else {
            left.setWidth("40%");
            left.setMinWidth("320px");
            right.setWidth("60%");
            HorizontalLayout panels = new HorizontalLayout(left, right);
            panels.setSizeFull();
            panels.setPadding(false);
            panels.setSpacing(false);
            panels.addClassName(CssStyles.MIN_HEIGHT_0);
            add(panels);
        }

        closeButton = new Button(T.localized(CLOSE), _ -> close());
        getFooter().add(closeButton);

        status.setText(last == 0 ? T.localized(THE_BOOK_HAS_NO_STORY) : T.localized(THE_NAME_AND_THE_DESCRIPTION_APPEAR_HERE));
        updateControls();
    }

    private BookDescriptionSettings loadSettings() {
        try {
            return service.getSettings();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return new BookDescriptionSettings();
        }
    }

    private IntegerField rangeField(String label, Integer value, int last) {
        IntegerField field = new IntegerField(label);
        field.setMin(1);
        field.setMax(Math.max(1, last));
        field.setStepButtonsVisible(true);
        field.setValue(value);
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.setWidthFull();
        field.setHelperText(preview(value));
        field.setEnabled(last > 0);
        return field;
    }

    private static String label(OutputFormat format) {
        return switch (format) {
            case AUTO -> T.localized(AUTOMATIC);
            case JSON -> T.localized(JSON);
            case JSON_NO_SCHEMA -> T.localized(JSON_WITHOUT_A_SCHEMA);
            case MARKDOWN -> T.localized(MARKDOWN);
        };
    }

    /** The beginning of the message, in one line. */
    private String preview(Integer number) {
        if (number == null || number < 1 || number > branch.messages().size()) {
            return T.localized(NO_SUCH_MESSAGE);
        }
        return StringUtils.abbreviate(StringUtils.normalizeSpace(branch.messages().get(number - 1).text()), PREVIEW_LENGTH);
    }

    private void rangeChanged() {
        fromField.setHelperText(preview(fromField.getValue()));
        toField.setHelperText(preview(toField.getValue()));
        updateTokenInfo();
    }

    // --- the range and the counter ---

    /**
     * What is sent for the chosen range.
     *
     * @throws IllegalArgumentException when the range is not valid
     */
    private List<Piece> pieces() {
        int count = branch.messages().size();
        int from = fromField.getValue() != null ? fromField.getValue() : 1;
        int to = toField.getValue() != null ? toField.getValue() : count;
        if (count == 0) {
            throw new IllegalArgumentException(T.localized(THE_BOOK_HAS_NO_STORY));
        }
        if (from < 1 || to > count) {
            throw new IllegalArgumentException(T.localized(THE_MESSAGES_ARE_NUMBERED_1).formatted(count));
        }
        if (from > to) {
            throw new IllegalArgumentException(T.localized(THE_FIRST_MESSAGE_IS_AFTER));
        }
        rangeCount = to - from + 1;
        // the summaries are of the branch, the range is in the numbers of the messages with text
        return StoryPlanner.pieces(branch.entries(), branch.messages().get(from - 1).order(), branch.messages().get(to - 1).order(),
                Boolean.TRUE.equals(useSummaries.getValue()));
    }

    private OutputFormat format() {
        return formatSelect.getValue() != null ? formatSelect.getValue() : OutputFormat.AUTO;
    }

    /**
     * Shows how big the range is and whether it needs notes, so the author knows what a range costs before the model starts.
     */
    private void updateTokenInfo() {
        // not built yet
        if (tokenInfo == null) {
            return;
        }
        tokenInfo.removeClassName(CssStyles.NOTICE_WARNING);
        List<Piece> pieces;
        try {
            pieces = pieces();
        } catch (IllegalArgumentException e) {
            tokenInfo.setText(e.getMessage());
            tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
            return;
        }

        NumberFormat number = NumberFormat.getIntegerInstance();
        long summaries = pieces.stream().filter(p -> p.label().startsWith("Summary")).count();
        StringBuilder text = new StringBuilder(T.localized(THE_RANGE_HAS).formatted(rangeCount, pieces.size(), summaries)).append(' ');
        try {
            long overhead = service.countTokens(service.buildDescriptionPrompt(manuscript, service.descriptionPrompt(manuscript), "", true, format()));
            long total = StoryPlanner.cost(pieces) + overhead;
            ModelSelection model = selection();
            int available = model == null ? 0 : TokenLimits.promptTokens(model.ai(), model.protocol());
            if (available <= 0) {
                text.append(T.localized(TOKENS_CONTEXT_UNKNOWN).formatted(number.format(total)));
            } else if (BookDescriptionService.needsNotes(pieces, overhead, available)) {
                long notesOverhead = service.countTokens(service.buildNotesPrompt(manuscript, service.notesPrompt(manuscript), "", 1, 1));
                long room = Math.max(StoryPlanner.MIN_BUDGET, BookDescriptionService.usable(available) - notesOverhead);
                int requests = StoryPlanner.chunk(pieces, room).size();
                text.append(T.localized(TOKENS_SENT_AS_NOTES).formatted(number.format(total), number.format(available), requests));
            } else {
                text.append(T.localized(TOKENS_SENT_AS_IT_IS).formatted(number.format(total), number.format(available)));
            }
        } catch (Exception e) {
            // most likely a prompt that is not a valid template
            text.append(T.localized(THE_PROMPT_CANT_BE_BUILT).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
            tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
        }
        tokenInfo.setText(text.toString());
    }

    private ModelSelection selection() {
        return modelPicker.getAi() == null ? null : new ModelSelection(modelPicker.getAi(), modelPicker.getProtocol());
    }

    // --- generation ---

    private void generate() {
        ModelSelection model = selection();
        if (model == null) {
            Notification.warning(T.localized(CHOOSE_A_MODEL));
            return;
        }
        List<Piece> pieces;
        String descriptionPrompt;
        String notesPrompt;
        try {
            pieces = pieces();
            // resolved here: the thread that runs has no book dialog to ask
            descriptionPrompt = service.descriptionPrompt(manuscript);
            notesPrompt = service.notesPrompt(manuscript);
        } catch (IllegalArgumentException e) {
            Notification.warning(e.getMessage());
            return;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        OutputFormat format = format();
        rememberOptions();
        reset();

        UI ui = UI.getCurrent();
        // the thread has no request of its own, it needs the session of this UI to resolve the user
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        generating = true;
        updateControls();
        status.setText(T.localized(WRITING_THE_DESCRIPTION));

        Progress progress = new Progress() {
            @Override
            public void notesRound(int round, int parts) {
                show(ui, () -> {
                    notesText.setLength(0);
                    notesMarkdown.setContent("");
                    notesDetails.setVisible(true);
                    status.setText(T.localized(NOTES_ROUND).formatted(round, parts));
                });
            }

            @Override
            public void notes(int part, int parts, String notes) {
                show(ui, () -> {
                    notesText.append("**").append(T.localized(NOTES_OF_A_PART).formatted(part, parts)).append("**\n\n").append(notes).append("\n\n");
                    notesMarkdown.setContent(notesText.toString());
                    status.setText(T.localized(NOTES_PART_DONE).formatted(part, parts));
                });
            }

            @Override
            public void describing(boolean fromNotes) {
                show(ui, () -> status.setText(T.localized(fromNotes ? WRITING_THE_DESCRIPTION_FROM_NOTES : WRITING_THE_DESCRIPTION)));
            }
        };

        List<Piece> sent = pieces;
        String descriptionTemplate = descriptionPrompt;
        String notesTemplate = notesPrompt;
        worker = new Thread(() -> {
            try (InRequestScope _ = new InRequestScope(attributes)) {
                Result result = service.generate(model, manuscript, sent, descriptionTemplate, notesTemplate, format, progress);
                show(ui, () -> finished(result));
            } catch (CancellationException e) {
                show(ui, () -> ended(T.localized(STOPPED), null));
            } catch (UnsupportedOperationException e) {
                // a service that can't take a response schema
                show(ui, () -> ended(null, StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
            } catch (Throwable e) {
                show(ui, () -> ended(null, e));
            }
        }, "book-description-generator");
        worker.setDaemon(true);
        worker.start();
    }

    /** Runs a change of the dialog from the thread that generates. */
    private static void show(UI ui, Runnable change) {
        try {
            ui.access(() -> {
                change.run();
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            // the screen is gone, nobody to show it to
        }
    }

    private void finished(Result result) {
        generating = false;
        worker = null;
        status.setText(T.localized(DONE));
        if (result.description() != null) {
            AnswerParser.Parsed parsed = result.description();
            nameField.setValue(parsed.name());
            descriptionField.setValue(parsed.description());
        } else {
            problem.setText(T.localized(THE_ANSWER_CANT_BE_READ));
            problem.setVisible(true);
            rawAnswer.setValue(result.answer());
            rawAnswer.setVisible(true);
        }
        updateControls();
    }

    /** The run is over without a result: stopped (a message) or failed (a reason). */
    private void ended(String message, Object failure) {
        generating = false;
        worker = null;
        status.setText(message != null ? message : "");
        if (failure instanceof String reason) {
            Notification.warning(reason);
        } else if (failure instanceof IllegalStateException e) {
            // the story can't be made to fit, a message for the author and not a fault
            Notification.error(T.localized(CANT_GENERATE).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
        } else if (failure instanceof Throwable e) {
            UIUtils.inferenceError(loc, e);
        }
        updateControls();
    }

    private void stop() {
        Thread running = worker;
        if (running != null) {
            running.interrupt();
        }
    }

    /** Saves the choices as the ones the dialog opens with next time. */
    private void rememberOptions() {
        try {
            // read again, the settings may have been changed in the meantime
            BookDescriptionSettings settings = service.getSettings();
            settings.setUseSummaries(Boolean.TRUE.equals(useSummaries.getValue()));
            settings.setFormat(format().name());
            service.saveSettings(settings);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- display ---

    /** Forgets the last generation. */
    private void reset() {
        notesText.setLength(0);
        notesMarkdown.setContent("");
        notesDetails.setVisible(false);
        nameField.clear();
        descriptionField.clear();
        problem.setVisible(false);
        rawAnswer.clear();
        rawAnswer.setVisible(false);
    }

    private void copy(boolean name) {
        try {
            if (name) {
                target.setName(nameField.getValue());
            } else {
                target.setDescription(descriptionField.getValue());
            }
            Notification.success(T.localized(COPIED_TO_THE_BOOK));
        } catch (Exception e) {
            Notification.error(T.localized(CANT_COPY_TO_THE_BOOK).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
        }
    }

    /** Freezes the controls while the model runs; the button that starts it then stops it. */
    private void updateControls() {
        boolean hasStory = !branch.messages().isEmpty();
        modelPicker.setEnabled(!generating);
        fromField.setEnabled(!generating && hasStory);
        toField.setEnabled(!generating && hasStory);
        useSummaries.setEnabled(!generating);
        formatSelect.setEnabled(!generating);
        closeButton.setEnabled(!generating);
        generateButton.setEnabled(generating || hasStory);
        copyName.setEnabled(!generating && StringUtils.isNotBlank(nameField.getValue()));
        copyDescription.setEnabled(!generating && StringUtils.isNotBlank(descriptionField.getValue()));

        generateButton.setText(generating ? T.localized(STOP) : T.localized(GENERATE));
        generateButton.setIcon((generating ? Solid.STOP : Solid.WAND_MAGIC_SPARKLES).create());
        generateButton.setThemeName(generating ? "error primary" : "primary");
    }
}
