package com.github.enerccio.marginalia.extensions.statetracker;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.GenerationOutcome;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.statetracker.commands.StateTrackerCommands;
import com.github.enerccio.marginalia.extensions.statetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.service.*;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.statetracker.service.StatePromptService.Prepared;
import com.github.enerccio.marginalia.extensions.statetracker.ui.StateRunScreens;
import com.github.enerccio.marginalia.extensions.statetracker.ui.StateSettingsForm;
import com.github.enerccio.marginalia.extensions.statetracker.ui.StateTrackerStyles;
import com.github.enerccio.marginalia.extensions.statetracker.ui.StateTrackerView;
import com.github.enerccio.marginalia.extensions.storytracker.block.PayloadInsertion;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRuns;
import com.github.enerccio.marginalia.extensions.storytracker.ui.ProgressPanel;
import com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerStyles;
import com.github.enerccio.marginalia.ui.dialogs.BookWindows;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.STATE;
import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.T;

/**
 * Keeps the current state of the tracked characters of a book (clothes, inventory, the weapon at hand...) in categories the user
 * defines, and puts the state of the characters of the scene into the prompt: by an insertion into the payload, like the Author's
 * Note, and/or by the {@code {{command::state-tracker-block::...}}} macro. See TODO.md for the plan.
 */
@Configurable
public class StateTrackerExtension implements MarginaliaExtension, ExtensionHasCss {

    /** The icon of the tab in the bar of the minimized tabs: a shirt, on a 24 x 24 grid. */
    private static final TabDockIcon STATE_ICON = TabDockIcon.glyph(
            "<path d='M9 3 4 6l2 4 2-1v12h8V9l2 1 2-4-5-3c-.4 1.4-1.6 2.3-3 2.3S9.4 4.4 9 3z'/>");

    private static final String VIEW_KEY = StateBook.KEY + ".view";
    private static final String TAB_ID = "state-tracker";
    private static final String USER_PART = "com.github.enerccio.marginalia.ui.workspace.parts.UserPart";
    private static final String SETTINGS_PANEL_KEY = StateBook.KEY + ".settingsPanel";

    /** What the generation prepared for the prompt, in the properties of the generation. */
    private static final String PREPARED_KEY = StateBook.KEY + ".prepared";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private InheritedSettingRegistry inheritedSettingRegistry;

    private final StateService stateService = new StateService();
    private final StatePromptService promptService = new StatePromptService(stateService);
    private final StateProcessService processService = new StateProcessService(stateService);
    private final StateExtractionService extractionService = new StateExtractionService(stateService, processService);
    private final TrackerRuns<Proposal> runs = StateSteps.runs(extractionService, processService, stateService);

