package com.github.enerccio.marginalia.extensions.reviewer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSetting;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.github.enerccio.marginalia.extensions.reviewer.service.ReviewerService;
import com.github.enerccio.marginalia.extensions.reviewer.ui.AdvancedOptionsDialog;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ManuscriptProfileSelect;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ReviewDialog;
import com.github.enerccio.marginalia.extensions.reviewer.ui.ReviewerSettingsForm;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.reviewer.ReviewerLocalization.T;

@Configurable
public class ReviewerExtension implements MarginaliaExtension {

    private static final String MENU_REVIEW = ReviewerSettings.KEY + ".review";
    private static final String MENU_VIEW = MENU_REVIEW + ".view";
    private static final String MENU_ADVANCED = MENU_REVIEW + ".advanced";
    private static final String MENU_DELETE = MENU_REVIEW + ".delete";
    private static final String SETTINGS_PANEL_KEY = ReviewerSettings.KEY + ".settingsPanel";

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private StoryMenuRegistry menuRegistry;

    private final ReviewerService reviewerService = new ReviewerService();
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private ExtensionDecorator infoPartCreateDecorator;
    private ExtensionDecorator infoPartLoadDecorator;

    private final Set<ManuscriptProfileSelect> activeProfileSelects = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        String owner = bundle.getSymbolicName();
        try {
            // the Review sub menu of the menu of a part (desktop and phone), its items are shown by the story tab with the keys of the user;
            // what each one needs is read from the part the menu is opened for
            menuRegistry.register(owner, StoryMenuItem.card(MENU_REVIEW, l -> T.localized("Review"), null)
                    .icon(VaadinIcon.STAR::create));
            menuRegistry.register(owner, StoryMenuItem.card(MENU_VIEW, l -> T.localized("View / Generate Review"), (story, card) -> {
                        try {
                            ChatMessage freshMessage = reviewerService.refreshMessage(card.getMessage());
                            Manuscript manuscript = manuscriptService.find(freshMessage.getParentScript());
                            ReviewDialog dialog = new ReviewDialog(reviewerService, freshMessage, manuscript, null);
                            dialog.open();
                        } catch (Exception ex) {
                            UIUtils.internalServerError(loc, ex);
                        }
                    })
                    .parent(MENU_REVIEW));
            menuRegistry.register(owner, StoryMenuItem.card(MENU_ADVANCED, l -> T.localized("Review: Advanced Options"), (story, card) -> {
                        try {
                            ChatMessage freshMessage = reviewerService.refreshMessage(card.getMessage());
                            Manuscript manuscript = manuscriptService.find(freshMessage.getParentScript());
                            ReviewerSettings settings = reviewerService.getSettings();
                            ReviewerSetting setting = reviewerService.resolveSetting(settings, manuscript);
                            AI targetAi = reviewerService.resolveAI(setting, manuscript);
                            InferenceService service = inferenceServices.forAI(targetAi);
                            AdvancedOptionsDialog dialog = new AdvancedOptionsDialog(
                                    reviewerService, service, manuscript, setting,
                                    freshMessage, null, opts -> {
                                ReviewDialog rDialog = new ReviewDialog(reviewerService, freshMessage, manuscript, opts);
                                rDialog.open();
                            });
                            dialog.open();
                        } catch (Exception ex) {
                            UIUtils.internalServerError(loc, ex);
                        }
                    })
                    .contextLabel((l, story, card) -> T.localized("Advanced Options"))
                    .parent(MENU_REVIEW));
            menuRegistry.register(owner, StoryMenuItem.card(MENU_DELETE, l -> T.localized("Delete Review"), (story, card) -> {
                        try {
                            ChatMessage freshMessage = reviewerService.refreshMessage(card.getMessage());
                            reviewerService.deleteReviewData(freshMessage);
                        } catch (Exception ex) {
                            UIUtils.internalServerError(loc, ex);
                        }
                    })
                    // the part menu reloads the message before it works the state of its items out
                    .enabledWhen((story, card) -> card != null && reviewerService.getReviewData(card.getMessage()) != null)
                    .parent(MENU_REVIEW));

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
                            contentPanel = extensionSettings.add(T.localized("Reviewer Settings"), form);
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

            // the profile of the reviewer for the book, next to the model and the protocol of the info tab
            infoPartCreateDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {

                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    ComboBox<?> protocolCombo = context.getReflectiveFieldValue(instrumented, "protocolCombo", ComboBox.class);
                    if (protocolCombo == null || findProfileSelect(protocolCombo) != null) {
                        return;
                    }
                    protocolCombo.getParent().ifPresent(layout -> {
                        ManuscriptProfileSelect select = new ManuscriptProfileSelect(reviewerService);
                        int index = layout.getElement().indexOfChild(protocolCombo.getElement());
                        layout.getElement().insertChild(index + 1, select.getElement());
                        activeProfileSelects.add(select);
                    });
                }
            };

            infoPartLoadDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {

                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    ComboBox<?> protocolCombo = context.getReflectiveFieldValue(instrumented, "protocolCombo", ComboBox.class);
                    Manuscript manuscript = context.getMethodArgument("manuscript", Manuscript.class);
                    if (protocolCombo == null || manuscript == null) {
                        return;
                    }
                    ManuscriptProfileSelect select = findProfileSelect(protocolCombo);
                    if (select != null) {
                        select.load(manuscript);
                    }
                }
            };

            extensionService.registerDecorator(infoPartCreateDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptInfoPart", "create");
            extensionService.registerDecorator(infoPartLoadDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptInfoPart", "load");
            // the phone dialog has its own info tab with the same fields
            extensionService.registerDecorator(infoPartCreateDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptInfoPart", "create");
            extensionService.registerDecorator(infoPartLoadDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptInfoPart", "load");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Reviewer extension", e);
        }
    }

    private static ManuscriptProfileSelect findProfileSelect(Component sibling) {
        return sibling.getParent().stream()
                .flatMap(Component::getChildren)
                .filter(ManuscriptProfileSelect.class::isInstance)
                .map(ManuscriptProfileSelect.class::cast)
                .findFirst().orElse(null);
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
        // the menus that are open hide the items and they run nothing any more
        menuRegistry.unregisterAll(b.getSymbolicName());
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }
        if (infoPartCreateDecorator != null) {
            extensionService.unregisterDecorator(infoPartCreateDecorator);
        }
        if (infoPartLoadDecorator != null) {
            extensionService.unregisterDecorator(infoPartLoadDecorator);
        }

        // the components belong to the UIs of all users - change each one in its own session
        synchronized (activeProfileSelects) {
            for (ManuscriptProfileSelect select : activeProfileSelects) {
                UIUtils.accessComponent(select, select::removeFromParent);
            }
            activeProfileSelects.clear();
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