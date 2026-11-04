package com.github.enerccio.marginalia.extensions.knowledgetracker;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
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
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.PayloadInsertion;
import com.github.enerccio.marginalia.extensions.knowledgetracker.commands.KnowledgeTrackerCommands;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgePromptService.Prepared;
import com.github.enerccio.marginalia.extensions.knowledgetracker.ui.KnowledgeTrackerStyles;
import com.github.enerccio.marginalia.extensions.knowledgetracker.ui.KnowledgeTrackerView;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.KNOWLEDGE;
import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.T;

/**
 * Keeps track of what each tracked character of the book knows (and does not know) and puts it into the prompt: by an
 * insertion into the payload, like the Author's Note, and/or by the {@code {{command::knowledge-tracker-block::...}}}
 * macro. See TODO.md for the plan.
 */
@Configurable
public class KnowledgeTrackerExtension implements MarginaliaExtension, ExtensionHasCss {

    /** The icon of the tab in the bar of the minimized tabs: a brain, two halves, on a 24 x 24 grid. */
    private static final TabDockIcon KNOWLEDGE_ICON = TabDockIcon.glyph(
            "<path d='M12 4.5C12 3.4 11.1 3 10 3 8.2 3 7 4.3 7 5.8 5.3 6.2 4 7.5 4 9.3c0 1 .4 1.8 1 2.4C4.4 12.3 4 13.1 4 14c0 1.6 1.1 2.8 2.6 3 .3 1.8 1.7 3 3.4 3 1.1 0 2-.6 2-1.8z'/>"
                    + "<path d='M12 4.5C12 3.4 12.9 3 14 3c1.8 0 3 1.3 3 2.8 1.7.4 3 1.7 3 3.5 0 1-.4 1.8-1 2.4.6.6 1 1.4 1 2.3 0 1.6-1.1 2.8-2.6 3-.3 1.8-1.7 3-3.4 3-1.1 0-2-.6-2-1.8z'/>"
                    + "<path d='M8 10.5c1 0 2 .6 2 1.8M16 10.5c-1 0-2 .6-2 1.8'/>");

    private static final String VIEW_KEY = KnowledgeState.KEY + ".view";

    /** What the generation prepared for the prompt, in the properties of the generation. */
    private static final String PREPARED_KEY = KnowledgeState.KEY + ".prepared";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private CommandService commandService;

    private final KnowledgeStateService stateService = new KnowledgeStateService();
    private final KnowledgePromptService promptService = new KnowledgePromptService(stateService);
    private final KnowledgeProcessService processService = new KnowledgeProcessService(stateService);
    private final ExtractionService extractionService = new ExtractionService(stateService, processService);
    private final ExtractionRuns runs = new ExtractionRuns(extractionService, processService, stateService);

