package com.github.enerccio.marginalia.extensions.mangagenerator;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSettings;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.MangaService;
import com.github.enerccio.marginalia.extensions.mangagenerator.ui.MangaDialog;
import com.github.enerccio.marginalia.extensions.mangagenerator.ui.MangaSettingsForm;
import com.github.enerccio.marginalia.extensions.mangagenerator.ui.MangaStyles;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.T;

/**
 * Adds <i>Manga Generator</i> to the settings menu of the story: a large window that turns the parts of the story into a manga
 * script and the image tags of it.
 */
@Configurable
public class MangaGeneratorExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String MENU_ITEM_KEY = "mangagenerator.open";

    @Autowired
    private StoryMenuRegistry menuRegistry;

    private static final String SETTINGS_PANEL_KEY = MangaSettings.KEY + ".settingsPanel";

    private final MangaService mangaService = new MangaService();

    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        MangaGeneratorLog.install(parentService.createLogger(bundle));
        // the story editor loads other books into the same instance, so the book is read when the item is chosen
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.tools(MENU_ITEM_KEY, l -> T.localized("Manga Generator"), story -> open(story.getCurrentManuscript()))
                        .icon(Solid.BOOK_OPEN::create));

        // the defaults are a panel of Settings → Extension Settings
        userPartDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
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
                        if (content instanceof MangaSettingsForm form) {
                            form.refresh();
                        }
                    });
                } else {
                    panel = extensionSettings.add(T.localized("Manga Generator Settings"), new MangaSettingsForm(mangaService));
                    ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                    activeAccordions.add(extensionSettings);
                }
            }
        };
        try {
            extensionService.registerDecorator(userPartDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "refresh");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Manga Generator extension", e);
        }

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
                        if (content instanceof MangaSettingsForm form) {
                            mangaService.saveSettings(form.save(), userSetting);
                        }
                    });
                }
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) {
            }
        };
        try {
            extensionService.registerDecorator(userPartSaveDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "save");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Manga Generator extension", e);
        }
    }

    private void open(Manuscript manuscript) {
        if (manuscript == null) {
            Notification.warning(T.localized("Open a book first"));
            return;
        }
        new MangaDialog(manuscript, mangaService).open();
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the window is opened from the workspace only
        return entryPoint instanceof Main ? MangaStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        MangaGeneratorLog.reset();
        // the settings menus that are open hide the item and it runs nothing any more
        menuRegistry.unregisterAll(bundle.getSymbolicName());
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }

        // the settings panels that are open: each in the session of its user
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