    /** Kept to unregister it, the service takes the object. */
    private StateTrackerCommands commands;

    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private Registration reserveRegistration;
    private Registration payloadRegistration;
    private Registration afterPartRegistration;
    private Registration cleanupRegistration;

    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        try {
            // what books and folders inherit (their dialogs get the fields); the user level is in the State Tracker section of the settings
            StateInherited.register(inheritedSettingRegistry, bundle.getSymbolicName(), stateService::getUserPrompt);

            // the State Tracker section (the user's categories and prompts) in the extension settings of the user settings
            userPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                    if (extensionSettings == null) {
                        return;
                    }
                    AccordionPanel panel = (AccordionPanel) ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY);
                    if (panel != null) {
                        panel.getContent().findFirst().ifPresent(content -> {
                            if (content instanceof StateSettingsForm form) {
                                form.refresh();
                            }
                        });
                    } else {
                        panel = extensionSettings.add(T.localized(StateTrackerLocalization.STATE_TRACKER), new StateSettingsForm(stateService));
                        ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                        activeAccordions.add(extensionSettings);
                    }
                }
            };
            extensionService.registerDecorator(userPartDecorator, USER_PART, "refresh");

            userPartSaveDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                    UserSetting userSetting = context.getReflectiveFieldValue(instrumented, "userSetting", UserSetting.class);
                    Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                    if (extensionSettings == null || userSetting == null) {
                        return;
                    }
                    AccordionPanel panel = (AccordionPanel) ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY);
                    if (panel != null) {
                        panel.getContent().findFirst().ifPresent(content -> {
                            if (content instanceof StateSettingsForm form) {
                                form.save(userSetting);
                            }
                        });
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                }
            };
            extensionService.registerDecorator(userPartSaveDecorator, USER_PART, "save");

            // the State tab in the left sidebar of the story view (the drawer on a phone)
            storyPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    TabDock leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", TabDock.class);
                    Manuscript currentManuscript = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
                    if (leftBar != null && currentManuscript != null) {
                        if (ComponentUtil.getData(leftBar, VIEW_KEY) != null) {
                            return;
                        }
                        StateTrackerView view = new StateTrackerView(currentManuscript, stateService, processService, extractionService, runs);
                        leftBar.add(TAB_ID, T.localized(STATE), STATE_ICON, view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, view);
                        activeTabSheets.add(leftBar);
                    }
                }
            };
            extensionService.registerDecorator(storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart", "renderStoryContent");
            extensionService.registerDecorator(storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart", "renderStoryContent");

            // a click on a run in the panel of the runs shows it in the page of the click
            runs.setOpener(this::openRun);

            // after a generated part, once its text is final (annotation, redraft, translations): read it then, while the author reads
            // it, instead of making the next generation wait
            afterPartRegistration = storyGenerationService.addAfterPartBinding(new AfterPart.Binding(StateBook.KEY + ".afterPart",
                    () -> T.localized(STATE), AfterPart.Stage.EXTRACT, AfterPart.Mode.PARALLEL,
                    new AfterPart.Handler() {
                        @Override
                        public boolean appliesTo(Manuscript book) throws Exception {
                            // the open tabs of the book show how many parts wait: a new part is one more
                            stateService.changed(book);
                            BookSettings settings = stateService.load(book).getSettings();
                            return settings.isAutoProcess() && !settings.isAskBeforeApplying() && !stateService.load(book).getCharacters().isEmpty();
                        }

                        @Override
                        public void run(AfterPart.Context context) throws Exception {
                            readFinishedPart(context);
                        }
                    }));

            commands = new StateTrackerCommands(stateService, processService, extractionService, runs);
            commandService.registerCommands(commands);

            // the context budget is computed in PREPARE_CONTENT - reserve room for the block before it
            reserveRegistration = storyGenerationService.addEventListener(Events.BEFORE_PREPARE_CONTENT, this::reserveBlock);
            // the payload is complete after PREPARE_PAYLOAD, what the listener sets is sent to the model
            payloadRegistration = storyGenerationService.addEventListener(Events.AFTER_PREPARE_PAYLOAD, this::insertBlock);
            // a regenerated part has a new text: what was read from the old one is taken back
            cleanupRegistration = storyGenerationService.addEventListener(Events.AFTER_CLEANUP, this::dropRegenerated);
            log.info("State Tracker loaded");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the State Tracker extension", e);
        }
    }

    /**
     * The step after a generated part (the book reads with the model and applies at once): a run reads what waits, on its own thread;
     * the step waits for it. A book that is being read already reads the part with that run.
     */
    private void readFinishedPart(AfterPart.Context context) throws Exception {
        TrackerRun<Proposal> run;
        try {
            run = runs.start(context.getManuscript(), false, null);
        } catch (IllegalStateException e) {
            log.debug("The story is being processed already: {}", e.getMessage());
            context.skip();
            return;
        }
        context.onStop(run::cancel);
        context.await(run.whenFinished());
        TrackerRun.Status<Proposal> status = run.getStatus();
        if (status.phase() == TrackerRun.Phase.FAILED) {
            throw new IllegalStateException(status.message());
        }
        if (status.phase() == TrackerRun.Phase.CANCELLED) {
            throw new AfterPart.Stopped();
        }
    }

    /**
     * Just in time: a generation that starts proves the parts before it are final, so the ones not read yet are read now (when the
     * book applies without asking), before the block is built. A part that this generation replaces and the ones after it are left
     * alone. A review is never forced on the user in the middle of a generation: a guided book only keeps the state in line with the
     * branch here and is read from the tab. A failure only means the block is built from the state as it is.
     */
    private void processBeforePrompt(Manuscript manuscript, GenerationRequest request) {
        try {
            StateBook state = stateService.load(manuscript);
            if (state.getCharacters().isEmpty()) {
                return;
            }
            // a swipe or another branch takes back what came from the parts that are not on it any more
            processService.align(manuscript);
            BookSettings settings = state.getSettings();
            if (!settings.isAutoProcess() || settings.isAskBeforeApplying()) {
                return;
            }
            String replaced = request != null && request.getRequestType() != GenerationRequestType.NEW_MESSAGE && request.getNode() != null
                    ? request.getNode().getUuid() : null;
            TrackerRun<Proposal> run = runs.start(manuscript, false, replaced);
            run.whenFinished().get();
        } catch (IllegalStateException e) {
            // a run of the tab is going on: the generation does not start another
            log.debug("The story is being processed already: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to process the story before the prompt: {}", e.getMessage(), e);
        }
    }

    private void dropRegenerated(GenerationControllerEvent event, EventChain chain) {
        try {
            GenerationRequest request = event.getRequest();
            if (request != null && request.getRequestType() == GenerationRequestType.REGENERATE && request.getNode() != null
                    && event.getManuscript() != null && producedNewText(event)) {
                processService.dropFrom(event.getManuscript(), request.getNode().getUuid());
            }
        } catch (Exception e) {
            log.warn("Failed to take back the state of a regenerated part: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    /** A regenerate that was stopped or failed before any text came leaves the old text: nothing is taken back then. */
    private static boolean producedNewText(GenerationControllerEvent event) {
        if (event.getProperty(GenerationProperties.OUTCOME) == GenerationOutcome.COMPLETED) {
            return true;
        }
        ChatMessage original = event.getProperty(GenerationProperties.ORIGINAL_MESSAGE);
        ChatMessage now = event.getMessage();
        String text = now == null ? null : now.getResponse();
        return text != null && !text.isBlank() && (original == null || !text.equals(original.getResponse()));
    }

    /**
     * Shows a run in the page of the click on its entry in the panel of the runs: in the State tab of its book (selected, with the
     * progress and the review that waits). A book that is not open in the page is opened first.
     */
    private void openRun(TrackerRun<Proposal> run) {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        if (showInTab(ui, run)) {
            return;
        }
        BookWindows.openHere(run.getManuscript(), _ -> {
            if (!showInTab(ui, run)) {
                ProgressPanel<Proposal> panel = StateRunScreens.panel(run);
                panel.open();
                panel.openReview();
            }
        });
    }

    /** Selects the State tab of the run's book in the page and shows the run there; false when the page has no such tab. */
    private boolean showInTab(UI ui, TrackerRun<Proposal> run) {
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                StateTrackerView view = (StateTrackerView) ComponentUtil.getData(leftBar, VIEW_KEY);
                if (view != null && leftBar.getUI().orElse(null) == ui && view.isFor(run.getManuscript())) {
                    leftBar.select(TAB_ID);
                    view.showRun(run);
                    return true;
                }
            }
        }
        return false;
    }

    private void reserveBlock(GenerationControllerEvent event, EventChain chain) {
        try {
            // listeners are global - every generation of every user comes here, the state is per book
            Manuscript manuscript = event.getManuscript();
            if (manuscript != null) {
                processBeforePrompt(manuscript, event.getRequest());
            }
            if (manuscript != null && event.getPrePromptData() != null && event.getInput() != null) {
                Prepared prepared = promptService.prepare(manuscript, event.getInput().presentCharacters());
                if (!prepared.block().isBlank()) {
                    // the same block is inserted later, even if the user edits the state during the generation
                    event.getProperties().put(PREPARED_KEY, prepared);
                    long tokens = promptService.countTokens(event.getAi(), prepared.block());
                    event.getPrePromptData().setReservedTokens(event.getPrePromptData().getReservedTokens() + tokens);
                }
            }
        } catch (Exception e) {
            // the block is then not inserted either - nothing is in the properties
            log.warn("Failed to prepare the state block: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    private void insertBlock(GenerationControllerEvent event, EventChain chain) {
        try {
            // only a block that was counted in the context budget is inserted
            Prepared prepared = event.getProperty(PREPARED_KEY);
            if (prepared != null) {
                event.setPayload(PayloadInsertion.insert(event.getPayload(), prepared.block(),
                        prepared.state().getSettings().getDepth(), prepared.state().getSettings().getRole()));
            }
        } catch (Exception e) {
            // generate without the block rather than fail the generation
            log.warn("Failed to insert the state block: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the State tab and the settings live in the workspace only
        return entryPoint instanceof Main ? StateTrackerStyles.css() + "\n" + StoryTrackerStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        runs.setOpener(null);
        inheritedSettingRegistry.unregisterAll(b.getSymbolicName());
        if (afterPartRegistration != null) {
            afterPartRegistration.unregister();
            afterPartRegistration = null;
        }
        if (cleanupRegistration != null) {
            cleanupRegistration.unregister();
            cleanupRegistration = null;
        }
        if (commands != null) {
            commandService.unregisterCommands(commands);
            commands = null;
        }
        for (ExtensionDecorator decorator : new ExtensionDecorator[]{storyPartDecorator, userPartDecorator, userPartSaveDecorator}) {
            if (decorator != null) {
                extensionService.unregisterDecorator(decorator);
            }
        }
        storyPartDecorator = null;
        userPartDecorator = null;
        userPartSaveDecorator = null;
        synchronized (activeAccordions) {
            for (Accordion accordion : activeAccordions) {
                UIUtils.accessComponent(accordion, () -> {
                    AccordionPanel panel = (AccordionPanel) ComponentUtil.getData(accordion, SETTINGS_PANEL_KEY);
                    if (panel != null) {
                        accordion.remove(panel);
                        ComponentUtil.setData(accordion, SETTINGS_PANEL_KEY, null);
                    }
                });
            }
            activeAccordions.clear();
        }
        if (reserveRegistration != null) {
            reserveRegistration.unregister();
            reserveRegistration = null;
        }
        if (payloadRegistration != null) {
            payloadRegistration.unregister();
            payloadRegistration = null;
        }
        // remove the tab from story views that are open; the components belong to the UIs of all users
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                UIUtils.accessComponent(leftBar, () -> {
                    StateTrackerView view = (StateTrackerView) ComponentUtil.getData(leftBar, VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }
        log.info("State Tracker unloaded");
    }
}
