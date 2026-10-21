package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
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
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.GeneratedTextHighlighter;
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
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.List;
import java.util.Set;

@Configurable
@Extendable
public class ManuscriptStoryPart implements ManuscriptDialogPart {

    /**
     * Key of the {@link Manuscript#getAttributes() attributes} of the manuscript that holds the layout of the sidebar tabs
     * ({@link TabDock#getLayout()}): which tabs the user detached into sheets of their own.
     */
    public static final String TABS_CONFIG_ATTRIBUTE = "$desktopManuscriptStoryPartTabsConfig";

    /**
     * Key of the {@link Manuscript#getAttributes() attributes} of the manuscript that holds the position of the splitter
     * between the sidebar and the story (percent of the width the sidebar has).
     */
    public static final String SPLIT_POSITION_ATTRIBUTE = "$desktopManuscriptStoryPartSplitPosition";

    /**
     * Id of the outline tab in the layout of the sidebar; extensions give their tabs ids of their own.
     */
    public static final String CHAPTERS_TAB = "chapters";

    /**
     * Id of the read only lorebook preview tab in the layout of the sidebar.
     */
    public static final String LOREBOOK_TAB = "lorebook";

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private SummaryService summaryService;

    private final ManuscriptDialog parent;
    private CancellationToken activeGenerationToken;

    private SplitLayout mainLayout;
    private VerticalLayout leftMarginLayout;
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
    private MenuItem changeStyles;
    private MenuItem highlightGenerated;
    private Button actionButton;

    private Popover newTurnPopover;
    private NewTurnForm newTurnForm;

    private Manuscript currentManuscript;
    private boolean isFrozen = false;
    // diff view, not stored: the response of the cards is shown with the generated text highlighted
    private boolean showGeneratedDiff = false;

    private final StoryCards<ChatMessageCard> cards = new StoryCards<>();
    private final StorySupport support = new StorySupport();
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
        mainLayout = new SplitLayout();
        mainLayout.setSizeFull();

        leftMarginLayout = new VerticalLayout();
        leftMarginLayout.setSizeFull();
        leftMarginLayout.setPadding(false);
        leftMarginLayout.setSpacing(false);
        leftMarginLayout.addClassName(CssStyles.BORDER_RIGHT_SUBTLE);

        // created once: a redraw of the story only refills the outline, so the selected tab (and the tabs extensions add) survive it
        leftBar = new TabDock();
        leftBar.setSizeFull();
        leftBar.setOnLayoutChange(this::saveTabsConfig);
        leftBar.setOnStateChange(() -> {
            if (parent != null) {
                parent.stateChanged();
            }
        });
        leftMarginLayout.add(leftBar);
        createStoryOutlineTab();
        createLorebookTab();

        centerLayout = new VerticalLayout();
        centerLayout.setSizeFull();
        centerLayout.setPadding(false);
        centerLayout.setSpacing(false);

        centerContentPanel = new ScrollPanel();
        centerContentPanel.setSizeFull();
        centerContentPanel.addClassName(CssStyles.STORY_SCROLL_PANEL);

        setupScrollListener();

        bottomControlsLayout = buildBottomControls();

        centerLayout.add(centerContentPanel, bottomControlsLayout);
        centerLayout.setFlexGrow(1, centerContentPanel);
        centerLayout.setFlexGrow(0, bottomControlsLayout);

        mainLayout.setOrientation(Orientation.HORIZONTAL);
        mainLayout.addToPrimary(leftMarginLayout);
        mainLayout.addToSecondary(centerLayout);
        mainLayout.setSplitterPosition(20);
        mainLayout.addSplitterDragEndListener(event -> saveSplitPosition());

