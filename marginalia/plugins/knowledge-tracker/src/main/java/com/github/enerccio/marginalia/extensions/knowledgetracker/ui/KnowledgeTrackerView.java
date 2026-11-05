package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeApplier;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeLog;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.BranchProcessor.Report;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.RunEstimate;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.SeparatorText;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionRun.Phase;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgeProcessService.Status;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * The Knowledge tab of the story sidebar: the tracked characters of the book, what each knows and does not know, the old
 * knowledge that was retracted, and the settings of the plugin. The state is kept in memory while the tab is open and
 * stored in the book after every change.
 */
@Configurable(preConstruction = true)
public class KnowledgeTrackerView extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final KnowledgeStateService service;
    private final KnowledgeProcessService processService;
    private final ExtractionService extractionService;
    private final ExtractionRuns runs;
    private final Manuscript manuscript;
    /** The story tab this one belongs to, to scroll it to a part; {@code null} when there is none. */
    private final StoryEditor story;
    private KnowledgeState state;
    /** The parts of the active branch by uuid, as the last refresh read them (the number of a part is its position in the branch). */
    private final Map<String, Integer> positions = new HashMap<>();
    private final Map<String, Long> messageIds = new HashMap<>();

    /** Ids of the characters that are shown open and of those whose old knowledge is open; kept over a refresh. */
    private final Set<String> open = new HashSet<>();
    private final Set<String> oldOpen = new HashSet<>();

    private final VerticalLayout list = new VerticalLayout();
    private Button settingsButton;
    private boolean processedOpen;
    private boolean logOpen;
    /** The progress panel of the run that is shown, and the listener that keeps this view in line with that run. */
    private ProgressPanel progressPanel;
    private ExtractionRun watched;
    /** At most one reload of the tab waits at a time, however fast the run reports. */
    private final Runnable runRefresh = UiAccess.coalesced(this, () -> {
        state = loadState();
        refresh();
    });
    /**
     * The tab shows the state, which a run changes only when it applies something (the {@code onChange} below tells) and the buttons
     * that depend on whether a run goes on: so it is rebuilt when the run waits for a review or ends, not at every phase. A rebuild reads
     * the whole branch of the story and the whole state, which is slow in a long book.
     */
    private final java.util.function.Consumer<ExtractionRun.Status> runListener = status -> {
        if (status.phase().isFinished() || status.phase() == ExtractionRun.Phase.REVIEW) {
            runRefresh.run();
        }
    };
    /** A command or a run stored the state: one reload however many commits come in a row. */
    private final Runnable changeRefresh = UiAccess.coalesced(this, () -> {
        state = loadState();
        showSettingsState();
        refresh();
    });
    /** Removes the listener that reads the state again when a command changes it, set while the view is attached. */
    private Runnable changeRegistration;

    /** An entry of the start point selector: a part, or {@code null} for the start of the book. */
    private record StartOption(String uuid, String label) {
    }

    /** How many of the processed parts the list shows, the latest first. */
    private static final int SHOWN_PROCESSED = 30;

    public KnowledgeTrackerView(Manuscript manuscript, StoryEditor story, KnowledgeStateService service,
                                KnowledgeProcessService processService, ExtractionService extractionService, ExtractionRuns runs) {
        this.manuscript = manuscript;
        this.story = story;
        this.service = service;
        this.processService = processService;
        this.extractionService = extractionService;
        this.runs = runs;
        this.state = loadState();

        setSizeFull();
        setPadding(false);
        buildUI();
        refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // a command (the command bar, an agent) stores the state behind the back of the view, which keeps it in memory
        changeRegistration = service.onChange(manuscript, changeRefresh);
        ExtractionRun running = runs.running(manuscript);
        if (running != null) {
            watch(running);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (changeRegistration != null) {
            changeRegistration.run();
            changeRegistration = null;
        }
        if (watched != null) {
            watched.removeListener(runListener);
            watched = null;
        }
        if (progressPanel != null) {
            progressPanel.close();
            progressPanel = null;
        }
        super.onDetach(detachEvent);
    }

    private KnowledgeState loadState() {
        try {
            // the story view keeps the book it was opened with, the state may have been saved since
            return service.load(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return service.read(manuscript);
        }
    }

    private void buildUI() {
        Button add = new Button(T.localized(TRACK_A_CHARACTER), Solid.PLUS.create(), _ -> openCharacterDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        settingsButton = new Button(Solid.GEAR.create());
        settingsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        settingsButton.setAriaLabel(T.localized(SETTINGS));
        settingsButton.setTooltipText(T.localized(SETTINGS));
        Popover popover = new Popover();
        popover.setTarget(settingsButton);
        popover.setModal(false);
        popover.add(createSettings());
        showSettingsState();

        Button reload = new Button(Solid.ARROWS_ROTATE.create(), _ -> changeRefresh.run());
        reload.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        reload.setAriaLabel(T.localized(REFRESH));
        reload.setTooltipText(T.localized(REFRESH));

        HorizontalLayout buttons = new HorizontalLayout(add, reload, settingsButton);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setPadding(true);
        buttons.setSpacing(false);

        list.setPadding(false);
        list.addClassName(KnowledgeTrackerStyles.LIST);
        list.setSpacing(false);
        Scroller scroller = new Scroller(list);
        scroller.setSizeFull();

        add(buttons, scroller);
        setFlexGrow(1, scroller);
    }

    // --- settings ---

    /** The button is green while the knowledge is put into the prompt. */
    private void showSettingsState() {
        if (state.getSettings().isInPrompt()) {
            settingsButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        } else {
            settingsButton.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
        }
    }

    private Component createSettings() {
        BookSettings settings = state.getSettings();

        Checkbox inPrompt = new Checkbox(T.localized(IN_PROMPT), settings.isInPrompt());
        inPrompt.setTooltipText(T.localized(INSERT_WHAT_THE_PRESENT_CHARACTERS_KNOW));
        inPrompt.addValueChangeListener(e -> {
            state.getSettings().setInPrompt(e.getValue());
            showSettingsState();
            saveState();
        });

        IntegerField depth = new IntegerField(T.localized(DEPTH));
        depth.setHelperText(T.localized(MESSAGES_AFTER_THE_KNOWLEDGE));
        depth.setMin(0);
        depth.setStepButtonsVisible(true);
        depth.setValue(settings.getDepth());
        depth.setWidthFull();
        depth.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 0) {
                state.getSettings().setDepth(e.getValue());
                saveState();
            }
        });

        Select<LLMRole> role = new Select<>();
        role.setLabel(T.localized(ROLE));
        // text the model reads, not the result of a function call
        role.setItems(LLMRole.SYSTEM, LLMRole.ASSISTANT, LLMRole.USER);
        role.setItemLabelGenerator(r -> T.localized(r == LLMRole.SYSTEM ? SYSTEM : r == LLMRole.ASSISTANT ? ASSISTANT : USER));
        role.setValue(settings.getRole());
        role.setWidthFull();
        role.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                state.getSettings().setRole(e.getValue());
                saveState();
            }
        });

        Checkbox autoProcess = new Checkbox(T.localized(PROCESS_NEW_PARTS_AUTOMATICALLY), settings.isAutoProcess());
        autoProcess.setTooltipText(T.localized(BEFORE_A_GENERATION_THE_NEW_PARTS));
        autoProcess.addValueChangeListener(e -> {
            state.getSettings().setAutoProcess(e.getValue());
            saveState();
        });

        VerticalLayout extraction = createExtractionSettings(settings);

        Checkbox closedWorld = new Checkbox(T.localized(SAY_THAT_NOTHING_ELSE_IS_KNOWN), settings.isClosedWorld());
        closedWorld.setTooltipText(T.localized(THE_CHARACTER_HAS_NEVER_MET_OR_HEARD));
        closedWorld.addValueChangeListener(e -> {
            state.getSettings().setClosedWorld(e.getValue());
            saveState();
        });

        Checkbox annotationFacts = new Checkbox(T.localized(GIVE_THE_FACTS_TO_THE_ANNOTATION), settings.isFactsForAnnotation());
        annotationFacts.setTooltipText(T.localized(WHEN_A_MODEL_ANNOTATES_A_PART));
        annotationFacts.addValueChangeListener(e -> {
            state.getSettings().setFactsForAnnotation(e.getValue());
            saveState();
        });

        TextArea bookSeparators = separatorsField(T.localized(SEPARATORS_OF_THIS_BOOK), SeparatorText.format(settings.getSeparators()));
        bookSeparators.setHelperText(T.localized(SEPARATORS_HELP));
        bookSeparators.addValueChangeListener(e -> {
            state.getSettings().setSeparators(SeparatorText.parse(e.getValue()));
            saveState();
        });

        TextArea mySeparators = separatorsField(T.localized(MY_SEPARATORS), SeparatorText.format(userSeparators()));
        mySeparators.addValueChangeListener(e -> {
            try {
                service.saveUserSeparators(SeparatorText.parse(e.getValue()));
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });

        VerticalLayout controls = new VerticalLayout(inPrompt, depth, role, closedWorld, annotationFacts, autoProcess, extraction, bookSeparators, mySeparators);
        controls.addClassName(KnowledgeTrackerStyles.SETTINGS);
        controls.setPadding(false);
        controls.setSpacing(false);
        return controls;
    }

    /** What the model reads: on or off, review or not, parts per run, the lorebook, the model. */
    private VerticalLayout createExtractionSettings(BookSettings settings) {
        Checkbox extract = new Checkbox(T.localized(READ_WHAT_THE_CHARACTERS_LEARN), settings.isExtractWithModel());
        extract.setTooltipText(T.localized(ASK_THE_MODEL_WHAT_EACH_TRACKED));
        Checkbox ask = new Checkbox(T.localized(ASK_BEFORE_APPLYING), settings.isAskBeforeApplying());
        ask.setTooltipText(T.localized(SHOW_WHAT_THE_MODEL_FOUND));
        Checkbox readBack = new Checkbox(T.localized(LET_THE_MODEL_READ_EARLIER_PARTS), settings.isReadEarlierParts());
        readBack.setTooltipText(T.localized(WHEN_AN_ITEM_CAME_FROM_AN_EARLIER_PART));
        IntegerField parts = new IntegerField(T.localized(PARTS_PER_RUN));
        parts.setMin(1);
        parts.setStepButtonsVisible(true);
        parts.setValue(settings.getPartsPerRun());
        parts.setWidthFull();
        IntegerField tidyParts = new IntegerField(T.localized(TIDY_PARTS_OF_CONTEXT));
        tidyParts.setMin(1);
        tidyParts.setStepButtonsVisible(true);
        tidyParts.setValue(settings.getTidyContextParts());
        tidyParts.setHelperText(T.localized(THE_LAST_PROCESSED_PARTS_THE_TIDY_PASS_READS));
        tidyParts.setWidthFull();
        Checkbox autoTidy = new Checkbox(T.localized(TIDY_AUTOMATICALLY), settings.isAutoTidy());
        autoTidy.setTooltipText(T.localized(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES));
        IntegerField tidyEvery = new IntegerField(T.localized(TIDY_AFTER_N_PARTS));
        tidyEvery.setMin(1);
        tidyEvery.setStepButtonsVisible(true);
        tidyEvery.setValue(settings.getTidyEveryParts());
        tidyEvery.setWidthFull();
        IntegerField tidyMin = new IntegerField(T.localized(TIDY_FROM_N_ITEMS));
        tidyMin.setMin(2);
        tidyMin.setStepButtonsVisible(true);
        tidyMin.setValue(settings.getTidyMinItems());
        tidyMin.setHelperText(T.localized(A_CHARACTER_WITH_FEWER_ITEMS_IS_NOT_TIDIED));
        tidyMin.setWidthFull();
        Select<LoreMode> lore = new Select<>();
        lore.setLabel(T.localized(LOREBOOK_FOR_THE_MODEL));
        lore.setItems(LoreMode.RESOLVE, LoreMode.WHOLE, LoreMode.NONE);
        lore.setItemLabelGenerator(m -> T.localized(m == LoreMode.RESOLVE ? ENTRIES_THE_PARTS_ACTIVATE
                : m == LoreMode.WHOLE ? THE_WHOLE_LOREBOOK : NO_LOREBOOK));
        lore.setValue(settings.getLoreMode());
        lore.setWidthFull();

        ComboBox<AI> model = new ComboBox<>(T.localized(MODEL_FOR_THE_EXTRACTION));
        ComboBox<Protocol> protocol = new ComboBox<>(T.localized(PROTOCOL_FOR_THE_EXTRACTION));
        model.setPlaceholder(T.localized(THE_MODEL_OF_THE_BOOK));
        protocol.setPlaceholder(T.localized(THE_MODEL_OF_THE_BOOK));
        try {
            List<AI> models = extractionService.availableModels();
            model.setItems(models);
            model.setItemLabelGenerator(AI::getName);
            models.stream().filter(ai -> ai.getUuid().equals(settings.getAiUuid())).findFirst().ifPresent(model::setValue);
            List<Protocol> protocols = extractionService.availableProtocols();
            protocol.setItems(protocols);
            protocol.setItemLabelGenerator(Protocol::getName);
            protocols.stream().filter(p -> p.getUuid().equals(settings.getProtocolUuid())).findFirst().ifPresent(protocol::setValue);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        model.setWidthFull();
        protocol.setWidthFull();

        Runnable enable = () -> {
            boolean on = extract.getValue();
            ask.setEnabled(on);
            readBack.setEnabled(on);
            parts.setEnabled(on);
            tidyParts.setEnabled(on);
            tidyMin.setEnabled(on);
            autoTidy.setEnabled(on);
            tidyEvery.setEnabled(on && autoTidy.getValue());
            lore.setEnabled(on);
            model.setEnabled(on);
            protocol.setEnabled(on);
        };
        extract.addValueChangeListener(e -> {
            state.getSettings().setExtractWithModel(e.getValue());
            enable.run();
            saveState();
        });
        ask.addValueChangeListener(e -> {
            state.getSettings().setAskBeforeApplying(e.getValue());
            saveState();
        });
        readBack.addValueChangeListener(e -> {
            state.getSettings().setReadEarlierParts(e.getValue());
            saveState();
        });
        parts.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                state.getSettings().setPartsPerRun(e.getValue());
                saveState();
            }
        });
        tidyParts.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                state.getSettings().setTidyContextParts(e.getValue());
                saveState();
            }
        });
        autoTidy.addValueChangeListener(e -> {
            state.getSettings().setAutoTidy(e.getValue());
            tidyEvery.setEnabled(extract.getValue() && e.getValue());
            saveState();
        });
        tidyEvery.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                state.getSettings().setTidyEveryParts(e.getValue());
                saveState();
            }
        });
        tidyMin.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 2) {
                state.getSettings().setTidyMinItems(e.getValue());
                saveState();
            }
        });
        lore.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                state.getSettings().setLoreMode(e.getValue());
                saveState();
            }
        });
        model.addValueChangeListener(e -> {
            state.getSettings().setAiUuid(e.getValue() == null ? null : e.getValue().getUuid());
            saveState();
        });
        protocol.addValueChangeListener(e -> {
            state.getSettings().setProtocolUuid(e.getValue() == null ? null : e.getValue().getUuid());
            saveState();
        });
        enable.run();

        VerticalLayout box = new VerticalLayout(extract, ask, readBack, parts, autoTidy, tidyEvery, tidyParts, tidyMin, lore, model, protocol);
        box.setPadding(false);
        box.setSpacing(false);
        return box;
    }

    private static TextArea separatorsField(String label, String value) {
        TextArea field = new TextArea(label);
        field.setWidthFull();
        field.setMinHeight("80px");
        field.setValue(value);
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.setValueChangeTimeout(1000);
        return field;
    }

    private List<String> userSeparators() {
        try {
            return service.getUserSeparators();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return List.of();
        }
    }

    // --- processing ---

    /**
     * Where the processing stands, the start point, the button that processes now and the parts that were processed.
     */
    private Component createProcessingPanel() {
        Div panel = new Div();
        panel.addClassNames(KnowledgeTrackerStyles.CARD, KnowledgeTrackerStyles.PANEL);
        Div title = new Div(new Span(T.localized(PROCESSING)));
        title.addClassName(KnowledgeTrackerStyles.NAME);
        panel.add(title);

        List<Part> branch;
        Status status;
        positions.clear();
        messageIds.clear();
        try {
            branch = processService.branch(manuscript);
            status = processService.status(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return panel;
        }
        for (int i = 0; i < branch.size(); i++) {
            positions.put(branch.get(i).uuid(), i + 1);
            if (branch.get(i).id() != null) {
                messageIds.put(branch.get(i).uuid(), branch.get(i).id());
            }
        }
        String line = branch.isEmpty() ? T.localized(THE_BOOK_HAS_NO_PARTS_YET)
                : status.cursor() > 0 ? T.localized(PROCESSED_UP_TO_PART).formatted(status.cursor(), status.parts())
                : T.localized(NOTHING_IS_PROCESSED_YET).formatted(status.parts());
        Div info = new Div(new Span(line));
        info.addClassName(KnowledgeTrackerStyles.COUNTS);
        panel.add(info);
        if (branch.isEmpty()) {
            return panel;
        }

        Button process = new Button(T.localized(PROCESS_NOW), Solid.PLAY.create(), _ -> processNow());
        process.addThemeVariants(ButtonVariant.LUMO_SMALL);
        process.setTooltipText(T.localized(READ_THE_PARTS_THAT_ARE_NOT_PROCESSED));
        ExtractionRun running = runs.running(manuscript);
        process.setEnabled(status.unread() > 0 && running == null);
        Button tidyButton = new Button(T.localized(TIDY_KNOWLEDGE), Solid.BROOM.create(), _ -> tidy());
        tidyButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        tidyButton.setTooltipText(T.localized(DROP_OR_MERGE_ITEMS_THAT_ARE_NOT_WORTH_KEEPING));
        tidyButton.setEnabled(running == null && state.getSettings().isExtractWithModel());
        Button next = new Button(T.localized(PROCESS_NEXT_PART), Solid.FORWARD_STEP.create(), _ -> processNext());
        next.addThemeVariants(ButtonVariant.LUMO_SMALL);
        next.setTooltipText(T.localized(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED));
        next.setEnabled(status.unread() > 0 && running == null);
        panel.add(new Div(process, next, tidyButton));
        if (running != null) {
            panel.add(createRunButton(running));
        }
        ExtractionRun last = runs.latest(manuscript);
        if (last != null && last.getStatus().phase().isFinished() && !last.getThoughts().isEmpty()) {
            // the reasoning of the last run, also when it proposed nothing and no review ever opened
            Button lastRun = new Button(T.localized(LAST_RUN_WHAT_THE_MODEL_THOUGHT), Solid.LIGHTBULB.create(), _ -> showProgress(last));
            lastRun.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            panel.add(lastRun);
        }
        ExtractionService.Proposal held = runs.peekHeld(manuscript);
        if (held != null) {
            panel.add(createHeldButton(held));
        }
        panel.add(createStartSelect(branch));

        Component processed = createProcessedParts(branch, status);
        if (processed != null) {
            panel.add(processed);
        }
        Component log = createChangeLog(branch);
        if (log != null) {
            panel.add(log);
        }
        return panel;
    }

    /**
     * What the command {@code knowledge-process} with {@code ask true} found and holds: the same review as a run of the tab has, so that
     * the author who typed the command can look at it and apply it here.
     */
    private Button createHeldButton(ExtractionService.Proposal held) {
        Button button = new Button(T.localized(CHANGES_FROM_A_COMMAND_TO_REVIEW).formatted(held.changes().changes().size()));
        button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(_ -> new ReviewDialog(1, held, () -> applyHeld(held), () -> discardHeld(held)).open());
        return button;
    }

    private void applyHeld(ExtractionService.Proposal held) {
        try {
            runs.takeHeld(manuscript);
            if (held.tidy()) {
                ChangeApplier.Result result = extractionService.commitTidy(manuscript, held, false);
                Notification.success(T.localized(TIDY_DONE).formatted(result.retracted(), result.added()));
            } else {
                ChangeApplier.Result result = extractionService.commit(manuscript, held);
                Notification.success(T.localized(DONE_PARTS_ADDED_RETRACTED).formatted(held.parts().size(), result.added(), result.retracted()));
            }
        } catch (StaleProposalException e) {
            Notification.warning(T.localized(THE_STORY_CHANGED_RUN_THE_COMMAND_AGAIN));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    private void discardHeld(ExtractionService.Proposal held) {
        if (runs.peekHeld(manuscript) == held) {
            runs.takeHeld(manuscript);
        }
        refresh();
    }

    /** A run is going on: a button to show its panel, or, when its proposal waits, to review it. */
    private Button createRunButton(ExtractionRun run) {
        ExtractionRun.Status status = run.getStatus();
        boolean review = status.phase() == Phase.REVIEW && status.proposal() != null;
        Button button = new Button(review ? T.localized(CHANGES_TO_REVIEW).formatted(status.proposal().changes().changes().size())
                : T.localized(PROCESSING_THE_STORY));
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        if (review) {
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        button.addClickListener(_ -> {
            if (progressPanel == null || !progressPanel.isOpened()) {
                showProgress(run);
            }
            if (review) {
                progressPanel.openReview();
            }
        });
        return button;
    }

    private Component createStartSelect(List<Part> branch) {
        List<StartOption> options = new ArrayList<>();
        StartOption startOfBook = new StartOption(null, T.localized(START_OF_THE_BOOK));
        options.add(startOfBook);
        StartOption selected = startOfBook;
        for (int i = 0; i < branch.size(); i++) {
            StartOption option = new StartOption(branch.get(i).uuid(), partLabel(i + 1, branch.get(i)));
            options.add(option);
            if (option.uuid().equals(state.getStartMessage())) {
                selected = option;
            }
        }
        ComboBox<StartOption> start = new ComboBox<>(T.localized(START_POINT));
        start.setItems(options);
        start.setItemLabelGenerator(StartOption::label);
        start.setHelperText(T.localized(PARTS_BEFORE_THE_START_POINT));
        start.setValue(selected);
        start.setAllowCustomValue(false);
        start.setWidthFull();
        start.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                run(() -> processService.setStart(manuscript, e.getValue().uuid()));
            }
        });
        return start;
    }

    /**
     * The latest processed parts, each with a button to reprocess from it, and a note on the ones whose text changed
     * since they were processed. Nothing is reprocessed by itself because of a change.
     */
    private Component createProcessedParts(List<Part> branch, Status status) {
        Set<String> processed = new HashSet<>();
        state.getProcessed().forEach(p -> processed.add(p.getUuid()));
        Set<String> changed = new HashSet<>(status.changed());
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < branch.size(); i++) {
            if (processed.contains(branch.get(i).uuid())) {
                positions.add(i + 1);
            }
        }
        if (positions.isEmpty()) {
            return null;
        }
        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        for (int n = positions.size() - 1; n >= 0 && positions.size() - n <= SHOWN_PROCESSED; n--) {
            int position = positions.get(n);
            Part part = branch.get(position - 1);
            Div text = new Div(new Span(partLabel(position, part)));
            text.addClassName(KnowledgeTrackerStyles.ITEM_TEXT);
            Div row = new Div(text);
            row.addClassName(KnowledgeTrackerStyles.ITEM);
            if (changed.contains(part.uuid())) {
                Span stale = new Span(T.localized(CHANGED_SINCE_PROCESSED));
                stale.addClassName(KnowledgeTrackerStyles.STALE);
                row.add(stale);
            }
            row.add(iconButton(Solid.ARROWS_ROTATE, T.localized(REPROCESS_FROM_HERE), _ ->
                    ConfirmDialog.show(T.localized(REPROCESS_FROM_PART).formatted(position), () -> reprocessFrom(part))));
            rows.add(row);
        }
        Details details = new Details(T.localized(PROCESSED_PARTS).formatted(positions.size()), rows);
        details.setOpened(processedOpen);
        details.addOpenedChangeListener(e -> processedOpen = e.isOpened());
        return details;
    }

    /**
     * What automatic runs applied without asking, newest first, each with a button that takes it back (the log itself is not
     * editable). Only the entries that can still be reverted have an enabled button.
     */
    private Component createChangeLog(List<Part> branch) {
        List<ChangeLogEntry> log = state.getChangeLog();
        if (log.isEmpty()) {
            return null;
        }
        Map<String, Integer> positions = new HashMap<>();
        for (int i = 0; i < branch.size(); i++) {
            positions.put(branch.get(i).uuid(), i + 1);
        }
        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        for (int n = log.size() - 1; n >= 0 && log.size() - n <= SHOWN_PROCESSED; n--) {
            ChangeLogEntry entry = log.get(n);
            boolean added = entry.getType() == ChangeLogEntry.Type.ADDED;
            Integer position = positions.get(entry.getMessageUuid());
            String what = (added ? T.localized(ADDED_TO) : T.localized(RETRACTED_FROM)).formatted(entry.getCharacterName())
                    + (position == null ? "" : " (" + T.localized(PART_N).formatted(position) + ")") + ": " + entry.getText();
            Div text = new Div(new Span(what));
            text.addClassName(KnowledgeTrackerStyles.ITEM_TEXT);
            Div row = new Div(text);
            row.addClassName(KnowledgeTrackerStyles.ITEM);
            if (entry.isReverted()) {
                Span done = new Span(T.localized(REVERTED));
                done.addClassName(KnowledgeTrackerStyles.STALE);
                row.add(done);
            } else {
                Button revert = iconButton(Solid.ROTATE_LEFT, T.localized(REVERT_THIS_CHANGE), _ -> revertChange(entry));
                revert.setEnabled(ChangeLog.canRevert(state, entry));
                row.add(revert);
            }
            rows.add(row);
        }
        Details details = new Details(T.localized(APPLIED_AUTOMATICALLY).formatted(log.size()), rows);
        details.setOpened(logOpen);
        details.addOpenedChangeListener(e -> logOpen = e.isOpened());
        return details;
    }

    private void revertChange(ChangeLogEntry entry) {
        try {
            ChangeLog.Outcome outcome = processService.revertChange(manuscript, entry.getId());
            if (outcome != ChangeLog.Outcome.REVERTED) {
                Notification.warning(T.localized(THE_ITEM_HAS_BEEN_CHANGED_SINCE));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    /**
     * <b>Process now</b>. Without the model it only notes who has met whom, which is quick and done here. With the model a run
     * starts on a worker thread and the progress panel shows it; the story can be read and edited meanwhile.
     */
    private void processNow() {
        process(0);
    }

    /** <b>Process the next part</b>: only the first part that is not processed yet, to try the settings or look at one step. */
    private void processNext() {
        process(1);
    }

    /** @param maxParts how many parts at most, 0 = all that are waiting */
    private void process(int maxParts) {
        try {
            if (!state.getSettings().isExtractWithModel()) {
                Report report = processService.process(manuscript, true, null, maxParts > 0 ? maxParts : KnowledgeProcessService.NO_LIMIT);
                Notification.success(report.processed() > 0 ? T.localized(PROCESSED_N_PARTS).formatted(report.processed())
                        : T.localized(NOTHING_NEW_TO_PROCESS));
            } else {
                RunEstimate estimate = maxParts == 0 && runs.running(manuscript) == null ? processService.estimate(manuscript) : null;
                if (estimate != null && estimate.isLong()) {
                    // a long series is many requests to the model: the user is told before it starts
                    ConfirmDialog.show(T.localized(READ_N_PARTS_IN_N_REQUESTS).formatted(estimate.parts(), estimate.runs(),
                            estimate.storyTokens()), this::startRunAndRefresh);
                    return;
                }
                startRun(maxParts);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        showSettingsState();
        refresh();
    }

    /** <b>Tidy knowledge</b>: the model drops or merges what is not worth keeping; reviewed or logged like any run. */
    private void tidy() {
        try {
            ExtractionRun run = runs.running(manuscript);
            if (run == null) {
                run = runs.startTidy(manuscript, null);
            }
            watch(run);
            showProgress(run);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        showSettingsState();
        refresh();
    }

    private void startRunAndRefresh() {
        try {
            startRun(0);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        showSettingsState();
        refresh();
    }

    /** Starts a run with the model (or shows the one that is going on) and its progress panel. */
    private void startRun() throws Exception {
        startRun(0);
    }

    private void startRun(int maxParts) throws Exception {
        ExtractionRun run = runs.running(manuscript);
        if (run == null) {
            run = runs.start(manuscript, null, null, maxParts);
        }
        watch(run);
        showProgress(run);
    }

    /**
     * Takes back what came from the part and the later ones. Without the model they are read again at once (who has met whom);
     * with the model a run reads them.
     */
    private void reprocessFrom(Part part) {
        try {
            if (state.getSettings().isExtractWithModel()) {
                if (processService.dropFrom(manuscript, part.uuid()) >= 0) {
                    state = loadState();
                    startRun();
                }
            } else {
                processService.reprocessFrom(manuscript, part.uuid(), true);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    /** Keeps this view in line with the run: its changes of the state and its phases show up in the tab. */
    private void watch(ExtractionRun run) {
        if (watched == run) {
            return;
        }
        if (watched != null) {
            watched.removeListener(runListener);
        }
        watched = run;
        run.addListener(runListener);
    }

    private void showProgress(ExtractionRun run) {
        if (progressPanel != null) {
            progressPanel.close();
        }
        progressPanel = new ProgressPanel(run);
        progressPanel.open();
    }

    /** Runs a change of the processing state and shows the state as it is stored afterwards. */
    private void run(ThrowingRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** "Part 12: the first words of the part", one line. */
    private static String partLabel(int position, Part part) {
        String text = part.text() == null ? "" : part.text().replaceAll("\\s+", " ").strip();
        String preview = text.length() > 50 ? text.substring(0, 50) + "…" : text;
        return T.localized(PART_N).formatted(position) + (preview.isEmpty() ? "" : ": " + preview);
    }

    // --- characters ---

    private void refresh() {
        list.removeAll();
        list.add(createProcessingPanel());
        if (state.getCharacters().isEmpty()) {
            Div empty = new Div(new Span(T.localized(NO_CHARACTER_IS_TRACKED_YET)));
            empty.addClassName(KnowledgeTrackerStyles.EMPTY);
            list.add(empty);
            return;
        }
        for (TrackedCharacter character : state.getCharacters()) {
            list.add(createCard(character));
        }
    }

    private void openCharacterDialog(TrackedCharacter character) {
        new TrackedCharacterDialog(state, character, this::persist).open();
    }

    private void confirmDeleteCharacter(TrackedCharacter character) {
        ConfirmDialog.show(T.localized(DELETE_THE_TRACKED_CHARACTER_AND_ALL).formatted(character.getName()), () -> {
            state.deleteCharacter(character.getId());
            open.remove(character.getId());
            oldOpen.remove(character.getId());
            persist();
        });
    }

    private Component createCard(TrackedCharacter character) {
        boolean isOpen = open.contains(character.getId());

        Button toggle = new Button((isOpen ? Solid.CHEVRON_UP : Solid.CHEVRON_DOWN).create(), _ -> {
            if (!open.remove(character.getId())) {
                open.add(character.getId());
            }
            refresh();
        });
        toggle.setAriaLabel(T.localized(SHOW_WHAT_THE_CHARACTER_KNOWS));

        Div name = new Div(new Span(character.getName()));
        name.addClassName(KnowledgeTrackerStyles.NAME);
        Div title = new Div(name);
        title.addClassName(KnowledgeTrackerStyles.CARD_TITLE);
        if (!character.getAliases().isEmpty()) {
            Div aliases = new Div(new Span(String.join(", ", character.getAliases())));
            aliases.addClassName(KnowledgeTrackerStyles.ALIASES);
            title.add(aliases);
        }
        Div counts = new Div(new Span(T.localized(ITEMS_ACTIVE_OLD)
                .formatted(character.activeItems().size(), character.oldItems().size())));
        counts.addClassName(KnowledgeTrackerStyles.COUNTS);
        title.add(counts);

        Button edit = iconButton(Solid.PEN, T.localized(EDIT), _ -> openCharacterDialog(character));
        Button delete = iconButton(Solid.TRASH, T.localized(DELETE), _ -> confirmDeleteCharacter(character));
        Div actions = new Div(edit, delete);
        actions.addClassName(KnowledgeTrackerStyles.ACTIONS);

        toggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        Div header = new Div(toggle, title, actions);
        header.addClassName(KnowledgeTrackerStyles.CARD_HEADER);

        Div card = new Div(header);
        card.addClassName(KnowledgeTrackerStyles.CARD);
        if (isOpen) {
            card.add(createBody(character));
        }
        return card;
    }

    private Component createBody(TrackedCharacter character) {
        Div body = new Div();
        body.addClassName(KnowledgeTrackerStyles.BODY);

        // the newest knowledge on top: the items are stored in the order they were added
        List<KnowledgeItem> active = newestFirst(character.activeItems());
        if (active.isEmpty()) {
            Div none = new Div(new Span(T.localized(NOTHING_IS_KNOWN_YET)));
            none.addClassName(KnowledgeTrackerStyles.EMPTY);
            body.add(none);
        }
        for (KnowledgeItem item : active) {
            body.add(createItem(character, item, false));
        }

        Button addItem = new Button(T.localized(ADD_KNOWLEDGE), Solid.PLUS.create(), _ -> openItemDialog(character, null));
        addItem.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        body.add(addItem);

        if (!character.getMet().isEmpty()) {
            String names = String.join(", ", character.getMet().stream().map(MetPerson::getName).toList());
            Div met = new Div(new Span(T.localized(HAS_MET).formatted(names)));
            met.addClassName(KnowledgeTrackerStyles.MET);
            body.add(met);
        }

        List<KnowledgeItem> old = newestFirst(character.oldItems());
        if (!old.isEmpty()) {
            VerticalLayout oldItems = new VerticalLayout();
            oldItems.setPadding(false);
            oldItems.setSpacing(false);
            old.forEach(item -> oldItems.add(createItem(character, item, true)));
            // collapsed by default; whether it is open is a convenience of this view
            Details details = new Details(T.localized(OLD_KNOWLEDGE).formatted(old.size()), oldItems);
            details.addClassName(KnowledgeTrackerStyles.OLD);
            details.setOpened(oldOpen.contains(character.getId()));
            details.addOpenedChangeListener(e -> {
                if (e.isOpened()) {
                    oldOpen.add(character.getId());
                } else {
                    oldOpen.remove(character.getId());
                }
            });
            body.add(details);
        }
        return body;
    }

    private Component createItem(TrackedCharacter character, KnowledgeItem item, boolean old) {
        boolean knows = item.getKind() == ItemKind.KNOWS;
        Span kind = new Span(knows ? T.localized(KNOWS) : T.localized(DOES_NOT_KNOW));
        kind.addClassName(KnowledgeTrackerStyles.KIND);
        if (!knows) {
            kind.addClassName(KnowledgeTrackerStyles.KIND_NOT);
        }

        Span words = new Span(item.getText());
        if (old) {
            words.addClassName(KnowledgeTrackerStyles.OLD_ITEM);
        }
        Div text = new Div(words);
        text.addClassName(KnowledgeTrackerStyles.ITEM_TEXT);
        Component source = createSourceLink(item);
        if (source != null) {
            text.add(source);
        }

        Div actions = new Div();
        actions.addClassName(KnowledgeTrackerStyles.ACTIONS);
        if (old) {
            actions.add(iconButton(Solid.COPY, T.localized(COPY), _ -> copy(item)));
        } else if (item.getOrigin() == ItemOrigin.LEARNED) {
            // the tidy pass never sees a locked item (the story can still contradict it); what the user wrote is never tidied anyway
            boolean locked = item.isLocked();
            Button lock = iconButton(locked ? Solid.LOCK : Solid.LOCK_OPEN,
                    locked ? T.localized(LOCKED_THE_TIDY_PASS_LEAVES_IT_ALONE) : T.localized(LOCK_IT_SO_THAT_THE_TIDY_PASS_LEAVES_IT_ALONE), _ -> {
                        item.setLocked(!item.isLocked());
                        persist();
                    });
            if (locked) {
                lock.addClassName(KnowledgeTrackerStyles.LOCKED);
            }
            actions.add(lock);
        }
        actions.add(iconButton(Solid.PEN, T.localized(EDIT_ITEM), _ -> openItemDialog(character, item)));
        if (old) {
            actions.add(iconButton(Solid.ROTATE_LEFT, T.localized(RESTORE) + ": " + T.localized(MAKE_IT_ACTIVE_AGAIN), _ -> {
                item.restore();
                persist();
            }));
        } else {
            actions.add(iconButton(Solid.BAN, T.localized(RETRACT) + ": " + T.localized(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD), _ -> {
                item.retractByHand();
                persist();
            }));
        }
        actions.add(iconButton(Solid.TRASH, T.localized(DELETE), _ -> ConfirmDialog.show(T.localized(DELETE_THIS_ITEM_FOR_GOOD), () -> {
            character.deleteItem(item.getId());
            persist();
        })));

        Div row = new Div(kind, text, actions);
        row.addClassName(KnowledgeTrackerStyles.ITEM);
        return row;
    }

    private static List<KnowledgeItem> newestFirst(List<KnowledgeItem> items) {
        List<KnowledgeItem> reversed = new ArrayList<>(items);
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * The small number of the part the item was learned in, which scrolls the story to that part. Nothing for an item that was written
     * by hand, and for one whose part is not on the active branch any more (another branch, or the part was deleted): the story can
     * only show the parts of the active branch.
     */
    private Component createSourceLink(KnowledgeItem item) {
        Integer position = item.getSourceMessage() == null ? null : positions.get(item.getSourceMessage());
        Long id = item.getSourceMessage() == null ? null : messageIds.get(item.getSourceMessage());
        if (position == null) {
            return null;
        }
        Span link = new Span("#" + position);
        link.addClassName(KnowledgeTrackerStyles.SOURCE);
        if (story != null && id != null) {
            link.addClassName(KnowledgeTrackerStyles.SOURCE_LINK);
            link.getElement().setAttribute("title", T.localized(SHOW_PART_IN_THE_STORY).formatted(position));
            link.getElement().setAttribute("role", "link");
            link.getElement().setAttribute("tabindex", "0");
            link.addClickListener(_ -> story.scrollToMessage(id));
            link.getElement().addEventListener("keydown", _ -> story.scrollToMessage(id)).setFilter("event.key === 'Enter'");
        }
        return link;
    }

    private void openItemDialog(TrackedCharacter character, KnowledgeItem existing) {
        new ItemDialog(existing, (text, kind) -> {
            if (existing == null) {
                character.getItems().add(KnowledgeItem.manual(text, kind));
            } else {
                existing.edit(text, kind);
            }
            persist();
        }).open();
    }

    private void copy(KnowledgeItem item) {
        getUI().ifPresent(ui -> JavascriptLibrary.copyToClipboard(ui.getPage(), item.getText()));
        Notification.success(T.localized(COPIED));
    }

    private static Button iconButton(Solid icon, String label, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(icon.create(), listener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        button.setAriaLabel(label);
        button.setTooltipText(label);
        return button;
    }

    /**
     * Stores the state after a change to the characters or items and shows it.
     */
    private void persist() {
        saveState();
        refresh();
    }

    private void saveState() {
        try {
            service.save(manuscript, state);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
