package com.github.enerccio.marginalia.extensions.charactertracker;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
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
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Adds the Characters tab to the left sidebar of the story view.
 */
@Configurable
public class CharacterTrackerExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String VIEW_KEY = CharacterTrackerData.KEY + ".view";

    private final CharacterTrackerService service = new CharacterTrackerService();

    private ExtensionDecorator storyPartDecorator;

    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
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
                        leftBar.add("character-tracker", "Characters", view);

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
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Character Tracker extension", e);
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the Characters tab lives in the workspace only
        return entryPoint instanceof Main ? CharacterTrackerStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
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
