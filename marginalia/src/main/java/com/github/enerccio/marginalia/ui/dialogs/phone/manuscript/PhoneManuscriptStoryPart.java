package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

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
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ImagesDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.SummaryDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.SummaryRemoval;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneSummariesDialog;
import com.github.enerccio.marginalia.ui.dialogs.search.ManuscriptSearchReplaceDialog;
import com.github.enerccio.marginalia.ui.dialogs.story.*;
import com.github.enerccio.marginalia.ui.widgets.LazyContextMenu;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Set;

/**
 * The story tab of the {@link PhoneManuscriptDialog}. One column: the parts of the story fill the screen and the
 * outline is the drawer of an {@link AppLayout}, slid in from the left by the toggle of the navbar. A part is a
 * {@code ChatMessageCard} with nothing but its text and a small header line (menu and information icons), the
 * metadata of the part and the turn details are in the popover of the information icon.
 * <p>
 * It is a separate class and not a subclass of the desktop {@code ManuscriptStoryPart}, so extensions know which of
 * them they decorate. The hooks for extensions are the same as there: {@link #populateMenuBar} and
 * {@link #createCogsMenuItem} fill the menu bar of the navbar, {@link #createStoryOutline} the drawer (its tab sheet {@code leftBar}).
 */
@Configurable
@Extendable
public class PhoneManuscriptStoryPart implements PhoneManuscriptDialogPart {

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

    private final PhoneManuscriptDialog parent;
    private CancellationToken activeGenerationToken;

    private AppLayout layout;
    private DrawerToggle drawerToggle;
    private Span navTitle;
    private VerticalLayout centerLayout;
    private ScrollPanel centerContentPanel;
    private HorizontalLayout bottomControlsLayout;
    private TabDock leftBar;
    private Span sidebarHeader;
    private VerticalLayout sidebarList;

    private MenuBar menuBar;
    private MenuItem summariesMenuItem;
    private MenuItem cogsMenuItem;
    private MenuItem changeStyles;
    private Button actionButton;

    private Popover newTurnPopover;
    private NewTurnForm newTurnForm;

    private Manuscript currentManuscript;
    private boolean isFrozen = false;

    private final StoryCards<ChatMessageCard> cards = new StoryCards<>();
    private final StorySupport support = new StorySupport();
    private StoryLazyLoader<ChatMessageCard> lazyLoader;
    private StoryBranchSearch branchSearch;
    private final StoryBackup backup = new StoryBackup();
    private Long scrollToMessageId;

