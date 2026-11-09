package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersInherited;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService;
import com.github.enerccio.marginalia.extensions.betareaders.ui.BetaReadersSettingsForm;
import com.github.enerccio.marginalia.extensions.betareaders.ui.BetaReadersStyles;
import com.github.enerccio.marginalia.extensions.betareaders.ui.BetaReadersView;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.BETA_READERS;
import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * Beta Readers: personas the user defines and a chat about the book with them. What exists so far: the settings panel in
 * <i>Settings → Extension Settings</i> (the prompts, the medium, the AI and the protocol, the libraries of personas and mediums), the
 * same settings as inherited settings of books and folders, and the tab of the chats in the story sidebar. See {@code TODO.md} for the rest.
 */
@Configurable
public class BetaReadersExtension implements MarginaliaExtension, ExtensionHasCss {

    /** The icon of the tab in the bar of the minimized tabs: a glyph on a 24 x 24 grid. */
    private static final TabDockIcon BETA_READERS_ICON = TabDockIcon.glyph(
            "<circle cx='9' cy='8' r='3.5'/><path d='M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6'/><circle cx='17.5' cy='9' r='2.5'/><path d='M17 14c2.9.2 4.8 2.2 4.8 5'/>");

    private static final String SETTINGS_PANEL_KEY = BetaReadersSettings.KEY + ".settingsPanel";
    private static final String VIEW_KEY = BetaReadersSettings.KEY + ".view";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private InheritedSettingRegistry inheritedSettingRegistry;

    @Autowired
    private NotificationService notificationService;

    /** The sources of the notifications that the beta readers answered, or a persona was written, see {@code NotificationService}. */
    public static final String NOTIFY_CHAT = BetaReadersSettings.KEY + ".chat";
    public static final String NOTIFY_PERSONA = BetaReadersSettings.KEY + ".persona";

    private final BetaReadersService service = new BetaReadersService();

    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;

    private final Set<TabDock> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        notificationService.register(bundle.getSymbolicName(), new NotificationService.Source(NOTIFY_CHAT, () -> T.localized(BetaReadersLocalization.BETA_READERS_ANSWER_IN_A_CHAT)));
        notificationService.register(bundle.getSymbolicName(), new NotificationService.Source(NOTIFY_PERSONA, () -> T.localized(BetaReadersLocalization.BETA_READERS_WRITE_A_PERSONA)));
        try {
            // what books and folders inherit: their dialogs get the fields, the user level is the panel below
            BetaReadersInherited.register(inheritedSettingRegistry, bundle.getSymbolicName(), service::settingsOrDefaults,
                    service::aiChoices, service::protocolChoices);

            // the tab with the chats in the left bar of the story, in the workspace and in the drawer of the phone
            storyPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    TabDock leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", TabDock.class);
                    Manuscript book = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
                    if (leftBar == null || book == null) {
                        return;
                    }
                    if (ComponentUtil.getData(leftBar, VIEW_KEY) instanceof BetaReadersView existing) {
                        // the story is drawn again after every change of it (a part added, edited or deleted, another branch)
                        existing.storyChanged();
                        return;
                    }
                    BetaReadersView view = new BetaReadersView(book, service);
                    leftBar.add("beta-readers", T.localized(BETA_READERS), BETA_READERS_ICON, view);
                    ComponentUtil.setData(leftBar, VIEW_KEY, view);
                    activeTabSheets.add(leftBar);
                }
            };
            extensionService.registerDecorator(storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart", "renderStoryContent");
            extensionService.registerDecorator(storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart", "renderStoryContent");

            // the panel in Settings → Extension Settings, filled when the Settings screen is refreshed and stored when it is saved
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
                            if (content instanceof BetaReadersSettingsForm form) {
                                form.refresh();
                            }
                        });
                    } else {
                        panel = extensionSettings.add(T.localized(BETA_READERS), new BetaReadersSettingsForm(service));
                        ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                        activeAccordions.add(extensionSettings);
                    }
                }
            };
            extensionService.registerDecorator(userPartDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "refresh");

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
                        // the body of save() stores the user settings, with these in them
                        panel.getContent().findFirst().ifPresent(content -> {
                            if (content instanceof BetaReadersSettingsForm form) {
                                service.saveSettings(form.save(), userSetting);
                            }
                        });
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                }
            };
            extensionService.registerDecorator(userPartSaveDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "save");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Beta Readers extension", e);
        }
        log.info("Beta Readers loaded");
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the dialogs of the plugin are in the workspace only
        return entryPoint instanceof Main ? BetaReadersStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        inheritedSettingRegistry.unregisterAll(b.getSymbolicName());
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }

        // the tab sheets and the accordions belong to the UIs of all users - change each one in its own session
        synchronized (activeTabSheets) {
            for (TabDock leftBar : activeTabSheets) {
                UIUtils.accessComponent(leftBar, () -> {
                    BetaReadersView view = (BetaReadersView) ComponentUtil.getData(leftBar, VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }
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
    }
}
