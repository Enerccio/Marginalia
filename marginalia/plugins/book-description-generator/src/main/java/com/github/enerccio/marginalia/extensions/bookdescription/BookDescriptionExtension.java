package com.github.enerccio.marginalia.extensions.bookdescription;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.bookdescription.model.BookDescriptionSettings;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionInherited;
import com.github.enerccio.marginalia.extensions.bookdescription.service.BookDescriptionService;
import com.github.enerccio.marginalia.extensions.bookdescription.ui.BookDescriptionDialog;
import com.github.enerccio.marginalia.extensions.bookdescription.ui.BookDescriptionSettingsForm;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static com.github.enerccio.marginalia.extensions.bookdescription.BookDescriptionLocalization.*;

/**
 * Adds the <em>Generate name and description</em> button to the About tab of the book window (desktop and phone), which opens a
 * dialog where the model writes the name and the description of the book from a range of its story.
 */
@Configurable
public class BookDescriptionExtension implements MarginaliaExtension {

    private static final String INFO_PART = "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptInfoPart";
    private static final String PHONE_INFO_PART = "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptInfoPart";
    private static final String USER_PART = "com.github.enerccio.marginalia.ui.workspace.parts.UserPart";
    private static final String SETTINGS_PANEL_KEY = BookDescriptionSettings.KEY + ".settingsPanel";
    private static final String BUTTON_KEY = BookDescriptionSettings.KEY + ".button";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private InheritedSettingRegistry inheritedSettingRegistry;

    private final BookDescriptionService service = new BookDescriptionService();

    private ExtensionDecorator infoPartCreateDecorator;
    private ExtensionDecorator infoPartLoadDecorator;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;

    /** The book each About tab shows, by the tab (it loads another book into the same instance). */
    private final Map<Object, Manuscript> books = Collections.synchronizedMap(new WeakHashMap<>());
    private final Set<Button> activeButtons = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        service.setLogger(log);
        try {
            // what books and folders inherit: their dialogs get the fields, the user level is the panel in the settings
            BookDescriptionInherited.register(inheritedSettingRegistry, bundle.getSymbolicName(), service::settingsOrBlank);

            // the button at the end of the About tab
            infoPartCreateDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    VerticalLayout mainLayout = context.getLocalVariable("mainLayout", VerticalLayout.class);
                    if (mainLayout == null || mainLayout.getChildren().anyMatch(child -> ComponentUtil.getData(child, BUTTON_KEY) != null)) {
                        return;
                    }
                    Button button = new Button(T.localized(GENERATE_NAME_AND_DESCRIPTION), Solid.WAND_MAGIC_SPARKLES.create(),
                            _ -> open(instrumented, context));
                    ComponentUtil.setData(button, BUTTON_KEY, button);
                    mainLayout.add(button);
                    activeButtons.add(button);
                }
            };

            infoPartLoadDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    Manuscript manuscript = context.getMethodArgument("manuscript", Manuscript.class);
                    if (manuscript != null) {
                        books.put(instrumented, manuscript);
                    }
                }
            };

            extensionService.registerDecorator(infoPartCreateDecorator, INFO_PART, "create");
            extensionService.registerDecorator(infoPartLoadDecorator, INFO_PART, "load");
            // the phone dialog has its own About tab with the same fields
            extensionService.registerDecorator(infoPartCreateDecorator, PHONE_INFO_PART, "create");
            extensionService.registerDecorator(infoPartLoadDecorator, PHONE_INFO_PART, "load");

            // the Book Description Generator section in the extension settings of the user settings
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
                            if (content instanceof BookDescriptionSettingsForm form) {
                                form.refresh();
                            }
                        });
                    } else {
                        panel = extensionSettings.add(T.localized(BOOK_DESCRIPTION_GENERATOR), new BookDescriptionSettingsForm(service));
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
                            if (content instanceof BookDescriptionSettingsForm form) {
                                service.saveSettings(form.save(), userSetting);
                            }
                        });
                    }
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                }
            };
            extensionService.registerDecorator(userPartSaveDecorator, USER_PART, "save");

            log.info("Book Description Generator extension loaded");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Book Description Generator extension", e);
        }
    }

    /**
     * Opens the dialog for the book the About tab shows. What is copied goes into the fields of the tab and is saved the way an
     * edit of them is, so the tab and the header of the book window show it at once.
     */
    private void open(Object infoPart, ExtendableMethodContext context) {
        Manuscript manuscript = books.get(infoPart);
        if (manuscript == null) {
            return;
        }
        BookDescriptionDialog.BookTarget target = new BookDescriptionDialog.BookTarget() {
            @Override
            public void setName(String name) {
                TextField field = context.getReflectiveFieldValue(infoPart, "nameField", TextField.class);
                field.setInvalid(false);
                field.setErrorMessage("");
                field.setValue(name);
                autosave(infoPart, context);
            }

            @Override
            public void setDescription(String description) {
                TextArea field = context.getReflectiveFieldValue(infoPart, "descriptionField", TextArea.class);
                field.setValue(description);
                autosave(infoPart, context);
            }
        };
        new BookDescriptionDialog(service, manuscript, target).open();
    }

    private void autosave(Object infoPart, ExtendableMethodContext context) {
        // the tab saves the fields when the author edits them; setting them from here is not an edit
        context.callReflectiveMethod(infoPart, "autosave", new Class<?>[0], new Object[0], Void.TYPE);
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        inheritedSettingRegistry.unregisterAll(bundle.getSymbolicName());
        for (ExtensionDecorator decorator : new ExtensionDecorator[] {infoPartCreateDecorator, infoPartLoadDecorator, userPartDecorator, userPartSaveDecorator}) {
            if (decorator != null) {
                extensionService.unregisterDecorator(decorator);
            }
        }

        // the components belong to the UIs of all users - change each one in its own session
        synchronized (activeButtons) {
            for (Button button : activeButtons) {
                UIUtils.accessComponent(button, button::removeFromParent);
            }
            activeButtons.clear();
        }
        books.clear();

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
        log.info("Book Description Generator extension unloaded");
    }
}
