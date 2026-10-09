package com.github.enerccio.marginalia.extensions.reviewer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.extensions.reviewer.ui.AdvancedOptionsDialog;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ReviewDialog;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ReviewerSettingsForm;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Configurable
public class ReviewerExtension implements MarginaliaExtension {

    private static final String REVIEW_MENU_ITEM_KEY = ReviewerSettings.KEY + ".rootMenuItem";
    private static final String DELETE_MENU_ITEM_KEY = ReviewerSettings.KEY + ".deleteMenuItem";
    private static final String SETTINGS_PANEL_KEY = ReviewerSettings.KEY + ".settingsPanel";

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ManuscriptService manuscriptService;

    private final ReviewerService reviewerService = new ReviewerService();
    private ExtensionDecorator cardDecorator;
    private ExtensionDecorator cardDecoratorRefresh;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;

    private final Set<ContextMenu> activeMenus = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            cardDecorator = new ExtensionDecorator() {

                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {

                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    ContextMenu hamburgerMenu = context.getReflectiveFieldValue(instrumented, "hamburgerMenu", ContextMenu.class);
                    ChatMessage message = context.getReflectiveFieldValue(instrumented, "message", ChatMessage.class);

                    if (hamburgerMenu != null && message != null) {
                        if (ComponentUtil.getData(hamburgerMenu, REVIEW_MENU_ITEM_KEY) != null) {
                            return;
                        }

                        MenuItem reviewMenuItem = hamburgerMenu.addItem("Review");
                        reviewMenuItem.addComponentAsFirst(VaadinIcon.STAR.create());

                        // Lazy evaluation inside click listeners prevents capturing heavy Manuscript/Tokenizer objects early
                        reviewMenuItem.getSubMenu().addItem("View / Generate Review", e -> {
                            try {
                                ChatMessage freshMessage = reviewerService.refreshMessage(message);
                                Manuscript manuscript = manuscriptService.find(freshMessage.getParentScript());
                                ReviewDialog dialog = new ReviewDialog(reviewerService, freshMessage, manuscript, null);
                                dialog.open();
                            } catch (Exception ex) {
                                UIUtils.internalServerError(loc, ex);
                            }
                        });

                        reviewMenuItem.getSubMenu().addItem("Advanced Options", e -> {
                            try {
                                ChatMessage freshMessage = reviewerService.refreshMessage(message);
                                Manuscript manuscript = manuscriptService.find(freshMessage.getParentScript());
                                ReviewerSettings settings = reviewerService.getSettings();
                                AI targetAi = reviewerService.resolveAI(settings, manuscript);
                                InferenceService service = inferenceServices.forAI(targetAi);
                                AdvancedOptionsDialog dialog = new AdvancedOptionsDialog(
                                        reviewerService, service, manuscript,
                                        settings.getSettings().get(settings.getDefaultSetting()),
                                        freshMessage, null, opts -> {
                                    ReviewDialog rDialog = new ReviewDialog(reviewerService, freshMessage, manuscript, opts);
                                    rDialog.open();
                                });
                                dialog.open();
                            } catch (Exception ex) {
                                UIUtils.internalServerError(loc, ex);
                            }
                        });

                        boolean hasReview = reviewerService.getReviewData(message) != null;
                        MenuItem deleteItem = reviewMenuItem.getSubMenu().addItem("Delete Review", e -> {
                            try {
                                ChatMessage freshMessage = reviewerService.refreshMessage(message);
                                reviewerService.deleteReviewData(freshMessage);
                            } catch (Exception ex) {
                                UIUtils.internalServerError(loc, ex);
                            }
                        });
                        deleteItem.setEnabled(hasReview);

                        // Attach component data to Vaadin components directly
                        ComponentUtil.setData(hamburgerMenu, REVIEW_MENU_ITEM_KEY, reviewMenuItem);
                        ComponentUtil.setData(hamburgerMenu, DELETE_MENU_ITEM_KEY, deleteItem);

                        // Register menu softly for unload cleanup
                        activeMenus.add(hamburgerMenu);
                    }
                }
            };

            extensionService.registerDecorator(
                    cardDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart$ChatMessageCard",
                    "createMenuItems"
            );

            cardDecoratorRefresh = new ExtensionDecorator() {

                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {

                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    ContextMenu hamburgerMenu = context.getReflectiveFieldValue(instrumented, "hamburgerMenu", ContextMenu.class);
                    ChatMessage message = context.getReflectiveFieldValue(instrumented, "message", ChatMessage.class);
                    if (hamburgerMenu != null && message != null) {
                        MenuItem deleteItem = (MenuItem) ComponentUtil.getData(hamburgerMenu, DELETE_MENU_ITEM_KEY);
                        if (deleteItem != null) {
                            boolean hasReview = reviewerService.getReviewData(message) != null;
                            deleteItem.setEnabled(hasReview);
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    cardDecoratorRefresh,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart$ChatMessageCard",
                    "refreshSummaryMenuItems"
            );

            // Hook into UserPart to insert extension settings panel into Accordion
            userPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {

                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    Accordion extensionSettings = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
                    if (extensionSettings != null) {
                        AccordionPanel contentPanel = (AccordionPanel) ComponentUtil.getData(extensionSettings, SETTINGS_PANEL_KEY);
                        if (contentPanel != null) {
                            contentPanel.getContent().findFirst().ifPresent(content -> {
                                if (content instanceof ReviewerSettingsForm form) {
                                    form.refresh();
                                }
                            });
                        } else {
                            ReviewerSettingsForm form = new ReviewerSettingsForm(reviewerService);
                            contentPanel = extensionSettings.add("Reviewer Settings", form);
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
                                if (content instanceof ReviewerSettingsForm form) {
                                    ReviewerSettings settings = form.save();
                                    reviewerService.saveSettings(settings, userSetting);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {

                }
            };

            extensionService.registerDecorator(
                    userPartSaveDecorator,
                    "com.github.enerccio.marginalia.ui.workspace.parts.UserPart",
                    "save"
            );
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Reviewer extension", e);
        }
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        if (cardDecorator != null) {
            extensionService.unregisterDecorator(cardDecorator);
        }
        if (cardDecoratorRefresh != null) {
            extensionService.unregisterDecorator(cardDecoratorRefresh);
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }

        // the components belong to the UIs of all users - change each one in its own session
        synchronized (activeMenus) {
            for (ContextMenu menu : activeMenus) {
                UIUtils.accessComponent(menu, () -> {
                    MenuItem reviewMenuItem = (MenuItem) ComponentUtil.getData(menu, REVIEW_MENU_ITEM_KEY);
                    if (reviewMenuItem != null) {
                        menu.remove(reviewMenuItem);
                        ComponentUtil.setData(menu, REVIEW_MENU_ITEM_KEY, null);
                        ComponentUtil.setData(menu, DELETE_MENU_ITEM_KEY, null);
                    }
                });
            }
            activeMenus.clear();
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