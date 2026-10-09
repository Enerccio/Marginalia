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
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;

import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Configurable
public class SideQueryExtension implements MarginaliaExtension {

    private static final String SIDE_QUERY_VIEW_KEY = "sidequery_view_component";
    private static final String SETTINGS_PANEL_KEY = "sidequery_settings_panel";

    private final SideQueryService sideQueryService = new SideQueryService();

    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;

    private final Set<VTabSheet> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

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
                        if (ComponentUtil.getData(leftBar, SIDE_QUERY_VIEW_KEY) != null) {
                            return;
                        }

                        SideQueryView sideQueryView = new SideQueryView(currentManuscript, sideQueryService);
                        leftBar.add("Side Query", sideQueryView);

                        ComponentUtil.setData(leftBar, SIDE_QUERY_VIEW_KEY, sideQueryView);
                        activeTabSheets.add(leftBar);
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
                        AccordionPanel contentPanel = (AccordionPanel) ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY);
                        if (contentPanel != null) {
                            contentPanel.getContent().findFirst().ifPresent(content -> {
                                if (content instanceof SideQuerySettingsForm form) {
                                    form.refresh();
                                }
                            });
                        } else {
                            SideQuerySettingsForm form = new SideQuerySettingsForm(sideQueryService);
                            contentPanel = extensionSettings.add("SideQuery Settings", form);
                            ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, contentPanel);
                            activeAccordions.add(extensionSettings);
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
                        AccordionPanel contentPanel = (AccordionPanel) ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY);
                        if (contentPanel != null) {
                            contentPanel.getContent().findFirst().ifPresent(content -> {
                                if (content instanceof SideQuerySettingsForm form) {
                                    SideQuerySettings settings = form.save();
                                    sideQueryService.saveSettings(settings, userSetting);
                                }
                            });
                        }
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
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Side Query extension", e);
        }
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }

        // Clean up living tabs from leftBar UI instances on unload
        synchronized (activeTabSheets) {
            for (VTabSheet leftBar : activeTabSheets) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    SideQueryView sideQueryView = (SideQueryView) ComponentUtil.getData(leftBar, SIDE_QUERY_VIEW_KEY);
                    if (sideQueryView != null) {
                        leftBar.remove(sideQueryView);
                        ComponentUtil.setData(leftBar, SIDE_QUERY_VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }

        // Clean up settings panels from accordion UI instances on unload
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