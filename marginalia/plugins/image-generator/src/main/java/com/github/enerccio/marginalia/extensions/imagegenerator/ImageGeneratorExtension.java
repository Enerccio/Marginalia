package com.github.enerccio.marginalia.extensions.imagegenerator;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.ImageSettings;
import com.github.enerccio.marginalia.extensions.imagegenerator.service.ImageGeneratorService;
import com.github.enerccio.marginalia.extensions.imagegenerator.ui.ImageGeneratorDialog;
import com.github.enerccio.marginalia.extensions.imagegenerator.ui.ImageSettingsForm;
import com.github.enerccio.marginalia.extensions.imagegenerator.ui.ImageStyles;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
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

import static com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLocalization.*;

/**
 * Adds <i>Generate image</i> to the menu of a part of the story: a window where the model writes the prompt, a backend (ComfyUI,
 * an OpenAI-style images API) makes the picture and the user accepts it as an image of the part.
 */
@Configurable
public class ImageGeneratorExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String MENU_ITEM_KEY = "imagegenerator.generate";

    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Autowired
    private NotificationService notificationService;

    private static final String SETTINGS_PANEL_KEY = ImageSettings.KEY + ".settingsPanel";
    /** The sources of the notifications that the prompt is written or the image is made, see {@code NotificationService}. */
    public static final String NOTIFY_PROMPT = ImageSettings.KEY + ".prompt";
    public static final String NOTIFY_IMAGE = ImageSettings.KEY + ".image";

    private final ImageGeneratorService imageService = new ImageGeneratorService();

    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        ImageGeneratorLog.install(parentService.createLogger(bundle));
        notificationService.register(bundle.getSymbolicName(), new NotificationService.Source(NOTIFY_PROMPT, () -> T.localized(IMAGE_GENERATOR_WRITE_THE_PROMPT)));
        notificationService.register(bundle.getSymbolicName(), new NotificationService.Source(NOTIFY_IMAGE, () -> T.localized(IMAGE_GENERATOR_GENERATE_THE_IMAGE)));
        // the story editor loads other books into the same instance, so the book is read when the item is chosen
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.card(MENU_ITEM_KEY, l -> T.localized(GENERATE_IMAGE), (story, card) -> open(story.getCurrentManuscript(), card))
                        .icon(Solid.IMAGE::create));

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
                        if (content instanceof ImageSettingsForm form) {
                            form.refresh();
                        }
                    });
                } else {
                    panel = extensionSettings.add(T.localized(IMAGE_GENERATOR_SETTINGS), new ImageSettingsForm(imageService));
                    ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                    activeAccordions.add(extensionSettings);
                }
            }
        };
        try {
            extensionService.registerDecorator(userPartDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "refresh");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Image Generator extension", e);
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
                        if (content instanceof ImageSettingsForm form) {
                            imageService.saveSettings(form.save(), userSetting);
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
            throw new IllegalStateException("Failed to load the Image Generator extension", e);
        }
    }

    private void open(Manuscript manuscript, StoryCard card) {
        if (manuscript == null || card == null || card.getMessage() == null) {
            return;
        }
        // the card takes the saved part and redraws its images (it keeps its own copy, a stale one would drop them on its next save)
        new ImageGeneratorDialog(card.getMessage(), manuscript, imageService, card::imagesChanged).open();
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the window is opened from the workspace only
        return entryPoint instanceof Main ? ImageStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        ImageGeneratorLog.reset();
        imageService.shutdown();
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
