package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.components.CommandBar;
import com.github.enerccio.marginalia.ui.components.CommandResultView;
import com.github.enerccio.marginalia.ui.components.LorebookPreview;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.dialogs.search.ManuscriptSearchReplaceDialog;
import com.github.enerccio.marginalia.ui.dialogs.story.*;
import com.github.enerccio.marginalia.ui.menu.*;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutAction;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutActions;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutRegistry;
import com.github.enerccio.marginalia.ui.widgets.LazyContextMenu;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.GeneratedTextHighlighter;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import com.github.enerccio.marginalia.utils.TextMarkup;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.dom.Element;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.*;

@Configurable
@Extendable
public class ManuscriptStoryPart implements ManuscriptDialogPart, StoryEditor {

    /**
     * Key of the {@link Manuscript#getAttributes() attributes} of the manuscript that holds the layout of the tabs of the
     * story editor ({@link TabDock#getLayout()}): which tabs are together in which sheet and column, and how big they are.
     */
    public static final String TABS_CONFIG_ATTRIBUTE = "$desktopManuscriptStoryPartTabsConfig";

    /**
     * Key of the {@link Manuscript#getAttributes() attributes} of the manuscript that held the position of the splitter
     * between the sidebar and the story (percent of the width the sidebar has) while the story was not a tab of the dock.
     * Only read now, to turn the old layout into one with the story tab (see {@link StoryDockLayout#migrate}); the next
     * save of the layout removes it.
     */
    public static final String SPLIT_POSITION_ATTRIBUTE = "$desktopManuscriptStoryPartSplitPosition";

    /**
     * Id of the story (the cards and the controls under them) in the layout of the tabs.
     */
    public static final String STORY_TAB = "story";

    /**
     * Id of the outline tab in the layout of the tabs; extensions give their tabs ids of their own.
     */
    public static final String CHAPTERS_TAB = "chapters";

    /**
     * Id of the read only lorebook preview tab in the layout of the tabs.
     */
    public static final String LOREBOOK_TAB = "lorebook";

    /**
     * Id of the tab with the annotations of the book in the layout of the tabs.
     */
    public static final String ANNOTATIONS_TAB = "annotations";

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private StoryMenuRegistry menuRegistry;

    private final ManuscriptDialog parent;
    private CancellationToken activeGenerationToken;

    private VerticalLayout centerLayout;
    private ScrollPanel centerContentPanel;
    private HorizontalLayout bottomControlsLayout;
    private TabDock leftBar;
    private Span sidebarHeader;
    private VerticalLayout sidebarList;
    private LorebookPreview lorebookPreview;

    private MenuBar menuBar;
    private CommandBar commandBar;
    private CommandResultView commandResultView;
    private MenuItem summariesMenuItem;
    private MenuItem cogsMenuItem;
    private BuiltMenu toolsMenu;
    private Button actionButton;

    private Popover newTurnPopover;
    private NewTurnForm newTurnForm;
    // the keys of the user for the shortcuts, null while the shortcuts are off (not the desktop app)
    private KeyBindings keyBindings;

    private Manuscript currentManuscript;
    private boolean isFrozen = false;
    // diff view, not stored: the response of the cards is shown with the generated text highlighted
    private boolean showGeneratedDiff = false;
    // the notes of the annotations are in a column beside the text of the card instead of in a popover (a setting of the user)
    private boolean annotationsInMargin = false;

    private final StoryCards<ChatMessageCard> cards = new StoryCards<>();
    private final StorySupport support = new StorySupport();
    private StoryLazyLoader<ChatMessageCard> lazyLoader;
    private StoryBranchSearch branchSearch;
    private StoryAnnotations annotations;
    private AnnotationsPanel annotationsPanel;
    private final StoryBackup backup = new StoryBackup();
    private Long scrollToMessageId;

