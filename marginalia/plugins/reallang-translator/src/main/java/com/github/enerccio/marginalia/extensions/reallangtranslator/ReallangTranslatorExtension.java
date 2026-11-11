package com.github.enerccio.marginalia.extensions.reallangtranslator;

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
import com.github.enerccio.marginalia.extensions.*;
import com.github.enerccio.marginalia.extensions.reallangtranslator.commands.ReallangCommands;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.*;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ui.ExtractionTools;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ui.LanguageSettingsForm;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ui.ReallangBookView;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ExportMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ReaderMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingSettings;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechExportContributor;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechPassageKind;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechTransformer;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.UnderstandingBlock;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.*;
import com.github.enerccio.marginalia.ui.components.PartView;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.main.Viewer;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuKeys;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
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
import java.util.concurrent.CancellationException;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * Translates the direct speech of the characters into a real language (Japanese, for a story set in Japan) while the story keeps the English behind
 * it, like the Conlang Translator does for an invented one; what the two share is the library {@code translator-common}, embedded in both bundles. The
 * story holds the translation; every path that gives the story to a model reads the English back (the transformer in the {@link PartTextRegistry}); the
 * plugin keeps each translated speech as a passage of its own kind. See TODO.md for the plan and what is left.
 * <p>
 * What is hooked: the passage kind and the transformer, the settings (the panel in the Settings screen and the inherited fields of books and folders),
 * the <b>Translate the speech</b> action of the menu of a part, <b>Translate the selection</b> in the menu of the text, <b>Translate parts</b> in the
 * tools menu, the tab of the story (the speakers and the quick translation), the viewer of a published book, the field of the export dialog, the
 * automatic run when a generated part is final (after the automatic annotation of the book, if it has it), the removal of the passages of a part that is
 * generated again, and the commands.
 */