        container.add(loc.getValue(L.LABEL_STORY_PART), mainLayout);
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void setupScrollListener() {
        support.trackScrollPosition(centerContentPanel, () -> currentManuscript, () -> isFrozen);
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
     * The story settings menu, extensions add their items to its sub menu ({@code cogsMenuItem.getSubMenu()}).
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "bar", type = MenuBar.class)},
            locals = {@APILocals(name = "cogs", type = MenuItem.class)})
    private MenuItem createCogsMenuItem(MenuBar bar) {
        MenuItem cogs = bar.addItem(Solid.COGS.create());
        changeStyles = createChangeStylesMenuItem(cogs);
        highlightGenerated = createHighlightGeneratedMenuItem(cogs);
        createSearchReplaceMenuItem(cogs);
        createImportArchiveMenuItem(cogs);
        createExportMenuItem(cogs);
        return cogs;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "cogs", type = MenuItem.class)})
    private MenuItem createChangeStylesMenuItem(MenuItem cogs) {
        return cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.PEN_FANCY.create(), loc.getValue(L.LABEL_CHANGE_STYLES)), event -> {
            try {
                Manuscript manuscript = parent.refreshManuscript();
                manuscript.setShowBookStyles(!manuscript.getShowBookStyles());
                manuscriptService.save(manuscript);
                applyBookStyles();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "cogs", type = MenuItem.class)},
            locals = {@APILocals(name = "item", type = MenuItem.class)})
    private MenuItem createHighlightGeneratedMenuItem(MenuItem cogs) {
        MenuItem item = cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.HIGHLIGHTER.create(), loc.getValue(L.LABEL_HIGHLIGHT_GENERATED_TEXT)),
                event -> setShowGeneratedDiff(event.getSource().isChecked()));
        item.setCheckable(true);
        item.setChecked(showGeneratedDiff);
        return item;
    }

    /**
     * Switches the diff view: the response of every card is drawn again, with the text that is still the LLM's
     * highlighted or without the highlight.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "show", type = boolean.class)},
            locals = {@APILocals(name = "card", type = ManuscriptStoryPart.ChatMessageCard.class)})
    private void setShowGeneratedDiff(boolean show) {
        showGeneratedDiff = show;
        for (ChatMessageCard card : cards.values()) {
            card.renderResponse();
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "cogs", type = MenuItem.class)})
    private MenuItem createSearchReplaceMenuItem(MenuItem cogs) {
        return cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.EXCHANGE_ALT.create(), loc.getValue(L.LABEL_SEARCH_AND_REPLACE)),
                event -> openSearchReplaceDialog());
    }

    /**
     * Opens the search and replace of the book, the story is shown again when something was replaced.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = ManuscriptSearchReplaceDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void openSearchReplaceDialog() {
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "cogs", type = MenuItem.class)})
    private MenuItem createImportArchiveMenuItem(MenuItem cogs) {
        return cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.FILE_IMPORT.create(), loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE)),
                event -> openArchiveImportDialog());
    }

    /**
     * Asks for a zip archive of {@code .md} files and adds a part for each file to the end of the story, see
     * {@link ChatMessageService#appendFromArchive}; the story is shown again.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void openArchiveImportDialog() {
        ArchiveUploadDialog.show(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE), loc.getValue(L.MSG_IMPORT_ARCHIVE_TO_BOOK),
                (archive, fileName) -> {
                    int parts = chatMessageService.appendFromArchive(parent.refreshManuscript(), archive);
                    Notification.success(String.format(loc.getValue(L.MSG_ARCHIVE_PARTS_ADDED), parts));
                    load(parent.refreshManuscript());
                });
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "cogs", type = MenuItem.class)})
    private MenuItem createExportMenuItem(MenuItem cogs) {
        return cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.FILE_EXPORT.create(), loc.getValue(L.LABEL_EXPORT_STORY)), event -> {
            try {
                new ExportDialog(parent.refreshManuscript()).open();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
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
            mainLayout.addClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        } else {
            mainLayout.removeClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        }
    }

    // lang drives the browser's hyphenation dictionary (see hyphens in shared-styles.css)
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    private void applyLanguage(Manuscript manuscript) {
        mainLayout.getElement().setAttribute("lang", manuscript.getLanguage().getCode());
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
        VerticalLayout content = newTurnForm.createContent(() -> {
            popover.close();
            if (parent != null) {
                newTurnForm.consume();
                parent.stateChanged();
                autosaveAndSwapAllToMarkdown();
                startGeneration();
            }
        });
        content.setWidth("540px");
        popover.add(content);

        popover.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                newTurnForm.syncIfUntouched(currentManuscript != null ? currentManuscript.getActiveLeaf() : null);
                newTurnForm.focusInstructions();
            }
        });

        return popover;
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
                    @APILocals(name = "dbId", type = Long.class),
                    @APILocals(name = "card", type = ManuscriptStoryPart.ChatMessageCard.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "target", type = ManuscriptStoryPart.ChatMessageCard.class)})
    private void renderStoryContent() {
        Set<Long> openedReasonings = cards.collectOpenedReasonings();

        centerContentPanel.removeAll();
        sidebarList.removeAll();
        sidebarHeader.setText("");
        cards.clear();

        if (currentManuscript == null || currentManuscript.getActiveLeaf() == null) {
            Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
            centerContentPanel.add(UIUtils.centerComponent(emptyLabel));
        } else {
            try {
                List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(currentManuscript.getActiveLeaf());
                int total = branch.size();
                sidebarHeader.setText(String.format(loc.getValue(L.LABEL_CHAPTER_OUTLINE_HEADER), total));

                for (int i = 0; i < total; i++) {
                    ChatMessage msg = branch.get(i);
                    int orderId = i + 1;
                    Long dbId = msg.getId();

                    createSidebarButton(msg, orderId, dbId);

                    boolean isLast = (i == total - 1);
                    ChatMessageCard card = new ChatMessageCard(msg, isLast, orderId);
                    card.setFrozen(isFrozen);
                    card.setReasoningOpened(openedReasonings.contains(dbId));
                    cards.put(card);
                    centerContentPanel.add(card);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }

            ChatMessageCard target = scrollToMessageId != null ? cards.get(scrollToMessageId) : null;
            scrollToMessageId = null;
            if (target != null) {
                scrollToCard(target);
            } else {
                restoreScrollPosition();
            }
        }
    }

    /**
     * The next render of the story (the tab being entered) scrolls to the part instead of restoring the scroll
     * position, used by the search of the tree.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "messageId", type = Long.class)})
    public void scrollToMessageOnNextRender(Long messageId) {
        this.scrollToMessageId = messageId;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "card", type = ManuscriptStoryPart.ChatMessageCard.class)})
    private void scrollToCard(ChatMessageCard card) {
        JavascriptLibrary.scrollToAndHighlight(card.getElement());
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
            if (dbId != null && cards.contains(dbId)) {
                cards.get(dbId).scrollIntoView();
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
        leftBar.add(CHAPTERS_TAB, loc.getValue(L.LABEL_CHAPTERS), leftMarginLayout);

        sidebarHeader = new Span();
        sidebarHeader.addClassName(CssStyles.SIDEBAR_HEADER);

        sidebarList = new VerticalLayout();
        sidebarList.setPadding(false);
        sidebarList.setSpacing(false);
        sidebarList.setWidthFull();

        leftMarginLayout.add(sidebarHeader, sidebarList);
        leftMarginLayout.setFlexGrow(1, sidebarList);
    }

    /**
     * The tab with the read only preview of the lore of the story, it is loaded again every time the story is entered.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    private void createLorebookTab() {
        lorebookPreview = new LorebookPreview().create();
        leftBar.add(LOREBOOK_TAB, loc.getValue(L.LABEL_LOREBOOK), lorebookPreview);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "leaf", type = ChatMessage.class),
                    @APILocals(name = "savedPos", type = Integer.class)})
    private void restoreScrollPosition() {
        ChatMessage leaf = currentManuscript != null ? currentManuscript.getActiveLeaf() : null;
        Integer savedPos = (leaf != null) ? leaf.getScrollPosition() : null;

        JavascriptLibrary.restoreScrollPosition(centerContentPanel.getElement(), savedPos);
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
        UI ui = UI.getCurrent();

        parent.freeze();

        try {
            this.currentManuscript = parent.refreshManuscript();
            this.activeGenerationToken = storyGenerationService.generateNextTurn(
                    currentManuscript,
                    turnInput,
                    request,
                    new StoryGenerationListener<>(ui, request, cards, centerContentPanel, ChatMessageCard::new, parent,
                            chatMessageService, loc, new StoryGenerationListener.Callbacks() {
                        @Override
                        public void generationEnded() {
                            activeGenerationToken = null;
                        }

                        @Override
                        public void partGenerated() {
                            checkAndApplyBackupStrategy();
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
                            renderStoryContent();
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

        leftMarginLayout.setEnabled(!frozen);
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
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    @Override
    public void load(Manuscript manuscript) {
        this.currentManuscript = manuscript;
        if (manuscript != null && mainLayout != null) {
            applyLanguage(manuscript);
        }
        // before the story is rendered, the tabs extensions add then find their place
        applyTabsConfig(manuscript);
        applySplitPosition(manuscript);
        renderStoryContent();
        refreshSummariesMenuItem();
        if (lorebookPreview != null) {
            // the user may have changed the lore in the other tabs
            lorebookPreview.refresh(manuscript == null ? null : manuscript.getLorebook());
        }
    }

    /**
     * Arranges the sidebar tabs as the manuscript has them saved (all in one sheet when it has nothing saved).
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "config", type = JsonElement.class)})
    private void applyTabsConfig(Manuscript manuscript) {
        if (leftBar == null || manuscript == null) {
            return;
        }
        JsonElement config = manuscript.getAttributes().get(TABS_CONFIG_ATTRIBUTE);
        leftBar.setLayout(config != null && config.isJsonObject() ? config.getAsJsonObject() : null);
    }

    /**
     * Puts the splitter between the sidebar and the story where the manuscript has it saved (it stays where it is when
     * nothing is saved).
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "position", type = JsonElement.class)})
    private void applySplitPosition(Manuscript manuscript) {
        if (mainLayout == null || manuscript == null) {
            return;
        }
        JsonElement position = manuscript.getAttributes().get(SPLIT_POSITION_ATTRIBUTE);
        if (position != null && position.isJsonPrimitive() && position.getAsJsonPrimitive().isNumber()) {
            double value = position.getAsDouble();
            if (value > 0 && value < 100) {
                mainLayout.setSplitterPosition(value);
            }
        }
    }

    /**
     * Saves the position of the splitter between the sidebar and the story into the attributes of the manuscript,
     * called after the user dragged it.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "position", type = Double.class),
                    @APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void saveSplitPosition() {
        try {
            Double position = mainLayout.getSplitterPosition();
            if (position == null) {
                return;
            }
            Manuscript manuscript = parent.refreshManuscript();
            manuscript.getAttributes().addProperty(SPLIT_POSITION_ATTRIBUTE, Math.round(position * 100) / 100.0);
            parent.save();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Saves the sidebar layout into the attributes of the manuscript, called after the user dragged a tab.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void saveTabsConfig() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            manuscript.getAttributes().add(TABS_CONFIG_ATTRIBUTE, leftBar.getLayout());
            parent.save();
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
        if (parent != null) {
            load(parent.refreshManuscript());
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
        private MenuItem editItem;
        private MenuItem regenerateItem;
        private MenuItem swipeItem;
        private MenuItem branchItem;
        private MenuItem summaryItem;
        private MenuItem deleteSummaryItem;
        private MenuItem deleteItem;
        private MenuItem showPromptItem;
        private MenuItem tokenUsageItem;
        private MenuItem imagesItem;
        private Details reasoningDetails;
        private Markdown reasoningMarkdown;
        private Markdown responseMarkdown;
        private MessageImages messageImages;
        private TextArea responseTextArea;
        private boolean editing = false;

        private final VerticalLayout metaLayout;
        private final MessageMetrics metrics;
        private Button turnDetailsBtn;
        private boolean wasReasoningOpenedByGeneration;

        private final TurnDetailsForm detailsForm;

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

            createMenuItems();

            headerBar.add(UIUtils.voidComponent(), menuBtn);

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

            responseTextArea = new TextArea();
            responseTextArea.setWidthFull();
            responseTextArea.setMinHeight("100px");
            responseTextArea.setValue(StringUtils.defaultString(message.getResponse()));
            responseTextArea.setVisible(false);

            messageImages = new MessageImages(message.getImages());

            contentLayout.add(headerBar, reasoningDetails, responseMarkdown, responseTextArea, messageImages);

            metaLayout = new VerticalLayout();
            metaLayout.setWidth("220px");
            metaLayout.addClassName(CssStyles.CHAT_MESSAGE_META);
            metaLayout.setPadding(false);
            metaLayout.setSpacing(false);

            metrics = new MessageMetrics(loc, message, this.orderId, false);

            turnDetailsBtn = new Button(loc.getValue(L.LABEL_TURN_DETAILS), Solid.INFO_CIRCLE.create());
            turnDetailsBtn.setThemeName("tertiary small");
            turnDetailsBtn.setWidthFull();

            Popover readOnlyPopover = new Popover();
            readOnlyPopover.setTarget(turnDetailsBtn);

            FormLayout detailsLayout = new FormLayout();
            detailsLayout.setWidth("520px");
            detailsLayout.addClassName(CssStyles.POPOVER_FORM);

            detailsForm = new TurnDetailsForm(loc, message, () -> this.message, () -> this.isLast, this::saveMessage);
            detailsForm.addTo(detailsLayout);
            readOnlyPopover.add(detailsLayout);

            metaLayout.add(metrics.getHeader());
            metaLayout.add(metrics.getLines());
            metaLayout.add(turnDetailsBtn);

            add(contentLayout, metaLayout);
            setFlexGrow(1, contentLayout);
            setFlexGrow(0, metaLayout);
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void createMenuItems() {
            hamburgerMenu = new ContextMenu();
            hamburgerMenu.setTarget(menuBtn);
            hamburgerMenu.setOpenOnClick(true);

            editItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_EDIT), event -> toggleEdit());

            regenerateItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_REGENERATE), event -> regenerate());
            regenerateItem.setVisible(this.isLast);

            swipeItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_SWIPE), event -> swipe());
            swipeItem.setVisible(this.isLast);

            branchItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_BRANCH_STORY), event -> branch());

            showPromptItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_SHOW_PROMPT), event -> showPrompt());

            tokenUsageItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_TOKEN_USAGE), event -> showTokenUsage());

            imagesItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_IMAGES), event -> openImagesDialog());

            summaryItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_GENERATE_SUMMARY), event -> openSummaryDialog());
            deleteSummaryItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DELETE_SUMMARY), event -> confirmDeleteSummary());

            deleteItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DELETE), event -> {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    JavascriptLibrary.getScrollTop(centerContentPanel.getElement()).then(Integer.class, scrollTop -> {
                        try {
                            Long parentId = message.getParent() != null ? message.getParent().getId() : null;

                            chatMessageService.deleteNodeAndMigrateChildren(message, currentManuscript, false);

                            parent.refreshManuscript();
                            load(parent.getManuscript());

                            if (scrollTop != null && scrollTop > 0) {
                                JavascriptLibrary.setScrollTop(centerContentPanel.getElement(), scrollTop);
                            } else if (parentId != null && cards.contains(parentId)) {
                                cards.get(parentId).scrollIntoView();
                            }
                        } catch (Exception e) {
                            UIUtils.internalServerError(loc, e);
                        }
                    });
                });
            });

            hamburgerMenu.addOpenedChangeListener(event -> {
                if (event.isOpened()) {
                    refreshSummaryMenuItems();
                }
            });
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void openImagesDialog() {
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

            boolean hasSummary = (message != null && message.getSummary() != null);
            if (hasSummary) {
                summaryItem.setText(loc.getValue(L.LABEL_SHOW_SUMMARY));
                deleteSummaryItem.setEnabled(true);
            } else {
                summaryItem.setText(loc.getValue(L.LABEL_GENERATE_SUMMARY));
                deleteSummaryItem.setEnabled(false);
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void confirmDeleteSummary() {
            SummaryRemoval.confirmAndRemove(loc, summaryService, message, updated -> {
                message = updated;
                refreshSummaryMenuItems();
                refreshSummariesMenuItem();
            });
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        private void branch() {
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
        private void openSummaryDialog() {
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
        private void regenerate() {
            startGeneration(GenerationRequest.regenerate(message), detailsForm.toTurnInput());
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void swipe() {
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
            if (regenerateItem != null) {
                regenerateItem.setVisible(isLast);
            }
            if (swipeItem != null) {
                swipeItem.setVisible(isLast);
            }
            detailsForm.setLastMessage(isLast);
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

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = boolean.class))
        public boolean isEditing() {
            return editing;
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        public void toggleEdit() {
            if (editing) {
                autosaveAndSwapToMarkdown();
            } else {
                editing = true;
                responseTextArea.setValue(StringUtils.defaultString(message.getResponse()));
                responseMarkdown.setVisible(false);
                responseTextArea.setVisible(true);
                editItem.setText(loc.getValue(L.LABEL_SAVE));
            }
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
                    renderResponse();
                    responseTextArea.setVisible(false);
                    responseMarkdown.setVisible(true);
                    editing = false;
                    editItem.setText(loc.getValue(L.LABEL_EDIT));
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
                locals = {@APILocals(name = "response", type = String.class)})
        public void renderResponse() {
            String response = StringUtils.defaultString(message.getResponse());
            responseMarkdown.setContent(showGeneratedDiff
                    ? GeneratedTextHighlighter.highlight(response, message.getOriginalResponse(), CssStyles.GENERATED_TEXT)
                    : response);
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "responseText", type = String.class)})
        public void updateResponse(String responseText) {
            message.setResponse(responseText);
            renderResponse();
            if (!editing) {
                responseTextArea.setValue(StringUtils.defaultString(responseText));
            }
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
        private void showTokenUsage() {
            // null for parts generated before the breakdown was recorded, the dialog says so
            TokenUsageDialog dialog = new TokenUsageDialog(message.getTokenBreakdown());
            dialog.create();
            dialog.open();
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "dialog", type = PromptDialog.class)})
        private void showPrompt() {
            PromptDialog dialog = new PromptDialog(message.getBuiltPrompt());
            dialog.create();
            dialog.open();
        }
    }
}
