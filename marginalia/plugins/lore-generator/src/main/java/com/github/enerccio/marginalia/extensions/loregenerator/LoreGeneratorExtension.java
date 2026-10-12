package com.github.enerccio.marginalia.extensions.loregenerator;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService;
import com.github.enerccio.marginalia.extensions.loregenerator.ui.LoreGeneratorDialog;
import com.github.enerccio.marginalia.extensions.loregenerator.ui.LoreGeneratorSettingsForm;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Configurable
public class LoreGeneratorExtension implements MarginaliaExtension {

    private static final String USER_PART = "com.github.enerccio.marginalia.ui.workspace.parts.UserPart";
    private static final String LOREBOOK_VIEW = "com.github.enerccio.marginalia.ui.components.LorebookView";
    private static final String LOREBOOK_PART = "com.github.enerccio.marginalia.ui.workspace.parts.LorebookPart";
    private static final String SETTINGS_PANEL_KEY = LoreGeneratorData.KEY + ".settingsPanel";
    private static final String GENERATE_COLUMN_KEY = LoreGeneratorData.KEY + ".generateColumn";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    private final LoreGeneratorService loreGeneratorService = new LoreGeneratorService();

    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private ExtensionDecorator lorebookViewDecorator;
    private ExtensionDecorator lorebookPartDecorator;

    private final Set<LorebookView> activeViews = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        loreGeneratorService.setLogger(log);
        try {
            // the Lore Generator section in the extension settings of the user settings
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
                            if (content instanceof LoreGeneratorSettingsForm form) {
                                form.refresh();
                            }
                        });
                    } else {
                        panel = extensionSettings.add("Lore Generator", new LoreGeneratorSettingsForm(loreGeneratorService));
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
                            if (content instanceof LoreGeneratorSettingsForm form) {
                                loreGeneratorService.saveData(form.save(), userSetting);
                            }
                        });
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {}
            };
            extensionService.registerDecorator(userPartSaveDecorator, USER_PART, "save");

            // the button in every row of the lorebook, only the desktop LorebookView (the phone has its own view class)
            lorebookViewDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof LorebookView lorebookView) {
                        addGenerateColumn(lorebookView, context);
                    }
                }
            };
            extensionService.registerDecorator(lorebookViewDecorator, LOREBOOK_VIEW, "create");

            // the Lorebooks tab of the workspace builds its view when the page opens, a page that was open before the
            // extension was loaded has no column yet: add it when the tab is shown or refreshed
            lorebookPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    LorebookView lorebookView = context.getReflectiveFieldValue(instrumented, "lorebookView", LorebookView.class);
                    if (lorebookView != null) {
                        addGenerateColumn(lorebookView, context);
                    }
                }
            };
            extensionService.registerDecorator(lorebookPartDecorator, LOREBOOK_PART, "refresh");

            log.info("Lore Generator extension loaded");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Lore Generator extension", e);
        }
    }

    /**
     * Adds the generate button column to the entry grid of the view, once per view.
     */
    private void addGenerateColumn(LorebookView lorebookView, ExtendableMethodContext context) {
        if (ComponentUtil.getData(lorebookView, GENERATE_COLUMN_KEY) != null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Grid<LorebookEntry> grid = context.getReflectiveFieldValue(lorebookView, "grid", Grid.class);
        if (grid == null) {
            return;
        }
        Grid.Column<LorebookEntry> column = grid.addComponentColumn(entry -> {
            Button button = new Button(Solid.WAND_MAGIC_SPARKLES.create(), e -> {
                if (lorebookView.getCurrentLorebook() != null) {
                    new LoreGeneratorDialog(loreGeneratorService, lorebookView.getCurrentLorebook(), entry,
                            lorebookView::refreshEntries).open();
                }
            });
            button.setThemeName("tertiary");
            button.setTooltipText("Generate lore entry");
            return button;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        ComponentUtil.setData(lorebookView, GENERATE_COLUMN_KEY, column);
        activeViews.add(lorebookView);
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }

        if (lorebookViewDecorator != null) {
            extensionService.unregisterDecorator(lorebookViewDecorator);
        }
        if (lorebookPartDecorator != null) {
            extensionService.unregisterDecorator(lorebookPartDecorator);
        }
        synchronized (activeViews) {
            for (LorebookView view : activeViews) {
                UIUtils.accessComponent(view, () -> {
                    @SuppressWarnings("unchecked")
                    Grid.Column<LorebookEntry> column = (Grid.Column<LorebookEntry>) ComponentUtil.getData(view, GENERATE_COLUMN_KEY);
                    if (column != null) {
                        ((Grid<LorebookEntry>) column.getGrid()).removeColumn(column);
                        ComponentUtil.setData(view, GENERATE_COLUMN_KEY, null);
                    }
                });
            }
            activeViews.clear();
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
        log.info("Lore Generator extension unloaded");
    }
}