    public PhoneManuscriptStoryPart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class))
    @Override
    public Component create() throws Exception {
        layout = new AppLayout();
        layout.addClassNames(CssStyles.FULL_SIZE, CssStyles.PHONE_STORY_LAYOUT);

        drawerToggle = new DrawerToggle();
        navTitle = new Span();
        navTitle.addClassName(CssStyles.TEXT_SECONDARY);
        menuBar = createMenuBar();
        menuBar.addClassName(CssStyles.MARGIN_LEFT_AUTO);
        layout.addToNavbar(drawerToggle, navTitle, menuBar);

        layout.addToDrawer(createStoryOutline());
        layout.setDrawerOpened(false);

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

        layout.setContent(centerLayout);
        applyBookStyles();
        return layout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            locals = {@APILocals(name = "controls", type = HorizontalLayout.class)})
    private HorizontalLayout buildBottomControls() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.setPadding(false);
        controls.setSpacing(false);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.addClassNames(CssStyles.PADDING_COMPACT, CssStyles.BORDER_TOP_SUBTLE);

        actionButton = new Button(Solid.PLUS.create());
        actionButton.setThemeName("primary");
        actionButton.setWidthFull();

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

        controls.add(actionButton);
        return controls;
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
        createSearchReplaceMenuItem(cogs);
        createImportArchiveMenuItem(cogs);
        createBranchTokensMenuItem(cogs);
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
            ManuscriptSearchReplaceDialog dialog = new ManuscriptSearchReplaceDialog(parent.refreshManuscript(), true);
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
     * {@link ArchiveImportService#appendParts}; the story is shown again.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void openArchiveImportDialog() {
        ArchiveUploadDialog.show(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE), loc.getValue(L.MSG_IMPORT_ARCHIVE_TO_BOOK),
                (archive, fileName) -> new ArchiveImportRunner().appendParts(parent.refreshManuscript(), MarkdownArchive.read(archive),
                        imported -> load(parent.refreshManuscript())));
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = MenuItem.class),
            parameters = {@APIParameter(name = "cogs", type = MenuItem.class)})
    private MenuItem createBranchTokensMenuItem(MenuItem cogs) {
        return cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.CHART_BAR.create(), loc.getValue(L.LABEL_BRANCH_TOKENS)),
                event -> openBranchTokensDialog());
    }

    /**
     * Shows the tokens the story of the active branch takes in the next prompt, with and without the summaries.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = BranchTokensDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void openBranchTokensDialog() {
        try {
            BranchTokensDialog dialog = new BranchTokensDialog(parent.refreshManuscript());
            dialog.create();
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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
            locals = {@APILocals(name = "dialog", type = PhoneSummariesDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void openSummariesDialog() {
        try {
            PhoneSummariesDialog dialog = new PhoneSummariesDialog(parent.refreshManuscript());
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
            layout.addClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        } else {
            layout.removeClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        }
    }

    // lang drives the browser's hyphenation dictionary (see hyphens in shared-styles.css)
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    private void applyLanguage(Manuscript manuscript) {
        layout.getElement().setAttribute("lang", manuscript.getLanguage().getCode());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Popover.class),
            parameters = {@APIParameter(name = "targetButton", type = Button.class)},
            locals = {@APILocals(name = "popover", type = Popover.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    private Popover buildNewTurnPopover(Button targetButton) {
        Popover popover = new Popover();
        popover.setTarget(targetButton);
        popover.setPosition(PopoverPosition.TOP);

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
        content.addClassName(CssStyles.PHONE_POPOVER_CONTENT);
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
                    @APILocals(name = "header", type = String.class),
                    @APILocals(name = "msg", type = ChatMessage.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void renderStoryContent() {
        Set<Long> openedReasonings = cards.collectOpenedReasonings();

        centerContentPanel.removeAll();
        sidebarList.removeAll();
        cards.clear();
        lazyLoader.clear();

        if (currentManuscript == null || currentManuscript.getActiveLeaf() == null) {
            sidebarHeader.setText("");
            navTitle.setText("");
            branchSearch.refresh(List.of());
            Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
            centerContentPanel.add(UIUtils.centerComponent(emptyLabel));
        } else {
            try {
                List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(currentManuscript.getActiveLeaf());
                int total = branch.size();
                String header = String.format(loc.getValue(L.LABEL_CHAPTER_OUTLINE_HEADER), total);
                sidebarHeader.setText(header);
                navTitle.setText(header);

                for (int i = 0; i < total; i++) {
                    ChatMessage msg = branch.get(i);
                    createSidebarButton(msg, i + 1, msg.getId());
                }

                branchSearch.refresh(branch);
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
            layout.setDrawerOpened(false);
            if (dbId != null) {
                lazyLoader.scrollTo(dbId);
            }
        });
        sidebarList.add(sidebarBtn);
    }

    /**
     * The content of the drawer: a tab sheet ({@code leftBar}) with the outline of the story, like the left bar of the
     * desktop story. Extensions add their own tabs to it.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = VerticalLayout.class),
            locals = {@APILocals(name = "outlineTab", type = VerticalLayout.class),
                    @APILocals(name = "outline", type = VerticalLayout.class)})
    private VerticalLayout createStoryOutline() {
        // the drawer is narrow, the tabs are not detachable there
        leftBar = new TabDock(false);
        leftBar.setSizeFull();

        VerticalLayout outlineTab = new VerticalLayout();
        outlineTab.setSizeFull();
        outlineTab.setPadding(false);
        outlineTab.setSpacing(false);

        sidebarHeader = new Span();
        sidebarHeader.addClassName(CssStyles.SIDEBAR_HEADER);

        sidebarList = new VerticalLayout();
        sidebarList.setPadding(false);
        sidebarList.setSpacing(false);
        sidebarList.setWidthFull();

        branchSearch = new StoryBranchSearch(this::currentBranch,
                ids -> lazyLoader.renderResponses(ids), id -> {
                    // the drawer covers the story, the user opens it again for the next match
                    layout.setDrawerOpened(false);
                    lazyLoader.showMatch(id, CssStyles.SEARCH_MATCH_CURRENT);
                });

        outlineTab.add(branchSearch, sidebarHeader, sidebarList);
        outlineTab.setFlexGrow(1, sidebarList);
        leftBar.add(ManuscriptStoryPart.CHAPTERS_TAB, loc.getValue(L.LABEL_CHAPTERS), outlineTab);

        VerticalLayout outline = new VerticalLayout(leftBar);
        outline.setSizeFull();
        outline.setPadding(false);
        outline.setSpacing(false);
        return outline;
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

        drawerToggle.setEnabled(!frozen);
        if (frozen) {
            layout.setDrawerOpened(false);
        }
        menuBar.setEnabled(!frozen);

        cards.setFrozen(frozen);

        if (frozen) {
            newTurnPopover.close();
            newTurnPopover.setTarget(null);
            actionButton.setIcon(Solid.STOP.create());
            actionButton.setThemeName("error primary");
        } else {
            newTurnPopover.setTarget(actionButton);
            actionButton.setIcon(Solid.PLUS.create());
            actionButton.setThemeName("primary");
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)})
    @Override
    public void load(Manuscript manuscript) {
        this.currentManuscript = manuscript;
        if (manuscript != null && layout != null) {
            applyLanguage(manuscript);
        }
        renderStoryContent();
        refreshSummariesMenuItem();
    }

    /**
     * What the user typed into the new turn form.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        RestorableState.put(state, "newTurn", newTurnForm == null ? null : newTurnForm.storeState());
        return state;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "newTurn", type = JsonObject.class)})
    @Override
    public void restoreState(JsonObject self) {
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

    /**
     * A part of the story: its text under a small header line with two icons, the menu and the information about
     * the inference (popover, with the turn details). Extensions decorate its methods like
     * the ones of the card of the desktop story.
     */
    @Extendable
    private class ChatMessageCard extends VerticalLayout implements StoryCard {
        private ChatMessage message;
        private boolean isLast;
        private final int orderId;

        private final Button menuBtn;
        private final Button infoBtn;
        private ContextMenu hamburgerMenu;
        private boolean menuItemsCreated;
        private MenuItem editItem;
        private MenuItem discardItem;
        private MenuItem regenerateItem;
        private MenuItem swipeItem;
        private MenuItem redraftItem;
        private MenuItem branchItem;
        private MenuItem mergeItem;
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

        private Popover infoPopover;
        private MessageMetrics metrics;
        private boolean wasReasoningOpenedByGeneration;

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
            addClassName(CssStyles.PHONE_MESSAGE);

            HorizontalLayout headerBar = new HorizontalLayout();
            headerBar.setWidthFull();
            headerBar.setPadding(false);
            headerBar.setSpacing(false);
            headerBar.setAlignItems(FlexComponent.Alignment.CENTER);
            headerBar.addClassName(CssStyles.PHONE_MESSAGE_HEADER);

            menuBtn = new Button(Solid.HAMBURGER.create());
            menuBtn.setThemeName("tertiary icon small");

            infoBtn = new Button(Solid.INFO_CIRCLE.create());
            infoBtn.setThemeName("tertiary icon small");
            UIUtils.addTooltip(infoBtn, loc.getValue(L.LABEL_NODE_METADATA));

            createHamburgerMenu();
            createInfoPopover();

            headerBar.add(menuBtn, infoBtn);

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

            messageImages = new MessageImages(message.getImages());

            add(headerBar, reasoningDetails, responseMarkdown, messageImages);
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void createInfoPopover() {
            infoPopover = new Popover();
            infoPopover.setTarget(infoBtn);
            // opened here, not by the click in the browser, so the content is in the popover before it is positioned (a
            // popover that grows after it opened can end up on the wrong side of its button and be cut off)
            infoPopover.setOpenOnClick(false);
            infoBtn.addClickListener(event -> {
                createDetails();
                infoPopover.setOpened(!infoPopover.isOpened());
            });

            metrics = new MessageMetrics(loc, message, this.orderId, true);
        }

        /**
         * Fills the info popover, once. It is not done when the card is created, because the text fields of the form
         * measure their height in the browser as soon as they are in the page, which makes a story of hundreds of parts
         * freeze it. Regenerating needs the form too, so it calls this as well.
         */
        @PublicAPI(from = "1.4.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "content", type = VerticalLayout.class),
                        @APILocals(name = "detailsHeader", type = Span.class)})
        private void createDetails() {
            if (detailsForm != null) {
                return;
            }
            VerticalLayout content = new VerticalLayout();
            content.setPadding(false);
            content.setSpacing(false);
            content.addClassName(CssStyles.PHONE_POPOVER_CONTENT);

            Span detailsHeader = new Span(loc.getValue(L.LABEL_TURN_DETAILS));
            detailsHeader.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_XS);

            detailsForm = new TurnDetailsForm(loc, message, () -> this.message, () -> this.isLast, this::saveMessage);

            content.add(metrics.getHeader());
            content.add(metrics.getLines());
            content.add(detailsHeader);
            detailsForm.addTo(content);
            infoPopover.add(content);
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
         * Runs each time the menu is about to open: creates the items the first time, then refreshes the ones that
         * depend on the part.
         */
        @PublicAPI(from = "1.4.0",
                returns = @APIReturn(type = void.class))
        private void prepareMenu() {
            if (!menuItemsCreated) {
                menuItemsCreated = true;
                createMenuItems();
            }
            refreshSummaryMenuItems();
            // there is something to discard only while the text is edited
            discardItem.setVisible(editing);
            // the text that is redrafted is the saved one
            redraftItem.setVisible(!editing && StringUtils.isNotBlank(message.getResponse()));
        }

        /**
         * Adds the items to {@code hamburgerMenu}. Called when the menu is opened for the first time, not when the
         * card is created, so the cards of a long story stay cheap.
         */
        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void createMenuItems() {
            editItem = hamburgerMenu.addItem(loc.getValue(editing ? L.LABEL_SAVE : L.LABEL_EDIT), event -> toggleEdit());

            discardItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DISCARD_CHANGES), event -> discardEdit());
            discardItem.setVisible(editing);

            regenerateItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_REGENERATE), event -> regenerate());
            regenerateItem.setVisible(this.isLast);

            swipeItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_SWIPE), event -> swipe());
            swipeItem.setVisible(this.isLast);

            redraftItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_REDRAFT), event -> redraft());

            branchItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_BRANCH_STORY), event -> branch());

            mergeItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_MERGE_INTO_PREVIOUS), event -> mergeIntoPrevious());
            mergeItem.setVisible(message.getParent() != null);

            showPromptItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_SHOW_PROMPT), event -> showPrompt());

            tokenUsageItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_TOKEN_USAGE), event -> showTokenUsage());

            imagesItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_IMAGES), event -> openImagesDialog());

            summaryItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_GENERATE_SUMMARY), event -> openSummaryDialog());
            deleteSummaryItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DELETE_SUMMARY), event -> confirmDeleteSummary());

            deleteItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DELETE), event -> {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        chatMessageService.deleteNodeAndMigrateChildren(message, currentManuscript, false);

                        parent.refreshManuscript();
                        load(parent.getManuscript());
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                });
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

            if (summaryItem == null) {
                return;
            }

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

        /**
         * Opens the dialog that lets the model write the part again from the instructions of the author, see
         * {@link RedraftDialog}. The part is replaced when the author applies the result.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class),
                locals = {@APILocals(name = "e", type = Exception.class)})
        private void redraft() {
            try {
                currentManuscript = parent.refreshManuscript();
                RedraftDialog.open(currentManuscript, message, saved -> {
                    message = saved;
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
        private void mergeIntoPrevious() {
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
            summaryDialog.onClosed(PhoneManuscriptStoryPart.this::refreshSummariesMenuItem);
            summaryDialog.open();
            if (!hasSummary) {
                summaryDialog.startGeneration();
            }
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void regenerate() {
            createDetails();
            startGeneration(GenerationRequest.regenerate(message), detailsForm.toTurnInput());
        }

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class))
        private void swipe() {
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
            if (regenerateItem != null) {
                regenerateItem.setVisible(isLast);
            }
            if (swipeItem != null) {
                swipeItem.setVisible(isLast);
            }
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
            if (infoBtn != null) {
                infoBtn.setEnabled(!frozen);
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
                // created only while a part is edited: a text area measures its height in the browser as soon as it
                // is in the page, which makes a story of hundreds of parts freeze it
                responseTextArea = new TextArea();
                responseTextArea.setWidthFull();
                responseTextArea.setMinHeight("100px");
                responseTextArea.setValue(StringUtils.defaultString(message.getResponse()));
                addComponentAtIndex(indexOf(responseMarkdown) + 1, responseTextArea);
                responseMarkdown.setVisible(false);
                if (editItem != null) {
                    editItem.setText(loc.getValue(L.LABEL_SAVE));
                }
            }
        }

        /**
         * Leaves the editor without saving: the part keeps the text it was last saved with. The author is asked first
         * when the text in the editor differs from it.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
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
            remove(responseTextArea);
            responseTextArea = null;
            responseMarkdown.setVisible(true);
            editing = false;
            if (editItem != null) {
                editItem.setText(loc.getValue(L.LABEL_EDIT));
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
                    remove(responseTextArea);
                    responseTextArea = null;
                    responseMarkdown.setVisible(true);
                    editing = false;
                    if (editItem != null) {
                        editItem.setText(loc.getValue(L.LABEL_EDIT));
                    }
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

        @PublicAPI(from = "1.1.0",
                returns = @APIReturn(type = void.class),
                parameters = {@APIParameter(name = "responseText", type = String.class)})
        public void updateResponse(String responseText) {
            message.setResponse(responseText);
            renderResponse();
        }

        /**
         * Draws the response of the message, the places the search of the outline found are marked.
         */
        @PublicAPI(from = "1.5.0",
                returns = @APIReturn(type = void.class))
        @Override
        public void renderResponse() {
            responseMarkdown.setContent(branchSearch.mark(message.getId(), StringUtils.defaultString(message.getResponse())));
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
