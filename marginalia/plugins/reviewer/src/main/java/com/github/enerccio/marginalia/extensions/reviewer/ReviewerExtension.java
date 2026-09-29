package com.github.enerccio.marginalia.extensions.reviewer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.extensions.reviewer.ui.AdvancedOptionsDialog;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ReviewDialog;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ReviewerSettingsForm;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.Map;

@Configurable
public class ReviewerExtension implements MarginaliaExtension {

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

    private final Map<ContextMenu, MenuItem> existingMenus = new HashMap<>();
    private final Map<Accordion, AccordionPanel> settingItems = new HashMap<>();

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
                        Manuscript manuscript = manuscriptService.find(message.getParentScript());

                        MenuItem reviewMenuItem = hamburgerMenu.addItem("Review");
                        reviewMenuItem.addComponentAsFirst(VaadinIcon.STAR.create());

                        reviewMenuItem.getSubMenu().addItem("View / Generate Review", e -> {
                            ReviewDialog dialog = new ReviewDialog(reviewerService,
                                    message, manuscript, null);
                            dialog.open();
                        });

                        reviewMenuItem.getSubMenu().addItem("Advanced Options", e -> {
                            try {
                                ReviewerSettings settings = reviewerService.getSettings();
                                AI targetAi = reviewerService.resolveAI(settings, manuscript);
                                InferenceService service = inferenceServices.forAI(targetAi);
                                AdvancedOptionsDialog dialog = new AdvancedOptionsDialog(reviewerService, service, manuscript, settings.getSettings().get(settings.getDefaultSetting()),
                                        message, null, opts -> {
                                    ReviewDialog rDialog = new ReviewDialog(reviewerService,
                                            message, manuscript, opts);
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
                                reviewerService.deleteReviewData(message);
                            } catch (Exception ex) {
                                UIUtils.internalServerError(loc, ex);
                            }
                        });
                        deleteItem.setEnabled(hasReview);
                        existingMenus.put(hamburgerMenu, deleteItem);
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
                        if (existingMenus.containsKey(hamburgerMenu)) {
                            boolean hasReview = reviewerService.getReviewData(message) != null;
                            existingMenus.get(hamburgerMenu).setEnabled(hasReview);
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
                        if (settingItems.containsKey(extensionSettings)) {
                            AccordionPanel contentPanel = settingItems.get(extensionSettings);
                            contentPanel.getContent().findFirst().ifPresent(content -> {
                                if (content instanceof ReviewerSettingsForm form) {
                                    form.refresh();
                                }
                            });
                        } else {
                            ReviewerSettingsForm form = new ReviewerSettingsForm(
                                    reviewerService);
                            AccordionPanel contentPanel = extensionSettings.add("Reviewer Settings", form);
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
                            if (content instanceof ReviewerSettingsForm form) {
                                ReviewerSettings settings = form.save();
                                reviewerService.saveSettings(settings, userSetting);
                            }
                        });
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
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (cardDecorator != null) {
            extensionService.unregisterDecorator(cardDecorator);
        }
        if (cardDecoratorRefresh != null) {
            extensionService.unregisterDecorator(cardDecoratorRefresh);
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