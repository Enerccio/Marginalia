package com.github.enerccio.marginalia.extensions.loregenerator;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.loregenerator.commands.LoreGeneratorCommands;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService;
import com.github.enerccio.marginalia.extensions.loregenerator.ui.LoreGeneratorDialog;
import com.github.enerccio.marginalia.extensions.loregenerator.ui.LoreGeneratorSettingsForm;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.ui.components.PhoneLorebookView;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import static com.github.enerccio.marginalia.extensions.loregenerator.LoreGeneratorLocalization.*;

@Configurable
public class LoreGeneratorExtension implements MarginaliaExtension {

    private static final String USER_PART = "com.github.enerccio.marginalia.ui.workspace.parts.UserPart";
    private static final String LOREBOOK_VIEW = "com.github.enerccio.marginalia.ui.components.LorebookView";
    private static final String LOREBOOK_PART = "com.github.enerccio.marginalia.ui.workspace.parts.LorebookPart";
    private static final String MANUSCRIPT_LOREBOOK_PART = "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptLorebookPart";
    private static final String PHONE_LOREBOOK_VIEW = "com.github.enerccio.marginalia.ui.components.PhoneLorebookView";
    private static final String PHONE_LOREBOOK_PART = "com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneLorebookPart";
    private static final String PHONE_MANUSCRIPT_LOREBOOK_PART = "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptLorebookPart";
    private static final String SETTINGS_PANEL_KEY = LoreGeneratorData.KEY + ".settingsPanel";
    private static final String GENERATE_COLUMN_KEY = LoreGeneratorData.KEY + ".generateColumn";
    /** On a lorebook view that is the Lorebook tab of a book: how to get that book (a {@code Callable<Manuscript>}). */
    private static final String BOOK_KEY = LoreGeneratorData.KEY + ".book";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    private final LoreGeneratorService loreGeneratorService = new LoreGeneratorService();

    @Autowired
    private CommandService commandService;

    /** Kept to unregister it, the service takes the object. */
    private LoreGeneratorCommands commands;

    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private ExtensionDecorator lorebookViewDecorator;
    private ExtensionDecorator lorebookPartDecorator;
    private ExtensionDecorator bookLorebookPartDecorator;

    // the desktop LorebookView and the PhoneLorebookView
    private final Set<VerticalLayout> activeViews = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        loreGeneratorService.setLogger(log);
        loreGeneratorService.setBundle(bundle);
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
                        panel = extensionSettings.add(T.localized(LORE_GENERATOR), new LoreGeneratorSettingsForm(loreGeneratorService));
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

            // the button in every row of the lorebook, in the desktop LorebookView and in the PhoneLorebookView
            lorebookViewDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (instrumented instanceof VerticalLayout lorebookView && isLorebookView(lorebookView)) {
                        addGenerateColumn(lorebookView, context);
                    }
                }
            };
            extensionService.registerDecorator(lorebookViewDecorator, LOREBOOK_VIEW, "create");
            extensionService.registerDecorator(lorebookViewDecorator, PHONE_LOREBOOK_VIEW, "create");

            // the Lorebooks tab of the workspace builds its view when the page opens, a page that was open before the
            // extension was loaded has no column yet: add it when the tab is shown or refreshed
            lorebookPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    VerticalLayout lorebookView = context.getReflectiveFieldValue(instrumented, "lorebookView", VerticalLayout.class);
                    if (lorebookView != null && isLorebookView(lorebookView)) {
                        addGenerateColumn(lorebookView, context);
                    }
                }
            };
            extensionService.registerDecorator(lorebookPartDecorator, LOREBOOK_PART, "refresh");
            extensionService.registerDecorator(lorebookPartDecorator, PHONE_LOREBOOK_PART, "refresh");

            // the Lorebook tab of a book knows the book, the lorebooks of the workspace do not: what the model costs is the book's when there is one
            bookLorebookPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    VerticalLayout lorebookView = context.getReflectiveFieldValue(instrumented, "lorebookView", VerticalLayout.class);
                    // the book window is a ManuscriptDialog on a desktop and a PhoneManuscriptDialog on a phone
                    Object parent = context.getReflectiveFieldValue(instrumented, "parent", Object.class);
                    if (lorebookView != null && isLorebookView(lorebookView)) {
                        Callable<Manuscript> book = null;
                        if (parent instanceof ManuscriptDialog dialog) {
                            book = dialog::refreshManuscript;
                        } else if (parent instanceof PhoneManuscriptDialog dialog) {
                            book = dialog::refreshManuscript;
                        }
                        if (book != null) {
                            ComponentUtil.setData(lorebookView, BOOK_KEY, book);
                        }
                    }
                }
            };
            extensionService.registerDecorator(bookLorebookPartDecorator, MANUSCRIPT_LOREBOOK_PART, "create");
            extensionService.registerDecorator(bookLorebookPartDecorator, PHONE_MANUSCRIPT_LOREBOOK_PART, "create");

            // the commands for the command bar and for agents
            commands = new LoreGeneratorCommands(loreGeneratorService);
            commandService.registerCommands(commands);

            log.info("Lore Generator extension loaded");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Lore Generator extension", e);
        }
    }

    /**
     * Adds the generate button column to the entry grid of the view, once per view.
     */
    private static boolean isLorebookView(VerticalLayout view) {
        return view instanceof LorebookView || view instanceof PhoneLorebookView;
    }

    private static com.github.enerccio.marginalia.domain.model.impl.Lorebook currentLorebookOf(VerticalLayout view) {
        return view instanceof PhoneLorebookView phone ? phone.getCurrentLorebook() : ((LorebookView) view).getCurrentLorebook();
    }

    private static void refreshEntriesOf(VerticalLayout view) {
        if (view instanceof PhoneLorebookView phone) {
            phone.refreshEntries();
        } else {
            ((LorebookView) view).refreshEntries();
        }
    }

    private void addGenerateColumn(VerticalLayout lorebookView, ExtendableMethodContext context) {
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
                if (currentLorebookOf(lorebookView) != null) {
                    @SuppressWarnings("unchecked")
                    Callable<Manuscript> book = (Callable<Manuscript>) ComponentUtil.getData(lorebookView, BOOK_KEY);
                    new LoreGeneratorDialog(loreGeneratorService, currentLorebookOf(lorebookView), entry, book,
                            () -> refreshEntriesOf(lorebookView)).open();
                }
            });
            button.setThemeName("tertiary");
            button.setTooltipText(T.localized(GENERATE_LORE_ENTRY));
            return button;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        ComponentUtil.setData(lorebookView, GENERATE_COLUMN_KEY, column);
        activeViews.add(lorebookView);
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        if (commands != null) {
            commandService.unregisterCommands(commands);
            commands = null;
        }
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
        if (bookLorebookPartDecorator != null) {
            extensionService.unregisterDecorator(bookLorebookPartDecorator);
        }
        synchronized (activeViews) {
            for (VerticalLayout view : activeViews) {
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