    /** Kept to unregister it, the service takes the object. */
    private KnowledgeTrackerCommands commands;

    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator partFinishedDecorator;
    private Registration reserveRegistration;
    private Registration payloadRegistration;
    private Registration cleanupRegistration;

    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        try {
            // the Knowledge tab in the left sidebar of the story view (the drawer on a phone)
            storyPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    TabDock leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", TabDock.class);
                    Manuscript currentManuscript = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
                    if (leftBar != null && currentManuscript != null) {
                        if (ComponentUtil.getData(leftBar, VIEW_KEY) != null) {
                            return;
                        }
                        StoryEditor story = instrumented instanceof StoryEditor editor ? editor : null;
                        KnowledgeTrackerView view = new KnowledgeTrackerView(currentManuscript, story, stateService, processService, extractionService, runs);
                        leftBar.add("knowledge-tracker", T.localized(KNOWLEDGE), KNOWLEDGE_ICON, view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, view);
                        activeTabSheets.add(leftBar);
                    }
                }
            };
            extensionService.registerDecorator(storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart", "renderStoryContent");
            // the drawer of the story in the phone dialog has the same left bar
            extensionService.registerDecorator(storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart", "renderStoryContent");

            // a generated part is final once the automatic annotation and redraft (if the book has them) are done: read it then,
            // while the author reads it, instead of making the next generation wait
            partFinishedDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (throwing == null) {
                        readFinishedPart(context.getReflectiveFieldValue(instrumented, "manuscript", Manuscript.class));
                    }
                }
            };
            extensionService.registerDecorator(partFinishedDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.story.AutoAnnotate", "finished");

            commands = new KnowledgeTrackerCommands(stateService, processService, extractionService, runs);
            commandService.registerCommands(commands);

            // the context budget is computed in PREPARE_CONTENT - reserve room for the block before it
            reserveRegistration = storyGenerationService.addEventListener(Events.BEFORE_PREPARE_CONTENT, this::reserveBlock);
            // the payload is complete after PREPARE_PAYLOAD, what the listener sets is sent to the model
            payloadRegistration = storyGenerationService.addEventListener(Events.AFTER_PREPARE_PAYLOAD, this::insertBlock);
            // a regenerated part has a new text: what was learned from the old one is taken back
            cleanupRegistration = storyGenerationService.addEventListener(Events.AFTER_CLEANUP, this::dropRegenerated);
            log.info("Knowledge Tracker loaded");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Knowledge Tracker extension", e);
        }
    }

    /**
     * The new part of the story is final (the automatic annotation is over, or the book has none): when the book is set to read with
     * the model and apply at once, a run starts on a worker thread and reads it while the author reads it. Otherwise nothing happens
     * here; the next generation reads what is left, see {@link #processBeforePrompt}. A failure never reaches the story.
     */
    private void readFinishedPart(Manuscript manuscript) {
        try {
            if (manuscript == null) {
                return;
            }
            BookSettings settings = stateService.load(manuscript).getSettings();
            if (settings.isAutoProcess() && settings.isExtractWithModel() && !settings.isAskBeforeApplying()) {
                runs.start(manuscript, false, null);
            }
        } catch (IllegalStateException e) {
            log.debug("The story is being processed already: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to read the finished part: {}", e.getMessage(), e);
        }
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
            log.warn("Failed to prepare the knowledge block: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    /**
     * Just in time: a generation that starts proves the parts before it are final, so the ones not processed yet are read
     * now, before the block is built. A part that this generation replaces (regenerate, swipe) and the ones after it are
     * left alone. A failure here only means the block is built from the state as it is.
     */
    private void processBeforePrompt(Manuscript manuscript, GenerationRequest request) {
        try {
            BookSettings settings = stateService.load(manuscript).getSettings();
            if (!settings.isAutoProcess()) {
                return;
            }
            String replaced = request != null && request.getRequestType() != GenerationRequestType.NEW_MESSAGE && request.getNode() != null
                    ? request.getNode().getUuid() : null;
            if (settings.isExtractWithModel() && !settings.isAskBeforeApplying()) {
                // the user does not want to review: the model reads the new parts and the changes are applied and logged; the
                // generation waits for it
                ExtractionRun run = runs.start(manuscript, false, replaced);
                run.whenFinished().get();
            } else {
                // who has met whom costs nothing. What the model finds is proposed for review, and a review is never forced on
                // the user in the middle of a generation: it is started from the tab.
                processService.process(manuscript, true, replaced, KnowledgeProcessService.NO_LIMIT);
            }
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
            log.warn("Failed to insert the knowledge block: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the Knowledge tab lives in the workspace only
        return entryPoint instanceof Main ? KnowledgeTrackerStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        if (commands != null) {
            commandService.unregisterCommands(commands);
            commands = null;
        }
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
            storyPartDecorator = null;
        }
        if (partFinishedDecorator != null) {
            extensionService.unregisterDecorator(partFinishedDecorator);
            partFinishedDecorator = null;
        }
        if (reserveRegistration != null) {
            reserveRegistration.unregister();
            reserveRegistration = null;
        }
        if (payloadRegistration != null) {
            payloadRegistration.unregister();
            payloadRegistration = null;
        }
        if (cleanupRegistration != null) {
            cleanupRegistration.unregister();
            cleanupRegistration = null;
        }
        // remove the tab from story views that are open
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    KnowledgeTrackerView view = (KnowledgeTrackerView) ComponentUtil.getData(leftBar, VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }
        log.info("Knowledge Tracker unloaded");
    }
}
