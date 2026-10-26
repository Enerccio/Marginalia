package com.github.enerccio.marginalia.extensions.charactertracker;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.charactertracker.commands.CharacterTrackerCommands;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterTrackerData;
import com.github.enerccio.marginalia.extensions.charactertracker.service.CharacterTrackerService;
import com.github.enerccio.marginalia.extensions.charactertracker.ui.CharacterTrackerStyles;
import com.github.enerccio.marginalia.extensions.charactertracker.ui.CharacterTrackerView;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.charactertracker.CharacterTrackerLocalization.T;

/**
 * Adds the Characters tab to the left sidebar of the story view.
 */
@Configurable
public class CharacterTrackerExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String VIEW_KEY = CharacterTrackerData.KEY + ".view";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private CommandService commandService;

    @Autowired
    private StoryGenerationService storyGenerationService;

    private final CharacterTrackerService service = new CharacterTrackerService();

    /** Kept to unregister it, the service takes the object. */
    private CharacterTrackerCommands commands;

    private ExtensionDecorator storyPartDecorator;
    private Registration reserveRegistration;
    private Registration payloadRegistration;

    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        try {
            // the Characters tab in the left sidebar of the story view (the drawer on a phone), next to the outline
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

                        CharacterTrackerView view = new CharacterTrackerView(currentManuscript, service);
                        leftBar.add("character-tracker", T.localized("Characters"), view);

                        ComponentUtil.setData(leftBar, VIEW_KEY, view);
                        activeTabSheets.add(leftBar);
                    }
                }
            };

            extensionService.registerDecorator(
                    storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "renderStoryContent"
            );
            // the drawer of the story in the phone dialog has the same left bar
            extensionService.registerDecorator(
                    storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart",
                    "renderStoryContent"
            );

            // the characters for the command bar and for agents
            commands = new CharacterTrackerCommands(service);
            commandService.registerCommands(commands);

            // the context budget is computed in PREPARE_CONTENT - reserve room for the characters before it
            reserveRegistration = storyGenerationService.addEventListener(Events.BEFORE_PREPARE_CONTENT,
                    this::reserveCharacters);
            // the payload is complete after PREPARE_PAYLOAD, what the listener sets is sent to the model
            payloadRegistration = storyGenerationService.addEventListener(Events.AFTER_PREPARE_PAYLOAD,
                    this::insertCharacters);
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Character Tracker extension", e);
        }
    }

    private void reserveCharacters(GenerationControllerEvent event, EventChain chain) {
        try {
            // listeners are global - every generation of every user comes here, the characters are per book
            Manuscript manuscript = event.getManuscript();
            if (manuscript != null && event.getPrePromptData() != null) {
                CharacterTrackerService.PromptInsertion insertion = service.promptInsertion(service.load(manuscript));
                if (insertion != null) {
                    // the same text is inserted later, even if the user edits the characters during the generation
                    event.getProperties().put(CharacterTrackerData.KEY, insertion);
                    long tokens = service.countTokens(event.getAi(), insertion);
                    event.getPrePromptData().setReservedTokens(event.getPrePromptData().getReservedTokens() + tokens);
                }
            }
        } catch (Exception e) {
            // the characters are then not inserted either - nothing is in the properties
            event.getProperties().remove(CharacterTrackerData.KEY);
            log.warn("Failed to reserve tokens for the characters: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    private void insertCharacters(GenerationControllerEvent event, EventChain chain) {
        try {
            // only characters that were counted in the context budget are inserted
            CharacterTrackerService.PromptInsertion insertion = event.getProperty(CharacterTrackerData.KEY);
            if (insertion != null) {
                event.setPayload(service.insert(event.getPayload(), insertion));
            }
        } catch (Exception e) {
            // generate without the characters rather than fail the generation
            log.warn("Failed to insert the characters: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the Characters tab lives in the workspace only
        return entryPoint instanceof Main ? CharacterTrackerStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        if (commands != null) {
            commandService.unregisterCommands(commands);
            commands = null;
        }
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }
        if (reserveRegistration != null) {
            reserveRegistration.unregister();
            reserveRegistration = null;
        }
        if (payloadRegistration != null) {
            payloadRegistration.unregister();
            payloadRegistration = null;
        }

        // remove the tab from story views that are open
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    CharacterTrackerView view = (CharacterTrackerView) ComponentUtil.getData(leftBar, VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }
    }
}
