package com.github.enerccio.marginalia.extensions.audiobook;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookExporter;
import com.github.enerccio.marginalia.extensions.audiobook.model.AudiobookSettings;
import com.github.enerccio.marginalia.extensions.audiobook.model.AutoMode;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.extensions.audiobook.service.PlayableParts;
import com.github.enerccio.marginalia.extensions.audiobook.ui.*;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.main.Viewer;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * Turns a book into an audiobook part by part with a text-to-speech server. So far: the settings of the user (the speech servers and the
 * requests made around a batch, a panel of <i>Settings → Extension Settings</i>) and <i>Convert to audio</i> in the tools menu of the story editor, which
 * puts parts into the queue of the user (the narrator reads, one voice). The plan is in {@code TODO.md}.
 */
@Configurable
public class AudiobookExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String MENU_ITEM_KEY = "audiobook.convert";
    private static final String STUDIO_ITEM_KEY = "audiobook.studio";

    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private InheritedSettingRegistry inheritedSettings;

    private static final String SETTINGS_PANEL_KEY = AudiobookSettings.KEY + ".settingsPanel";

    private final AudiobookService audiobookService = new AudiobookService();

    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private ExtensionDecorator viewerDecorator;
    private ExtensionDecorator exportFieldsDecorator;
    private ExtensionDecorator exportDecorator;
    private ExtensionDecorator partFinishedDecorator;
    private final List<AudiobookExporter> exporters = new java.util.ArrayList<>();
    /** The check box of each open export dialog. */
    private final Map<Object, Checkbox> finalOnly = Collections.synchronizedMap(new WeakHashMap<>());
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        AudiobookLog.install(parentService.createLogger(bundle));
        audiobookService.start();
        // the story editor loads other books into the same instance, so the book is read when the item is chosen
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.tools(MENU_ITEM_KEY, l -> T.localized(CONVERT_TO_AUDIO), story -> new ConvertDialog(story.getCurrentManuscript(), audiobookService).open())
                        .icon(Solid.HEADPHONES::create));
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.tools(STUDIO_ITEM_KEY, l -> T.localized(AUDIOBOOK_STUDIO), story -> new StudioDialog(story.getCurrentManuscript(), audiobookService).open())
                        .icon(Solid.SLIDERS::create));

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
                        if (content instanceof AudiobookSettingsForm form) {
                            form.refresh();
                        }
                    });
                } else {
                    panel = extensionSettings.add(T.localized(AUDIOBOOK_SETTINGS), new AudiobookSettingsForm(audiobookService));
                    ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                    activeAccordions.add(extensionSettings);
                }
            }
        };
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
                        if (content instanceof AudiobookSettingsForm form) {
                            audiobookService.saveSettings(form.save(), userSetting);
                        }
                    });
                }
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) {
            }
        };
        try {
            extensionService.registerDecorator(userPartDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "refresh");
            extensionService.registerDecorator(userPartSaveDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "save");
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Audiobook extension", e);
        }
        // the player: in the reader of a book that has parts with audio
        viewerDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                // no book (not found), no parts to read or an error: nothing is added
                if (throwing != null || !context.hasLocalVariable("scrollPanel") || !context.hasLocalVariable("contentWrapper")
                        || !context.hasLocalVariable("container") || !context.hasLocalVariable("branch") || !context.hasLocalVariable("manuscript")) {
                    return;
                }
                VerticalLayout container = context.getLocalVariable("container", VerticalLayout.class);
                ScrollPanel scrollPanel = context.getLocalVariable("scrollPanel", ScrollPanel.class);
                VerticalLayout contentWrapper = context.getLocalVariable("contentWrapper", VerticalLayout.class);
                List<ChatMessage> branch = context.getLocalVariable("branch", List.class);
                Manuscript manuscript = context.getLocalVariable("manuscript", Manuscript.class);
                if (container == null || scrollPanel == null || contentWrapper == null || branch == null || manuscript == null) {
                    return;
                }
                Map<Long, String> playable = PlayableParts.of(branch);
                if (!playable.isEmpty()) {
                    container.add(new AudioPlayer(scrollPanel, contentWrapper, manuscript.getName(), playable, audiobookService));
                }
            }
        };
        try {
            extensionService.registerDecorator(viewerDecorator, "com.github.enerccio.marginalia.ui.main.Viewer", "renderManuscriptReadingView");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Audiobook extension", e);
        }
        // the audiobook as a format of the export of the book, and the choice of the parts that are final
        for (AudiobookExporter.Format format : AudiobookExporter.Format.values()) {
            AudiobookExporter exporter = new AudiobookExporter(format, part -> audiobookService.exportableAudio(part.getUuid()));
            exporters.add(exporter);
            exporterService.registerExporter(exporter);
        }
        exportFieldsDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                if (throwing != null || !context.hasReturn()) {
                    return;
                }
                Select<?> format = context.getReflectiveFieldValue(instrumented, "format", Select.class);
                if (format == null) {
                    return;
                }
                Checkbox choice = new Checkbox(T.localized(ONLY_THE_PARTS_MARKED_FINAL));
                choice.setVisible(format.getValue() instanceof AudiobookExporter);
                format.addValueChangeListener(e -> choice.setVisible(e.getValue() instanceof AudiobookExporter));
                finalOnly.put(instrumented, choice);
                context.getCurrentReturn(List.class).add(choice);
            }
        };
        exportDecorator = new ExtensionDecorator() {
            @Override
            @SuppressWarnings("unchecked")
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                Checkbox choice = finalOnly.get(instrumented);
                if (choice == null || !choice.getValue() || !(context.getMethodArgument("exporter", ExporterService.Exporter.class) instanceof AudiobookExporter)) {
                    return;
                }
                List<ChatMessage> parts = context.getMethodArgument("messages", List.class);
                // the parts the export holds (a copy when a plugin changed its text keeps the uuid): only those marked final in the studio
                context.registerMethodArgument("messages", parts.stream().filter(p -> audiobookService.isFinal(p.getUuid())).toList(), List.class);
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) {
            }
        };
        try {
            extensionService.registerDecorator(exportFieldsDecorator, "com.github.enerccio.marginalia.ui.dialogs.ExportDialog", "createExtensionFields");
            extensionService.registerDecorator(exportDecorator, "com.github.enerccio.marginalia.ui.dialogs.ExportDialog", "export");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Audiobook extension", e);
        }
        // a new part that is final (after the automatic annotation and redraft of the book, if it has them) is made into audio, when the book says so
        inheritedSettings.register(bundle.getSymbolicName(),
                InheritedSetting.choice(AudiobookService.AUTO_SETTING, () -> T.localized(CONVERT_NEW_PARTS_TO_AUDIO_BY_ITSELF), () -> List.of(
                                new InheritedSetting.Choice(AutoMode.NO.id(), T.localized(AUTO_NO)),
                                new InheritedSetting.Choice(AutoMode.PLAIN.id(), T.localized(AUTO_PLAIN)),
                                new InheritedSetting.Choice(AutoMode.LABELLED.id(), T.localized(AUTO_LABELLED))))
                        .group(() -> T.localized(AUDIOBOOK))
                        .help(() -> T.localized(A_GENERATED_PART_IS_MADE_INTO_AUDIO_WHEN_IT_IS_FINAL))
                        .userValue(() -> {
                            AutoMode mode = audiobookService.getSettings().getAutoMode();
                            return mode == AutoMode.NO ? null : mode.id();
                        })
                        .fallback(AutoMode.NO::id));
        partFinishedDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                if (throwing == null) {
                    convertFinishedPart(context.getReflectiveFieldValue(instrumented, "manuscript", Manuscript.class),
                            context.getMethodArgument("part", ChatMessage.class));
                }
            }
        };
        try {
            extensionService.registerDecorator(partFinishedDecorator, "com.github.enerccio.marginalia.ui.dialogs.story.AutoAnnotate", "finished");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Audiobook extension", e);
        }
        AudiobookLog.get().info("Audiobook loaded");
    }

    /** The new part is final: when the book says so it goes into the queue; a failure never reaches the story (the author is told). */
    private void convertFinishedPart(Manuscript manuscript, ChatMessage part) {
        try {
            if (manuscript == null || part == null) {
                return;
            }
            UI ui = UI.getCurrent();
            switch (audiobookService.convertFinishedPart(manuscript, part, snapshot -> reportAutoJob(ui, snapshot))) {
                case QUEUED -> Notification.success(T.localized(THE_NEW_PART_IS_BEING_MADE_INTO_AUDIO));
                case QUEUED_PLAIN -> Notification.warning(T.localized(THE_BOOK_HAS_NO_MODEL_THE_NARRATOR_READS_THE_NEW_PART));
                case NO_BACKEND -> Notification.error(T.localized(NO_BACKEND_YET));
                case NOTHING -> {
                }
            }
        } catch (IllegalArgumentException e) {
            Notification.error(T.localized(THE_CONVERSION_COULD_NOT_BE_STARTED).formatted(e.getMessage()));
        } catch (Exception e) {
            AudiobookLog.get().warn("The automatic conversion of a new part could not be started: {}", e.getMessage(), e);
        }
    }

    /** From the worker thread: tells the author in the page that started it, if the page is still there. */
    private static void reportAutoJob(UI ui, ConversionJob.Snapshot job) {
        if (ui == null || job.state() == ConversionJob.State.CANCELLED) {
            return;
        }
        try {
            ui.access(() -> {
                if (job.state() == ConversionJob.State.DONE) {
                    Notification.success(T.localized(THE_AUDIO_OF_THE_NEW_PART_IS_READY));
                } else {
                    Notification.error(T.localized(THE_AUDIO_OF_THE_NEW_PART_COULD_NOT_BE_MADE).formatted(job.message()));
                }
            });
        } catch (RuntimeException e) {
            // the page is gone: the job is in the list of the dialog
            AudiobookLog.get().debug("The page of an automatic conversion is gone: {}", e.getMessage());
        }
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the settings and the conversion are in the workspace, the player in the viewer
        return entryPoint instanceof Main || entryPoint instanceof Viewer ? AudiobookStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        AudiobookLog.get().info("Audiobook unloaded");
        AudiobookLog.reset();
        menuRegistry.unregisterAll(bundle.getSymbolicName());
        audiobookService.shutdown();
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
        }
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
        }
        if (viewerDecorator != null) {
            extensionService.unregisterDecorator(viewerDecorator);
            viewerDecorator = null;
        }
        if (exportFieldsDecorator != null) {
            extensionService.unregisterDecorator(exportFieldsDecorator);
            exportFieldsDecorator = null;
        }
        if (partFinishedDecorator != null) {
            extensionService.unregisterDecorator(partFinishedDecorator);
            partFinishedDecorator = null;
        }
        inheritedSettings.unregisterAll(bundle.getSymbolicName());
        if (exportDecorator != null) {
            extensionService.unregisterDecorator(exportDecorator);
            exportDecorator = null;
        }
        exporters.forEach(exporterService::unregisterExporter);
        exporters.clear();
        finalOnly.clear();

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
