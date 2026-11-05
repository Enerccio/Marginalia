package com.github.enerccio.marginalia.extensions.prompthistory;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.prompthistory.PromptHistoryLocalization.*;

/**
 * Keeps the last requests sent to the models, with their answers, in the session of each user and shows them in a
 * dialog opened from <i>Settings → Extension Settings</i>. The requests come from an {@link InferenceListener} installed
 * with {@link InferenceSession#addListener}; the history is in the plugin data of the {@link SessionPoint}, in memory only,
 * packed. The requests of an agent are kept per run (its session is made up again at every turn) and shown by the
 * <b>Prompt history</b> button of the console of the agent.
 */
@Configurable
public class PromptHistoryExtension implements MarginaliaExtension {

    private static final String SETTINGS_PANEL_KEY = "com.github.enerccio.marginalia.extensions.prompthistory.settingsPanel";

    private final PromptHistory histories = new PromptHistory();

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    /** The session scoped bean: a proxy that resolves to the session of the thread that calls it. */
    @Autowired
    private SessionPoint sessionPoint;

    private InferenceListener listener;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator consoleDecorator;
    private final Set<Button> consoleButtons = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);

        listener = this::onRequest;
        InferenceSession.addListener(listener);

        userPartDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                if (extensionSettings == null || ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY) != null) {
                    return;
                }
                AccordionPanel panel = extensionSettings.add(T.localized(PROMPT_HISTORY), createPanel());
                ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                activeAccordions.add(extensionSettings);
            }
        };
        try {
            extensionService.registerDecorator(userPartDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "refresh");
        } catch (Exception e) {
            log.error("Failed to add the settings panel: {}", e.getMessage(), e);
        }

        consoleDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                HorizontalLayout footer = context.getLocalVariable("footer", HorizontalLayout.class);
                if (footer == null) {
                    return;
                }
                Button history = new Button(T.localized(PROMPT_HISTORY), Solid.CLOCK_ROTATE_LEFT.create(), event -> {
                    // the run is the one the console shows at the moment of the click
                    AgentRun run = context.getReflectiveFieldValue(instrumented, "run", AgentRun.class);
                    if (run != null) {
                        new PromptHistoryDialog(histories.ofAgentRun(String.valueOf(run.getId()))).open();
                    }
                });
                if (Boolean.TRUE.equals(context.getReflectiveFieldValue(instrumented, "phone", boolean.class))) {
                    history.setText(null);
                    history.setAriaLabel(T.localized(PROMPT_HISTORY));
                }
                footer.addComponentAsFirst(history);
                consoleButtons.add(history);
            }
        };
        try {
            extensionService.registerDecorator(consoleDecorator, "com.github.enerccio.marginalia.ui.dialogs.agent.AgentConsoleDialog", "create");
        } catch (Exception e) {
            log.error("Failed to add the button to the console of the agent: {}", e.getMessage(), e);
        }
        log.info("Prompt History loaded");
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        if (listener != null) {
            InferenceSession.removeListener(listener);
            listener = null;
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
            userPartDecorator = null;
        }
        if (consoleDecorator != null) {
            extensionService.unregisterDecorator(consoleDecorator);
            consoleDecorator = null;
        }
        synchronized (consoleButtons) {
            for (Button button : consoleButtons) {
                UIUtils.accessComponent(button, () -> button.getParent().ifPresent(parent -> {
                    if (parent instanceof HorizontalLayout layout) {
                        layout.remove(button);
                    }
                }));
            }
            consoleButtons.clear();
        }
        // the accordions belong to the UIs of all users: remove the panel in each one's own session
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
        log.info("Prompt History unloaded");
    }

    /**
     * A request starts, on the thread that started it: the session of the user is found here and the history is kept
     * for the end of the request, which comes on another thread.
     */
    private InferenceListener.Observation onRequest(InferenceListener.Request request) {
        HistoryStore store;
        if (InferenceSession.AGENT.equals(request.kind()) && request.scope() != null) {
            // an agent works in a session of its own that does not outlive a turn of the run: kept by the run
            store = histories.ofAgentRun(request.scope());
        } else {
            try {
                store = PromptHistory.of(sessionPoint);
            } catch (Exception e) {
                // a thread that belongs to no session (a background job): there is nobody to show the history to
                log.debug("The request has no session: {}", e.getMessage());
                return null;
            }
        }
        // listed as soon as it starts; the answer is written into the entry as it arrives
        HistoryEntry entry = new HistoryEntry(request, store);
        store.add(entry);
        return new InferenceListener.Observation() {
            @Override
            public void onChunk(InferenceService.ChunkType type, String text) {
                entry.chunk(type, text);
            }

            @Override
            public void onFinish(InferenceListener.Response response) {
                entry.finish(response);
            }
        };
    }

    private VerticalLayout createPanel() {
        Paragraph description = new Paragraph(T.localized(PROMPT_HISTORY_DESCRIPTION).formatted(HistoryStore.LIMIT));
        // the history is looked up when the button is pressed, on the request thread of the user
        Button show = new Button(T.localized(SHOW_PROMPT_HISTORY), event -> {
            try {
                new PromptHistoryDialog(PromptHistory.of(sessionPoint)).open();
            } catch (Exception e) {
                log.warn("Failed to open the prompt history: {}", e.getMessage(), e);
            }
        });
        VerticalLayout layout = new VerticalLayout(description, show);
        layout.setPadding(false);
        return layout;
    }
}
