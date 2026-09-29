package com.github.enerccio.marginalia.extensions.sidequery;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.sidequery.model.SideQuerySettings;
import com.github.enerccio.marginalia.extensions.sidequery.service.SideQueryService;
import com.github.enerccio.marginalia.extensions.sidequery.ui.SideQuerySettingsForm;
import com.github.enerccio.marginalia.extensions.sidequery.ui.SideQueryView;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;

import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.HashMap;
import java.util.Map;

@Configurable
public class SideQueryExtension implements MarginaliaExtension {

    @Autowired
    private Localization loc;

    private final SideQueryService sideQueryService = new SideQueryService();

    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;

    private final Map<Accordion, AccordionPanel> settingItems = new HashMap<>();

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            // Decorate ManuscriptStoryPart to inject SideQuery tab in leftMarginLayout (leftBar VTabSheet)
            storyPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    VTabSheet leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", VTabSheet.class);
                    Manuscript currentManuscript = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);

                    if (leftBar != null && currentManuscript != null) {
                        SideQueryView sideQueryView = new SideQueryView(currentManuscript, sideQueryService);
                        leftBar.add("Side Query", sideQueryView);
                    }
                }
            };

            extensionService.registerDecorator(
                    storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "renderStoryContent"
            );

            // Inject extension settings panel into UserPart settings Accordion
            userPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                    if (extensionSettings != null) {
                        if (settingItems.containsKey(extensionSettings)) {
                            AccordionPanel contentPanel = settingItems.get(extensionSettings);
                            contentPanel.getContent().findFirst().ifPresent(content -> {
                                if (content instanceof SideQuerySettingsForm form) {
                                    form.refresh();
                                }
                            });
                        } else {
                            SideQuerySettingsForm form = new SideQuerySettingsForm(sideQueryService);
                            AccordionPanel contentPanel = extensionSettings.add("SideQuery Settings", form);
                            settingItems.put(extensionSettings, contentPanel);
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    userPartDecorator,
                    "com.github.enerccio.marginalia.ui.workspace.parts.UserPart",
                    "refresh"
            );

            userPartSaveDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                    UserSetting userSetting = context.getReflectiveFieldValue(instrumented, "userSetting", UserSetting.class);
                    Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                    if (extensionSettings != null && userSetting != null) {
                        AccordionPanel contentPanel = settingItems.get(extensionSettings);
                        contentPanel.getContent().findFirst().ifPresent(content -> {
                            if (content instanceof SideQuerySettingsForm form) {
                                SideQuerySettings settings = form.save();
                                sideQueryService.saveSettings(settings, userSetting);
                            }
                        });
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {}
            };

            extensionService.registerDecorator(
                    userPartSaveDecorator,
                    "com.github.enerccio.marginalia.ui.workspace.parts.UserPart",
                    "save"
            );

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
            for (Accordion accordion : settingItems.keySet()) {
                accordion.remove(settingItems.get(accordion));
            }
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }
    }
}