@Configurable
public class ReallangTranslatorExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String MENU_ITEM_KEY = "reallangtranslator.translate";
    private static final String SELECTION_ITEM_KEY = "reallangtranslator.selection";
    private static final String RANGE_ITEM_KEY = "reallangtranslator.range";
    private static final String BOOK_TAB_ID = "reallang-translator";
    private static final String BOOK_VIEW_KEY = "com.github.enerccio.marginalia.extensions.reallangtranslator.bookView";
    private static final String SETTINGS_PANEL_KEY = "com.github.enerccio.marginalia.extensions.reallangtranslator.settingsPanel";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private PassageKinds passageKinds;
    @Autowired
    private PartTextRegistry partTexts;
    @Autowired
    private ExportTextRegistry exportTexts;
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
    @Autowired
    private CommandService commandService;
    @Autowired
    private AnnotationFactsRegistry annotationFacts;

    private final LanguageLibrary library = new LanguageLibrary();
    private final GlossaryService glossaryService = new GlossaryService(library);
    private final TranslationService translationService = new TranslationService(glossaryService);
    private final BookReal bookReal = new BookReal(translationService, glossaryService);
    private final ExtractionService extractionService = new ExtractionService(translationService, glossaryService, library);
    private final PassageAccess passageAccess = new PassageAccess(ReallangKind.ID);
    private final SpeechPassageKind kind = new SpeechPassageKind(ReallangKind.ID, ReallangKind.MARK);
    private final SpeechTransformer transformer = new SpeechTransformer(passageAccess);
    private final List<Object> commandSuites = new ArrayList<>();

    private TranslationRuns runs;
    private RangeRuns rangeRuns;
    private ExtractionRuns extractionRuns;
    private ExtensionDecorator partFinishedDecorator;
    private Registration cleanupRegistration;
    private UnderstandingPrompt understandingPrompt;
    private ExtensionDecorator userPartDecorator;
    private ExtensionDecorator userPartSaveDecorator;
    private ExtensionDecorator viewerDecorator;
    private ExtensionDecorator storyPartDecorator;
    private ExtensionDecorator exportFieldsDecorator;
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
        PluginLog.set(log);
        owner = bundle.getSymbolicName();
        runs = new TranslationRuns(translationService, log);
        rangeRuns = new RangeRuns(this::rangeWork, "reallang-translator-range", log);
        extractionRuns = new ExtractionRuns(extractionService, log);
        try {
            passageKinds.register(kind);
            partTexts.register(owner, transformer);

            // what books and folders choose: their dialogs get the fields, the user level is the panel in the Settings screen
            RealInherited.register(inheritedSettingRegistry, owner, library::defaultsOrBlank, library::languageChoices, library::aiChoices,
                    library::protocolChoices);
            registerSettingsPanel(extensionService);

            menuRegistry.register(owner, StoryMenuItem.card(MENU_ITEM_KEY, l -> T.localized(TRANSLATE_DIALOGS_REAL),
                            (story, card) -> translateByHand(story, card))
                    .icon(Solid.LANGUAGE::create).path(StoryMenuKeys.GROUP_TRANSLATE).visibleWhen((story, card) -> hasLanguage(story)));
            // the right click on a selected sentence (the menu of the text): the author's own sentence in the language
            menuRegistry.register(owner, StoryMenuItem.text(SELECTION_ITEM_KEY, l -> T.localized(TRANSLATE_SELECTED_TEXT_REAL),
                            (story, card) -> translateSelection(story, card))
                    .icon(Solid.LANGUAGE::create).path(StoryMenuKeys.GROUP_TRANSLATE).visibleWhen((story, card) -> hasLanguage(story)));
            menuRegistry.register(owner, StoryMenuItem.tools(RANGE_ITEM_KEY, l -> T.localized(TRANSLATE_PARTS_REAL), this::openRange)
                    .icon(Solid.LAYER_GROUP::create).visibleWhen((story, card) -> hasLanguage(story)));

            registerCommands();
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

            // who does not follow the language (the levels in the notes of the speakers): the story model gets a message, the reviewer of a part the facts
            understandingPrompt = new UnderstandingPrompt(new UnderstandingPrompt.Source() {
                @Override
                public List<UnderstandingBlock.Entry> entries(Manuscript book) throws Exception {
                    return bookReal.understandingEntries(book);
                }

                @Override
                public UnderstandingSettings settings(Manuscript book) throws Exception {
                    return bookReal.understanding(book);
                }
            }, BookSettings.KEY + ".understanding", log);
            understandingPrompt.register(storyGenerationService);
            annotationFacts.register(owner, understandingPrompt.factsProvider());
            log.info("Real Language Translator loaded");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Real Language Translator extension", e);
        }
    }

    /**
     * The commands: everything the dialogs do, for the command bar, for agents and for templates. They call the services the dialogs call.
     */
    private void registerCommands() throws Exception {
        commandSuites.add(new ReallangCommands(translationService, bookReal, library, extractionService));
        for (Object suite : commandSuites) {
            commandService.registerCommands(suite);
        }
    }

    /**
     * The <b>Real language</b> tab of the story (the left dock, the drawer on a phone): who speaks the language in this book and the quick translation.
     * It is added once per story part and reads the language when it is built.
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
                ReallangBookView view = new ReallangBookView(currentManuscript, bookReal,
                        new ExtractionTools(extractionRuns, () -> rangeParts(currentManuscript), library::extractionPrompts));
                leftBar.add(BOOK_TAB_ID, T.localized(REAL_LANGUAGE), ReallangIcon.TAB, view);
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
     * The published book: the parts the viewer builds show the translated speech the way the book asks for (<i>translation with a click</i>,
     * <i>translation only</i>, <i>English only</i>). The reader is not the author, so nothing of the author's own settings is read. A part with the
     * speech of both translator plugins is drawn once, for both (see {@link ReaderView}).
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
                if (!passageService.listForParts(List.of(part.getId()), ReallangKind.ID).isEmpty()) {
                    ReaderView.apply(view, part, readerMode(instrumented, context), TranslatedPassages.of(passageService, passageKinds, part.getId()),
                            passage -> TranslatedPassages.markClass(passageKinds, passage, ReallangKind.MARK));
                }
            }
        };
        extensionService.registerDecorator(viewerDecorator, "com.github.enerccio.marginalia.ui.main.Viewer", "createPart");
    }

    /**
     * The export dialog: a field for what the export holds for the translated speech (only for a book that has some), and what the contributor
     * answers for the registry of the core. The exporters are not touched: they get another text.
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
                if (book == null || passageService.listForManuscript(book, ReallangKind.ID).isEmpty()) {
                    return;
                }
                Select<ExportMode> choice = ExportChoice.field(T.localized(TRANSLATED_SPEECH_REAL), ReallangTranslatorExtension::exportLabel);
                exportChoices.put(instrumented, choice);
                context.getCurrentReturn(List.class).add(choice);
            }
        };
        extensionService.registerDecorator(exportFieldsDecorator, "com.github.enerccio.marginalia.ui.dialogs.ExportDialog", "createExtensionFields");

        // what the export changes in the text of the parts is the core's registry's to put together (all plugins at once, on the stored text)
        exportTexts.register(owner, new SpeechExportContributor(passageAccess, session -> {
            Select<ExportMode> choice = exportChoices.get(session.source());
            return choice == null ? null : choice.getValue();
        }));
    }

    private static String exportLabel(ExportMode mode) {
        return switch (mode) {
            case TRANSLATED -> T.localized(THE_TRANSLATION_AS_IT_IS);
            case SOURCE -> T.localized(ENGLISH_ONLY);
            case TRANSLATED_NOTES -> T.localized(THE_TRANSLATION_THE_ENGLISH_IN_FOOTNOTES);
            case SOURCE_NOTES -> T.localized(THE_ENGLISH_THE_TRANSLATION_IN_FOOTNOTES);
        };
    }

    /**
     * What a range run does to one part: translates it, or puts the English back.
     */
    private RangeLoop.PartResult rangeWork(Manuscript book, RangeLoop.Mode mode, ChatMessage part, String languageId) throws Exception {
        if (mode == RangeLoop.Mode.REVERT) {
            TranslationWriter.Reverted reverted = translationService.revert(book, part, languageId);
            return reverted.reverted() == 0 ? RangeLoop.PartResult.nothing() : RangeLoop.PartResult.done(reverted.reverted(), 0, reverted.kept());
        }
        Outcome outcome = translationService.translate(book, part, languageId);
        return switch (outcome.status()) {
            case TRANSLATED -> RangeLoop.PartResult.done(outcome.translated(), 0, 0);
            case NOTHING_TO_TRANSLATE, EMPTY -> RangeLoop.PartResult.nothing();
            case NO_LANGUAGE, NO_MODEL -> RangeLoop.PartResult.abort(outcome.note());
            case BAD_ANSWER, OVERLAPS, CHANGED -> RangeLoop.PartResult.failed(outcome.note());
        };
    }

    /**
     * How the book of the viewer shows the speech: its own choice, else the one of a folder, else the click. The book is found as the viewer found it,
     * so a book the reader may not see has no mode (the default).
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
            mode = ReaderMode.of(manuscriptService.resolveExtension(book, RealInherited.READER).value());
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
                    panel = extensionSettings.add(T.localized(REAL_LANGUAGE_TRANSLATOR), new LanguageSettingsForm(library));
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
     * The dialog of the actions on a range of the parts of the active branch: the parts that have text, counted from 1, each with whether it has a
     * translated speech.
     */
    private void openRange(StoryEditor story) {
        try {
            Manuscript book = story.getCurrentManuscript() == null ? null : manuscriptService.find(story.getCurrentManuscript());
            if (book == null || translationService.languageOf(translationService.settings(book)) == null) {
                Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
                return;
            }
            new RangeDialog(book, bookReal.languages(book), () -> rangeParts(book), rangeRuns, story::reloadStory).open();
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
                Set<String> languages = new HashSet<>();
                translated.getOrDefault(part.getId(), List.of()).forEach(located -> languages.add(located.span().getLanguage()));
                parts.add(new RangeLoop.RangePart(i + 1, part, translated.containsKey(part.getId()), languages));
            }
            return parts;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * The action of the menu of a part: translates its speech on a worker thread and shows what came of it.
     */
    private void translateByHand(StoryEditor story, StoryCard card) {
        Manuscript manuscript = story.getCurrentManuscript();
        if (manuscript == null || card == null || card.getMessage() == null) {
            return;
        }
        List<LanguageRef> languages = languagesOf(manuscript);
        // a book with several languages asks which one (or all of them)
        if (languages.isEmpty()) {
            // the translation tells what is missing
            translatePart(story, manuscript, card, null);
            return;
        }
        LanguageChoiceDialog.ask(languages, true, languageId -> translatePart(story, manuscript, card, languageId));
    }

    private void translatePart(StoryEditor story, Manuscript manuscript, StoryCard card, String languageId) {
        ChatMessage part = card.getMessage();
        // the window opens when a request goes to the model: the thinking, the answer and a Stop button
        RunWindow window = new RunWindow(T.localized(TRANSLATING_THE_SPEECH));
        boolean started = runs.start(manuscript, part, languageId, window.progress(), (outcome, failure) -> {
            window.finish();
            report(outcome, failure);
            // a stopped run may have written the languages before it
            if (outcome != null && outcome.status() == TranslationService.Status.TRANSLATED || failure instanceof CancellationException) {
                // the story is drawn again, so the marks of the new passages are there (it keeps the passages it read before)
                story.reloadStory();
            }
        });
        if (!started) {
            Notification.error(T.localized(THE_PART_IS_BEING_TRANSLATED));
        }
    }

    /** Whether the book the story shows has a language of this plugin, the translation is of no use without one. */
    private boolean hasLanguage(StoryEditor story) {
        Manuscript manuscript = story == null ? null : story.getCurrentManuscript();
        return manuscript != null && !languagesOf(manuscript).isEmpty();
    }

    /** The languages of the book that are in the library; none when they can't be read. */
    private List<LanguageRef> languagesOf(Manuscript manuscript) {
        try {
            return bookReal.languages(manuscript);
        } catch (Exception e) {
            log.warn("Failed to read the languages of the book: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * The item of the menu of the text: translates the sentence the author selected on a worker thread, and shows the part again.
     */
    private void translateSelection(StoryEditor story, StoryCard card) {
        Manuscript manuscript = story.getCurrentManuscript();
        if (manuscript == null || card == null || card.getMessage() == null) {
            return;
        }
        List<LanguageRef> languages = languagesOf(manuscript);
        if (languages.isEmpty()) {
            Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
            return;
        }
        card.withSelection(range -> LanguageChoiceDialog.ask(languages, false, languageId -> {
            ChatMessage part = card.getMessage();
            String expected = part.getResponse();
            RunWindow window = new RunWindow(T.localized(TRANSLATING_THE_SELECTION));
            boolean started = runs.startSelection(manuscript, part, expected, range[0], range[1], languageId, window.progress(), (outcome, failure) -> {
                window.finish();
                report(outcome, failure);
                if (outcome != null && outcome.status() == TranslationService.Status.TRANSLATED) {
                    story.reloadStory();
                }
            });
            if (!started) {
                Notification.error(T.localized(THE_PART_IS_BEING_TRANSLATED));
            }
        }));
    }

    /**
     * The new part is final: when the book translates by itself, a run starts on a worker thread, and the story is loaded again when it is over.
     * Otherwise nothing happens. A failure never reaches the story.
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
            // a model with one slot has nothing else to do meanwhile: the window blocks the page and shows the work, with a Stop button
            RunWindow window = new RunWindow(T.localized(TRANSLATING_THE_SPEECH));
            runs.start(manuscript, part, null, window.progress(), (outcome, failure) -> {
                window.finish();
                report(outcome, failure);
                boolean written = outcome != null && outcome.status() == TranslationService.Status.TRANSLATED || failure instanceof CancellationException;
                if (written && onChanged != null) {
                    onChanged.run();
                }
            });
        } catch (Exception e) {
            log.warn("Failed to start the translation of a finished part: {}", e.getMessage(), e);
        }
    }

    private void report(Outcome outcome, Throwable failure) {
        if (failure instanceof CancellationException) {
            Notification.success(T.localized(THE_TRANSLATION_WAS_STOPPED));
            return;
        }
        if (failure != null) {
            Notification.error(String.format(T.localized(THE_TRANSLATION_FAILED), failure.getMessage()));
            return;
        }
        switch (outcome.status()) {
            case TRANSLATED -> {
                Notification.success(String.format(T.localized(TRANSLATED_D_SPEECHES), outcome.translated()));
                if (outcome.wrongScript() > 0) {
                    Notification.error(String.format(T.localized(D_TRANSLATIONS_ARE_NOT_IN_THE_SCRIPT_OF_THE_LANGUAGE), outcome.wrongScript()));
                }
                if (outcome.glossaryMisses() > 0) {
                    Notification.error(String.format(T.localized(D_TRANSLATIONS_WRITE_A_NAME_OF_THE_GLOSSARY_ANOTHER_WAY), outcome.glossaryMisses()));
                }
                if (outcome.proposed() > 0) {
                    Notification.success(String.format(T.localized(D_NAMES_WERE_ADDED_TO_THE_GLOSSARY_TO_LOOK_AT), outcome.proposed()));
                }
            }
            case NOTHING_TO_TRANSLATE -> Notification.success(T.localized(NO_SPEECH_IN_THE_LANGUAGE));
            case NO_LANGUAGE -> Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
            case NO_MODEL -> Notification.error(T.localized(THE_BOOK_HAS_NO_MODEL));
            case EMPTY -> Notification.error(T.localized(THE_PART_HAS_NO_TEXT));
            case BAD_ANSWER -> Notification.error(T.localized(THE_MODEL_ANSWERED_WRONG));
            case OVERLAPS -> Notification.error(T.localized(THE_SELECTION_HOLDS_A_TRANSLATED_SPEECH));
            case CHANGED -> Notification.error(T.localized(THE_PART_WAS_CHANGED_MEANWHILE));
        }
    }

    private void dropRegenerated(GenerationControllerEvent event, EventChain chain) {
        try {
            GenerationRequest request = event.getRequest();
            if (request != null && request.getRequestType() == GenerationRequestType.REGENERATE && request.getNode() != null
                    && event.getManuscript() != null && producedNewText(event)) {
                for (AnchoredPassage passage : passageService.listForParts(List.of(request.getNode().getId()), ReallangKind.ID)) {
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
        return entryPoint instanceof Main || entryPoint instanceof Viewer ? CommonStyles.css() + "\n" + ReallangStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        for (Object suite : commandSuites) {
            commandService.unregisterCommands(suite);
        }
        commandSuites.clear();
        passageKinds.unregister(kind);
        partTexts.unregisterAll(owner);
        annotationFacts.unregisterAll(owner);
        if (understandingPrompt != null) {
            understandingPrompt.unregister();
            understandingPrompt = null;
        }
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
        exportTexts.unregisterAll(owner);
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
        log.info("Real Language Translator unloaded");
    }
}
