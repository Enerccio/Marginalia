package com.github.enerccio.marginalia.extensions.stconverter;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.stconverter.model.ConverterData;
import com.github.enerccio.marginalia.extensions.stconverter.model.TurnItem;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService;
import com.github.enerccio.marginalia.extensions.stconverter.ui.ConverterView;
import com.github.enerccio.marginalia.extensions.stconverter.ui.STConverterStyles;
import com.github.enerccio.marginalia.ui.dialogs.story.NewTurnForm;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.stconverter.STConverterLocalization.T;

/**
 * Adds the <i>Converter</i> tab to the sidebar of the story editor (the drawer of the story in the phone layout): the source of the book (a SillyTavern chat,
 * or the parts of another book) and the turn details made of it. The tab id, the attribute and the names of the module are those
 * of the plugin when it only converted SillyTavern chats, so what is stored with the books and the layouts still fits.
 */
@Configurable
public class STConverterExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String VIEW_KEY = ConverterData.KEY + ".view";
    private static final String TAB_ID = "stconverter";

    private final ConverterService converterService = new ConverterService();

    private ExtensionDecorator storyPartDecorator;

    private final Set<TabDock> activeTabDocks = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        STConverterLog.install(parentService.createLogger(bundle));
        try {
            storyPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (throwing != null) {
                        return;
                    }
                    TabDock leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", TabDock.class);
                    Manuscript currentManuscript = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
                    if (leftBar == null || currentManuscript == null || ComponentUtil.getData(leftBar, VIEW_KEY) != null) {
                        return;
                    }
                    // the story part loads other books into the same instance, the turn is put into the form of the part as it is then
                    boolean phone = isPhone(instrumented);
                    ConverterView.TurnOpener opener = turn -> {
                        NewTurnForm form = context.getReflectiveFieldValue(instrumented, "newTurnForm", NewTurnForm.class);
                        Popover popover = context.getReflectiveFieldValue(instrumented, "newTurnPopover", Popover.class);
                        if (form == null || popover == null) {
                            return false;
                        }
                        form.restoreState(toFormState(turn));
                        if (phone) {
                            // the drawer covers the story and the button the form opens from
                            AppLayout layout = leftBar.findAncestor(AppLayout.class);
                            if (layout != null) {
                                layout.setDrawerOpened(false);
                            }
                        }
                        popover.setOpened(true);
                        return true;
                    };
                    ConverterView view = new ConverterView(converterService, currentManuscript, opener, phone);
                    leftBar.add(TAB_ID, T.localized("Converter"), "Cv", view);
                    ComponentUtil.setData(leftBar, VIEW_KEY, view);
                    activeTabDocks.add(leftBar);
                }
            };

            extensionService.registerDecorator(
                    storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "renderStoryContent"
            );
            // the drawer of the story in the phone dialog has the same left bar, new turn form and popover
            extensionService.registerDecorator(
                    storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart",
                    "renderStoryContent"
            );
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Converter extension", e);
        }
    }

    /**
     * @param storyPart the instrumented story part: the desktop one or its phone twin
     */
    static boolean isPhone(Object storyPart) {
        return storyPart.getClass().getName().contains(".phone.");
    }

    /**
     * The state {@link NewTurnForm#restoreState} takes: the four fields of the turn details, marked as typed by the user so
     * that opening the form does not replace them with the ones of the last part.
     */
    static JsonObject toFormState(TurnItem turn) {
        JsonObject state = new JsonObject();
        state.addProperty("scene", StringUtils.defaultString(turn.getSceneSetting()));
        state.addProperty("pov", StringUtils.defaultString(turn.getPovCharacter()));
        state.addProperty("present", StringUtils.defaultString(turn.getPresentCharacters()));
        state.addProperty("instructions", StringUtils.defaultString(turn.getInstructions()));
        state.addProperty("edited", true);
        return state;
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the tab lives in the workspace only
        return entryPoint instanceof Main ? STConverterStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        STConverterLog.reset();
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }

        // remove the tab from the story editors that are open
        synchronized (activeTabDocks) {
            for (TabDock leftBar : activeTabDocks) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    ConverterView view = (ConverterView) ComponentUtil.getData(leftBar, VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, null);
                    }
                });
            }
            activeTabDocks.clear();
        }
    }
}
