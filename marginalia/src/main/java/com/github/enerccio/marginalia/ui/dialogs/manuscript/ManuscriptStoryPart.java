package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.dialogs.story.*;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
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
    private VTabSheet leftBar;
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
    private final StoryBackup backup = new StoryBackup();
    private Long scrollToMessageId;

    public ManuscriptStoryPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

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
        leftBar = new VTabSheet();
        leftBar.setSizeFull();
        leftMarginLayout.add(leftBar);
        createStoryOutlineTab();

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

        container.add(loc.getValue(L.LABEL_STORY_PART), mainLayout);
        return mainLayout;
    }

    private void setupScrollListener() {
        support.trackScrollPosition(centerContentPanel, () -> currentManuscript, () -> isFrozen);
    }

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
        layout.add(actionButton);

        return layout;
    }

    /**
     * Menu bar of the story: the summaries item and the settings (cogs) item. Extensions add to it by decorating the
     * methods that fill it (their argument {@code bar} is the menu bar): {@link #populateMenuBar} for new items of the
     * bar, {@link #createCogsMenuItem} (local variable {@code cogs}) for new items of the settings menu.
     */
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
    private void populateMenuBar(MenuBar bar) {
    }

    private MenuItem createSummariesMenuItem(MenuBar bar) {
        MenuItem item = bar.addItem(Solid.LIST_UL.create(), event -> openSummariesDialog());
        UIUtils.addTooltip(item, loc.getValue(L.LABEL_SUMMARIES));
        return item;
    }

    /**
     * The story settings menu, extensions add their items to its sub menu ({@code cogsMenuItem.getSubMenu()}).
     */
    private MenuItem createCogsMenuItem(MenuBar bar) {
        MenuItem cogs = bar.addItem(Solid.COGS.create());
        changeStyles = createChangeStylesMenuItem(cogs);
        createExportMenuItem(cogs);
        return cogs;
    }

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

    private MenuItem createExportMenuItem(MenuItem cogs) {
        return cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.FILE_EXPORT.create(), loc.getValue(L.LABEL_EXPORT_STORY)), event -> {
            try {
                new ExportDialog(parent.refreshManuscript()).open();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

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
    private void applyLanguage(Manuscript manuscript) {
        mainLayout.getElement().setAttribute("lang", manuscript.getLanguage().getCode());
    }

    private Popover buildNewTurnPopover(Button targetButton) {
        Popover popover = new Popover();
        popover.setTarget(targetButton);

        newTurnForm = new NewTurnForm(loc);
        VerticalLayout content = newTurnForm.createContent(() -> {
            popover.close();
            if (parent != null) {
                newTurnForm.consume();
                autosaveAndSwapAllToMarkdown();
                startGeneration();
            }
        });
        content.setWidth("540px");
        popover.add(content);

        popover.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                newTurnForm.syncIfUntouched(currentManuscript != null ? currentManuscript.getActiveLeaf() : null);
            }
        });

        return popover;
    }

    /**
     * Redraws the message cards and the chapter outline. The left bar (and so the selected tab) is kept, the state of
     * the cards that is not stored in the message - which reasonings are opened - is carried over to the new cards.
     */
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
    public void scrollToMessageOnNextRender(Long messageId) {
        this.scrollToMessageId = messageId;
    }

    private void scrollToCard(ChatMessageCard card) {
        JavascriptLibrary.scrollToAndHighlight(card.getElement());
    }

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

    private void createStoryOutlineTab() {
        VerticalLayout leftMarginLayout = new VerticalLayout();
        leftMarginLayout.setSizeFull();
        leftBar.add(loc.getValue(L.LABEL_CHAPTERS), leftMarginLayout);

        sidebarHeader = new Span();
        sidebarHeader.addClassName(CssStyles.SIDEBAR_HEADER);

        sidebarList = new VerticalLayout();
        sidebarList.setPadding(false);
        sidebarList.setSpacing(false);
        sidebarList.setWidthFull();

        leftMarginLayout.add(sidebarHeader, sidebarList);
        leftMarginLayout.setFlexGrow(1, sidebarList);
    }

    private void restoreScrollPosition() {
        ChatMessage leaf = currentManuscript != null ? currentManuscript.getActiveLeaf() : null;
        Integer savedPos = (leaf != null) ? leaf.getScrollPosition() : null;

        JavascriptLibrary.restoreScrollPosition(centerContentPanel.getElement(), savedPos);
    }

    public void autosaveAndSwapAllToMarkdown() {
        cards.autosaveAndSwapAllToMarkdown();
    }

    private void startGeneration() {
        startGeneration(GenerationRequest.newMessage(), newTurnForm.toTurnInput());
    }

    private void checkAndApplyBackupStrategy() {
        backup.checkAndApplyBackupStrategy(parent);
    }

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

    @Override
    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;

        leftMarginLayout.setEnabled(!frozen);
        menuBar.setEnabled(!frozen);

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

    @Override
    public void load(Manuscript manuscript) {
        this.currentManuscript = manuscript;
        if (manuscript != null && mainLayout != null) {
            applyLanguage(manuscript);
        }
        renderStoryContent();
        refreshSummariesMenuItem();
    }

    @Override
    public void onTabLeave() throws Exception {
        autosaveAndSwapAllToMarkdown();
    }

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
            if (StringUtils.isNotBlank(message.getResponse())) {
                responseMarkdown.setContent(message.getResponse());
            }

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

        private void openImagesDialog() {
            new ImagesDialog(message, updated -> {
                message = updated;
                messageImages.setImages(updated.getImages());
            }).open();
        }

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

        private void confirmDeleteSummary() {
            SummaryRemoval.confirmAndRemove(loc, summaryService, message, updated -> {
                message = updated;
                refreshSummaryMenuItems();
                refreshSummariesMenuItem();
            });
        }

        private void branch() {
            try {
                currentManuscript = parent.refreshManuscript();
                currentManuscript.setActiveLeaf(chatMessageService.branch(currentManuscript, message));
                load(parent.save());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

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

        private void regenerate() {
            startGeneration(GenerationRequest.regenerate(message), detailsForm.toTurnInput());
        }

        private void swipe() {
            startGeneration(GenerationRequest.newSwipe(message), detailsForm.toTurnInput());
        }

        private void saveMessage() {
            try {
                this.message = chatMessageService.save(this.message);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

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

        public void setFrozen(boolean frozen) {
            if (menuBtn != null) {
                menuBtn.setEnabled(!frozen);
            }
            if (turnDetailsBtn != null) {
                turnDetailsBtn.setEnabled(!frozen);
            }
        }

        public boolean isEditing() {
            return editing;
        }

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

        public void autosaveAndSwapToMarkdown() {
            if (editing) {
                try {
                    String newResponse = responseTextArea.getValue();
                    message = support.saveEditedResponse(currentManuscript, message, newResponse);
                    responseMarkdown.setContent(StringUtils.defaultString(newResponse));
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

        public boolean isReasoningOpened() {
            return reasoningDetails.isOpened();
        }

        public void setReasoningOpened(boolean opened) {
            if (opened) {
                reasoningDetails.setOpened(true);
                wasReasoningOpenedByGeneration = true;
            }
        }

        public void updateReasoning(String reasoningText) {
            if (!wasReasoningOpenedByGeneration) {
                reasoningDetails.setOpened(true);
                wasReasoningOpenedByGeneration = true;
            }
            message.setResponseReasoning(reasoningText);
            reasoningMarkdown.setContent(StringUtils.defaultString(reasoningText));
        }

        public void updateResponse(String responseText) {
            message.setResponse(responseText);
            responseMarkdown.setContent(StringUtils.defaultString(responseText));
            if (!editing) {
                responseTextArea.setValue(StringUtils.defaultString(responseText));
            }
        }

        @Override
        public ChatMessage getMessage() {
            return message;
        }

        @Override
        public void restartStreaming(ChatMessage msg) {
            updateReasoning("");
            updateResponse("");
            updateMetrics(msg);
            wasReasoningOpenedByGeneration = false;
        }

        public void updateMetrics(ChatMessage msg) {
            metrics.update(message, msg);
        }

        private void showTokenUsage() {
            // null for parts generated before the breakdown was recorded, the dialog says so
            TokenUsageDialog dialog = new TokenUsageDialog(message.getTokenBreakdown());
            dialog.create();
            dialog.open();
        }

        private void showPrompt() {
            PromptDialog dialog = new PromptDialog(message.getBuiltPrompt());
            dialog.create();
            dialog.open();
        }
    }
}
