package com.github.enerccio.marginalia.extensions.arcoutline;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.IPCService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.arcoutline.commands.ArcGenerationCommands;
import com.github.enerccio.marginalia.extensions.arcoutline.commands.ArcOutlineCommands;
import com.github.enerccio.marginalia.extensions.arcoutline.model.ArcOutlineData;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.ui.ArcOutlineSettingsForm;
import com.github.enerccio.marginalia.extensions.arcoutline.ui.ArcOutlineStyles;
import com.github.enerccio.marginalia.extensions.arcoutline.ui.ArcOutlineView;
import com.github.enerccio.marginalia.ui.dialogs.story.NewTurnForm;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.popover.Popover;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization.T;

/**
 * Adds the Arcs tab to the left sidebar of the story view.
 */
@Configurable
public class ArcOutlineExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String USER_PART = "com.github.enerccio.marginalia.ui.workspace.parts.UserPart";
    private static final String VIEW_KEY = ArcOutlineData.KEY + ".view";
    private static final String SETTINGS_PANEL_KEY = ArcOutlineData.KEY + ".settingsPanel";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    /** The keys of the outline among the sources of data about a book other extensions can ask for. */
    private static final String OUTLINE_SOURCE = "outline";
    private static final String FULL_OUTLINE_SOURCE = "outline-with-notes";
    private static final String CURRENT_CHAPTER_SOURCE = "current-chapter";

    @Autowired
    private IPCService ipcService;

    @Autowired
    private CommandService commandService;

    private final ArcOutlineService service = new ArcOutlineService();

    /** Kept to unregister them, the service takes the objects. */
    private ArcOutlineCommands outlineCommands;
    private ArcGenerationCommands generationCommands;

    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;

    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        service.setLogger(log);
        service.setBundle(bundle);
        // the outline as a source of data about the book, for the Side Query for one: the terse version (the
        // descriptions) and the full one (with the notes), so the user chooses how much of it the model gets
        ipcService.registerManuscriptProvider(bundle.getSymbolicName(), OUTLINE_SOURCE, "Arc outline",
                (manuscript, caller, callerIdentifier) -> service.outlineText(manuscript, false));
        ipcService.registerManuscriptProvider(bundle.getSymbolicName(), FULL_OUTLINE_SOURCE, "Arc outline (with notes)",
                (manuscript, caller, callerIdentifier) -> service.outlineText(manuscript, true));
        // the chapter the story is at, to point a question or the drafting of the next instructions at
        ipcService.registerManuscriptProvider(bundle.getSymbolicName(), CURRENT_CHAPTER_SOURCE, "Arc outline: current chapter",
                (manuscript, caller, callerIdentifier) -> service.currentChapterText(manuscript));
        try {
            // the Arcs tab in the left sidebar of the story view (the drawer on a phone), next to the outline
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

                        // the story part loads other books into the same instance, the instructions go into the form of the part as it is then
                        ArcOutlineView.InstructionsTarget target = new ArcOutlineView.InstructionsTarget() {
                            @Override
                            public TurnInput turn() {
                                NewTurnForm form = context.getReflectiveFieldValue(instrumented, "newTurnForm", NewTurnForm.class);
                                return form == null ? null : form.toTurnInput();
                            }

                            @Override
                            public boolean put(String instructions) {
                                NewTurnForm form = context.getReflectiveFieldValue(instrumented, "newTurnForm", NewTurnForm.class);
                                Popover popover = context.getReflectiveFieldValue(instrumented, "newTurnPopover", Popover.class);
                                if (form == null || popover == null) {
                                    return false;
                                }
                                form.setInstructions(instructions);
                                popover.setOpened(true);
                                return true;
                            }
                        };
                        ArcOutlineView view = new ArcOutlineView(currentManuscript, service, target);
                        leftBar.add("arc-outline", T.localized("Arcs"), view);

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

            // the Arc Outline section in the extension settings of the user settings
            userPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                    if (extensionSettings == null) {
                        return;
                    }
                    AccordionPanel panel = (AccordionPanel) ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY);
                    if (panel != null) {
                        panel.getContent().findFirst().ifPresent(content -> {
                            if (content instanceof ArcOutlineSettingsForm form) {
                                form.refresh();
                            }
                        });
                    } else {
                        panel = extensionSettings.add(T.localized("Arc Outline"), new ArcOutlineSettingsForm(service));
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
                            if (content instanceof ArcOutlineSettingsForm form) {
                                service.saveSettings(form.save(), userSetting);
                            }
                        });
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {}
            };
            extensionService.registerDecorator(userPartSaveDecorator, USER_PART, "save");

            // the outline and its generators for the command bar and for agents
            outlineCommands = new ArcOutlineCommands(service);
            commandService.registerCommands(outlineCommands);
            generationCommands = new ArcGenerationCommands(service);
            commandService.registerCommands(generationCommands);
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Arc Outline extension", e);
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the Arcs tab lives in the workspace only
        return entryPoint instanceof Main ? ArcOutlineStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        ipcService.unregisterAll(b.getSymbolicName());
        if (outlineCommands != null) {
            commandService.unregisterCommands(outlineCommands);
            outlineCommands = null;
        }
        if (generationCommands != null) {
            commandService.unregisterCommands(generationCommands);
            generationCommands = null;
        }
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }

        // remove the tab from story views that are open
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    ArcOutlineView view = (ArcOutlineView) ComponentUtil.getData(leftBar, VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }

        // remove the section from settings that are open
        synchronized (activeAccordions) {
            for (Accordion accordion : activeAccordions) {
                // the components belong to the UIs of all users - change each one in its own session
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
    }
}