    public ManuscriptStoryPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "container", type = VTabSheet.class)})
    @Override
    public Component create(VTabSheet container) throws Exception {
        // the menus show the keys of the user
        loadKeyBindings();
        // created once: a redraw of the story only refills the outline, so the selected tab (and the tabs extensions add) survive it
        leftBar = new TabDock();
        leftBar.setSizeFull();
        leftBar.setOnLayoutChange(this::saveTabsConfig);
        leftBar.setOnStateChange(() -> {
            if (parent != null) {
                parent.stateChanged();
            }
        });
        // the default arrangement from the start: the tabs added below find their place in it
        leftBar.setLayout(StoryDockLayout.defaultLayout(STORY_TAB, null, CHAPTERS_TAB, LOREBOOK_TAB, ANNOTATIONS_TAB));
        createStoryOutlineTab();
        createLorebookTab();
        createAnnotationsTab();

        centerLayout = new VerticalLayout();
        centerLayout.setSizeFull();
        centerLayout.setPadding(false);
        centerLayout.setSpacing(false);

        centerContentPanel = new ScrollPanel();
        centerContentPanel.setSizeFull();
        centerContentPanel.addClassName(CssStyles.STORY_SCROLL_PANEL);

        lazyLoader = new StoryLazyLoader<>(centerContentPanel, cards, ChatMessageCard::new, () -> isFrozen, loc);

        bottomControlsLayout = buildBottomControls();

        centerLayout.add(centerContentPanel, bottomControlsLayout);
        centerLayout.setFlexGrow(1, centerContentPanel);
        centerLayout.setFlexGrow(0, bottomControlsLayout);

        // the story is a tab like the others, the user puts it where they want it
        leftBar.add(STORY_TAB, loc.getValue(L.LABEL_STORY_PART), centerLayout);
        // the one tab that stays on the screen when all the others are minimized
        leftBar.setMinimizable(STORY_TAB, false);
        // a tab sheet drops the content of the tabs it does not show from the page: the browser builds the story again
        // when its tab is shown, without the scripts that watch the edges
        centerLayout.addAttachListener(event -> lazyLoader.reattached());

        container.add(loc.getValue(L.LABEL_STORY_PART), leftBar);
        installShortcuts();
        return leftBar;
    }

    /**
     * Keyboard shortcuts of the story (the phone twin has none): the browser script fires an event on the story when the user
     * presses the key of an action, {@link #handleShortcut} does it.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    private void installShortcuts() {
        JavascriptLibrary.fireShortcutEvents(leftBar.getElement(), keyBindings.toJson());
        leftBar.getElement().addEventListener("marginalia-shortcut",
                event -> handleShortcut(event.getEventData().get("event.detail").asString())).addEventData("event.detail");
        newTurnForm.setGenerateShortcut(keyBindings.get(ShortcutActions.STORY_GENERATE));
        refreshActionTooltip();
    }

    /**
     * Reads the keys of the user again, they may have changed them in the settings since.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void loadKeyBindings() {
        try {
            keyBindings = KeyBindings.of(settingService.getOrCreate(UserSetting.class));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            keyBindings = KeyBindings.of(null);
        }
    }

    /**
     * The tooltip of the round button names what it does now and its key.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "key", type = String.class),
                    @APILocals(name = "label", type = String.class)})
    private void refreshActionTooltip() {
        if (keyBindings == null || actionButton == null) {
            return;
        }
        String key = keyBindings.get(isFrozen ? ShortcutActions.STORY_STOP : ShortcutActions.STORY_NEW_TURN);
        String label = loc.getValue(isFrozen ? L.LABEL_STOP : L.LABEL_OPEN_NEW_TURN_FORM);
        actionButton.setTooltipText(key == null ? label : label + " (" + key + ")");
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "actionId", type = String.class)},
            locals = {@APILocals(name = "item", type = StoryMenuItem.class),
                    @APILocals(name = "action", type = ShortcutAction.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void handleShortcut(String actionId) {
        if (isFrozen) {
            if (ShortcutActions.STORY_STOP.equals(actionId) && activeGenerationToken != null) {
                activeGenerationToken.cancel();
            }
            return;
        }
        switch (actionId) {
            case ShortcutActions.STORY_NEW_TURN -> {
                if (currentManuscript != null) {
                    newTurnPopover.setOpened(true);
                }
            }
            case ShortcutActions.STORY_GENERATE -> {
                if (newTurnPopover.isOpened()) {
                    generateFromNewTurnForm();
                }
            }
            case ShortcutActions.STORY_SAVE_EDIT -> cards.values().stream().filter(ChatMessageCard::isEditing)
                    .forEach(ChatMessageCard::autosaveAndSwapToMarkdown);
            case ShortcutActions.STORY_COMMAND_BAR -> {
                leftBar.select(STORY_TAB);
                commandBar.focusInput();
            }
            case ShortcutActions.STORY_SEARCH -> {
                leftBar.select(CHAPTERS_TAB);
                branchSearch.focusField();
            }
            case ShortcutActions.DOCK_NEXT_TAB -> leftBar.selectRelative(1);
            case ShortcutActions.DOCK_PREVIOUS_TAB -> leftBar.selectRelative(-1);
            default -> {
                StoryMenuItem item = menuRegistry.findByAction(actionId);
                if (item != null) {
                    runMenuItem(item);
                    return;
                }
                ShortcutAction action = ShortcutRegistry.get(actionId);
                if (action != null && action.getHandler() != null) {
                    try {
                        action.getHandler().accept(leftBar);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                }
            }
        }
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "item", type = StoryMenuItem.class)},
            locals = {@APILocals(name = "editing", type = ChatMessageCard.class),
                    @APILocals(name = "candidates", type = List.class)})
    private void runMenuItem(StoryMenuItem item) {
        if (item.getKind() == MenuKind.TOOLS) {
            if (item.isVisible(this, null) && item.isEnabled(this, null)) {
                item.run(this, null);
                toolsMenu.refresh();
            }
            return;
        }
        // the part that is edited, else the part that holds the selection, else the last part
        ChatMessageCard editing = cards.values().stream().filter(ChatMessageCard::isEditing).findFirst().orElse(null);
        if (editing != null) {
            runOnCard(item, editing);
            return;
        }
        List<ChatMessageCard> candidates = new ArrayList<>(cards.values());
        JavascriptLibrary.selectionOwner(leftBar.getElement(),
                candidates.stream().map(ChatMessageCard::getResponseElement).toList()).then(Integer.class, index ->
                runOnCard(item, index != null && index >= 0 && index < candidates.size() ? candidates.get(index) : null));
    }

    /**
     * Runs the item on the part, or on the last part when the item does not apply to it (a swipe of a part that is not the last).
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "item", type = StoryMenuItem.class),
                    @APIParameter(name = "target", type = ChatMessageCard.class)},
            locals = {@APILocals(name = "last", type = ChatMessageCard.class),
                    @APILocals(name = "card", type = ChatMessageCard.class)})
    private void runOnCard(StoryMenuItem item, ChatMessageCard target) {
        ChatMessageCard last = cards.size() == 0 ? null : cards.last();
        ChatMessageCard card = target != null && item.isVisible(this, target) && item.isEnabled(this, target) ? target : last;
        if (card != null && item.isVisible(this, card) && item.isEnabled(this, card)) {
            item.run(this, card);
        }
    }
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            locals = {@APILocals(name = "layout", type = HorizontalLayout.class)})
    private HorizontalLayout buildBottomControls() throws Exception {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.addClassNames(CssStyles.PADDING_COMPACT, CssStyles.BORDER_TOP_SUBTLE);

        menuBar = createMenuBar();
        applyBookStyles();

        actionButton = new Button(Solid.PLUS.create());
        actionButton.setThemeName("primary icon small");

        newTurnPopover = buildNewTurnPopover(actionButton);

        actionButton.addClickListener(event -> {
            if (this.isFrozen && parent != null) {
                if (activeGenerationToken != null) {
                    activeGenerationToken.cancel();
                } else {
                    parent.unfreeze();
                }
            }
        });

        layout.add(menuBar);
        layout.setFlexGrow(1, menuBar);
        layout.add(createCommandBox());
        layout.add(actionButton);

        return layout;
    }

    /**
     * The command line next to the menu. The story tab is the place that shows the results: the bar only hands them to
     * its result target, here a popover above the bar (like the help of the bar) that moves nothing in the layout.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class))
    private Component createCommandBox() {
        commandBar = new CommandBar();
        commandBar.addClassName(CssStyles.COMMAND_BOX);
        commandResultView = new CommandResultView();
        commandResultView.create(commandBar);
        commandBar.setResultTarget(commandResultView);
        return commandBar;
    }

    /**
     * Menu bar of the story: the summaries item and the settings (cogs) item. Extensions add to it by decorating the
     * methods that fill it (their argument {@code bar} is the menu bar): {@link #populateMenuBar} for new items of the
     * bar, {@link #createCogsMenuItem} (local variable {@code cogs}) for new items of the settings menu.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = MenuBar.class),
            locals = {@APILocals(name = "bar", type = MenuBar.class)})
    private MenuBar createMenuBar() {
        MenuBar bar = new MenuBar();
        summariesMenuItem = createSummariesMenuItem(bar);
        cogsMenuItem = createCogsMenuItem(bar);
        populateMenuBar(bar);
        return bar;
    }

    /**
     * Nothing to do here, runs after the application's items were added: the hook for extensions that add their own
     * items to the menu bar.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "bar", type = MenuBar.class)})
    private void populateMenuBar(MenuBar bar) {
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "bar", type = MenuBar.class)},
            locals = {@APILocals(name = "item", type = MenuItem.class)})
    private MenuItem createSummariesMenuItem(MenuBar bar) {
        MenuItem item = bar.addItem(Solid.LIST_UL.create(), event -> openSummariesDialog());
        UIUtils.addTooltip(item, loc.getValue(L.LABEL_SUMMARIES));
        return item;
    }

    /**
     * The story settings menu, filled from the {@link StoryMenuRegistry} (kind {@link MenuKind#TOOLS}); extensions that decorate this
     * method add their items to its sub menu ({@code cogs.getSubMenu()}).
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "bar", type = MenuBar.class)},
            locals = {@APILocals(name = "cogs", type = MenuItem.class),
                    @APILocals(name = "e", type = Exception.class)})
    private MenuItem createCogsMenuItem(MenuBar bar) {
        MenuItem cogs = bar.addItem(Solid.COGS.create());
        try {
            annotationsInMargin = settingService.getOrCreate(UserSetting.class).isAnnotationsInMargin();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        toolsMenu = menuDefinition(MenuKind.TOOLS).build(cogs.getSubMenu(), loc, this, () -> null);
        return cogs;
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = Manuscript.class))
    @Override
    public Manuscript getCurrentManuscript() {
        return currentManuscript;
    }

    /**
     * The menu of the kind with the keys the user has.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = MenuDefinition.class),
            parameters = {@APIParameter(name = "kind", type = MenuKind.class)})
    private MenuDefinition menuDefinition(MenuKind kind) {
        return menuRegistry.definition(kind, false, keyBindings);
    }

    /**
     * Turns the styles of the book on or off for the story.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void toggleBookStyles() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            manuscript.setShowBookStyles(!manuscript.getShowBookStyles());
            manuscriptService.save(manuscript);
            applyBookStyles();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = boolean.class))
    @Override
    public boolean isShowGeneratedDiff() {
        return showGeneratedDiff;
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = boolean.class))
    @Override
    public boolean isAnnotationsInMargin() {
        return annotationsInMargin;
    }

    /**
     * Opens the export of the story.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    @Override
    public void openExportDialog() {
        try {
            new ExportDialog(parent.refreshManuscript()).open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Asks, then puts the tabs back the way a new book has them, see {@link #restoreDefaultLayout}.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void confirmRestoreDefaultLayout() {
        ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_RESTORE_DEFAULT_LAYOUT), this::restoreDefaultLayout);
    }

    /**
     * Moves the notes of the annotations of every card into the margin or back into the popover, and remembers the choice for the user.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "inMargin", type = boolean.class)},
            locals = {@APILocals(name = "setting", type = UserSetting.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "card", type = ManuscriptStoryPart.ChatMessageCard.class)})
    @Override
    public void setAnnotationsInMargin(boolean inMargin) {
        annotationsInMargin = inMargin;
        try {
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setAnnotationsInMargin(inMargin);
            settingService.save(setting);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        for (ChatMessageCard card : cards.values()) {
            card.applyAnnotationMargin();
        }
    }

    /**
     * Switches the diff view: the response of every card is drawn again, with the text that is still the LLM's
     * highlighted or without the highlight.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "show", type = boolean.class)},
            locals = {@APILocals(name = "card", type = ManuscriptStoryPart.ChatMessageCard.class)})
    @Override
    public void setShowGeneratedDiff(boolean show) {
        showGeneratedDiff = show;
        for (ChatMessageCard card : cards.values()) {
            card.renderResponse();
        }
    }

    /**
     * Opens the search and replace of the book, the story is shown again when something was replaced.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = ManuscriptSearchReplaceDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void openSearchReplaceDialog() {
        try {
            ManuscriptSearchReplaceDialog dialog = new ManuscriptSearchReplaceDialog(parent.refreshManuscript(), false);
            dialog.onReplaced(() -> {
                try {
                    load(parent.refreshManuscript());
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Asks for a zip archive of {@code .md} files and adds a part for each file to the end of the story, see
     * {@link ArchiveImportService#appendParts}; the story is shown again.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void openArchiveImportDialog() {
        ArchiveUploadDialog.show(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE), loc.getValue(L.MSG_IMPORT_ARCHIVE_TO_BOOK),
                (archive, fileName) -> new ArchiveImportRunner().appendParts(parent.refreshManuscript(), MarkdownArchive.read(archive),
                        imported -> load(parent.refreshManuscript())));
    }

    /**
     * Opens the annotation by a model of a range of the parts of the story ({@link AnnotateDialog#openRange}); the story is shown
     * again when annotations were added.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    @Override
    public void openAnnotateRangeDialog() {
        try {
            AnnotateDialog.openRange(parent.refreshManuscript(), () -> {
                try {
                    load(parent.refreshManuscript());
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Opens the settings of the automatic annotation of the book; the check mark of the menu item follows what was saved.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    @Override
    public void openAutoAnnotateDialog() {
        try {
            AutoAnnotateDialog.open(parent.refreshManuscript(), () -> {
                try {
                    currentManuscript = parent.refreshManuscript();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = boolean.class))
    @Override
    public boolean isAutoAnnotate() {
        return AutoAnnotate.isEnabled(currentManuscript);
    }

    /**
     * Shows the tokens the story of the active branch takes in the next prompt, with and without the summaries.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = BranchTokensDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void openBranchTokensDialog() {
        try {
            BranchTokensDialog dialog = new BranchTokensDialog(parent.refreshManuscript());
            dialog.create();
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = SummariesDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void openSummariesDialog() {
        try {
            SummariesDialog dialog = new SummariesDialog(parent.refreshManuscript());
            dialog.onClosed(this::refreshSummariesMenuItem);
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows how many tokens the summaries generation uses take, as the description of the summaries item.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void refreshSummariesMenuItem() {
        if (summariesMenuItem == null || currentManuscript == null) {
            return;
        }
        try {
            long tokens = summaryService.collectTree(currentManuscript).stream().mapToLong(SummaryNode::getTokens).sum();
            UIUtils.addTooltip(summariesMenuItem, loc.getValue(L.LABEL_SUMMARIES) + "\n"
                    + String.format(loc.getValue(L.MSG_ACTIVE_SUMMARIES_TOKENS), tokens));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class)})
    private void applyBookStyles() throws Exception {
        Manuscript manuscript = parent.refreshManuscript();
        applyLanguage(manuscript);
        if (manuscript.getShowBookStyles()) {
            leftBar.addClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        } else {
            leftBar.removeClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        }
    }

    // lang drives the browser's hyphenation dictionary (see hyphens in shared-styles.css)
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void applyLanguage(Manuscript manuscript) {
        try {
            leftBar.getElement().setAttribute("lang", manuscriptService.getLanguage(manuscript).getCode());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Popover.class),
            parameters = {@APIParameter(name = "targetButton", type = Button.class)},
            locals = {@APILocals(name = "popover", type = Popover.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    private Popover buildNewTurnPopover(Button targetButton) {
        Popover popover = new Popover();
        popover.setTarget(targetButton);

        newTurnForm = new NewTurnForm(loc);
        newTurnForm.setOnChange(() -> {
            if (parent != null) {
                parent.stateChanged();
            }
        });
        newTurnForm.setOnImpersonate(() -> ImpersonateDialog.open(currentManuscript, newTurnForm.toTurnInput(), null, text -> {
            newTurnForm.setInstructions(text);
            popover.setOpened(true);
        }));
        VerticalLayout content = newTurnForm.createContent(this::generateFromNewTurnForm);
        content.setWidth("540px");
        popover.add(content);

        popover.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                newTurnForm.syncIfUntouched(currentManuscript != null ? currentManuscript.getActiveLeaf() : null);
                newTurnForm.focusInstructions();
            } else {
                // the keyboard goes on in the story
                JavascriptLibrary.focusIfFocusLost(targetButton.getElement());
            }
        });

        return popover;
    }

    /**
     * Closes the new turn popover and generates the next part from what is in it.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    private void generateFromNewTurnForm() {
        newTurnPopover.close();
        if (parent != null) {
            newTurnForm.consume();
            parent.stateChanged();
            autosaveAndSwapAllToMarkdown();
            startGeneration();
        }
    }

    /**
     * Redraws the message cards and the chapter outline. The left bar (and so the selected tab) is kept, the state of
     * the cards that is not stored in the message - which reasonings are opened - is carried over to the new cards.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "openedReasonings", type = Set.class),
                    @APILocals(name = "emptyLabel", type = Span.class),
                    @APILocals(name = "branch", type = List.class),
                    @APILocals(name = "msg", type = ChatMessage.class),
                    @APILocals(name = "positions", type = Map.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void renderStoryContent() {
        Set<Long> openedReasonings = cards.collectOpenedReasonings();
        // the texts may have changed since the annotations were read
        annotations.invalidate();

        centerContentPanel.removeAll();
        sidebarList.removeAll();
        sidebarHeader.setText("");
        cards.clear();
        lazyLoader.clear();

        if (currentManuscript == null || currentManuscript.getActiveLeaf() == null) {
            branchSearch.refresh(List.of());
            annotationsPanel.refresh(Map.of());
            Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
            centerContentPanel.add(UIUtils.centerComponent(emptyLabel));
        } else {
            try {
                List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(currentManuscript.getActiveLeaf());
                int total = branch.size();
                sidebarHeader.setText(String.format(loc.getValue(L.LABEL_CHAPTER_OUTLINE_HEADER), total));

                for (int i = 0; i < total; i++) {
                    ChatMessage msg = branch.get(i);
                    createSidebarButton(msg, i + 1, msg.getId());
                }

                branchSearch.refresh(branch);
                Map<Long, Integer> positions = new HashMap<>();
                for (int i = 0; i < total; i++) {
                    positions.put(branch.get(i).getId(), i + 1);
                }
                annotationsPanel.refresh(positions);
                // the cards of the last parts only, the earlier ones are added when the user scrolls up
                lazyLoader.render(branch, support.partsLimit(), scrollToMessageId, openedReasonings);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
            scrollToMessageId = null;
        }
    }

    /**
     * The branch of the story as it is in the database now, what the search of the outline searches.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = List.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class)})
    private List<ChatMessage> currentBranch() throws Exception {
        Manuscript manuscript = parent.refreshManuscript();
        return manuscript == null || manuscript.getActiveLeaf() == null
                ? List.of() : chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf());
    }

    /**
     * The next render of the story (the tab being entered) scrolls to the part instead of to the bottom (the parts
     * before it are loaded), used by the search of the tree.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "messageId", type = Long.class)})
    public void scrollToMessageOnNextRender(Long messageId) {
        this.scrollToMessageId = messageId;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "msg", type = ChatMessage.class),
                    @APIParameter(name = "orderId", type = int.class),
                    @APIParameter(name = "dbId", type = Long.class)},
            locals = {@APILocals(name = "sidebarBtn", type = Button.class)})
    private void createSidebarButton(ChatMessage msg, int orderId, Long dbId) {
        Button sidebarBtn = new Button(String.format(loc.getValue(L.LABEL_CHAPTER_OUTLINE_NODE), orderId, dbId));
        sidebarBtn.setThemeName("tertiary small");
        sidebarBtn.setWidthFull();
        sidebarBtn.addClassName(CssStyles.SIDEBAR_BUTTON);

        sidebarBtn.addClickListener(event -> {
            if (dbId != null) {
                showStory();
                lazyLoader.scrollTo(dbId);
            }
        });
        sidebarList.add(sidebarBtn);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "leftMarginLayout", type = VerticalLayout.class)})
    private void createStoryOutlineTab() {
        VerticalLayout leftMarginLayout = new VerticalLayout();
        leftMarginLayout.setSizeFull();
        leftBar.add(CHAPTERS_TAB, loc.getValue(L.LABEL_CHAPTERS), "Ch", leftMarginLayout);

        sidebarHeader = new Span();
        sidebarHeader.addClassName(CssStyles.SIDEBAR_HEADER);

        sidebarList = new VerticalLayout();
        sidebarList.setPadding(false);
        sidebarList.setSpacing(false);
        sidebarList.setWidthFull();

        branchSearch = new StoryBranchSearch(this::currentBranch,
                ids -> lazyLoader.renderResponses(ids), id -> {
            showStory();
            lazyLoader.showMatch(id, CssStyles.SEARCH_MATCH_CURRENT);
        });

        leftMarginLayout.add(branchSearch, sidebarHeader, sidebarList);
        leftMarginLayout.setFlexGrow(1, sidebarList);
    }

    /**
     * The tab with the read only preview of the lore of the story, it is loaded again every time the story is entered.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    private void createLorebookTab() {
        lorebookPreview = new LorebookPreview().create();
        leftBar.add(LOREBOOK_TAB, loc.getValue(L.LABEL_LOREBOOK), "Lo", lorebookPreview);
    }

    /**
     * The tab with the annotations of the whole book. The annotations are read once for the book and kept until one changes, the
     * cards draw their marks from them.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    private void createAnnotationsTab() {
        annotations = new StoryAnnotations(() -> currentManuscript, ids -> lazyLoader.renderResponses(ids));
        annotationsPanel = new AnnotationsPanel(annotations, new AnnotationView.Host() {
            @Override
            public boolean canGoToPart(Annotation annotation) {
                return true;
            }

            @Override
            public void goToPart(Annotation annotation) {
                showStory();
                lazyLoader.scrollTo(annotation.getPartId());
                ChatMessageCard card = cards.get(annotation.getPartId());
                if (card != null && annotation.hasRange()) {
                    card.flashAnnotation(annotation);
                }
            }

            @Override
            public boolean canAttach(Annotation annotation) {
                return cards.contains(annotation.getPartId());
            }

            @Override
            public void attachToSelection(Annotation annotation) {
                ChatMessageCard card = cards.get(annotation.getPartId());
                if (card != null) {
                    card.attachToSelection(annotation);
                }
            }
        });
        leftBar.add(ANNOTATIONS_TAB, loc.getValue(L.LABEL_ANNOTATIONS), "An", annotationsPanel);
    }

    /**
     * The text of a part changed (an edit, a redraft): the passages of its annotations moved, so they are read again.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    private void annotationsChanged() {
        annotations.invalidate();
        annotationsPanel.refresh();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void autosaveAndSwapAllToMarkdown() {
        cards.autosaveAndSwapAllToMarkdown();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void startGeneration() {
        startGeneration(GenerationRequest.newMessage(), newTurnForm.toTurnInput());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void checkAndApplyBackupStrategy() {
        backup.checkAndApplyBackupStrategy(parent);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "request", type = GenerationRequest.class),
                    @APIParameter(name = "turnInput", type = TurnInput.class)},
            locals = {@APILocals(name = "ui", type = UI.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void startGeneration(GenerationRequest request, TurnInput turnInput) {
        autosaveAndSwapAllToMarkdown();
        showStory();
        // the new part is added after the last one, which the user may have scrolled away from
        lazyLoader.showTail();
        UI ui = UI.getCurrent();

        parent.freeze();

        try {
            this.currentManuscript = parent.refreshManuscript();
            this.activeGenerationToken = storyGenerationService.generateNextTurn(
                    currentManuscript,
                    turnInput,
                    request,
                    new StoryGenerationListener<>(ui, request, cards, centerContentPanel, ChatMessageCard::new, parent,
                            loc, new StoryGenerationListener.Callbacks() {
                        @Override
                        public void generationEnded() {
                            activeGenerationToken = null;
                        }

                        @Override
                        public void partGenerated() {
                            checkAndApplyBackupStrategy();
                        }

                        @Override
                        public void partCompleted(ChatMessage message) {
                            try {
                                new AutoAnnotate(parent.refreshManuscript(), () -> {
                                    try {
                                        load(parent.refreshManuscript());
                                    } catch (Exception e) {
                                        UIUtils.internalServerError(loc, e);
                                    }
                                }).run(message);
                            } catch (Exception e) {
                                UIUtils.internalServerError(loc, e);
                            }
                        }

                        @Override
                        public void turnDiscarded() {
                            newTurnForm.keepTypedValues();
                        }

                        @Override
                        public void reloadManuscript() throws Exception {
                            currentManuscript = parent.refreshManuscript();
                        }

                        @Override
                        public void renderStory() {
                            lazyLoader.keepViewportWhile(() -> renderStoryContent());
                        }
                    })
            );
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "frozen", type = boolean.class)})
    @Override
    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;

        // the story has to go on working (the stop button), the other tabs wait for the generation
        leftBar.setTabsEnabled(!frozen, STORY_TAB);
        menuBar.setEnabled(!frozen);
        commandBar.setEnabled(!frozen);

        cards.setFrozen(frozen);

        if (frozen) {
            newTurnPopover.close();
            newTurnPopover.setTarget(null);
            actionButton.setIcon(Solid.STOP.create());
            actionButton.setThemeName("error primary icon small");
        } else {
            newTurnPopover.setTarget(actionButton);
            actionButton.setIcon(Solid.PLUS.create());
            actionButton.setThemeName("primary icon small");
        }
        refreshActionTooltip();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    @Override
    public void load(Manuscript manuscript) {
        this.currentManuscript = manuscript;
        if (manuscript != null && leftBar != null) {
            applyLanguage(manuscript);
        }
        // before the story is rendered, the tabs extensions add then find their place
        applyTabsConfig(manuscript);
        renderStoryContent();
        refreshSummariesMenuItem();
        if (lorebookPreview != null) {
            // the user may have changed the lore in the other tabs
            lorebookPreview.refresh(manuscript == null ? null : manuscript.getLorebook());
        }
    }

    /**
     * Arranges the tabs as the manuscript has them saved. A manuscript that has nothing saved gets the default layout, one that
     * was saved before the story was a tab gets its old arrangement with the story at the right (see
     * {@link StoryDockLayout#migrate}).
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "config", type = JsonElement.class),
                    @APILocals(name = "position", type = JsonElement.class),
                    @APILocals(name = "stored", type = JsonObject.class)})
    private void applyTabsConfig(Manuscript manuscript) {
        if (leftBar == null || manuscript == null) {
            return;
        }
        JsonElement config = manuscript.getAttributes().get(TABS_CONFIG_ATTRIBUTE);
        JsonElement position = manuscript.getAttributes().get(SPLIT_POSITION_ATTRIBUTE);
        JsonObject stored = config != null && config.isJsonObject() ? config.getAsJsonObject() : null;
        if (stored == null) {
            // a book that has no arrangement of its own opens with the one the user saved as their default
            stored = userDefaultLayout();
        }
        leftBar.setLayout(StoryDockLayout.migrate(stored,
                position != null && position.isJsonPrimitive() && position.getAsJsonPrimitive().isNumber() ? position.getAsDouble() : null,
                STORY_TAB, CHAPTERS_TAB, LOREBOOK_TAB, ANNOTATIONS_TAB));
    }

    /**
     * The arrangement the user saved as their default, null when there is none.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "layout", type = JsonObject.class),
                    @APILocals(name = "e", type = Exception.class)})
    private JsonObject userDefaultLayout() {
        try {
            JsonObject layout = settingService.getOrCreate(UserSetting.class).getDefaultStoryLayout();
            return layout == null || layout.size() == 0 ? null : layout;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }

    /**
     * Saves the current arrangement of the tabs (including the minimized ones) as the default of the user, which the books
     * that have no arrangement of their own open with.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "setting", type = UserSetting.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void saveLayoutAsDefault() {
        try {
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setDefaultStoryLayout(leftBar.getLayout());
            settingService.save(setting);
            Notification.success(loc.getValue(L.MSG_LAYOUT_SAVED_AS_DEFAULT));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Saves the layout of the tabs into the attributes of the manuscript, called after the user dragged a tab or a splitter.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void saveTabsConfig() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            manuscript.getAttributes().add(TABS_CONFIG_ATTRIBUTE, leftBar.getLayout());
            // the splitter of the old layout is a column of the layout now
            manuscript.getAttributes().remove(SPLIT_POSITION_ATTRIBUTE);
            parent.save();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows the story: its tab becomes the selected one of the sheet it is in, whatever the user has put into that sheet.
     * For everything that is about to show a part (a click in the outline, a hit of the search, a branch chosen in the tree,
     * a generation).
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    public void showStory() {
        if (leftBar != null) {
            leftBar.select(STORY_TAB);
        }
    }

    /**
     * Puts the tabs back the way a new book has them: forgets the layout saved with the manuscript. The contents of the tabs and
     * the tabs of extensions stay, the latter go to the first sheet.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void restoreDefaultLayout() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            manuscript.getAttributes().remove(TABS_CONFIG_ATTRIBUTE);
            manuscript.getAttributes().remove(SPLIT_POSITION_ATTRIBUTE);
            parent.save();
            applyTabsConfig(manuscript);
            showStory();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * What the user typed into the new turn form and the selected tab of every sheet of the sidebar (which tabs the
     * sheets hold is saved with the manuscript, see {@link #TABS_CONFIG_ATTRIBUTE}).
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        RestorableState.put(state, "newTurn", newTurnForm == null ? null : newTurnForm.storeState());
        RestorableState.put(state, "tabs", leftBar == null ? null : leftBar.storeState());
        return state;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "tabs", type = JsonObject.class),
                    @APILocals(name = "newTurn", type = JsonObject.class)})
    @Override
    public void restoreState(JsonObject self) {
        JsonObject tabs = RestorableState.object(self, "tabs");
        if (tabs != null && leftBar != null) {
            leftBar.restoreState(tabs);
        }
        JsonObject newTurn = RestorableState.object(self, "newTurn");
        if (newTurn != null && newTurnForm != null) {
            newTurnForm.restoreState(newTurn);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabLeave() throws Exception {
        autosaveAndSwapAllToMarkdown();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabEnter() throws Exception {
        if (scrollToMessageId != null) {
            // a part was asked for (a branch chosen in the tree): the story shows it, not the tab that was open
            showStory();
        }
        if (parent != null) {
            load(parent.refreshManuscript());
        }
        if (keyBindings != null) {
            // the user may have changed the keys in the settings
            loadKeyBindings();
            toolsMenu.refresh(menuDefinition(MenuKind.TOOLS));
            JavascriptLibrary.setShortcutKeys(UI.getCurrent().getPage(), keyBindings.toJson());
            newTurnForm.setGenerateShortcut(keyBindings.get(ShortcutActions.STORY_GENERATE));
            refreshActionTooltip();
        }
    }

    @Extendable
    private class ChatMessageCard extends HorizontalLayout implements StoryCard {
        private ChatMessage message;
        private boolean isLast;
        private final int orderId;

        private final VerticalLayout contentLayout;
        private Button menuBtn;
        private ContextMenu hamburgerMenu;
        private BuiltMenu cardMenu;
        private BuiltMenu textMenu;
        private boolean menuItemsCreated;
        private Details reasoningDetails;
        private Markdown reasoningMarkdown;
        private Markdown responseMarkdown;
        private MessageImages messageImages;
        private TextArea responseTextArea;
        private boolean editing = false;

        private CardAnnotations cardAnnotations;
        // the text is being generated again: the passages of the annotations are not in it yet
        private boolean generating = false;

        private final VerticalLayout metaLayout;
        private final MessageMetrics metrics;
        private Button turnDetailsBtn;
        private boolean wasReasoningOpenedByGeneration;

        private Popover detailsPopover;
        private TurnDetailsForm detailsForm;

        public ChatMessageCard(ChatMessage message, int orderId) {
            this(message, false, orderId);
        }

        public ChatMessageCard(ChatMessage message, boolean isLast, int orderId) {
            this.message = message;
            this.isLast = isLast;
            this.orderId = orderId;

            setWidthFull();
            setPadding(false);
            setSpacing(false);
            addClassName(CssStyles.CHAT_MESSAGE);

            contentLayout = new VerticalLayout();
            contentLayout.setWidthFull();
            contentLayout.setPadding(false);
            contentLayout.setSpacing(false);
            contentLayout.addClassName(CssStyles.CHAT_MESSAGE_CONTENT);

            HorizontalLayout headerBar = new HorizontalLayout();
            headerBar.setWidthFull();
            headerBar.setPadding(false);
            headerBar.setSpacing(false);
            headerBar.setAlignItems(FlexComponent.Alignment.CENTER);

            menuBtn = new Button(Solid.HAMBURGER.create());
            menuBtn.setThemeName("tertiary icon small");
            menuBtn.addClassName(CssStyles.MARGIN_LEFT_AUTO);

            createHamburgerMenu();

            // the badge of the annotations is added to the header when the card has its markdown
            HorizontalLayout actions = new HorizontalLayout(menuBtn);
            actions.setSpacing(false);
            actions.setPadding(false);
            actions.setAlignItems(FlexComponent.Alignment.CENTER);
            actions.addClassName(CssStyles.MARGIN_LEFT_AUTO);
            headerBar.add(UIUtils.voidComponent(), actions);

            reasoningMarkdown = new Markdown();
            reasoningMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
            if (StringUtils.isNotBlank(message.getResponseReasoning())) {
                reasoningMarkdown.setContent(message.getResponseReasoning());
            }

            reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
            reasoningDetails.setWidthFull();
            reasoningDetails.addClassNames(CssStyles.MAX_WIDTH_FULL, CssStyles.MIN_WIDTH_0, CssStyles.BORDER_BOX);

            responseMarkdown = new Markdown();
            responseMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
            renderResponse();

            // a click on a mark of an annotation opens its note at the mark
            cardAnnotations = new CardAnnotations(loc, annotations, () -> this.message, responseMarkdown,
                    () -> responseTextArea, this::autosaveAndSwapToMarkdown, this::redraftSelection, this::prepareTextMenu);
            actions.addComponentAsFirst(cardAnnotations.getBadge());
            refreshAnnotationBadge();

            messageImages = new MessageImages(message.getImages());

            contentLayout.add(headerBar, reasoningDetails, cardAnnotations.getMargin(), responseMarkdown, messageImages, cardAnnotations.getAnchor());
            applyAnnotationMargin();

            metaLayout = new VerticalLayout();
            metaLayout.setWidth("220px");
            metaLayout.addClassName(CssStyles.CHAT_MESSAGE_META);
            metaLayout.setPadding(false);
            metaLayout.setSpacing(false);

            metrics = new MessageMetrics(loc, message, this.orderId, false);

            turnDetailsBtn = new Button(loc.getValue(L.LABEL_TURN_DETAILS), Solid.INFO_CIRCLE.create());
            turnDetailsBtn.setThemeName("tertiary small");
            turnDetailsBtn.setWidthFull();

            detailsPopover = new Popover();
            detailsPopover.setTarget(turnDetailsBtn);
            // opened here, not by the click in the browser, so the content is in the popover before it is positioned (a
            // popover that grows after it opened can end up on the wrong side of its button and be cut off)
            detailsPopover.setOpenOnClick(false);
            turnDetailsBtn.addClickListener(event -> {
                createDetails();
                detailsPopover.setOpened(!detailsPopover.isOpened());
            });

            metaLayout.add(metrics.getHeader());
            metaLayout.add(metrics.getLines());
            metaLayout.add(turnDetailsBtn);

            add(contentLayout, metaLayout);
            setFlexGrow(1, contentLayout);
            setFlexGrow(0, metaLayout);
        }

        /**
         * Fills the popover of the turn details, once. It is not done when the card is created, because the four text
         * fields of the form measure their height in the browser as soon as they are in the page, which makes a story
         * of hundreds of parts freeze it. Regenerating needs the form too, so it calls this as well.
         */
        @PublicAPI(from = "1.4.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "detailsLayout", type = FormLayout.class)})
        private void createDetails() {
            if (detailsForm != null) {
                return;
            }
            FormLayout detailsLayout = new FormLayout();
            detailsLayout.setWidth("520px");
            detailsLayout.addClassName(CssStyles.POPOVER_FORM);

            detailsForm = new TurnDetailsForm(loc, message, () -> this.message, () -> this.isLast, this::saveMessage);
            detailsForm.addTo(detailsLayout);
            detailsPopover.add(detailsLayout);
        }

        /**
         * Creates the empty menu of the card. The items are added when the menu is opened for the first time, see
         * {@link #prepareMenu()}.
         */
        @PublicAPI(from = "1.4.0",
                returns = @APIReturn(type = void.class))
        private void createHamburgerMenu() {
            hamburgerMenu = new LazyContextMenu(menuBtn, this::prepareMenu);
            hamburgerMenu.setOpenOnClick(true);
        }

        /**
         * Runs each time the menu is about to open: creates the items the first time, then refreshes their text, visibility and keys.
         */
        @PublicAPI(from = "1.4.0",
                returns = @APIReturn(type = void.class))
        private void prepareMenu() {
            if (!menuItemsCreated) {
                menuItemsCreated = true;
                createMenuItems();
            }
            refreshSummaryMenuItems();
            cardMenu.refresh(menuDefinition(MenuKind.CARD));
        }

        /**
         * Runs each time the menu of the text is about to open: creates its items from the registry the first time, then refreshes them.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "menu", type = ContextMenu.class)})
        private void prepareTextMenu(ContextMenu menu) {
            if (textMenu == null) {
                textMenu = menuDefinition(MenuKind.TEXT).build(menu, loc, ManuscriptStoryPart.this, () -> this);
            }
            textMenu.refresh(menuDefinition(MenuKind.TEXT));
        }

        /**
         * Adds the items of the registry (kind {@link MenuKind#CARD}) to {@code hamburgerMenu}. Called when the menu is opened for the
         * first time, not when the card is created, so the cards of a long story stay cheap; extensions that decorate it add their items
         * after these.
         */
        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void createMenuItems() {
            cardMenu = menuDefinition(MenuKind.CARD).build(hamburgerMenu, loc, ManuscriptStoryPart.this, () -> this);
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void openImagesDialog() {
            new ImagesDialog(message, updated -> {
                message = updated;
                messageImages.setImages(updated.getImages());
            }).open();
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "latest", type = ChatMessage.class)})
        private void refreshSummaryMenuItems() {
            try {
                if (message != null && message.getId() != null) {
                    ChatMessage latest = chatMessageService.find(message.getId());
                    if (latest != null) {
                        this.message = latest;
                    }
                }
            } catch (Exception ignored) {}
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void confirmDeleteSummary() {
            SummaryRemoval.confirmAndRemove(loc, summaryService, message, updated -> {
                message = updated;
                refreshSummaryMenuItems();
                refreshSummariesMenuItem();
            });
        }

        /**
         * Opens the dialog that lets the model write the part again from the instructions of the author, see
         * {@link RedraftDialog}. The part is replaced when the author applies the result.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        @Override
        public void redraft() {
            try {
                currentManuscript = parent.refreshManuscript();
                RedraftDialog.open(currentManuscript, message, saved -> {
                    message = saved;
                    annotationsChanged();
                    renderResponse();
                    updateMetrics(message);
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Opens the redraft for the passage of the part the author selected (the item of the menu of the text), see
         * {@link RedraftDialog}. Only the passage is replaced when the author applies the result.
         */
        @PublicAPI(from = "1.5.0",
                parameters = {@APIParameter(name = "passage", type = RedraftService.Passage.class)},
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        private void redraftSelection(RedraftService.Passage passage) {
            try {
                currentManuscript = parent.refreshManuscript();
                RedraftDialog.open(currentManuscript, message, passage, saved -> {
                    message = saved;
                    annotationsChanged();
                    renderResponse();
                    updateMetrics(message);
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Opens the review of merging the part into the part before it, see {@link PartMergeDialog}.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        @Override
        public void mergeIntoPrevious() {
            try {
                currentManuscript = parent.refreshManuscript();
                PartMergeDialog.open(currentManuscript, message, merged -> {
                    try {
                        load(parent.refreshManuscript());
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        @Override
        public void branch() {
            try {
                currentManuscript = parent.refreshManuscript();
                currentManuscript.setActiveLeaf(chatMessageService.branch(currentManuscript, message));
                load(parent.save());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "summaryDialog", type = SummaryDialog.class)})
        @Override
        public void openSummaryDialog() {
            refreshSummaryMenuItems();
            boolean hasSummary = (message != null && message.getSummary() != null);
            SummaryDialog summaryDialog = new SummaryDialog(currentManuscript, message, !hasSummary);
            summaryDialog.onClosed(ManuscriptStoryPart.this::refreshSummariesMenuItem);
            summaryDialog.open();
            if (!hasSummary) {
                summaryDialog.startGeneration();
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void regenerate() {
            createDetails();
            startGeneration(GenerationRequest.regenerate(message), detailsForm.toTurnInput());
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void swipe() {
            createDetails();
            startGeneration(GenerationRequest.newSwipe(message), detailsForm.toTurnInput());
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        private void saveMessage() {
            try {
                this.message = chatMessageService.save(this.message);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "isLast", type = boolean.class)})
        public void setLastMessage(boolean isLast) {
            this.isLast = isLast;
            if (detailsForm != null) {
                detailsForm.setLastMessage(isLast);
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "frozen", type = boolean.class)})
        public void setFrozen(boolean frozen) {
            if (menuBtn != null) {
                menuBtn.setEnabled(!frozen);
            }
            if (turnDetailsBtn != null) {
                turnDetailsBtn.setEnabled(!frozen);
            }
        }

        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = boolean.class))
        @Override
        public boolean isLast() {
            return isLast;
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = boolean.class))
        public boolean isEditing() {
            return editing;
        }

        /**
         * Makes a note about the text selected in the part, see {@link CardAnnotations#annotateSelection}.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void annotateSelection() {
            cardAnnotations.annotateSelection();
        }

        /**
         * Makes a note about the whole part.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void annotatePart() {
            cardAnnotations.annotatePart();
        }

        /**
         * Has the model write the passage the author selected again.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void redraftSelection() {
            cardAnnotations.redraftSelection();
        }

        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void copySelection() {
            cardAnnotations.copySelection();
        }

        /**
         * Asks, then deletes the part: what followed it continues from its parent.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void confirmDelete() {
            ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                try {
                    chatMessageService.deleteNodeAndMigrateChildren(message, currentManuscript, false);

                    parent.refreshManuscript();
                    load(parent.getManuscript());
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        }

        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = Element.class))
        public Element getResponseElement() {
            return responseMarkdown.getElement();
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void toggleEdit() {
            if (editing) {
                autosaveAndSwapToMarkdown();
            } else {
                editing = true;
                // created only while a part is edited: a text area measures its height in the browser as soon as it
                // is in the page, which makes a story of hundreds of parts freeze it
                responseTextArea = new TextArea();
                responseTextArea.setWidthFull();
                responseTextArea.setMinHeight("100px");
                responseTextArea.setValue(StringUtils.defaultString(message.getResponse()));
                contentLayout.addComponentAtIndex(contentLayout.indexOf(responseMarkdown) + 1, responseTextArea);
                responseMarkdown.setVisible(false);
                JavascriptLibrary.focusAfterRender(responseTextArea.getElement());
            }
        }

        /**
         * Leaves the editor without saving: the part keeps the text it was last saved with. The author is asked first
         * when the text in the editor differs from it.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void discardEdit() {
            if (!editing) {
                return;
            }
            if (StringUtils.defaultString(responseTextArea.getValue()).equals(StringUtils.defaultString(message.getResponse()))) {
                closeEditor();
            } else {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DISCARD_EDIT), this::closeEditor);
            }
        }

        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        private void closeEditor() {
            if (!editing) {
                return;
            }
            contentLayout.remove(responseTextArea);
            responseTextArea = null;
            responseMarkdown.setVisible(true);
            editing = false;
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "newResponse", type = String.class),
                        @APILocals(name = "e", type = Exception.class)})
        public void autosaveAndSwapToMarkdown() {
            if (editing) {
                try {
                    String newResponse = responseTextArea.getValue();
                    message = support.saveEditedResponse(currentManuscript, message, newResponse);
                    // the passages of the annotations moved with the edit
                    annotationsChanged();
                    renderResponse();
                    contentLayout.remove(responseTextArea);
                    responseTextArea = null;
                    responseMarkdown.setVisible(true);
                    editing = false;
                    updateMetrics(message);
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = boolean.class))
        public boolean isReasoningOpened() {
            return reasoningDetails.isOpened();
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "opened", type = boolean.class)})
        public void setReasoningOpened(boolean opened) {
            if (opened) {
                reasoningDetails.setOpened(true);
                wasReasoningOpenedByGeneration = true;
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "reasoningText", type = String.class)})
        public void updateReasoning(String reasoningText) {
            if (!wasReasoningOpenedByGeneration) {
                reasoningDetails.setOpened(true);
                wasReasoningOpenedByGeneration = true;
            }
            message.setResponseReasoning(reasoningText);
            reasoningMarkdown.setContent(StringUtils.defaultString(reasoningText));
        }

        /**
         * Draws the response of the message, with the text that is still the LLM's highlighted in the diff view.
         */
        @PublicAPI(from = "1.2.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "response", type = String.class),
                        @APILocals(name = "marks", type = List.class),
                        @APILocals(name = "range", type = int[].class)})
        public void renderResponse() {
            String response = StringUtils.defaultString(message.getResponse());
            // all the layers are ranges of the text as it is, put in at once (see TextMarkup)
            List<TextMarkup.Mark> marks = new ArrayList<>();
            if (showGeneratedDiff) {
                for (int[] range : GeneratedTextHighlighter.ranges(response, message.getOriginalResponse())) {
                    marks.add(new TextMarkup.Mark(range[0], range[1], CssStyles.GENERATED_TEXT));
                }
            }
            marks.addAll(branchSearch.marks(message.getId(), response));
            if (!generating) {
                marks.addAll(annotations.marks(message, response));
            }
            responseMarkdown.setContent(TextMarkup.apply(response, marks));
            refreshAnnotationBadge();
        }

        /**
         * Opens the dialog that lets a model propose annotations for the part, see {@link AnnotateDialog}. The ones the author
         * accepts are marked in the text.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        @Override
        public void annotateWithAi() {
            try {
                currentManuscript = parent.refreshManuscript();
                AnnotateDialog.open(currentManuscript, message, () -> {
                    annotationsChanged();
                    renderResponse();
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Puts the notes of the part beside its text (the card is a grid of two columns then) or leaves them to the popover.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        private void applyAnnotationMargin() {
            contentLayout.setClassName(CssStyles.ANNOTATION_MARGIN_LAYOUT, annotationsInMargin);
            cardAnnotations.setMarginMode(annotationsInMargin);
        }

        /**
         * Shows how many annotations of the part need a look.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        private void refreshAnnotationBadge() {
            if (cardAnnotations != null) {
                cardAnnotations.refresh(generating);
            }
        }

        /**
         * Scrolls to the passage of the annotation and flashes it.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "annotation", type = Annotation.class)})
        private void flashAnnotation(Annotation annotation) {
            cardAnnotations.flash(annotation);
        }

        /**
         * Attaches the note to the text the author selected, instead of the passage it lost.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "annotation", type = Annotation.class)})
        private void attachToSelection(Annotation annotation) {
            cardAnnotations.attachToSelection(annotation);
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "responseText", type = String.class)})
        public void updateResponse(String responseText) {
            message.setResponse(responseText);
            renderResponse();
        }

        @PublicAPI(from = "1.2.0",
                returns = @APIReturn(type = ChatMessage.class))
        @Override
        public ChatMessage getMessage() {
            return message;
        }

        @PublicAPI(from = "1.2.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "msg", type = ChatMessage.class)})
        @Override
        public void restartStreaming(ChatMessage msg) {
            generating = true;
            updateReasoning("");
            updateResponse("");
            updateMetrics(msg);
            wasReasoningOpenedByGeneration = false;
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "msg", type = ChatMessage.class)})
        public void updateMetrics(ChatMessage msg) {
            metrics.update(message, msg);
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "dialog", type = TokenUsageDialog.class)})
        @Override
        public void showTokenUsage() {
            // null for parts generated before the breakdown was recorded, the dialog says so
            TokenUsageDialog dialog = new TokenUsageDialog(message.getTokenBreakdown());
            dialog.create();
            dialog.open();
        }

        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "dialog", type = PromptDialog.class),
                        @APILocals(name = "e", type = Exception.class)})
        @Override
        public void showPrompt() {
            try {
                PromptDialog dialog = new PromptDialog(messagePromptService.getBuiltPrompt(message));
                dialog.create();
                dialog.open();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
    }
}
