package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotateService;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Mode;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Proposal;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets a model annotate a part of the story (the desktop and the phone story tab): the author says what to look for and with which
 * model, the notes the model proposes are listed, each with a check box, and only <b>Add the selected annotations</b> makes
 * annotations of them. The model reads the part with the story around it, see {@link AnnotateService}.
 */
@Configurable(preConstruction = true)
public class AnnotateDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private AnnotateService annotateService;

    private final Manuscript manuscript;
    private final ChatMessage part;
    private final Runnable onAdded;

    private final TextArea instructions = new TextArea();
    private final ComboBox<Mode> mode = new ComboBox<>();
    private final ModelProtocolPicker model = new ModelProtocolPicker();
    private final ModelProtocolPicker extractModel = new ModelProtocolPicker();
    private final Button run = new Button();
    private final ProgressBar progress = new ProgressBar();
    private final Details answerDetails;
    private final TextArea answer = new TextArea();
    private final Span message = new Span();
    private final VerticalLayout proposalsLayout = new VerticalLayout();
    private final Button add = new Button();
    private final Map<Proposal, Checkbox> proposals = new LinkedHashMap<>();

    private CancellationToken cancellationToken;
    private boolean running;
    private String modelName;

    /**
     * @param onAdded called after annotations were added to the part
     */
    public static void open(Manuscript manuscript, ChatMessage part, Runnable onAdded) {
        create(manuscript, part, onAdded).open();
    }

    /**
     * @return the dialog, not opened
     */
    static AnnotateDialog create(Manuscript manuscript, ChatMessage part, Runnable onAdded) {
        return new AnnotateDialog(manuscript, part, onAdded);
    }

    private AnnotateDialog(Manuscript manuscript, ChatMessage part, Runnable onAdded) {
        this.manuscript = manuscript;
        this.part = part;
        this.onAdded = onAdded;

        setHeaderTitle(loc.getValue(L.LABEL_ANNOTATE_WITH_AI));
        setWidth("min(900px, 95vw)");
        setHeight("85vh");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Span info = new Span(loc.getValue(L.MSG_ANNOTATE_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        instructions.setLabel(loc.getValue(L.LABEL_ANNOTATE_INSTRUCTIONS));
        instructions.setPlaceholder(loc.getValue(L.MSG_ANNOTATE_PLACEHOLDER));
        instructions.setWidthFull();
        instructions.setMinRows(2);
        instructions.setMaxRows(6);

        mode.setLabel(loc.getValue(L.LABEL_ANNOTATE_MODE));
        mode.setItems(Mode.values());
        mode.setItemLabelGenerator(this::label);
        mode.setAllowCustomValue(false);
        mode.setValue(Mode.AUTO);
        mode.setWidthFull();

        try {
            GenerationOptions defaults = annotateService.defaultOptions();
            model.setSelection(defaults.ai(), defaults.protocol());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Span modelHint = new Span(loc.getValue(L.MSG_ANNOTATE_MODEL_HINT));
        modelHint.addClassName(CssStyles.HINT_TEXT);

        Span extractLabel = new Span(loc.getValue(L.LABEL_ANNOTATE_EXTRACT_MODEL));
        VerticalLayout extract = new VerticalLayout(extractLabel, extractModel);
        extract.setPadding(false);
        extract.setSpacing(false);
        extract.setVisible(false);
        mode.addValueChangeListener(event -> extract.setVisible(event.getValue() == Mode.TWO_STAGE));

        run.addClickListener(_ -> startOrStop());
        showRunning(false);
        progress.setIndeterminate(true);
        progress.setVisible(false);

        answer.setWidthFull();
        answer.setMinRows(6);
        answer.setMaxRows(20);
        answer.setReadOnly(true);
        answerDetails = new Details(loc.getValue(L.LABEL_ANNOTATE_RAW), answer);
        answerDetails.setWidthFull();
        answerDetails.setVisible(false);

        message.addClassName(CssStyles.HINT_TEXT);
        message.setVisible(false);

        proposalsLayout.setPadding(false);
        proposalsLayout.setSpacing(false);
        proposalsLayout.addClassName(CssStyles.GAP_S);
        proposalsLayout.setVisible(false);

        VerticalLayout content = new VerticalLayout(info, instructions, mode, model, modelHint, extract, run, progress, message,
                answerDetails, proposalsLayout);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        add(content);

        add.setText(loc.getValue(L.LABEL_ANNOTATE_ACCEPT));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setEnabled(false);
        add.addClickListener(_ -> accept());
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        getFooter().add(close, add);

        addOpenedChangeListener(e -> {
            if (!e.isOpened() && running && cancellationToken != null) {
                cancellationToken.cancel();
            }
        });
    }

    private String label(Mode value) {
        return loc.getValue(switch (value) {
            case STRUCTURED -> L.ENUM_ANNOTATE_MODE_STRUCTURED;
            case DELIMITED -> L.ENUM_ANNOTATE_MODE_DELIMITED;
            case TWO_STAGE -> L.ENUM_ANNOTATE_MODE_TWO_STAGE;
            default -> L.ENUM_ANNOTATE_MODE_AUTO;
        });
    }

    private void showRunning(boolean value) {
        running = value;
        setHeaderTitle(loc.getValue(value ? L.LABEL_ANNOTATING : L.LABEL_ANNOTATE_WITH_AI));
        run.setText(loc.getValue(value ? L.LABEL_STOP : L.LABEL_ANNOTATE_RUN));
        run.setThemeName(value ? "error primary" : "primary");
        instructions.setReadOnly(value);
        mode.setReadOnly(value);
        model.setEnabled(!value);
        extractModel.setEnabled(!value);
        progress.setVisible(value);
        add.setEnabled(!value && !proposals.isEmpty());
    }

    private void startOrStop() {
        if (running) {
            if (cancellationToken != null && !cancellationToken.isCancelled()) {
                cancellationToken.cancel();
            }
            return;
        }

        UI ui = UI.getCurrent();
        AnnotateService.Callback callback = new AnnotateService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
                ui.access(() -> {
                    showAnswer(text);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onFinished(List<Proposal> found, String raw) {
                ui.access(() -> {
                    showRunning(false);
                    showProposals(found);
                    if (StringUtils.isNotBlank(raw)) {
                        showAnswer(raw);
                    }
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onUnreadable(String raw, String reason) {
                ui.access(() -> {
                    showRunning(false);
                    showMessage(String.format(loc.getValue(L.MSG_ANNOTATE_UNREADABLE), reason));
                    showAnswer(raw);
                    answerDetails.setOpened(true);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onTerminated(String raw) {
                ui.access(() -> {
                    showRunning(false);
                    showAnswer(raw);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                ui.access(() -> {
                    showRunning(false);
                    UIUtils.inferenceError(loc, throwable);
                    UIPushGuard.push(ui);
                });
            }
        };

        clear();
        showRunning(true);
        try {
            GenerationOptions options = GenerationOptions.defaults().withModel(model.getAi(), model.getProtocol());
            AI used = model.getAi() != null ? model.getAi() : manuscript.getAi();
            modelName = used == null ? null : used.getName();
            cancellationToken = annotateService.annotate(manuscript, part, new AnnotateService.Request(instructions.getValue(), null, options,
                    mode.getValue(), extractModel.getAi(), extractModel.getProtocol()), callback);
            if (cancellationToken == null) {
                showRunning(false);
                Notification.error(loc.getValue(L.ERROR_ANNOTATE_FAILED));
            }
        } catch (AnnotateService.ContextInsufficient e) {
            showRunning(false);
            Notification.error(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
        } catch (Exception e) {
            showRunning(false);
            UIUtils.internalServerError(loc, e);
        }
    }

    private void clear() {
        proposals.clear();
        proposalsLayout.removeAll();
        proposalsLayout.setVisible(false);
        message.setVisible(false);
        answer.setValue("");
        answerDetails.setVisible(false);
        answerDetails.setOpened(false);
    }

    private void showAnswer(String text) {
        answer.setValue(StringUtils.defaultString(text));
        answerDetails.setVisible(StringUtils.isNotBlank(text));
    }

    private void showMessage(String text) {
        message.setText(text);
        message.setVisible(true);
    }

    /**
     * Lists the notes the model proposed, every one chosen.
     */
    private void showProposals(List<Proposal> found) {
        proposals.clear();
        proposalsLayout.removeAll();
        if (found.isEmpty()) {
            showMessage(loc.getValue(L.MSG_ANNOTATE_NO_FINDINGS));
            proposalsLayout.setVisible(false);
            add.setEnabled(false);
            return;
        }
        message.setVisible(false);
        proposalsLayout.add(new Span(loc.getValue(L.LABEL_ANNOTATE_PROPOSALS)));
        for (Proposal proposal : found) {
            Checkbox chosen = new Checkbox(StringUtils.defaultString(proposal.note()));
            chosen.setValue(true);
            chosen.addValueChangeListener(_ -> add.setEnabled(!running && proposals.values().stream().anyMatch(Checkbox::getValue)));
            VerticalLayout row = new VerticalLayout(chosen);
            row.setPadding(false);
            row.setSpacing(false);
            row.addClassNames(CssStyles.ANNOTATION_VIEW);
            if (proposal.located()) {
                Div quote = new Div(new Span(StringUtils.abbreviate(proposal.quote(), 240)));
                quote.addClassName(CssStyles.ANNOTATION_QUOTE);
                row.add(quote);
            } else {
                Span lost = new Span(loc.getValue(L.MSG_ANNOTATE_NOT_LOCATED)
                        + (StringUtils.isBlank(proposal.quote()) ? "" : ": " + StringUtils.abbreviate(proposal.quote(), 160)));
                lost.addClassName(CssStyles.HINT_TEXT);
                row.add(lost);
            }
            proposals.put(proposal, chosen);
            proposalsLayout.add(row);
        }
        proposalsLayout.setVisible(true);
        add.setEnabled(true);
    }

    private void accept() {
        List<Proposal> chosen = new ArrayList<>();
        proposals.forEach((proposal, checkbox) -> {
            if (checkbox.getValue()) {
                chosen.add(proposal);
            }
        });
        if (chosen.isEmpty()) {
            return;
        }
        try {
            int made = annotateService.accept(manuscript, part, chosen, modelName).size();
            Notification.success(String.format(loc.getValue(L.MSG_ANNOTATE_ADDED), made));
            close();
            onAdded.run();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests

    ComboBox<Mode> modeField() {
        return mode;
    }

    TextArea instructionsField() {
        return instructions;
    }

    ModelProtocolPicker modelPicker() {
        return model;
    }

    Button runButton() {
        return run;
    }

    Button addButton() {
        return add;
    }

    void showProposalsForTest(List<Proposal> found) {
        showProposals(found);
    }

    Map<Proposal, Checkbox> proposalChecks() {
        return proposals;
    }

    void acceptForTest() {
        accept();
    }

    void setModelNameForTest(String name) {
        this.modelName = name;
    }

}
