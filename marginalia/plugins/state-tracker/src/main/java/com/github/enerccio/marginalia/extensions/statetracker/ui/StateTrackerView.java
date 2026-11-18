package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateApplier;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateLog;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateProcessService;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateProcessService.Status;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateService;
import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters;
import com.github.enerccio.marginalia.extensions.storytracker.llm.LoreMode;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.process.ProcessedPart;
import com.github.enerccio.marginalia.extensions.storytracker.process.RunEstimate;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun.Phase;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRuns;
import com.github.enerccio.marginalia.extensions.storytracker.scene.SeparatorText;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.Checkpoint;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.TrackerCheckpoints;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ChangeLogPanel;
import com.github.enerccio.marginalia.extensions.storytracker.ui.CheckpointsPanel;
import com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerLocalization;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ImportCharactersDialog;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ProgressPanel;
import com.github.enerccio.marginalia.extensions.storytracker.ui.UiAccess;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * The State tab of the story sidebar: the tracked characters of the book with the current values of their categories and the
 * history of each, the categories of the book, and the settings of the block.
 */
@Configurable
public class StateTrackerView extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final StateService service;
    private final StateProcessService processService;
    private final StateExtractionService extractionService;
    private final TrackerRuns<Proposal> runs;
    private final Manuscript manuscript;
    private StateBook state;
    /** The positions of the parts of the active branch by uuid (the first part is 1), as the last refresh read them. */
    private final Map<String, Integer> positions = new HashMap<>();
    private boolean processedOpen;
    private boolean logOpen;
    private boolean checkpointsOpen;
    /** The progress panel of the run that is shown, and the run this view follows. */
    private ProgressPanel<Proposal> progressPanel;
    private TrackerRun<Proposal> watched;
    /** At most one reload of the tab waits at a time, however fast the run reports. */
    private final Runnable runRefresh = UiAccess.coalesced(this, () -> {
        state = loadState();
        refresh();
    });
    /** The tab is rebuilt when the run waits for a review or ends, not at every phase. */
    private final Consumer<TrackerRun.Status<Proposal>> runListener = status -> {
        if (status.phase().isFinished() || status.phase() == Phase.REVIEW) {
            runRefresh.run();
        }
    };

    private final VerticalLayout list = new VerticalLayout();
    private Button settingsButton;
    /** The bar at the top and the list's scroller: the bar shows {@link #dock} while the buttons of the Processing panel are scrolled away. */
    private HorizontalLayout toolbar;
    private Scroller scroller;
    /** Icon-only copies of Process now, Process next part and Tidy the state, in the top bar. */
    private final Div dock = new Div();
    private final Button dockProcess = iconButton(Solid.PLAY, T.localized(PROCESS_NOW), () -> process(0));
    private final Button dockNext = iconButton(Solid.FORWARD_STEP, T.localized(PROCESS_NEXT_PART), () -> process(1));
    private final Button dockTidy = iconButton(Solid.BROOM, T.localized(TIDY_THE_STATE), this::tidy);
    /** The row of the processing buttons the last refresh made, {@code null} when it has none. */
    private Div processButtons;
    /** Ids of the characters whose card is shown open (collapsed by default); kept over a refresh. */
    private final Set<String> open = new HashSet<>();
    /** The categories (character id + category id) whose history is shown open; kept over a refresh. */
    private final Set<String> historyOpen = new HashSet<>();
    /** A command stored the state: one reload however many come in a row. */
    private final Runnable changeRefresh = UiAccess.coalesced(this, () -> {
        state = loadState();
        showSettingsState();
        refresh();
    });
    /** Removes the listener that reads the state again when a command changes it, set while the view is attached. */
    private Runnable changeRegistration;

    public StateTrackerView(Manuscript manuscript, StateService service, StateProcessService processService,
                            StateExtractionService extractionService, TrackerRuns<Proposal> runs) {
        this.manuscript = manuscript;
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

    /** Whether the view shows {@code book}. */
    public boolean isFor(Manuscript book) {
        return book != null && manuscript.getUuid().equals(book.getUuid());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // a command (the command bar, an agent) stores the state behind the back of the view
        changeRegistration = service.onChange(manuscript, changeRefresh);
        watchProcessButtons();
        TrackerRun<Proposal> running = runs.running(manuscript);
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

    private StateBook loadState() {
        try {
            return service.load(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return service.read(manuscript);
        }
    }

    private void buildUI() {
        Button add = new Button(T.localized(TRACK_A_CHARACTER), Solid.PLUS.create(), _ -> openCharacterDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button importCharacters = iconButton(Solid.USER_PLUS, T.localized(IMPORT_FROM_THE_CHARACTER_TRACKER), this::openImportCharacters);

        Button categories = iconButton(Solid.TAGS, T.localized(CATEGORIES_OF_THIS_BOOK), () -> openCategories());

        Button reload = iconButton(Solid.ARROWS_ROTATE, T.localized(REFRESH), changeRefresh);

        settingsButton = new Button(Solid.GEAR.create());
        settingsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        settingsButton.setAriaLabel(T.localized(SETTINGS));
        settingsButton.setTooltipText(T.localized(SETTINGS));
        Popover popover = new Popover();
        popover.setTarget(settingsButton);
        popover.setModal(false);
        popover.add(createSettings());
        showSettingsState();

        dock.add(dockProcess, dockNext, dockTidy);
        dock.addClassName(StateTrackerStyles.DOCK);
        toolbar = new HorizontalLayout(add, importCharacters, categories, reload, settingsButton, dock);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setPadding(true);
        toolbar.setSpacing(false);
        toolbar.setWidthFull();
        toolbar.addClassName(StateTrackerStyles.TOOLBAR);

        list.setPadding(false);
        list.setSpacing(false);
        list.addClassName(StateTrackerStyles.LIST);
        scroller = new Scroller(list);
        scroller.setSizeFull();

        add(toolbar, scroller);
        setFlexGrow(1, scroller);
    }

    /**
     * Docks the processing buttons into the top bar while their row in the Processing panel is scrolled out of sight above the
     * list: an observer in the browser sets the attribute {@code docked} on the bar and the CSS shows the icon buttons then, so
     * scrolling sends nothing to the server. Called after every refresh (the panel is made again) and when the view is attached.
     */
    private void watchProcessButtons() {
        if (toolbar == null) {
            return;
        }
        if (processButtons == null) {
            toolbar.getElement().executeJs("""
                    if (this.__stDock) { this.__stDock.disconnect(); this.__stDock = null; }
                    this.removeAttribute('docked');""");
            return;
        }
        toolbar.getElement().executeJs("""
                const bar = this, row = $0, root = $1;
                if (bar.__stDock) { bar.__stDock.disconnect(); }
                bar.removeAttribute('docked');
                if (!row || typeof IntersectionObserver === 'undefined') { bar.__stDock = null; return; }
                bar.__stDock = new IntersectionObserver(entries => {
                    for (const e of entries) {
                        const top = e.rootBounds ? e.rootBounds.top : 0;
                        bar.toggleAttribute('docked', !e.isIntersecting && e.boundingClientRect.bottom <= top + 1);
                    }
                }, {root: root, threshold: 0});
                bar.__stDock.observe(row);""", processButtons.getElement(), scroller.getElement());
    }

    // --- settings ---

    /** The button is green while the state is put into the prompt. */
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
        inPrompt.setTooltipText(T.localized(INSERT_THE_STATE_INTO_THE_PROMPT));
        inPrompt.addValueChangeListener(e -> {
            state.getSettings().setInPrompt(e.getValue());
            showSettingsState();
            saveState();
        });

        IntegerField depth = new IntegerField(T.localized(DEPTH));
        depth.setHelperText(T.localized(MESSAGES_AFTER_THE_STATE));
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

        TextArea separators = new TextArea(T.localized(NAME_SEPARATORS));
        separators.setWidthFull();
        separators.setHelperText(T.localized(HOW_THE_PRESENT_CHARACTERS_FIELD_IS_SPLIT));
        separators.setValue(SeparatorText.format(settings.getSeparators()));
        separators.setValueChangeMode(ValueChangeMode.ON_BLUR);
        separators.addValueChangeListener(e -> {
            state.getSettings().setSeparators(SeparatorText.parse(e.getValue()));
            saveState();
        });

        VerticalLayout controls = new VerticalLayout(inPrompt, depth, role, separators, createModelSettings(settings), createClockSettings());
        controls.addClassName(StateTrackerStyles.SETTINGS);
        controls.setPadding(false);
        return controls;
    }

    /** The story clock: on or off, the event day 1 is the day of, and the time at the start point. */
    private Component createClockSettings() {
        StoryClock clock = state.getClock();
        Checkbox enabled = new Checkbox(T.localized(STORY_CLOCK), clock.isEnabled());
        enabled.setTooltipText(T.localized(FOLLOW_THE_TIME_OF_THE_STORY));
        TextField anchor = new TextField(T.localized(DAY_ONE_IS_THE_DAY_OF));
        anchor.setValue(clock.getAnchor());
        anchor.setHelperText(T.localized(AN_EVENT_FOR_EXAMPLE_THE_AWAKENING));
        anchor.setValueChangeMode(ValueChangeMode.ON_BLUR);
        anchor.setWidthFull();
        IntegerField day = new IntegerField(T.localized(DAY_AT_THE_START_POINT));
        day.setMin(1);
        day.setStepButtonsVisible(true);
        day.setValue(clock.getStartDay());
        day.setWidthFull();
        Select<TimeOfDay> timeOfDay = new Select<>();
        timeOfDay.setLabel(T.localized(TIME_OF_DAY));
        timeOfDay.setItems(TimeOfDay.values());
        timeOfDay.setEmptySelectionAllowed(true);
        timeOfDay.setItemLabelGenerator(t -> t == null ? "" : ClockText.of(t));
        timeOfDay.setValue(clock.getStartTimeOfDay());
        timeOfDay.setWidthFull();
        DatePicker date = new DatePicker(T.localized(DATE_AT_THE_START_POINT));
        date.setValue(StoryClock.parse(clock.getStartDate()));
        date.setClearButtonVisible(true);
        date.setHelperText(T.localized(OPTIONAL_A_CALENDAR_DATE));
        date.setWidthFull();
        Runnable shown = () -> {
            boolean on = enabled.getValue();
            anchor.setEnabled(on);
            day.setEnabled(on);
            timeOfDay.setEnabled(on);
            date.setEnabled(on);
        };
        shown.run();
        enabled.addValueChangeListener(e -> {
            state.getClock().setEnabled(e.getValue());
            shown.run();
            persist();
        });
        anchor.addValueChangeListener(e -> {
            state.getClock().setAnchor(e.getValue().strip());
            persist();
        });
        day.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                state.getClock().setStartDay(e.getValue());
                persist();
            }
        });
        timeOfDay.addValueChangeListener(e -> {
            state.getClock().setStartTimeOfDay(e.getValue());
            persist();
        });
        date.addValueChangeListener(e -> {
            state.getClock().setStartDate(e.getValue() == null ? null : e.getValue().toString());
            persist();
        });
        VerticalLayout box = new VerticalLayout(enabled, anchor, day, timeOfDay, date);
        box.setPadding(false);
        box.setSpacing(false);
        return box;
    }

    /** How the model reads the story: by itself or not, reviewed or not, how many parts at once, the lorebook, which model. */
    private Component createModelSettings(BookSettings settings) {
        Checkbox auto = new Checkbox(T.localized(PROCESS_NEW_PARTS_AUTOMATICALLY), settings.isAutoProcess());
        auto.setTooltipText(T.localized(WHEN_A_PART_IS_FINAL_THE_MODEL_READS_IT));
        auto.addValueChangeListener(e -> {
            state.getSettings().setAutoProcess(e.getValue());
            saveState();
        });
        Checkbox ask = new Checkbox(T.localized(ASK_BEFORE_APPLYING), settings.isAskBeforeApplying());
        ask.setTooltipText(T.localized(SHOW_THE_CHANGES_FOR_REVIEW_FIRST));
        ask.addValueChangeListener(e -> {
            state.getSettings().setAskBeforeApplying(e.getValue());
            saveState();
        });
        IntegerField parts = new IntegerField(T.localized(PARTS_PER_RUN));
        parts.setMin(1);
        parts.setStepButtonsVisible(true);
        parts.setValue(settings.getPartsPerRun());
        parts.setWidthFull();
        parts.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                state.getSettings().setPartsPerRun(e.getValue());
                saveState();
            }
        });
        Select<LoreMode> lore = new Select<>();
        lore.setLabel(T.localized(LOREBOOK_FOR_THE_MODEL));
        lore.setItems(LoreMode.RESOLVE, LoreMode.WHOLE, LoreMode.NONE);
        lore.setItemLabelGenerator(m -> T.localized(m == LoreMode.RESOLVE ? ENTRIES_THE_PARTS_ACTIVATE
                : m == LoreMode.WHOLE ? THE_WHOLE_LOREBOOK : NO_LOREBOOK));
        lore.setValue(settings.getLoreMode());
        lore.setWidthFull();
        lore.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                state.getSettings().setLoreMode(e.getValue());
                saveState();
            }
        });
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
        model.addValueChangeListener(e -> {
            state.getSettings().setAiUuid(e.getValue() == null ? null : e.getValue().getUuid());
            saveState();
        });
        protocol.addValueChangeListener(e -> {
            state.getSettings().setProtocolUuid(e.getValue() == null ? null : e.getValue().getUuid());
            saveState();
        });
        Checkbox autoTidy = new Checkbox(T.localized(TIDY_AUTOMATICALLY), settings.isAutoTidy());
        autoTidy.setTooltipText(T.localized(AT_THE_END_OF_A_RUN_THAT_APPLIES_ITS_CHANGES));
        IntegerField tidyEvery = new IntegerField(T.localized(TIDY_AFTER_N_PARTS));
        tidyEvery.setMin(1);
        tidyEvery.setStepButtonsVisible(true);
        tidyEvery.setValue(settings.getTidyEveryParts());
        tidyEvery.setHelperText(T.localized(THE_TIDY_READS_THIS_MANY_OF_THE_LAST_PARTS));
        tidyEvery.setWidthFull();
        autoTidy.addValueChangeListener(e -> {
            state.getSettings().setAutoTidy(e.getValue());
            saveState();
        });
        tidyEvery.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                state.getSettings().setTidyEveryParts(e.getValue());
                saveState();
            }
        });
        VerticalLayout box = new VerticalLayout(auto, ask, parts, autoTidy, tidyEvery, lore, model, protocol);
        box.setPadding(false);
        box.setSpacing(false);
        return box;
    }

    // --- processing ---

    /** Where the processing stands, the buttons that process, the start point, the run and the processed parts. */
    private Component createProcessingPanel() {
        Div panel = new Div();
        panel.addClassNames(StateTrackerStyles.CARD, StateTrackerStyles.PANEL);
        Div title = new Div(new Span(T.localized(PROCESSING)));
        title.addClassName(StateTrackerStyles.NAME);
        panel.add(title);

        List<Part> branch;
        Status status;
        positions.clear();
        try {
            branch = processService.branch(manuscript);
            status = processService.status(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return panel;
        }
        for (int i = 0; i < branch.size(); i++) {
            positions.put(branch.get(i).uuid(), i + 1);
        }
        String line = branch.isEmpty() ? T.localized(THE_BOOK_HAS_NO_PARTS_YET)
                : status.cursor() > 0 ? T.localized(PROCESSED_UP_TO_PART).formatted(status.cursor(), status.parts())
                : T.localized(NOTHING_IS_PROCESSED_YET).formatted(status.parts());
        Div info = new Div(new Span(line));
        info.addClassName(StateTrackerStyles.MUTED);
        panel.add(info);
        if (state.getClock().isEnabled()) {
            panel.add(createClockLine(branch));
        }
        if (branch.isEmpty()) {
            dockProcess.setEnabled(false);
            dockNext.setEnabled(false);
            dockTidy.setEnabled(false);
            return panel;
        }

        TrackerRun<Proposal> running = runs.running(manuscript);
        Button process = new Button(T.localized(PROCESS_NOW), Solid.PLAY.create(), _ -> process(0));
        process.addThemeVariants(ButtonVariant.LUMO_SMALL);
        process.setTooltipText(T.localized(ASK_THE_MODEL_WHAT_CHANGED));
        process.setEnabled(status.unread() > 0 && running == null);
        Button next = new Button(T.localized(PROCESS_NEXT_PART), Solid.FORWARD_STEP.create(), _ -> process(1));
        next.addThemeVariants(ButtonVariant.LUMO_SMALL);
        next.setTooltipText(T.localized(READ_ONLY_THE_NEXT_PART_THAT_IS_NOT_PROCESSED));
        next.setEnabled(status.unread() > 0 && running == null);
        Button tidy = new Button(T.localized(TIDY_THE_STATE), Solid.BROOM.create(), _ -> tidy());
        tidy.addThemeVariants(ButtonVariant.LUMO_SMALL);
        tidy.setTooltipText(T.localized(ASK_THE_MODEL_WHICH_VALUES_FADED));
        tidy.setEnabled(status.cursor() > 0 && running == null);
        processButtons = new Div(process, next, tidy);
        panel.add(processButtons);
        dockProcess.setEnabled(process.isEnabled());
        dockNext.setEnabled(next.isEnabled());
        dockTidy.setEnabled(tidy.isEnabled());
        if (running != null) {
            panel.add(createRunButton(running));
        }
        TrackerRun<Proposal> last = runs.latest(manuscript);
        if (last != null && last.getStatus().phase().isFinished() && !last.getThoughts().isEmpty()) {
            // the reasoning of the last run, also when it proposed nothing and no review ever opened
            Button lastRun = new Button(T.localized(LAST_RUN_WHAT_THE_MODEL_THOUGHT), Solid.LIGHTBULB.create(), _ -> showProgress(last));
            lastRun.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            panel.add(lastRun);
        }
        Proposal held = runs.peekHeld(manuscript);
        if (held != null) {
            Button review = new Button(T.localized(CHANGES_TO_REVIEW).formatted(held.changeCount()));
            review.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            review.addClickListener(_ -> StateRunScreens.review(1, held, () -> applyHeld(held), () -> discardHeld(held)).open());
            panel.add(review);
        }
        panel.add(createStartSelect(branch));
        Component processed = createProcessedParts(branch, status);
        if (processed != null) {
            panel.add(processed);
        }
        Component log = ChangeLogPanel.create(state.getChangeLog(), this::describe, entry -> StateLog.canRevert(state, entry),
                this::revertChange, logOpen, opened -> logOpen = opened);
        if (log != null) {
            panel.add(log);
        }
        Component checkpoints = createCheckpoints(branch, running == null);
        if (checkpoints != null) {
            panel.add(checkpoints);
        }
        return panel;
    }

    /** The checkpoints the runs took whose part is on the active branch, each with a button that returns the tracker to it. */
    private Component createCheckpoints(List<Part> branch, boolean enabled) {
        try {
            return CheckpointsPanel.create(processService.checkpoints(manuscript, branch), false, enabled, this::restoreCheckpoint,
                    checkpointsOpen, opened -> checkpointsOpen = opened);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }

    private void restoreCheckpoint(Checkpoint checkpoint) {
        try {
            if (runs.running(manuscript) != null) {
                return;
            }
            TrackerCheckpoints.Restore outcome = processService.restoreCheckpoint(manuscript, checkpoint.id());
            switch (outcome) {
                case RESTORED -> Notification.success(CheckpointsPanel.restoredMessage(checkpoint, positions.getOrDefault(checkpoint.partUuid(), 0)));
                case NOT_ON_BRANCH -> Notification.warning(StoryTrackerLocalization.T.localized(
                        StoryTrackerLocalization.THE_PART_OF_THE_CHECKPOINT_IS_NOT_ON_THE_ACTIVE_BRANCH));
                case GONE -> Notification.warning(StoryTrackerLocalization.T.localized(StoryTrackerLocalization.THE_CHECKPOINT_CAN_NOT_BE_READ));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        showSettingsState();
        refresh();
    }

    /** A line of the log: "Fiora, Carrying (part 12): + lantern", "Fiora, Mood (tidied): angry → cheerful". */
    private String describe(StateLogEntry entry) {
        Integer position = entry.getMessageUuid() == null ? null : positions.get(entry.getMessageUuid());
        String where = entry.isTidy() ? " (" + T.localized(TIDIED) + ")"
                : position == null ? "" : " (" + T.localized(PART_N).formatted(position) + ")";
        String what = switch (entry.getAction()) {
            case ADD -> "+ " + entry.getText();
            case CLEAR, REMOVE -> "− " + entry.getEndedText();
            case SET, REPLACE -> entry.getEndedText() == null ? entry.getText() : entry.getEndedText() + " → " + entry.getText();
        };
        return entry.getCharacterName() + ", " + entry.getCategoryName() + where + ": " + what;
    }

    private void revertChange(StateLogEntry entry) {
        try {
            if (processService.revertChange(manuscript, entry.getId()) != TrackerChangeLog.Outcome.REVERTED) {
                Notification.warning(T.localized(THE_VALUE_HAS_BEEN_CHANGED_SINCE));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    /** "Story time: Day 14 after the awakening, evening", with a button that sets it after the last processed part. */
    private Component createClockLine(List<Part> branch) {
        StoryClock clock = state.getClock();
        Span text = new Span(T.localized(STORY_TIME_S).formatted(ClockText.of(clock.now(state), clock.getAnchor())));
        text.addClassName(StateTrackerStyles.VALUE_TEXT);
        Div row = new Div(text);
        row.addClassName(StateTrackerStyles.VALUE);
        List<ProcessedPart> processed = state.getProcessed();
        if (!processed.isEmpty()) {
            String last = processed.getLast().getUuid();
            Button set = iconButton(Solid.CLOCK, T.localized(SET_THE_STORY_TIME), () -> openClockDialog(last));
            row.add(set);
        }
        return row;
    }

    /** Sets (or removes) the fixed point of the story time after a processed part. */
    private void openClockDialog(String part) {
        StoryClock clock = state.getClock();
        ClockMark mark = clock.getMarks().get(part);
        Integer position = positions.get(part);
        new ClockDialog(part, position == null ? 0 : position, clock.upTo(state, part), mark != null && mark.isManual(), fixed -> {
            if (fixed == null && (mark == null || !mark.isManual())) {
                return;
            }
            clock.fix(part, fixed);
            persist();
        }).open();
    }

    private Button createRunButton(TrackerRun<Proposal> run) {
        TrackerRun.Status<Proposal> status = run.getStatus();
        boolean review = status.phase() == Phase.REVIEW && status.proposal() != null;
        Button button = new Button(review ? T.localized(CHANGES_TO_REVIEW).formatted(status.proposal().changeCount())
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

    /** An entry of the start point selector: a part, or {@code null} for the start of the book. */
    private record StartOption(String uuid, String label) {
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
                try {
                    processService.setStart(manuscript, e.getValue().uuid());
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
                state = loadState();
                refresh();
            }
        });
        return start;
    }

    /** The latest processed parts, each with a button to reprocess from it, and a note on the ones whose text changed since. */
    private Component createProcessedParts(List<Part> branch, Status status) {
        Set<String> processed = new HashSet<>();
        state.getProcessed().forEach(p -> processed.add(p.getUuid()));
        Set<String> changed = new HashSet<>(status.changed());
        Map<String, StoryTime> times = state.getClock().isEnabled() ? state.getClock().timeline(state) : Map.of();
        List<Integer> done = new ArrayList<>();
        for (int i = 0; i < branch.size(); i++) {
            if (processed.contains(branch.get(i).uuid())) {
                done.add(i + 1);
            }
        }
        if (done.isEmpty()) {
            return null;
        }
        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        for (int n = done.size() - 1; n >= 0 && done.size() - n <= 30; n--) {
            int position = done.get(n);
            Part part = branch.get(position - 1);
            Span text = new Span(partLabel(position, part));
            text.addClassName(StateTrackerStyles.VALUE_TEXT);
            Div row = new Div(text);
            row.addClassName(StateTrackerStyles.VALUE);
            if (times.containsKey(part.uuid())) {
                // the story time after the part, and whether the user fixed it there
                ClockMark mark = state.getClock().getMarks().get(part.uuid());
                Span time = new Span(ClockText.of(times.get(part.uuid()), state.getClock().getAnchor())
                        + (mark != null && mark.isManual() ? " · " + T.localized(SET_BY_HAND) : "")
                        + (mark != null && mark.getKind() == ClockMark.Kind.FLASHBACK ? " · " + T.localized(FLASHBACK) : ""));
                time.addClassName(StateTrackerStyles.SOURCE);
                if (mark != null && mark.getEvidence() != null) {
                    time.setTitle(mark.getEvidence());
                }
                text.add(time);
                row.add(iconButton(Solid.CLOCK, T.localized(SET_THE_STORY_TIME), () -> openClockDialog(part.uuid())));
            }
            if (changed.contains(part.uuid())) {
                Span stale = new Span(T.localized(CHANGED_SINCE_PROCESSED));
                stale.addClassName(StateTrackerStyles.BADGE);
                row.add(stale);
            }
            row.add(iconButton(Solid.ARROWS_ROTATE, T.localized(REPROCESS_FROM_HERE), () ->
                    ConfirmDialog.show(T.localized(REPROCESS_FROM_PART_N).formatted(position), () -> reprocessFrom(part))));
            rows.add(row);
        }
        Details details = new Details(T.localized(PROCESSED_PARTS).formatted(done.size()), rows);
        details.setOpened(processedOpen);
        details.addOpenedChangeListener(e -> processedOpen = e.isOpened());
        return details;
    }

    /** @param maxParts how many parts at most, 0 = all that are waiting */
    private void process(int maxParts) {
        try {
            RunEstimate estimate = maxParts == 0 && runs.running(manuscript) == null ? processService.estimate(manuscript) : null;
            if (estimate != null && estimate.parts() == 0) {
                Notification.success(T.localized(NOTHING_NEW_TO_PROCESS));
                return;
            }
            if (estimate != null && estimate.isLong()) {
                // a long series is many requests to the model: the user is told before it starts
                ConfirmDialog.show(T.localized(READ_N_PARTS_WITH_THE_MODEL).formatted(estimate.parts(), estimate.runs(), estimate.storyTokens()),
                        () -> startRun(0));
                return;
            }
            startRun(maxParts);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /** Starts a run with the model (or shows the one that is going on) and its progress panel. */
    private void startRun(int maxParts) {
        try {
            TrackerRun<Proposal> run = runs.running(manuscript);
            if (run == null) {
                run = runs.start(manuscript, null, null, maxParts);
            }
            watch(run);
            showProgress(run);
        } catch (IllegalStateException e) {
            Notification.warning(T.localized(THE_STORY_IS_BEING_PROCESSED_ALREADY));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    /** <b>Tidy the state</b>: the model drops, updates or merges the values that faded; reviewed or applied like any run. */
    private void tidy() {
        try {
            TrackerRun<Proposal> run = runs.running(manuscript);
            if (run == null) {
                run = runs.startTidy(manuscript, null);
            }
            watch(run);
            showProgress(run);
        } catch (IllegalStateException e) {
            Notification.warning(T.localized(THE_STORY_IS_BEING_PROCESSED_ALREADY));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    /** Takes back what came from the part and the later ones, then a run reads them again. */
    private void reprocessFrom(Part part) {
        try {
            if (processService.dropFrom(manuscript, part.uuid()) >= 0) {
                state = loadState();
                startRun(0);
                return;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    private void applyHeld(Proposal held) {
        try {
            if (runs.takeHeld(manuscript) == held) {
                if (held.tidy()) {
                    StateApplier.Result result = extractionService.commitTidy(manuscript, held, false);
                    Notification.success(T.localized(TIDY_DONE).formatted(result.applied()));
                } else {
                    extractionService.commit(manuscript, held, false);
                }
            }
        } catch (com.github.enerccio.marginalia.extensions.statetracker.service.StaleProposalException e) {
            Notification.warning(T.localized(THE_STORY_CHANGED_WHILE_YOU_REVIEWED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        state = loadState();
        refresh();
    }

    private void discardHeld(Proposal held) {
        if (runs.peekHeld(manuscript) == held) {
            runs.takeHeld(manuscript);
        }
        refresh();
    }

    /** Keeps this view in line with the run: its changes of the state and its phases show up in the tab. */
    private void watch(TrackerRun<Proposal> run) {
        if (watched == run) {
            return;
        }
        if (watched != null) {
            watched.removeListener(runListener);
        }
        watched = run;
        run.addListener(runListener);
    }

    /** Shows the run (the panel of the runs was clicked): its progress, and the review when its proposal waits. */
    public void showRun(TrackerRun<Proposal> run) {
        watch(run);
        showProgress(run);
        progressPanel.openReview();
    }

    private void showProgress(TrackerRun<Proposal> run) {
        if (progressPanel != null) {
            progressPanel.close();
        }
        progressPanel = StateRunScreens.panel(run);
        progressPanel.open();
    }

    /** "Part 12: the first words of the part", one line. */
    private static String partLabel(int position, Part part) {
        String text = part.text() == null ? "" : part.text().replaceAll("\\s+", " ").strip();
        String preview = text.length() > 50 ? text.substring(0, 50) + "…" : text;
        return T.localized(PART_N).formatted(position) + (preview.isEmpty() ? "" : ": " + preview);
    }

    // --- the characters ---

    private void refresh() {
        list.removeAll();
        processButtons = null;
        list.add(createProcessingPanel());
        watchProcessButtons();
        if (state.getCharacters().isEmpty()) {
            Div empty = new Div(new Span(T.localized(NO_CHARACTER_IS_TRACKED_YET)));
            empty.addClassName(StateTrackerStyles.MUTED);
            list.add(empty);
            return;
        }
        for (TrackedCharacter character : state.getCharacters()) {
            list.add(createCard(character));
        }
    }

    private Component createCard(TrackedCharacter character) {
        boolean isOpen = open.contains(character.getId());
        Button toggle = new Button((isOpen ? Solid.CHEVRON_UP : Solid.CHEVRON_DOWN).create(), _ -> {
            if (!open.remove(character.getId())) {
                open.add(character.getId());
            }
            refresh();
        });
        toggle.setAriaLabel(T.localized(SHOW_THE_STATE_OF_THE_CHARACTER));
        toggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);

        Div name = new Div(new Span(character.getName()));
        name.addClassName(StateTrackerStyles.NAME);
        Div title = new Div(name);
        title.addClassName(StateTrackerStyles.CARD_TITLE);
        if (!character.getAliases().isEmpty()) {
            Div aliases = new Div(new Span(String.join(", ", character.getAliases())));
            aliases.addClassName(StateTrackerStyles.MUTED);
            title.add(aliases);
        }
        Div header = new Div(toggle, title,
                iconButton(Solid.PEN, T.localized(EDIT), () -> openCharacterDialog(character)),
                iconButton(Solid.TRASH, T.localized(DELETE), () -> deleteCharacter(character)));
        header.addClassName(StateTrackerStyles.CARD_HEADER);

        Div card = new Div(header);
        card.addClassName(StateTrackerStyles.CARD);
        if (!isOpen) {
            return card;
        }
        List<Category> categories = state.categoriesOf(character);
        if (categories.isEmpty()) {
            Div none = new Div(new Span(T.localized(NO_CATEGORY_IS_TRACKED_FOR_THIS_CHARACTER)));
            none.addClassName(StateTrackerStyles.MUTED);
            card.add(none);
        }
        for (Category category : categories) {
            card.add(createCategory(character, category));
        }
        return card;
    }

    private Component createCategory(TrackedCharacter character, Category category) {
        String addLabel = category.getKind().isSingleValued() ? T.localized(SET_THE_VALUE) : T.localized(ADD_AN_ENTRY);
        Div header = new Div(new Span(category.getName()),
                iconButton(Solid.PLUS, addLabel, () -> openItemDialog(character, category, null)));
        header.addClassName(StateTrackerStyles.CATEGORY_HEADER);

        Div box = new Div(header);
        box.addClassName(StateTrackerStyles.CATEGORY);
        List<StateItem> current = character.active(category.getId());
        if (current.isEmpty()) {
            Div none = new Div(new Span(T.localized(NOTHING_YET)));
            none.addClassName(StateTrackerStyles.MUTED);
            box.add(none);
        }
        for (StateItem item : current) {
            box.add(createValue(character, category, item));
        }

        List<StateItem> history = character.history(category.getId());
        if (!history.isEmpty()) {
            VerticalLayout past = new VerticalLayout();
            past.setPadding(false);
            past.setSpacing(false);
            past.addClassName(StateTrackerStyles.HISTORY);
            history.forEach(item -> past.add(createPastValue(character, category, item)));
            Details details = new Details(T.localized(HISTORY_N).formatted(history.size()), past);
            String key = character.getId() + "/" + category.getId();
            details.setOpened(historyOpen.contains(key));
            details.addOpenedChangeListener(e -> {
                if (e.isOpened()) {
                    historyOpen.add(key);
                } else {
                    historyOpen.remove(key);
                }
            });
            box.add(details);
        }
        return box;
    }

    private Component createValue(TrackedCharacter character, Category category, StateItem item) {
        Span text = new Span(item.getText());
        Integer position = item.getSourceMessage() == null ? null : positions.get(item.getSourceMessage());
        if (position != null) {
            // where the model read it (or the tidy wrote it, after that part): a reprocess from that part takes it back
            Span source = new Span(T.localized(item.isFromTidy() ? BY_THE_TIDY_PART_N : PART_N).formatted(position));
            source.addClassName(StateTrackerStyles.SOURCE);
            source.setTitle(T.localized(item.isFromTidy() ? WRITTEN_BY_THE_TIDY_AFTER_PART_N : READ_FROM_THE_STORY_IN_PART_N).formatted(position));
            text.add(source);
        }
        if (item.getStoryDay() != null && state.getClock().isEnabled()) {
            Span day = new Span("· " + T.localized(SET_ON_DAY_N).formatted(item.getStoryDay()));
            day.addClassName(StateTrackerStyles.SOURCE);
            day.setTitle(T.localized(SET_ON_DAY_N_OF_THE_STORY).formatted(item.getStoryDay()));
            text.add(day);
        }
        text.addClassName(StateTrackerStyles.VALUE_TEXT);
        Button lock = iconButton(item.isLocked() ? Solid.LOCK : Solid.LOCK_OPEN, T.localized(item.isLocked() ? UNLOCK : LOCK), () -> {
            item.setLocked(!item.isLocked());
            persist();
        });
        if (!item.isLocked()) {
            lock.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        }
        Button edit = iconButton(Solid.PEN, T.localized(EDIT), () -> openItemDialog(character, category, item));
        Button remove = iconButton(Solid.XMARK, T.localized(REMOVE), () -> {
            character.remove(item, System.currentTimeMillis());
            persist();
        });
        remove.setTooltipText(T.localized(NOT_TRUE_ANY_MORE_KEEP_IT_IN_THE_HISTORY));
        Div row = new Div(text, lock, edit, remove);
        row.addClassName(StateTrackerStyles.VALUE);
        return row;
    }

    private Component createPastValue(TrackedCharacter character, Category category, StateItem item) {
        boolean tidied = item.getEndReason() == StateItem.EndReason.TIDIED;
        Span why = new Span(tidied ? T.localized(TIDIED)
                : item.getEndReason() == StateItem.EndReason.REPLACED ? T.localized(REPLACED) : T.localized(REMOVED));
        why.addClassName(StateTrackerStyles.MUTED);
        Integer endedIn = item.getEndedBy() == null ? null : positions.get(item.getEndedBy());
        if (endedIn != null) {
            // the part whose reading (or the tidy after it) ended it: a reprocess from that part brings it back
            Span part = new Span(T.localized(PART_N).formatted(endedIn));
            part.addClassName(StateTrackerStyles.SOURCE);
            part.setTitle(T.localized(tidied ? TIDIED_AFTER_PART_N : ENDED_BY_THE_STORY_IN_PART_N).formatted(endedIn));
            why.add(part);
        }
        Span text = new Span(new Span(item.getText()), why);
        text.addClassName(StateTrackerStyles.VALUE_TEXT);
        Button restore = iconButton(Solid.ROTATE_LEFT, T.localized(RESTORE), () -> {
            character.restore(item, category, System.currentTimeMillis());
            persist();
        });
        restore.setTooltipText(T.localized(MAKE_IT_CURRENT_AGAIN));
        Button delete = iconButton(Solid.TRASH, T.localized(DELETE), () -> ConfirmDialog.show(T.localized(DELETE_THIS_VALUE_FOR_GOOD), () -> {
            character.delete(item);
            persist();
        }));
        Div row = new Div(text, restore, delete);
        row.addClassName(StateTrackerStyles.VALUE);
        return row;
    }

    // --- dialogs ---

    private void openCharacterDialog(TrackedCharacter existing) {
        List<Category> user = userCategories();
        new TrackedCharacterDialog(state, user, existing, (name, aliases, picked) -> {
            TrackedCharacter character = existing != null ? existing : new TrackedCharacter(name);
            character.setName(name);
            character.setAliases(aliases);
            List<String> ids = new ArrayList<>();
            for (Category category : picked) {
                // a user category the book has no copy of yet is copied into the book now
                Category own = state.findCategory(category.getId()) != null ? category : state.adopt(category);
                ids.add(own.getId());
            }
            // the categories the character kept stay in their order, the new ones follow
            List<String> ordered = new ArrayList<>(character.getCategories().stream().filter(ids::contains).toList());
            ids.stream().filter(id -> !ordered.contains(id)).forEach(ordered::add);
            character.setCategories(ordered);
            if (existing == null) {
                state.getCharacters().add(character);
            }
            persist();
        }).open();
    }

    /**
     * <b>Import from the Character Tracker</b>: the characters of that plugin the book does not track yet, and the categories every
     * one of them tracks (the book's, and those of the user that the book has no copy of yet).
     */
    private void openImportCharacters() {
        try {
            List<CharacterTrackerCharacters.Candidate> all = service.characterTrackerCharacters(manuscript);
            List<Category> offered = new ArrayList<>(state.getCategories());
            for (Category user : userCategories()) {
                if (state.copyOf(user.getId()) == null) {
                    offered.add(user);
                }
            }
            CheckboxGroup<Category> categories = new CheckboxGroup<>(T.localized(TRACKED_CATEGORIES));
            categories.setItems(offered);
            categories.setItemLabelGenerator(c -> state.findCategory(c.getId()) != null ? c.getName()
                    : T.localized(FROM_MY_CATEGORIES).formatted(c.getName()));
            categories.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
            categories.setHelperText(T.localized(EVERY_IMPORTED_CHARACTER_TRACKS_THE_CHECKED_CATEGORIES));
            ImportCharactersDialog dialog = new ImportCharactersDialog(all, CharacterTrackerImport.untracked(state, all), categories,
                    () -> !categories.getValue().isEmpty(), chosen -> {
                Set<Category> picked = categories.getValue();
                int added = CharacterTrackerImport.importInto(state, chosen, offered.stream().filter(picked::contains).toList());
                persist();
                Notification.success(T.localized(N_CHARACTERS_NOW_TRACKED).formatted(added));
            });
            categories.addValueChangeListener(_ -> dialog.updateImportButton());
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void deleteCharacter(TrackedCharacter character) {
        ConfirmDialog.show(T.localized(STOP_TRACKING_AND_DELETE_ITS_STATE).formatted(character.getName()), () -> {
            state.deleteCharacter(character.getId());
            open.remove(character.getId());
            persist();
        });
    }

    private void openItemDialog(TrackedCharacter character, Category category, StateItem existing) {
        new ItemDialog(state.categoriesOf(character), category, existing, c -> !character.active(c.getId()).isEmpty(),
                (chosen, text, locked) -> {
                    StateItem item = existing;
                    if (item == null) {
                        item = character.set(chosen, text, System.currentTimeMillis());
                        item.setStoryDay(state.today());
                    } else {
                        item.setText(text);
                        // a value the user corrected is theirs: a reprocess does not take it back
                        item.setSourceMessage(null);
                    }
                    item.setLocked(locked);
                    persist();
                }).open();
    }

    private void openCategories() {
        new BookCategoriesDialog(new BookCategoriesDialog.Host() {
            @Override
            public StateBook state() {
                return state;
            }

            @Override
            public List<Category> userCategories() throws Exception {
                return service.getUserCategories();
            }

            @Override
            public void persist() {
                StateTrackerView.this.persist();
            }

            @Override
            public void saveToMyCategories(Category category) throws Exception {
                service.addUserCategory(category);
            }

            @Override
            public void failed(Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }).open();
    }

    private List<Category> userCategories() {
        try {
            return service.getUserCategories();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return List.of();
        }
    }

    // --- storing ---

    /** Stores the state after a change and shows it. */
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

    private static Button iconButton(Solid icon, String label, Runnable action) {
        Button button = new Button(icon.create(), _ -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        button.setAriaLabel(label);
        button.setTooltipText(label);
        return button;
    }
}
