package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotateService;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Mode;
import com.github.enerccio.marginalia.domain.service.AnnotateService.Proposal;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
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
import org.springframework.web.context.request.RequestAttributes;

import java.util.*;

/**
 * Lets a model annotate a part of the story (the desktop and the phone story tab): the author says what to look for and with which
 * model, the notes the model proposes are listed, each with a check box, and only <b>Add the selected annotations</b> makes
 * annotations of them. The model reads the part with the story around it, see {@link AnnotateService}.
 * <p>
 * Opened on a part it annotates that part only. Opened without a part ({@link #openRange}, the tools menu of the story) it asks for a
 * range of the parts of the story: they are annotated one after another with the same instructions, mode and model, and the proposals
 * are listed by part.
 */
@Configurable(preConstruction = true)
public class AnnotateDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private AnnotateService annotateService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;
    /**
     * The part the dialog annotates, null for the dialog of a range.
     */
    private final ChatMessage part;
    private final Runnable onAdded;

    private final ComboBox<Integer> fromPart = new ComboBox<>();
    private final ComboBox<Integer> toPart = new ComboBox<>();
    private final VerticalLayout batchRange;
    private final Span batchProgress = new Span();
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
    private final List<Entry> proposals = new ArrayList<>();

    /**
     * The parts of the branch of the story that have a text, oldest first, and where each is in the branch (the numbers of the
     * annotations panel).
     */
    private final List<ChatMessage> branch = new ArrayList<>();
    private final Map<Long, Integer> positions = new LinkedHashMap<>();

    /**
     * The parts the run still has to annotate; touched on the UI thread only.
     */
    private final Deque<ChatMessage> pending = new ArrayDeque<>();
    private final List<String> notes = new ArrayList<>();
    private boolean batchRun;
    private int total;
    private int finished;
    private RequestAttributes attributes;

    private CancellationToken cancellationToken;
    private boolean running;
    private String modelName;

    /**
     * A proposal of the model and the part it is about.
     */
    private record Entry(ChatMessage part, Proposal proposal, Checkbox chosen) {
    }

    /**
     * @param onAdded called after annotations were added to the part
     */
    public static void open(Manuscript manuscript, ChatMessage part, Runnable onAdded) {
        create(manuscript, part, onAdded).open();
    }

    /**
     * Opens the dialog for a range of the parts of the active branch of the story, both ends of the range have to be chosen.
     *
     * @param onAdded called after annotations were added
     */
    public static void openRange(Manuscript manuscript, Runnable onAdded) {
        createRange(manuscript, onAdded).open();
    }

    /**
     * @return the dialog, not opened
     */
    static AnnotateDialog create(Manuscript manuscript, ChatMessage part, Runnable onAdded) {
        return new AnnotateDialog(manuscript, Objects.requireNonNull(part, "part"), onAdded);
    }

    /**
     * @return the dialog of a range, not opened
     */
    static AnnotateDialog createRange(Manuscript manuscript, Runnable onAdded) {
        return new AnnotateDialog(manuscript, null, onAdded);
    }

    private AnnotateDialog(Manuscript manuscript, ChatMessage part, Runnable onAdded) {
        this.manuscript = manuscript;
        this.part = part;
        this.onAdded = onAdded;

        setHeaderTitle(loc.getValue(isRange() ? L.LABEL_ANNOTATE_BATCH : L.LABEL_ANNOTATE_WITH_AI));
        setWidth("min(900px, 95vw)");
        setHeight("85vh");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Span info = new Span(loc.getValue(L.MSG_ANNOTATE_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        loadBranch();
        fromPart.setLabel(loc.getValue(L.LABEL_ANNOTATE_FROM_PART));
        toPart.setLabel(loc.getValue(L.LABEL_ANNOTATE_TO_PART));
        for (ComboBox<Integer> range : List.of(fromPart, toPart)) {
            range.setItems(indices());
            range.setItemLabelGenerator(this::partLabel);
            range.setAllowCustomValue(false);
            range.setWidthFull();
        }
        fromPart.addValueChangeListener(_ -> refreshRunEnabled());
        toPart.addValueChangeListener(_ -> refreshRunEnabled());
        Span batchHint = new Span(loc.getValue(L.MSG_ANNOTATE_BATCH_HINT));
        batchHint.addClassName(CssStyles.HINT_TEXT);
        batchRange = new VerticalLayout(fromPart, toPart, batchHint);
        batchRange.setPadding(false);
        batchRange.setSpacing(false);
        batchRange.setVisible(isRange());
        batchProgress.addClassName(CssStyles.HINT_TEXT);
        batchProgress.setVisible(false);

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

        VerticalLayout content = new VerticalLayout(info, batchRange, instructions, mode, model, modelHint, extract, run, progress,
                batchProgress, message, answerDetails, proposalsLayout);
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
        setHeaderTitle(loc.getValue(value ? L.LABEL_ANNOTATING : isRange() ? L.LABEL_ANNOTATE_BATCH : L.LABEL_ANNOTATE_WITH_AI));
        run.setText(loc.getValue(value ? L.LABEL_STOP : L.LABEL_ANNOTATE_RUN));
        run.setThemeName(value ? "error primary" : "primary");
        instructions.setReadOnly(value);
        mode.setReadOnly(value);
        model.setEnabled(!value);
        extractModel.setEnabled(!value);
        fromPart.setReadOnly(value);
        toPart.setReadOnly(value);
        refreshRunEnabled();
        progress.setVisible(value);
        batchProgress.setVisible(value && batchRun);
        add.setEnabled(!value && !proposals.isEmpty());
    }

    /**
     * The run of a range needs both ends of it; a running one can always be stopped.
     */
    private void refreshRunEnabled() {
        run.setEnabled(running || !isRange() || (fromPart.getValue() != null && toPart.getValue() != null));
    }

    private boolean isRange() {
        return part == null;
    }

    private void startOrStop() {
        if (running) {
            // nothing more is started after the part in progress
            pending.clear();
            if (cancellationToken != null && !cancellationToken.isCancelled()) {
                cancellationToken.cancel();
            }
            return;
        }

        List<ChatMessage> parts = selectedParts();
        if (parts.isEmpty()) {
            return;
        }
        clear();
        batchRun = parts.size() > 1;
        total = parts.size();
        finished = 0;
        pending.addAll(parts);
        attributes = ThreadCopyRequestAttributes.create();
        showRunning(true);
        annotateNext(UI.getCurrent());
    }

    /**
     * Starts the annotation of the next part of the run, the run is over when there is none. Called on the UI thread: when the
     * part before it is done, from {@code ui.access}.
     */
    private void annotateNext(UI ui) {
        ChatMessage next;
        boolean aborted = false;
        while ((next = pending.poll()) != null) {
            if (batchRun) {
                batchProgress.setText(String.format(loc.getValue(L.MSG_ANNOTATE_BATCH_PROGRESS), positionOf(next), finished + 1, total));
            }
            try (InRequestScope _ = new InRequestScope(attributes)) {
                GenerationOptions options = GenerationOptions.defaults().withModel(model.getAi(), model.getProtocol());
                AI used = model.getAi() != null ? model.getAi() : manuscript.getAi();
                modelName = used == null ? null : used.getName();
                cancellationToken = annotateService.annotate(manuscript, next, new AnnotateService.Request(instructions.getValue(), null,
                        options, mode.getValue(), extractModel.getAi(), extractModel.getProtocol()), callback(ui, next));
                if (cancellationToken != null) {
                    return;
                }
                pending.clear();
                aborted = true;
                Notification.error(loc.getValue(L.ERROR_ANNOTATE_FAILED));
            } catch (AnnotateService.ContextInsufficient e) {
                if (!batchRun) {
                    aborted = true;
                    Notification.error(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
                } else {
                    skipped(next, loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
                    finished++;
                    continue;
                }
            } catch (Exception e) {
                pending.clear();
                aborted = true;
                UIUtils.internalServerError(loc, e);
            }
            break;
        }
        endRun(!aborted);
    }

    /**
     * The run is over: what the model found is listed, or the lack of it said.
     *
     * @param complete every part was asked, not stopped and not failed
     */
    private void endRun(boolean complete) {
        showRunning(false);
        if (proposals.isEmpty() && notes.isEmpty()) {
            if (complete) {
                showMessage(loc.getValue(L.MSG_ANNOTATE_NO_FINDINGS));
            }
        } else if (!notes.isEmpty()) {
            showMessage(String.join(" ", notes));
        }
        add.setEnabled(proposals.stream().anyMatch(entry -> entry.chosen().getValue()));
    }

    private void skipped(ChatMessage part, String reason) {
        notes.add(batchRun ? String.format(loc.getValue(L.MSG_ANNOTATE_BATCH_SKIPPED), positionOf(part), reason) : reason);
    }

    /**
     * What the annotation of one part ends in; the next part of the run follows, unless the run ended with it.
     */
    private AnnotateService.Callback callback(UI ui, ChatMessage current) {
        return new AnnotateService.Callback() {
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
                    addProposals(current, found);
                    if (StringUtils.isNotBlank(raw)) {
                        showAnswer(raw);
                    }
                    finished++;
                    annotateNext(ui);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onUnreadable(String raw, String reason) {
                ui.access(() -> {
                    skipped(current, String.format(loc.getValue(L.MSG_ANNOTATE_UNREADABLE), reason));
                    showAnswer(raw);
                    answerDetails.setOpened(true);
                    finished++;
                    annotateNext(ui);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onTerminated(String raw) {
                ui.access(() -> {
                    pending.clear();
                    endRun(false);
                    showAnswer(raw);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                ui.access(() -> {
                    pending.clear();
                    endRun(false);
                    UIUtils.inferenceError(loc, throwable);
                    UIPushGuard.push(ui);
                });
            }
        };
    }

    private void clear() {
        proposals.clear();
        pending.clear();
        notes.clear();
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

    // ---- the parts of the story

    /**
     * Reads the branch the story is at: the parts a range can be chosen from. A part of another branch has only the parts before it.
     */
    private void loadBranch() {
        try {
            List<ChatMessage> all = manuscript.getActiveLeaf() == null ? List.of()
                    : chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf());
            if (part != null && all.stream().noneMatch(message -> Objects.equals(message.getId(), part.getId()))) {
                all = chatMessageService.getBranchFromLeaf(part);
            }
            for (int i = 0; i < all.size(); i++) {
                ChatMessage candidate = all.get(i);
                if (StringUtils.isNotBlank(candidate.getResponse())) {
                    branch.add(candidate);
                    positions.put(candidate.getId(), i + 1);
                }
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private List<Integer> indices() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < branch.size(); i++) {
            indices.add(i);
        }
        return indices;
    }

    private int indexOf(ChatMessage message) {
        for (int i = 0; i < branch.size(); i++) {
            if (Objects.equals(branch.get(i).getId(), message.getId())) {
                return i;
            }
        }
        return -1;
    }

    private int positionOf(ChatMessage message) {
        return positions.getOrDefault(message.getId(), 0);
    }

    private String partLabel(Integer index) {
        ChatMessage message = branch.get(index);
        return String.format(loc.getValue(L.LABEL_ANNOTATION_PART_NUMBER), positionOf(message)) + ": "
                + StringUtils.abbreviate(StringUtils.normalizeSpace(message.getResponse()), 60);
    }

    /**
     * The parts the run annotates: the range (either way round), or the part the dialog was opened on.
     */
    private List<ChatMessage> selectedParts() {
        if (!isRange()) {
            return List.of(part);
        }
        if (fromPart.getValue() == null || toPart.getValue() == null) {
            return List.of();
        }
        int from = Math.min(fromPart.getValue(), toPart.getValue());
        int to = Math.max(fromPart.getValue(), toPart.getValue());
        return new ArrayList<>(branch.subList(from, to + 1));
    }

    // ---- the proposals

    /**
     * Lists the notes the model proposed for the part, every one chosen. In a batch run the notes of a part follow its number.
     */
    private void addProposals(ChatMessage about, List<Proposal> found) {
        if (found.isEmpty()) {
            return;
        }
        if (proposals.isEmpty()) {
            proposalsLayout.add(new Span(loc.getValue(L.LABEL_ANNOTATE_PROPOSALS)));
        }
        if (batchRun) {
            Span header = new Span(String.format(loc.getValue(L.LABEL_ANNOTATION_PART_NUMBER), positionOf(about)));
            header.addClassName(CssStyles.ANNOTATION_PART);
            proposalsLayout.add(header);
        }
        for (Proposal proposal : found) {
            Checkbox chosen = new Checkbox(StringUtils.defaultString(proposal.note()));
            chosen.setValue(true);
            chosen.addValueChangeListener(_ -> add.setEnabled(!running && proposals.stream().anyMatch(entry -> entry.chosen().getValue())));
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
            proposals.add(new Entry(about, proposal, chosen));
            proposalsLayout.add(row);
        }
        proposalsLayout.setVisible(true);
    }

    private void accept() {
        // by part, in the order of the story
        Map<ChatMessage, List<Proposal>> chosen = new LinkedHashMap<>();
        for (Entry entry : proposals) {
            if (entry.chosen().getValue()) {
                chosen.computeIfAbsent(entry.part(), _ -> new ArrayList<>()).add(entry.proposal());
            }
        }
        if (chosen.isEmpty()) {
            return;
        }
        try {
            int made = 0;
            for (Map.Entry<ChatMessage, List<Proposal>> about : chosen.entrySet()) {
                made += annotateService.accept(manuscript, about.getKey(), about.getValue(), modelName).size();
            }
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
        addProposals(part, found);
        if (proposals.isEmpty()) {
            showMessage(loc.getValue(L.MSG_ANNOTATE_NO_FINDINGS));
        }
        add.setEnabled(!proposals.isEmpty());
    }

    Map<Proposal, Checkbox> proposalChecks() {
        Map<Proposal, Checkbox> checks = new LinkedHashMap<>();
        proposals.forEach(entry -> checks.put(entry.proposal(), entry.chosen()));
        return checks;
    }

    ComboBox<Integer> fromField() {
        return fromPart;
    }

    ComboBox<Integer> toField() {
        return toPart;
    }

    List<ChatMessage> selectedPartsForTest() {
        return selectedParts();
    }

    void addProposalsForTest(ChatMessage about, List<Proposal> found) {
        batchRun = true;
        addProposals(about, found);
        add.setEnabled(!proposals.isEmpty());
    }

    void acceptForTest() {
        accept();
    }

    void setModelNameForTest(String name) {
        this.modelName = name;
    }

}
