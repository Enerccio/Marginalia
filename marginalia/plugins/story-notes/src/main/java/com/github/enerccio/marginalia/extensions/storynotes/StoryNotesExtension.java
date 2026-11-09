package com.github.enerccio.marginalia.extensions.storynotes;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.IPCService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.storynotes.commands.StoryNotesCommands;
import com.github.enerccio.marginalia.extensions.storynotes.model.StoryNotesData;
import com.github.enerccio.marginalia.extensions.storynotes.service.StoryNotesService;
import com.github.enerccio.marginalia.extensions.storynotes.ui.StoryNotesStyles;
import com.github.enerccio.marginalia.extensions.storynotes.ui.StoryNotesView;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.storynotes.StoryNotesLocalization.NOTES;
import static com.github.enerccio.marginalia.extensions.storynotes.StoryNotesLocalization.T;

/**
 * Adds the Notes tab to the left sidebar of the story view: a small wiki of the author's pages about plots, ideas and rules
 * of the world. The pages are for the author, and for the assistants they allow to see: the commands of the plugin
 * ({@code list-notes}, {@code get-note}...) are what agents and the plot wizard of the Arc Outline read and write them with,
 * and two sources of {@link IPCService} offer them as text.
 */
@Configurable
public class StoryNotesExtension implements MarginaliaExtension, ExtensionHasCss {

    /** The key of the source with the titles of the notes, see {@link IPCService}; other extensions remember it, don't rename it. */
    public static final String INDEX_SOURCE = "notes-index";
    /** The key of the source with the text of all the notes the assistants may see. */
    public static final String FULL_SOURCE = "notes";

    /** The icon of the tab in the bar of the minimized tabs: a page with a folded corner and lines, on a 24 x 24 grid. */
    private static final TabDockIcon NOTES_ICON = TabDockIcon.glyph(
            "<path d='M6 3h9l4 4v14H6z'/><path d='M15 3v4h4M9 12h7M9 16h7'/>");

    private static final String VIEW_KEY = StoryNotesData.KEY + ".view";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private CommandService commandService;

    @Autowired
    private IPCService ipcService;

    private final StoryNotesService service = new StoryNotesService();

    /** Kept to unregister it, the service takes the object. */
    private StoryNotesCommands commands;

    private ExtensionDecorator storyPartDecorator;

    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        try {
            // the notes as text for whoever asks (the Side Query for one); the pages hidden from the assistants are not in it
            ipcService.registerManuscriptProvider(bundle.getSymbolicName(), INDEX_SOURCE, "Story notes (titles)",
                    (manuscript, caller, callerIdentifier) -> StringUtils.trimToNull(StoryNotesService.indexText(service.load(manuscript))));
            ipcService.registerManuscriptProvider(bundle.getSymbolicName(), FULL_SOURCE, "Story notes (all)",
                    (manuscript, caller, callerIdentifier) -> StringUtils.trimToNull(StoryNotesService.fullText(service.load(manuscript))));

            // the Notes tab in the left sidebar of the story view (the drawer on a phone), next to the outline
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
                        StoryNotesView view = new StoryNotesView(currentManuscript, service);
                        leftBar.add("story-notes", T.localized(NOTES), NOTES_ICON, view);

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

            // the notes for the command bar, for macros, for agents and for the plot wizard
            commands = new StoryNotesCommands(service);
            commandService.registerCommands(commands);
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Story Notes extension", e);
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the Notes tab lives in the workspace only
        return entryPoint instanceof Main ? StoryNotesStyles.css() : null;
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
        // the sources are removed by the application with the bundle

        // remove the tab from story views that are open
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    StoryNotesView view = (StoryNotesView) ComponentUtil.getData(leftBar, VIEW_KEY);
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
