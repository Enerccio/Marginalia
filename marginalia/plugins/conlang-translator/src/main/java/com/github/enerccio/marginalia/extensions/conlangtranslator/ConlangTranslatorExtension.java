package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.GenerationOutcome;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ExportMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ReaderMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangPassageKind;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangTransformer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ExportText;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ui.*;
import com.github.enerccio.marginalia.ui.components.PartView;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.main.Viewer;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.select.Select;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Translates the direct speech of the characters who speak a constructed language into that language and lets the reader reveal the English
 * source. The story holds the conlang; every path that gives the story to a model reads the English source back (the transformer in the
 * {@link PartTextRegistry}); the plugin keeps each translated speech as a passage of its own kind. See TODO.md for the plan and what is left.
 * <p>
 * What is hooked: the registration of the passage kind and of the transformer, the <b>Translate the speech</b> action of the menu of a part,
 * the automatic run when a generated part is final (after the automatic annotation and redraft of the book, if it has them), and the removal
 * of the passages of a part that is generated again.
 */
@Configurable
public class ConlangTranslatorExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String MENU_ITEM_KEY = "conlangtranslator.translate";
    private static final String RANGE_ITEM_KEY = "conlangtranslator.range";
    private static final String BOOK_TAB_ID = "conlang-translator";
    private static final String BOOK_VIEW_KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator.bookView";
    private static final String SETTINGS_PANEL_KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator.settingsPanel";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private PassageKinds passageKinds;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private PassageService passageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private InheritedSettingRegistry inheritedSettingRegistry;

    private final LanguageLibrary library = new LanguageLibrary();
    private final TranslationService translationService = new TranslationService();
    private final BookConlang bookConlang = new BookConlang(translationService);
    private final PassageAccess passageAccess = new PassageAccess();
    private final ConlangPassageKind kind = new ConlangPassageKind();
    private final ConlangTransformer transformer = new ConlangTransformer(passageAccess);

    private TranslationRuns runs;
    private RangeRuns rangeRuns;
    private ExtensionDecorator partFinishedDecorator;
    private Registration cleanupRegistration;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private ExtensionDecorator viewerDecorator;
    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator exportFieldsDecorator;
    private ExtensionDecorator exportDecorator;
    // the field of each export dialog that is open
    private final Map<Object, Select<ExportMode>> exportChoices = Collections.synchronizedMap(new WeakHashMap<>());
    // the reader mode of the book a viewer shows, found once per viewer
    private final Map<Object, ReaderMode> readerModes = Collections.synchronizedMap(new WeakHashMap<>());
    private final Set<TabDock> activeTabDocks = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private final Set<Accordion> activeAccordions = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private String owner;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        com.github.enerccio.marginalia.extensions.conlangtranslator.service.PluginLog.set(log);
        owner = bundle.getSymbolicName();
        runs = new TranslationRuns(translationService, log);
        rangeRuns = new RangeRuns(translationService, log);
        try {
            passageKinds.register(kind);
            partTexts.register(owner, transformer);

            // what books and folders choose: their dialogs get the fields, the user level is the panel in the Settings screen
            ConlangInherited.register(inheritedSettingRegistry, owner, library::defaultsOrBlank, library::languageChoices,
                    library::aiChoices, library::protocolChoices);
            registerSettingsPanel(extensionService);

            menuRegistry.register(owner, StoryMenuItem.card(MENU_ITEM_KEY, l -> T.localized(TRANSLATE_THE_SPEECH),
                            (story, card) -> translateByHand(story.getCurrentManuscript(), card))
                    .icon(Solid.LANGUAGE::create));
            menuRegistry.register(owner, StoryMenuItem.tools(RANGE_ITEM_KEY, l -> T.localized(TRANSLATE_PARTS), this::openRange)
                    .icon(Solid.LAYER_GROUP::create));

            registerBookTab(extensionService);
            registerViewer(extensionService);
            registerExport(extensionService);

            // a generated part is final once the automatic annotation and redraft (if the book has them) are done
            partFinishedDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (throwing == null) {
                        translateFinishedPart(context.getReflectiveFieldValue(instrumented, "manuscript", Manuscript.class),
                                context.getMethodArgument("part", ChatMessage.class),
                                context.getReflectiveFieldValue(instrumented, "onChanged", Runnable.class));
                    }
                }
            };
            extensionService.registerDecorator(partFinishedDecorator, "com.github.enerccio.marginalia.ui.dialogs.story.AutoAnnotate", "finished");

            // a part that is generated again has a new English text: the passages of the old one go
            cleanupRegistration = storyGenerationService.addEventListener(Events.AFTER_CLEANUP, this::dropRegenerated);
            log.info("Conlang Translator loaded");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Conlang Translator extension", e);
        }
    }

    /**
     * The <b>Conlang</b> tab of the story (the left dock, the drawer on a phone): the words of the book's language, who speaks it in this book
     * and the quick translation. It is added once per story part and reads the language when it is built.
     */
    private void registerBookTab(ExtensionService extensionService) throws Exception {
        storyPartDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                TabDock leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", TabDock.class);
                Manuscript currentManuscript = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
                if (leftBar == null || currentManuscript == null || ComponentUtil.getData(leftBar, BOOK_VIEW_KEY) != null) {
                    return;
                }
                ConlangBookView view = new ConlangBookView(currentManuscript, bookConlang);
                leftBar.add(BOOK_TAB_ID, T.localized(CONLANG), TabDockIcon.letters("CL"), view);
                ComponentUtil.setData(leftBar, BOOK_VIEW_KEY, view);
                activeTabDocks.add(leftBar);
            }
        };
        extensionService.registerDecorator(storyPartDecorator,
                "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart", "renderStoryContent");
        extensionService.registerDecorator(storyPartDecorator,
                "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart", "renderStoryContent");
    }

    /**
     * The published book: the parts the viewer builds show the translated speech the way the book asks for (<i>conlang with a click</i>, <i>conlang
     * only</i>, <i>English only</i>). The reader is not the author, so nothing of the author's own settings is read.
     */
    private void registerViewer(ExtensionService extensionService) throws Exception {
        viewerDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                if (throwing != null || !context.hasReturn()) {
                    return;
                }
                ChatMessage part = context.getMethodArgument("msg", ChatMessage.class);
                Component built = context.getCurrentReturn(Component.class);
                if (part == null || part.getId() == null || !(built instanceof PartView view)) {
                    return;
                }
                List<AnchoredPassage> passages = passageService.listForParts(List.of(part.getId()), Span.KIND);
                if (!passages.isEmpty()) {
                    ReaderView.apply(view, part, readerMode(instrumented, context), passages);
                }
            }
        };
        extensionService.registerDecorator(viewerDecorator, "com.github.enerccio.marginalia.ui.main.Viewer", "createPart");
    }

    /**
     * The export dialog: a field for what the export holds for the translated speech (only for a book that has some), and the parts the exporter
     * gets are changed accordingly. The exporters are not touched: they get another text.
     */
    private void registerExport(ExtensionService extensionService) throws Exception {
        exportFieldsDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                if (throwing != null || !context.hasReturn()) {
                    return;
                }
                Manuscript book = context.getReflectiveFieldValue(instrumented, "manuscript", Manuscript.class);
                if (book == null || passageService.listForManuscript(book, Span.KIND).isEmpty()) {
                    return;
                }
                Select<ExportMode> choice = ExportChoice.field();
                exportChoices.put(instrumented, choice);
                context.getCurrentReturn(List.class).add(choice);
            }
        };
        extensionService.registerDecorator(exportFieldsDecorator, "com.github.enerccio.marginalia.ui.dialogs.ExportDialog", "createExtensionFields");

        exportDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                Select<ExportMode> choice = exportChoices.get(instrumented);
                ExportMode mode = choice == null || choice.getValue() == null ? ExportMode.CONLANG : choice.getValue();
                if (mode == ExportMode.CONLANG) {
                    return;
                }
                List<ChatMessage> parts = context.getMethodArgument("messages", List.class);
                context.registerMethodArgument("messages", exportParts(mode, parts), List.class);
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) {
            }
        };
        extensionService.registerDecorator(exportDecorator, "com.github.enerccio.marginalia.ui.dialogs.ExportDialog", "export");
    }

    /**
     * Copies of the parts with the text the export gets, in the order of the parts; the stored parts are not touched.
     */
    private List<ChatMessage> exportParts(ExportMode mode, List<ChatMessage> parts) throws Exception {
        Map<Long, List<Located>> passages = passageAccess.of(parts);
        List<ChatMessage> exported = new ArrayList<>();
        int number = 1;
        for (ChatMessage part : parts) {
            ExportText.Rendered rendered = ExportText.render(mode, part.getResponse(), passages.get(part.getId()), number);
            number = rendered.next();
            ChatMessage copy = ChatMessage.copyOf(part);
            copy.setResponse(rendered.text());
            exported.add(copy);
        }
        return exported;
    }

    /**
     * How the book of the viewer shows the speech: its own choice, else the one of a folder, else the click. The book is found as the viewer
     * found it, so a book the reader may not see has no mode (the default).
     */
    private ReaderMode readerMode(Object viewer, ExtendableMethodContext context) throws Exception {
        ReaderMode known = readerModes.get(viewer);
        if (known != null) {
            return known;
        }
        ReaderMode mode = ReaderMode.CLICK;
        String uuid = context.getReflectiveFieldValue(viewer, "manuscriptParam", String.class);
        Manuscript book = uuid == null ? null : manuscriptService.findViewable(uuid);
        if (book != null) {
            mode = ReaderMode.of(manuscriptService.resolveExtension(book, ConlangInherited.READER).value());
        }
        readerModes.put(viewer, mode);
        return mode;
    }

    /**
     * The panel in <i>Settings → Extension Settings</i>: filled when the Settings screen is refreshed and stored when it is saved.
     */
    private void registerSettingsPanel(ExtensionService extensionService) throws Exception {
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
                        if (content instanceof LanguageSettingsForm form) {
                            form.refresh();
                        }
                    });
                } else {
                    panel = extensionSettings.add(T.localized(CONLANG_TRANSLATOR), new LanguageSettingsForm(library));
                    ComponentUtil.setData(extensionSettings, SETTINGS_PANEL_KEY, panel);
                    activeAccordions.add(extensionSettings);
                }
            }
        };
        extensionService.registerDecorator(userPartDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "refresh");

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
                    // the body of save() stores the user settings, with the library in them
                    LanguageSettingsForm form = panel.getContent().filter(LanguageSettingsForm.class::isInstance)
                            .map(LanguageSettingsForm.class::cast).findFirst().orElse(null);
                    if (form != null) {
                        form.save(userSetting);
                    }
                }
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) {
            }
        };
        extensionService.registerDecorator(userPartSaveDecorator, "com.github.enerccio.marginalia.ui.workspace.parts.UserPart", "save");
    }

    /**
     * The dialog of the actions on a range of the parts of the active branch: the parts that have text, counted from 1, each with whether it
     * has a translated speech.
     */
    private void openRange(StoryEditor story) {
        try {
            Manuscript book = story.getCurrentManuscript() == null ? null : manuscriptService.find(story.getCurrentManuscript());
            if (book == null || translationService.languageOf(translationService.settings(book)) == null) {
                Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                return;
            }
            new RangeDialog(book, () -> rangeParts(book), rangeRuns, story::reloadStory).open();
        } catch (Exception e) {
            log.warn("Failed to open the range dialog: {}", e.getMessage(), e);
            Notification.error(String.format(T.localized(THE_LANGUAGES_CAN_NOT_BE_LOADED), e.getMessage()));
        }
    }

    /**
     * The parts of the active branch that have text, counted from 1, each with whether it has a translated speech, as they are stored now.
     */
    private List<RangeLoop.RangePart> rangeParts(Manuscript bookToRead) {
        try {
            Manuscript book = manuscriptService.find(bookToRead);
            List<ChatMessage> branch = book == null || book.getActiveLeaf() == null ? List.of()
                    : chatMessageService.getBranchFromLeaf(book.getActiveLeaf()).stream()
                    .filter(part -> part.getResponse() != null && !part.getResponse().isBlank()).toList();
            Map<Long, List<Located>> translated = passageAccess.of(branch);
            List<RangeLoop.RangePart> parts = new ArrayList<>();
            for (int i = 0; i < branch.size(); i++) {
                ChatMessage part = branch.get(i);
                parts.add(new RangeLoop.RangePart(i + 1, part, translated.containsKey(part.getId())));
            }
            return parts;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * The action of the menu of a part: translates its speech on a worker thread and shows what came of it.
     */
    private void translateByHand(Manuscript manuscript, StoryCard card) {
        if (manuscript == null || card == null || card.getMessage() == null) {
            return;
        }
        ChatMessage part = card.getMessage();
        Notification.success(T.localized(TRANSLATING_THE_SPEECH));
        boolean started = runs.start(manuscript, part, (outcome, failure) -> {
            report(outcome, failure);
            if (outcome != null && outcome.status() == TranslationService.Status.TRANSLATED) {
                try {
                    card.updateResponse(translationService.find(part).getResponse());
                } catch (Exception e) {
                    log.warn("Failed to show the translated part: {}", e.getMessage(), e);
                }
            }
        });
        if (!started) {
            Notification.error(T.localized(THE_PART_IS_BEING_TRANSLATED));
        }
    }

    /**
     * The new part is final: when the book translates by itself, a run starts on a worker thread, and the story is loaded again when it is
     * over. Otherwise nothing happens. A failure never reaches the story.
     */
    private void translateFinishedPart(Manuscript manuscript, ChatMessage part, Runnable onChanged) {
        try {
            if (manuscript == null || part == null) {
                return;
            }
            BookSettings settings = translationService.settings(manuscript);
            if (!settings.isAuto() || settings.getLanguageId() == null) {
                return;
            }
            runs.start(manuscript, part, (outcome, failure) -> {
                report(outcome, failure);
                if (outcome != null && outcome.status() == TranslationService.Status.TRANSLATED && onChanged != null) {
                    onChanged.run();
                }
            });
        } catch (Exception e) {
            log.warn("Failed to start the translation of a finished part: {}", e.getMessage(), e);
        }
    }

    private void report(Outcome outcome, Throwable failure) {
        if (failure != null) {
            Notification.error(String.format(T.localized(THE_TRANSLATION_FAILED), failure.getMessage()));
            return;
        }
        switch (outcome.status()) {
            case TRANSLATED -> {
                Notification.success(String.format(T.localized(TRANSLATED_SPEECHES), outcome.translated(), outcome.newWords()));
                if (outcome.needsRepair() > 0) {
                    Notification.error(String.format(T.localized(SOME_WORDS_NOT_IN_THE_LEXICON), outcome.needsRepair()));
                }
            }
            case NOTHING_TO_TRANSLATE -> Notification.success(T.localized(NO_SPEECH_IN_THE_LANGUAGE));
            case NO_LANGUAGE -> Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
            case NO_MODEL -> Notification.error(T.localized(THE_BOOK_HAS_NO_MODEL));
            case EMPTY -> Notification.error(T.localized(THE_PART_HAS_NO_TEXT));
            case BAD_ANSWER -> Notification.error(T.localized(THE_MODEL_ANSWERED_WRONG));
        }
    }

    private void dropRegenerated(GenerationControllerEvent event, EventChain chain) {
        try {
            GenerationRequest request = event.getRequest();
            if (request != null && request.getRequestType() == GenerationRequestType.REGENERATE && request.getNode() != null
                    && event.getManuscript() != null && producedNewText(event)) {
                for (AnchoredPassage passage : passageService.listForParts(java.util.List.of(request.getNode().getId()), Span.KIND)) {
                    passageService.remove(event.getManuscript(), passage);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to remove the passages of a regenerated part: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    /** A regenerate that was stopped or failed before any text came leaves the old text: its passages stay. */
    private static boolean producedNewText(GenerationControllerEvent event) {
        if (event.getProperty(GenerationProperties.OUTCOME) == GenerationOutcome.COMPLETED) {
            return true;
        }
        ChatMessage original = event.getProperty(GenerationProperties.ORIGINAL_MESSAGE);
        ChatMessage now = event.getMessage();
        String text = now == null ? null : now.getResponse();
        return text != null && !text.isBlank() && (original == null || !text.equals(original.getResponse()));
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        return entryPoint instanceof Main || entryPoint instanceof Viewer ? ConlangStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        passageKinds.unregister(kind);
        partTexts.unregisterAll(owner);
        inheritedSettingRegistry.unregisterAll(owner);
        menuRegistry.unregisterAll(owner);
        if (partFinishedDecorator != null) {
            extensionService.unregisterDecorator(partFinishedDecorator);
            partFinishedDecorator = null;
        }
        if (cleanupRegistration != null) {
            cleanupRegistration.unregister();
            cleanupRegistration = null;
        }
        if (userPartDecorator != null) {
            extensionService.unregisterDecorator(userPartDecorator);
            userPartDecorator = null;
        }
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
            storyPartDecorator = null;
        }
        // the tab docks belong to the UIs of all users: remove the tab in each one's own session
        synchronized (activeTabDocks) {
            for (TabDock dock : activeTabDocks) {
                UIUtils.accessComponent(dock, () -> {
                    Component view = (Component) ComponentUtil.getData(dock, BOOK_VIEW_KEY);
                    if (view != null) {
                        dock.remove(view);
                        ComponentUtil.setData(dock, BOOK_VIEW_KEY, null);
                    }
                });
            }
            activeTabDocks.clear();
        }
        if (viewerDecorator != null) {
            extensionService.unregisterDecorator(viewerDecorator);
            viewerDecorator = null;
        }
        readerModes.clear();
        if (exportFieldsDecorator != null) {
            extensionService.unregisterDecorator(exportFieldsDecorator);
            exportFieldsDecorator = null;
        }
        if (exportDecorator != null) {
            extensionService.unregisterDecorator(exportDecorator);
            exportDecorator = null;
        }
        exportChoices.clear();
        if (userPartSaveDecorator != null) {
            extensionService.unregisterDecorator(userPartSaveDecorator);
            userPartSaveDecorator = null;
        }
        // the accordions belong to the UIs of all users: remove the panel in each one's own session
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
        log.info("Conlang Translator unloaded");
    }
}
