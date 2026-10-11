package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequestType;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.JavaTokkitStrategy;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ExportDialog;
import com.github.enerccio.marginalia.ui.dialogs.PromptDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ImagesDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.SummaryDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.SummaryRemoval;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneSummariesDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
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
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final String NOT_AVAILABLE = "N/A";

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

    @Autowired
    private BackupService backupService;

    @Autowired
    private InferenceServices inferenceServices;

    private final PhoneManuscriptDialog parent;
    private CancellationToken activeGenerationToken;

    private AppLayout layout;
    private DrawerToggle drawerToggle;
    private Span navTitle;
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
    private TextArea sceneSettingField;
    private TextField povCharacterField;
    private TextArea presentCharactersField;
    private TextArea instructionsField;

    private String pendingSceneSetting = "";
    private String pendingPovCharacter = "";
    private String pendingPresentCharacters = "";
    private String pendingInstructions = "";
    private boolean editedByUser = false;

    private Manuscript currentManuscript;
    private boolean isFrozen = false;

    private final Map<Long, ChatMessageCard> activeCardMap = new LinkedHashMap<>();
    private ChatMessageCard streamingCard;
    private Long scrollToMessageId;

    public PhoneManuscriptStoryPart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

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

        setupScrollListener();

        bottomControlsLayout = buildBottomControls();

        centerLayout.add(centerContentPanel, bottomControlsLayout);
        centerLayout.setFlexGrow(1, centerContentPanel);
        centerLayout.setFlexGrow(0, bottomControlsLayout);

        layout.setContent(centerLayout);
        applyBookStyles();
        return layout;
    }

    private void setupScrollListener() {
        centerContentPanel.getElement().addEventListener("panel-scroll", event -> {
            if (isFrozen) {
                return;
            }
            double pos = event.getEventData().get("event.detail.scrollTop").asDouble();
            if (currentManuscript != null && currentManuscript.getActiveLeaf() != null) {
                try {
                    ChatMessage leaf = chatMessageService.find(currentManuscript.getActiveLeaf());
                    leaf.setScrollPosition((int) pos);
                    chatMessageService.save(leaf);
                } catch (Exception ignored) {}
            }
        }).addEventData("event.detail.scrollTop");

        JavascriptLibrary.fireDebouncedScrollEvent(centerContentPanel.getElement());
    }

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
            layout.addClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        } else {
            layout.removeClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);
        }
    }

    // lang drives the browser's hyphenation dictionary (see hyphens in shared-styles.css)
    private void applyLanguage(Manuscript manuscript) {
        layout.getElement().setAttribute("lang", manuscript.getLanguage().getCode());
    }

    private Popover buildNewTurnPopover(Button targetButton) {
        Popover popover = new Popover();
        popover.setTarget(targetButton);
        popover.setPosition(PopoverPosition.TOP);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.addClassName(CssStyles.PHONE_POPOVER_CONTENT);

        Span title = new Span(loc.getValue(L.LABEL_NEW_TURN_INSTRUCTIONS));
        title.addClassName(CssStyles.TEXT_BOLD);

        sceneSettingField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING));
        sceneSettingField.setWidthFull();
        ResizableTextArea.install(loc, sceneSettingField, "100px");
        sceneSettingField.addValueChangeListener(e -> {
            pendingSceneSetting = e.getValue();
            editedByUser = true;
        });

        povCharacterField = new TextField(loc.getValue(L.LABEL_POV_CHARACTER));
        povCharacterField.setWidthFull();
        povCharacterField.addValueChangeListener(e -> {
            pendingPovCharacter = e.getValue();
            editedByUser = true;
        });

        presentCharactersField = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS));
        presentCharactersField.setWidthFull();
        ResizableTextArea.install(loc, presentCharactersField, "80px");
        presentCharactersField.addValueChangeListener(e -> {
            pendingPresentCharacters = e.getValue();
            editedByUser = true;
        });

        instructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS));
        instructionsField.setWidthFull();
        ResizableTextArea.install(loc, instructionsField, "120px");
        instructionsField.addValueChangeListener(e -> {
            pendingInstructions = e.getValue();
            editedByUser = true;
        });

        Button generateBtn = new Button(loc.getValue(L.LABEL_GENERATE), Solid.PLAY.create(), event -> {
            popover.close();
            if (parent != null) {
                editedByUser = false;
                autosaveAndSwapAllToMarkdown();
                startGeneration();
            }
        });
        generateBtn.setThemeName("primary small");
        generateBtn.setWidthFull();

        content.add(title, sceneSettingField, povCharacterField, presentCharactersField, instructionsField, generateBtn);
        popover.add(content);

        popover.addOpenedChangeListener(event -> {
            if (event.isOpened() && !editedByUser) {
                syncFieldsFromState();
            }
        });

        return popover;
    }

    private void syncFieldsFromState() {
        ChatMessage lastMsg = currentManuscript != null ? currentManuscript.getActiveLeaf() : null;
        if (lastMsg != null) {
            pendingSceneSetting = StringUtils.defaultString(lastMsg.getSceneSetting());
            pendingPovCharacter = StringUtils.defaultString(lastMsg.getPovCharacter());
            pendingPresentCharacters = StringUtils.defaultString(lastMsg.getPresentCharacters());
        }
        pendingInstructions = "";

        sceneSettingField.setValue(pendingSceneSetting);
        povCharacterField.setValue(pendingPovCharacter);
        presentCharactersField.setValue(pendingPresentCharacters);
        instructionsField.setValue(pendingInstructions);
    }

    private void renderStoryContent() {
        centerContentPanel.removeAll();
        sidebarList.removeAll();
        activeCardMap.clear();

        if (currentManuscript == null || currentManuscript.getActiveLeaf() == null) {
            sidebarHeader.setText("");
            navTitle.setText("");
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
                    int orderId = i + 1;
                    Long dbId = msg.getId();

                    createSidebarButton(msg, orderId, dbId);

                    boolean isLast = (i == total - 1);
                    ChatMessageCard card = new ChatMessageCard(msg, isLast, orderId);
                    card.setFrozen(isFrozen);
                    if (msg.getId() != null) {
                        activeCardMap.put(msg.getId(), card);
                    }
                    centerContentPanel.add(card);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }

            ChatMessageCard target = scrollToMessageId != null ? activeCardMap.get(scrollToMessageId) : null;
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
            layout.setDrawerOpened(false);
            if (dbId != null && activeCardMap.containsKey(dbId)) {
                activeCardMap.get(dbId).scrollIntoView();
            }
        });
        sidebarList.add(sidebarBtn);
    }

    /**
     * The content of the drawer: a tab sheet ({@code leftBar}) with the outline of the story, like the left bar of the
     * desktop story. Extensions add their own tabs to it.
     */
    private VerticalLayout createStoryOutline() {
        leftBar = new VTabSheet();
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

        outlineTab.add(sidebarHeader, sidebarList);
        outlineTab.setFlexGrow(1, sidebarList);
        leftBar.add(loc.getValue(L.LABEL_CHAPTERS), outlineTab);

        VerticalLayout outline = new VerticalLayout(leftBar);
        outline.setSizeFull();
        outline.setPadding(false);
        outline.setSpacing(false);
        return outline;
    }

    private void updateLastFlagsForNewCard() {
        for (ChatMessageCard card : activeCardMap.values()) {
            card.setLastMessage(false);
        }
        if (streamingCard != null) {
            streamingCard.setLastMessage(false);
        }
    }

    private void restoreScrollPosition() {
        ChatMessage leaf = currentManuscript != null ? currentManuscript.getActiveLeaf() : null;
        Integer savedPos = (leaf != null) ? leaf.getScrollPosition() : null;

        JavascriptLibrary.restoreScrollPosition(centerContentPanel.getElement(), savedPos);
    }

    public void autosaveAndSwapAllToMarkdown() {
        for (ChatMessageCard card : activeCardMap.values()) {
            if (card.isEditing()) {
                card.autosaveAndSwapToMarkdown();
            }
        }
        if (streamingCard != null && streamingCard.isEditing()) {
            streamingCard.autosaveAndSwapToMarkdown();
        }
    }

    private void scrollToBottom() {
        JavascriptLibrary.scrollToBottomAfterLayout(centerContentPanel.getElement());
    }

    private void scrollToBottomIfAtBottom() {
        JavascriptLibrary.scrollToBottomIfAtBottom(centerContentPanel.getElement());
    }

    private void startGeneration() {
        TurnInput input = new TurnInput(
                pendingSceneSetting,
                pendingPovCharacter,
                pendingPresentCharacters,
                pendingInstructions
        );

        startGeneration(GenerationRequest.newMessage(), input);
    }

    private void checkAndApplyBackupStrategy() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            if (manuscript == null) {
                return;
            }

            BackupStrategy strategy = manuscriptService.getBackupStrategy(manuscript);
            String strategyValStr = manuscriptService.getBackupStrategyValue(manuscript);

            if (strategy == null || strategy == BackupStrategy.DISABLED) {
                if (manuscript.getBackupStrategyCurrentValue() != null) {
                    manuscript.setBackupStrategyCurrentValue(null);
                    manuscriptService.save(manuscript);
                }
                return;
            }

            int limit = 1;
            if (StringUtils.isNotBlank(strategyValStr)) {
                try {
                    limit = Math.max(1, Integer.parseInt(strategyValStr.trim()));
                } catch (NumberFormatException ignored) {}
            }

            String prefix = strategy.name() + ":" + limit + ":";
            String currentVal = manuscript.getBackupStrategyCurrentValue();

            if (strategy == BackupStrategy.AFTER_N_MESSAGES) {
                int count = 0;
                if (currentVal != null && currentVal.startsWith(prefix)) {
                    try {
                        count = Integer.parseInt(currentVal.substring(prefix.length()));
                    } catch (NumberFormatException ignored) {}
                }

                count++;

                if (count >= limit) {
                    try {
                        backupService.takeBackup(manuscript);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                    count = 0;
                }

                manuscript.setBackupStrategyCurrentValue(prefix + count);
                parent.save();
            } else if (strategy == BackupStrategy.AFTER_N_MINUTES) {
                long lastBackupTime = System.currentTimeMillis();
                boolean prefixValid = currentVal != null && currentVal.startsWith(prefix);

                if (prefixValid) {
                    try {
                        lastBackupTime = Long.parseLong(currentVal.substring(prefix.length()));
                    } catch (NumberFormatException ignored) {
                        prefixValid = false;
                    }
                }

                if (!prefixValid) {
                    lastBackupTime = System.currentTimeMillis();
                }

                long now = System.currentTimeMillis();
                long elapsedMinutes = (now - lastBackupTime) / (1000 * 60);

                if (elapsedMinutes >= limit) {
                    try {
                        backupService.takeBackup(manuscript);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                    lastBackupTime = now;
                }

                manuscript.setBackupStrategyCurrentValue(prefix + lastBackupTime);
                parent.save();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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
                    new GenerationListener() {

                        @Override
                        public void onNodeCreated(ChatMessage message) {
                            ui.access(() -> {
                                updateLastFlagsForNewCard();
                                int newOrderId = activeCardMap.size() + 1;

                                if (request.getRequestType() == GenerationRequestType.NEW_MESSAGE) {
                                    ChatMessageCard card = new ChatMessageCard(message, true, newOrderId);
                                    card.setFrozen(true);
                                    if (message.getId() != null) {
                                        activeCardMap.put(message.getId(), card);
                                    }
                                    streamingCard = card;
                                    centerContentPanel.add(card);
                                } else if (request.getRequestType() == GenerationRequestType.REGENERATE) {
                                    ChatMessageCard card = getOrCreateCard(message, newOrderId);
                                    card.updateReasoning("");
                                    card.updateResponse("");
                                    card.updateMetrics(message);
                                    card.wasReasoningOpenedByGeneration = false;
                                    streamingCard = card;
                                } else {
                                    List<ChatMessageCard> cards = activeCardMap.values().stream().toList();
                                    ChatMessageCard last = cards.getLast();
                                    activeCardMap.remove(last.message.getId());
                                    centerContentPanel.remove(last);
                                    ChatMessageCard card = new ChatMessageCard(message, true, newOrderId);
                                    card.setFrozen(true);
                                    if (message.getId() != null) {
                                        activeCardMap.put(message.getId(), card);
                                    }
                                    streamingCard = card;
                                    centerContentPanel.add(card);
                                }
                                scrollToBottom();
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onReasoningChunk(String chunk, ChatMessage message) {
                            ui.access(() -> {
                                ChatMessageCard card = getOrCreateCard(message, activeCardMap.size());
                                if (card != null) {
                                    card.updateReasoning(message.getResponseReasoning());
                                    scrollToBottomIfAtBottom();
                                }
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onResponseChunk(String chunk, ChatMessage message) {
                            ui.access(() -> {
                                ChatMessageCard card = getOrCreateCard(message, activeCardMap.size());
                                if (card != null) {
                                    card.updateResponse(message.getResponse());
                                    scrollToBottomIfAtBottom();
                                }
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onMetricsUpdated(ChatMessage message) {
                            ui.access(() -> {
                                ChatMessageCard card = getOrCreateCard(message, activeCardMap.size());
                                if (card != null) {
                                    card.updateMetrics(message);
                                }
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onComplete(ChatMessage message) {
                            ui.access(() -> {
                                activeGenerationToken = null;
                                streamingCard = null;
                                parent.unfreeze();

                                checkAndApplyBackupStrategy();

                                JavascriptLibrary.getScrollTop(centerContentPanel.getElement())
                                        .then(Integer.class, scrollTop -> {
                                            if (scrollTop != null && message != null) {
                                                message.setScrollPosition(scrollTop);
                                                try {
                                                    chatMessageService.save(message);
                                                } catch (Exception ignored) {
                                                }
                                            }
                                            renderStoryContent();
                                            UIPushGuard.push(ui);
                                        });
                            });
                        }

                        @Override
                        public void onCancelled(ChatMessage partialMessage) {
                            ui.access(() -> {
                                updateLastFlagsForNewCard();
                                if (partialMessage == null) {
                                    // the turn was discarded: keep what the user typed instead of re-syncing (and clearing) the form
                                    editedByUser = true;
                                    if (streamingCard != null)
                                        centerContentPanel.remove(streamingCard);
                                } else {
                                    if (streamingCard != null && !partialMessage.getId().equals(streamingCard.message.getId())) {
                                        centerContentPanel.remove(streamingCard);
                                        ChatMessageCard card = new ChatMessageCard(partialMessage, true, activeCardMap.size() + 1);
                                        card.setFrozen(true);
                                        activeCardMap.put(partialMessage.getId(), card);
                                        streamingCard = null;
                                        centerContentPanel.add(card);
                                    }
                                }

                                activeGenerationToken = null;
                                streamingCard = null;
                                parent.unfreeze();

                                JavascriptLibrary.getScrollTop(centerContentPanel.getElement())
                                        .then(Integer.class, scrollTop -> {
                                            if (scrollTop != null && partialMessage != null) {
                                                partialMessage.setScrollPosition(scrollTop);
                                                try {
                                                    chatMessageService.save(partialMessage);
                                                } catch (Exception ignored) {
                                                }
                                            }
                                            try {
                                                // cleanup may have removed the new part and changed the active one
                                                currentManuscript = parent.refreshManuscript();
                                            } catch (Exception e) {
                                                UIUtils.internalServerError(loc, e);
                                            }
                                            renderStoryContent();
                                            UIPushGuard.push(ui);
                                        });
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onError(Throwable cause) {
                            ui.access(() -> {
                                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), cause);
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onSimpleError(String error) {
                            ui.access(() -> {
                                Notification.error(error);
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void onWarning(String warning) {
                            ui.access(() -> {
                                Notification.warning(warning);
                                UIPushGuard.push(ui);
                            });
                        }

                        @Override
                        public void askQuestion(String question, Runnable yes, Runnable no) {
                            ui.access(() -> {
                                ConfirmDialog.show(question, yes, no);
                                UIPushGuard.push(ui);
                            });
                        }
                    }
            );
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private ChatMessageCard getOrCreateCard(ChatMessage message, int fallbackOrderId) {
        if (message == null) return streamingCard;
        if (message.getId() != null && activeCardMap.containsKey(message.getId())) {
            return activeCardMap.get(message.getId());
        }
        return streamingCard;
    }

    @Override
    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;

        drawerToggle.setEnabled(!frozen);
        if (frozen) {
            layout.setDrawerOpened(false);
        }
        menuBar.setEnabled(!frozen);

        for (ChatMessageCard card : activeCardMap.values()) {
            card.setFrozen(frozen);
        }
        if (streamingCard != null) {
            streamingCard.setFrozen(frozen);
        }

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

    @Override
    public void load(Manuscript manuscript) {
        this.currentManuscript = manuscript;
        if (manuscript != null && layout != null) {
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

    /**
     * A part of the story: its text under a small header line with two icons, the menu and the information about
     * the inference (popover, with the turn details). Extensions decorate its methods like
     * the ones of the card of the desktop story.
     */
    @Extendable
    private class ChatMessageCard extends VerticalLayout {
        private ChatMessage message;
        private boolean isLast;
        private final int orderId;

        private final Button menuBtn;
        private final Button infoBtn;
        private ContextMenu hamburgerMenu;
        private MenuItem editItem;
        private MenuItem regenerateItem;
        private MenuItem swipeItem;
        private MenuItem branchItem;
        private MenuItem summaryItem;
        private MenuItem deleteSummaryItem;
        private MenuItem deleteItem;
        private MenuItem showPromptItem;
        private MenuItem imagesItem;
        private Details reasoningDetails;
        private Markdown reasoningMarkdown;
        private Markdown responseMarkdown;
        private MessageImages messageImages;
        private TextArea responseTextArea;
        private boolean editing = false;

        private Popover infoPopover;
        private Span idSpan;
        private Span modelSpan;
        private Span protocolSpan;
        private Span tokensSpan;
        private Span wordsSpan;
        private boolean wasReasoningOpenedByGeneration;

        private TextArea detailsSceneField;
        private TextField detailsPovField;
        private TextArea detailsPresentField;
        private TextArea detailsInstructionsField;

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

            createMenuItems();
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
            if (StringUtils.isNotBlank(message.getResponse())) {
                responseMarkdown.setContent(message.getResponse());
            }

            responseTextArea = new TextArea();
            responseTextArea.setWidthFull();
            responseTextArea.setMinHeight("100px");
            responseTextArea.setValue(StringUtils.defaultString(message.getResponse()));
            responseTextArea.setVisible(false);

            messageImages = new MessageImages(message.getImages());

            add(headerBar, reasoningDetails, responseMarkdown, responseTextArea, messageImages);
        }

        private void createInfoPopover() {
            infoPopover = new Popover();
            infoPopover.setTarget(infoBtn);

            VerticalLayout content = new VerticalLayout();
            content.setPadding(false);
            content.setSpacing(false);
            content.addClassName(CssStyles.PHONE_POPOVER_CONTENT);

            Span metaHeader = new Span(loc.getValue(L.LABEL_NODE_METADATA));
            metaHeader.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_XS);

            idSpan = new Span(String.format(loc.getValue(L.LABEL_MESSAGE_IDS), this.orderId, (message.getId() != null ? message.getId() : NOT_AVAILABLE)));
            idSpan.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_XS);

            String modelName = StringUtils.defaultIfBlank(message.getModelUsed(), NOT_AVAILABLE);
            modelSpan = new Span(loc.getValue(L.LABEL_MODEL) + ": " + modelName);
            modelSpan.addClassName(CssStyles.TEXT_XS);

            protocolSpan = new Span(loc.getValue(L.LABEL_PROTOCOLS) + ": " + StringUtils.defaultIfBlank(message.getProtocolUsed(), NOT_AVAILABLE));
            protocolSpan.addClassName(CssStyles.TEXT_XS);

            tokensSpan = new Span(loc.getValue(L.LABEL_TOKENS) + ": " + message.getTokenCount() + " (" + message.getPromptTokens() + ")");
            tokensSpan.addClassName(CssStyles.TEXT_XS);

            wordsSpan = new Span(loc.getValue(L.LABEL_WORDS) + ": " + message.getWordCount());
            wordsSpan.addClassName(CssStyles.TEXT_XS);

            Span detailsHeader = new Span(loc.getValue(L.LABEL_TURN_DETAILS));
            detailsHeader.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_XS);

            detailsSceneField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING), StringUtils.defaultString(message.getSceneSetting()), "");
            detailsSceneField.setReadOnly(!this.isLast);
            detailsSceneField.setWidthFull();
            ResizableTextArea.install(loc, detailsSceneField, "100px");
            detailsSceneField.addValueChangeListener(e -> {
                if (e.isFromClient() && this.isLast) {
                    this.message.setSceneSetting(e.getValue());
                    saveMessage();
                }
            });

            detailsPovField = new TextField(loc.getValue(L.LABEL_POV_CHARACTER), StringUtils.defaultString(message.getPovCharacter()), "");
            detailsPovField.setReadOnly(!this.isLast);
            detailsPovField.setWidthFull();
            detailsPovField.addValueChangeListener(e -> {
                if (e.isFromClient() && this.isLast) {
                    this.message.setPovCharacter(e.getValue());
                    saveMessage();
                }
            });

            detailsPresentField = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS), StringUtils.defaultString(message.getPresentCharacters()), "");
            detailsPresentField.setReadOnly(!this.isLast);
            detailsPresentField.setWidthFull();
            ResizableTextArea.install(loc, detailsPresentField, "80px");
            detailsPresentField.addValueChangeListener(e -> {
                if (e.isFromClient() && this.isLast) {
                    this.message.setPresentCharacters(e.getValue());
                    saveMessage();
                }
            });

            detailsInstructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS), StringUtils.defaultString(message.getInstructions()), "");
            detailsInstructionsField.setReadOnly(!this.isLast);
            detailsInstructionsField.setWidthFull();
            ResizableTextArea.install(loc, detailsInstructionsField, "120px");
            detailsInstructionsField.addValueChangeListener(e -> {
                if (e.isFromClient() && this.isLast) {
                    this.message.setInstructions(e.getValue());
                    saveMessage();
                }
            });

            content.add(metaHeader, idSpan, modelSpan, protocolSpan, tokensSpan, wordsSpan, detailsHeader,
                    detailsSceneField, detailsPovField, detailsPresentField, detailsInstructionsField);
            infoPopover.add(content);
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
                            } else if (parentId != null && activeCardMap.containsKey(parentId)) {
                                activeCardMap.get(parentId).scrollIntoView();
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
            summaryDialog.onClosed(PhoneManuscriptStoryPart.this::refreshSummariesMenuItem);
            summaryDialog.open();
            if (!hasSummary) {
                summaryDialog.startGeneration();
            }
        }

        private void regenerate() {
            TurnInput input = new TurnInput(
                    detailsSceneField.getValue(),
                    detailsPovField.getValue(),
                    detailsPresentField.getValue(),
                    detailsInstructionsField.getValue()
            );
            startGeneration(GenerationRequest.regenerate(message), input);
        }

        private void swipe() {
            TurnInput input = new TurnInput(
                    detailsSceneField.getValue(),
                    detailsPovField.getValue(),
                    detailsPresentField.getValue(),
                    detailsInstructionsField.getValue()
            );
            startGeneration(GenerationRequest.newSwipe(message), input);
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
            if (detailsSceneField != null) {
                detailsSceneField.setReadOnly(!isLast);
            }
            if (detailsPovField != null) {
                detailsPovField.setReadOnly(!isLast);
            }
            if (detailsPresentField != null) {
                detailsPresentField.setReadOnly(!isLast);
            }
            if (detailsInstructionsField != null) {
                detailsInstructionsField.setReadOnly(!isLast);
            }
        }

        public void setFrozen(boolean frozen) {
            if (menuBtn != null) {
                menuBtn.setEnabled(!frozen);
            }
            if (infoBtn != null) {
                infoBtn.setEnabled(!frozen);
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
                    message.setResponse(newResponse);

                    InferenceService inferenceService = inferenceServices.forAI(currentManuscript.getAi());
                    if (inferenceService == null) {
                        JavaTokkitStrategy strategy = new JavaTokkitStrategy();
                        message.setTokenCount(strategy.countTokens(null, newResponse));
                    } else {
                        message.setTokenCount(inferenceService.countTokens(newResponse));
                    }
                    message.setWordCount(chatMessageService.countWords(newResponse));
                    message.setEdited(true);
                    message = chatMessageService.save(message);
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

        public void updateMetrics(ChatMessage msg) {
            String modelName = StringUtils.defaultIfBlank(msg.getModelUsed(), NOT_AVAILABLE);
            idSpan.setText(String.format(loc.getValue(L.LABEL_MESSAGE_IDS), this.orderId, (message.getId() != null ? message.getId() : NOT_AVAILABLE)));
            modelSpan.setText(loc.getValue(L.LABEL_MODEL) + ": " + modelName);
            protocolSpan.setText(loc.getValue(L.LABEL_PROTOCOLS) + ": " + StringUtils.defaultIfBlank(msg.getProtocolUsed(), NOT_AVAILABLE));
            tokensSpan.setText(loc.getValue(L.LABEL_TOKENS) + ": " + msg.getTokenCount() + " (" + msg.getPromptTokens() + ")");
            wordsSpan.setText(loc.getValue(L.LABEL_WORDS) + ": " + msg.getWordCount());
        }

        private void showPrompt() {
            PromptDialog dialog = new PromptDialog(message.getBuiltPrompt());
            dialog.create();
            dialog.open();
        }
    }
}
