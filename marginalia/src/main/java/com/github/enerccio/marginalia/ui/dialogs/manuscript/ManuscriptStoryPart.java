package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.SharedStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequestType;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.PromptDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configurable
public class ManuscriptStoryPart implements ManuscriptDialogPart {

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

    private Manuscript currentManuscript;
    private boolean isFrozen = false;

    private final Map<Long, ChatMessageCard> activeCardMap = new LinkedHashMap<>();
    private ChatMessageCard streamingCard;

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
        leftMarginLayout.getStyle().set("border-right", "1px solid var(--lumo-contrast-10pct)");

        centerLayout = new VerticalLayout();
        centerLayout.setSizeFull();
        centerLayout.setPadding(false);
        centerLayout.setSpacing(false);

        centerContentPanel = new ScrollPanel();
        centerContentPanel.setSizeFull();
        centerContentPanel.getStyle().set("padding", "8px");
        centerContentPanel.getStyle().set("overscroll-behavior", "contain");

        setupScrollListener();

        bottomControlsLayout = buildBottomControls();

        centerLayout.add(centerContentPanel, bottomControlsLayout);
        centerLayout.setFlexGrow(1, centerContentPanel);
        centerLayout.setFlexGrow(0, bottomControlsLayout);

        mainLayout.setOrientation(Orientation.HORIZONTAL);
        mainLayout.addToPrimary(leftMarginLayout);
        mainLayout.addToSecondary(centerLayout);
        mainLayout.setSplitterPosition(20);

        UI.getCurrent().getPage().executeJs(
                "if (!document.getElementById('marginalia-markdown-fix-style')) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = 'marginalia-markdown-fix-style';" +
                        "  style.textContent = `" +
                        "    .markdown-content pre, .markdown-content code, .markdown-content p, .markdown-content span {" +
                        "      white-space: pre-wrap !important;" +
                        "      word-break: break-word !important;" +
                        "      overflow-wrap: anywhere !important;" +
                        "      max-width: 100% !important;" +
                        "      box-sizing: border-box !important;" +
                        "    }" +
                        "  `;" +
                        "  document.head.appendChild(style);" +
                        "}"
        );

        container.add(loc.getValue(L.LABEL_STORY_PART), mainLayout);
        return mainLayout;
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

        centerContentPanel.getElement().executeJs(
                "const el = $0;" +
                        "if (!el._hasScrollListener) {" +
                        "  el._hasScrollListener = true;" +
                        "  let timer;" +
                        "  el.addEventListener('scroll', () => {" +
                        "    clearTimeout(timer);" +
                        "    timer = setTimeout(() => {" +
                        "      el.dispatchEvent(new CustomEvent('panel-scroll', { detail: { scrollTop: Math.round(el.scrollTop) } }));" +
                        "    }, 300);" +
                        "  });" +
                        "}"
                , centerContentPanel.getElement());
    }

    private HorizontalLayout buildBottomControls() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("padding", "6px 12px");
        layout.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        menuBar = new MenuBar();
        MenuItem cogs = menuBar.addItem(Solid.COGS.create());
        changeStyles = cogs.getSubMenu().addItem(UIUtils.menuItemWithIcon(Solid.PEN_FANCY.create(), loc.getValue(L.LABEL_CHANGE_STYLES)), event -> {
            try {
                Manuscript manuscript = parent.refreshManuscript();
                manuscript.setShowBookStyles(!manuscript.getShowBookStyles());
                manuscriptService.save(manuscript);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
            applyBookStyles();
        });
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

    private void applyBookStyles() {
        Manuscript manuscript = parent.getManuscript();
        if (manuscript.getShowBookStyles()) {
            mainLayout.addClassName(SharedStyles.MARKDOWN_MANUSCRIPT_STYLES);
        } else {
            mainLayout.removeClassName(SharedStyles.MARKDOWN_MANUSCRIPT_STYLES);
        }
    }

    private Popover buildNewTurnPopover(Button targetButton) {
        Popover popover = new Popover();
        popover.setTarget(targetButton);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.setWidth("540px");

        Span title = new Span(loc.getValue(L.LABEL_NEW_TURN_INSTRUCTIONS));
        title.getStyle().set("font-weight", "bold");

        sceneSettingField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING));
        sceneSettingField.setWidthFull();
        sceneSettingField.setMinHeight("60px");
        sceneSettingField.addValueChangeListener(e -> pendingSceneSetting = e.getValue());

        povCharacterField = new TextField(loc.getValue(L.LABEL_POV_CHARACTER));
        povCharacterField.setWidthFull();
        povCharacterField.addValueChangeListener(e -> pendingPovCharacter = e.getValue());

        presentCharactersField = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS));
        presentCharactersField.setWidthFull();
        presentCharactersField.setMinHeight("50px");
        presentCharactersField.addValueChangeListener(e -> pendingPresentCharacters = e.getValue());

        instructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS));
        instructionsField.setWidthFull();
        instructionsField.setMinHeight("80px");
        instructionsField.addValueChangeListener(e -> pendingInstructions = e.getValue());

        Button generateBtn = new Button(loc.getValue(L.LABEL_GENERATE), Solid.PLAY.create(), event -> {
            popover.close();
            if (parent != null) {
                autosaveAndSwapAllToMarkdown();
                startGeneration();
            }
        });
        generateBtn.setThemeName("primary small");
        generateBtn.setWidthFull();

        content.add(title, sceneSettingField, povCharacterField, presentCharactersField, instructionsField, generateBtn);
        popover.add(content);

        popover.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
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
        leftMarginLayout.removeAll();
        activeCardMap.clear();

        leftBar = new VTabSheet();
        leftBar.setSizeFull();
        leftMarginLayout.add(leftBar);

        createStoryOutlineTab();

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

                    Button sidebarBtn = new Button(String.format(loc.getValue(L.LABEL_CHAPTER_OUTLINE_NODE), orderId, dbId));
                    sidebarBtn.setThemeName("tertiary small");
                    sidebarBtn.setWidthFull();
                    sidebarBtn.getStyle().set("text-align", "left");
                    sidebarBtn.getStyle().set("justify-content", "flex-start");
                    sidebarBtn.getStyle().set("padding-left", "12px");

                    sidebarBtn.addClickListener(event -> {
                        if (dbId != null && activeCardMap.containsKey(dbId)) {
                            activeCardMap.get(dbId).scrollIntoView();
                        }
                    });
                    sidebarList.add(sidebarBtn);

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

            restoreScrollPosition();
        }
    }

    private void createStoryOutlineTab() {
        VerticalLayout leftMarginLayout = new VerticalLayout();
        leftMarginLayout.setSizeFull();
        leftBar.add(loc.getValue(L.LABEL_CHAPTERS), leftMarginLayout);

        sidebarHeader = new Span();
        sidebarHeader.getStyle().set("font-weight", "bold");
        sidebarHeader.getStyle().set("font-size", "var(--lumo-font-size-s)");
        sidebarHeader.getStyle().set("padding", "12px 12px 4px 12px");

        sidebarList = new VerticalLayout();
        sidebarList.setPadding(false);
        sidebarList.setSpacing(false);
        sidebarList.setWidthFull();

        leftMarginLayout.add(sidebarHeader, sidebarList);
        leftMarginLayout.setFlexGrow(1, sidebarList);
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

        centerContentPanel.getElement().executeJs(
                "requestAnimationFrame(() => {" +
                        "  requestAnimationFrame(() => {" +
                        "    const el = $0;" +
                        "    const target = $1;" +
                        "    if (target !== null && target !== undefined) {" +
                        "      el.scrollTop = target;" +
                        "    } else {" +
                        "      el.scrollTop = el.scrollHeight;" +
                        "    }" +
                        "  });" +
                        "});",
                centerContentPanel.getElement(),
                savedPos
        );
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
        centerContentPanel.getElement().executeJs(
                "requestAnimationFrame(() => {" +
                        "  requestAnimationFrame(() => {" +
                        "    $0.scrollTop = $0.scrollHeight;" +
                        "  });" +
                        "});"
        );
    }

    private void scrollToBottomIfAtBottom() {
        centerContentPanel.getElement().executeJs(
                "requestAnimationFrame(() => {" +
                        "  const el = $0;" +
                        "  const isAtBottom = (el.scrollHeight - el.scrollTop - el.clientHeight) < 100;" +
                        "  if (isAtBottom) {" +
                        "    el.scrollTop = el.scrollHeight;" +
                        "  }" +
                        "});"
        );
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

    private void startGeneration(GenerationRequest request, TurnInput turnInput) {
        autosaveAndSwapAllToMarkdown();
        UI ui = UI.getCurrent();

        parent.freeze();

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

                            centerContentPanel.getElement().executeJs("return Math.round($0.scrollTop);")
                                    .then(Integer.class, scrollTop -> {
                                        if (scrollTop != null && message != null) {
                                            message.setScrollPosition(scrollTop);
                                            try {
                                                chatMessageService.save(message);
                                            } catch (Exception ignored) {}
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
                                if (streamingCard != null)
                                    centerContentPanel.remove(streamingCard);
                            } else {
                                if (!partialMessage.getId().equals(streamingCard.message.getId())) {
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

                            centerContentPanel.getElement().executeJs("return Math.round($0.scrollTop);")
                                    .then(Integer.class, scrollTop -> {
                                        if (scrollTop != null && partialMessage != null) {
                                            partialMessage.setScrollPosition(scrollTop);
                                            try {
                                                chatMessageService.save(partialMessage);
                                            } catch (Exception ignored) {}
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
                    public void askQuestion(String question, Runnable yes, Runnable no) {
                        ui.access(() -> {
                            ConfirmDialog.show(question, yes, no);
                            UIPushGuard.push(ui);
                        });
                    }
                }
        );
    }

    private ChatMessageCard getOrCreateCard(ChatMessage message, int fallbackOrderId) {
        if (message == null) return streamingCard;
        if (message.getId() != null && activeCardMap.containsKey(message.getId())) {
            return activeCardMap.get(message.getId());
        }
        return streamingCard;
    }

    private void applyMarkdownStyles(Markdown markdown) {
        markdown.setWidthFull();
        markdown.getStyle().set("min-width", "0");
        markdown.getStyle().set("max-width", "100%");
        markdown.getStyle().set("box-sizing", "border-box");
        markdown.getElement().executeJs(
                "const el = this;" +
                        "const enforceWrap = () => {" +
                        "  if (!el) return;" +
                        "  const root = el.shadowRoot || el;" +
                        "  const elements = root.querySelectorAll('pre, code, p, div, span');" +
                        "  elements.forEach(node => {" +
                        "    node.style.setProperty('white-space', 'pre-wrap', 'important');" +
                        "    node.style.setProperty('word-break', 'break-word', 'important');" +
                        "    node.style.setProperty('overflow-wrap', 'anywhere', 'important');" +
                        "    node.style.setProperty('max-width', '100%', 'important');" +
                        "    node.style.setProperty('box-sizing', 'border-box', 'important');" +
                        "  });" +
                        "};" +
                        "enforceWrap();" +
                        "const observer = new MutationObserver(enforceWrap);" +
                        "observer.observe(el.shadowRoot || el, { childList: true, subtree: true, characterData: true });"
        );
    }

    @Override
    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;

        leftMarginLayout.setEnabled(!frozen);
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
        renderStoryContent();
    }

    @Override
    public void onTabLeave() throws Exception {
        autosaveAndSwapAllToMarkdown();
    }

    @Override
    public void onTabEnter() throws Exception {
        if (parent != null) {
            load(parent.getManuscript());
        }
    }

    private class ChatMessageCard extends HorizontalLayout {
        private ChatMessage message;
        private boolean isLast;
        private final int orderId;

        private final VerticalLayout contentLayout;
        private Button menuBtn;
        private ContextMenu hamburgerMenu;
        private MenuItem editItem;
        private MenuItem regenerateItem;
        private MenuItem swipeItem;
        private MenuItem summaryItem;
        private MenuItem deleteSummaryItem;
        private MenuItem deleteItem;
        private MenuItem showPromptItem;
        private Details reasoningDetails;
        private Markdown reasoningMarkdown;
        private Markdown responseMarkdown;
        private TextArea responseTextArea;
        private boolean editing = false;

        private final VerticalLayout metaLayout;
        private Span idSpan;
        private Span modelSpan;
        private Span tokensSpan;
        private Span wordsSpan;
        private Button turnDetailsBtn;
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
            getStyle().set("margin-bottom", "8px");
            getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            getStyle().set("border-radius", "var(--lumo-border-radius-s)");
            getStyle().set("min-width", "0");
            getStyle().set("max-width", "100%");
            getStyle().set("box-sizing", "border-box");
            getStyle().set("overflow", "hidden");
            addClassName(SharedStyles.CHAT_MESSAGE);

            contentLayout = new VerticalLayout();
            contentLayout.setWidthFull();
            contentLayout.setPadding(false);
            contentLayout.setSpacing(false);
            contentLayout.getStyle().set("padding", "8px 12px");
            contentLayout.getStyle().set("min-width", "0");
            contentLayout.getStyle().set("max-width", "100%");
            contentLayout.getStyle().set("box-sizing", "border-box");
            contentLayout.getStyle().set("overflow", "hidden");

            HorizontalLayout headerBar = new HorizontalLayout();
            headerBar.setWidthFull();
            headerBar.setPadding(false);
            headerBar.setSpacing(false);
            headerBar.setAlignItems(FlexComponent.Alignment.CENTER);

            menuBtn = new Button(Solid.HAMBURGER.create());
            menuBtn.setThemeName("tertiary icon small");
            menuBtn.getStyle().set("margin-left", "auto");

            hamburgerMenu = new ContextMenu();
            hamburgerMenu.setTarget(menuBtn);
            hamburgerMenu.setOpenOnClick(true);

            editItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_EDIT), event -> toggleEdit());

            regenerateItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_REGENERATE), event -> regenerate());
            regenerateItem.setVisible(this.isLast);

            swipeItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_SWIPE), event -> swipe());
            swipeItem.setVisible(this.isLast);

            showPromptItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_SHOW_PROMPT), event -> showPrompt());

            summaryItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_GENERATE_SUMMARY), event -> openSummaryDialog());
            deleteSummaryItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DELETE_SUMMARY), event -> confirmDeleteSummary());

            deleteItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_DELETE), event -> {
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    centerContentPanel.getElement().executeJs("return $0.scrollTop").then(Integer.class, scrollTop -> {
                        try {
                            Long parentId = message.getParent() != null ? message.getParent().getId() : null;

                            chatMessageService.deleteNodeAndMigrateChildren(message, currentManuscript, false);

                            parent.refreshManuscript();
                            load(parent.getManuscript());

                            if (scrollTop != null && scrollTop > 0) {
                                centerContentPanel.getElement().executeJs("$0.scrollTop = $1", scrollTop);
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

            headerBar.add(UIUtils.voidComponent(), menuBtn);

            reasoningMarkdown = new Markdown();
            reasoningMarkdown.addClassName(SharedStyles.CHAT_MESSAGE_MARKDOWN);
            applyMarkdownStyles(reasoningMarkdown);
            if (StringUtils.isNotBlank(message.getResponseReasoning())) {
                reasoningMarkdown.setContent(message.getResponseReasoning());
            }

            reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
            reasoningDetails.setWidthFull();
            reasoningDetails.getStyle().set("max-width", "100%");
            reasoningDetails.getStyle().set("min-width", "0");
            reasoningDetails.getStyle().set("box-sizing", "border-box");

            responseMarkdown = new Markdown();
            responseMarkdown.addClassName(SharedStyles.CHAT_MESSAGE_MARKDOWN);
            if (StringUtils.isNotBlank(message.getResponse())) {
                responseMarkdown.setContent(message.getResponse());
            }

            responseTextArea = new TextArea();
            responseTextArea.setWidthFull();
            responseTextArea.setMinHeight("100px");
            responseTextArea.setValue(StringUtils.defaultString(message.getResponse()));
            responseTextArea.setVisible(false);

            contentLayout.add(headerBar, reasoningDetails, responseMarkdown, responseTextArea);

            metaLayout = new VerticalLayout();
            metaLayout.setWidth("220px");
            metaLayout.getStyle().set("min-width", "220px");
            metaLayout.getStyle().set("max-width", "220px");
            metaLayout.getStyle().set("flex-shrink", "0");
            metaLayout.getStyle().set("flex-grow", "0");
            metaLayout.getStyle().set("box-sizing", "border-box");
            metaLayout.setPadding(false);
            metaLayout.setSpacing(false);
            metaLayout.getStyle().set("padding", "8px 12px");
            metaLayout.getStyle().set("border-left", "1px solid var(--lumo-contrast-10pct)");
            metaLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

            Span metaHeader = new Span(loc.getValue(L.LABEL_NODE_METADATA));
            metaHeader.getStyle().set("font-weight", "600");
            metaHeader.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            idSpan = new Span(String.format(loc.getValue(L.LABEL_MESSAGE_IDS), this.orderId, (message.getId() != null ? message.getId() : NOT_AVAILABLE)));
            idSpan.getStyle().set("font-weight", "600");
            idSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            String modelName = StringUtils.defaultIfBlank(message.getModelUsed(), NOT_AVAILABLE);
            modelSpan = new Span(loc.getValue(L.LABEL_MODEL) + ": " + modelName);
            modelSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            tokensSpan = new Span(loc.getValue(L.LABEL_TOKENS) + ": " + message.getTokenCount() + " (" + message.getPromptTokens() + ")");
            tokensSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            wordsSpan = new Span(loc.getValue(L.LABEL_WORDS) + ": " + message.getWordCount());
            wordsSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");

            turnDetailsBtn = new Button(loc.getValue(L.LABEL_TURN_DETAILS), Solid.INFO_CIRCLE.create());
            turnDetailsBtn.setThemeName("tertiary small");
            turnDetailsBtn.setWidthFull();

            Popover readOnlyPopover = new Popover();
            readOnlyPopover.setTarget(turnDetailsBtn);

            FormLayout detailsForm = new FormLayout();
            detailsForm.setWidth("520px");
            detailsForm.getStyle().set("padding", "12px");

            detailsSceneField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING), StringUtils.defaultString(message.getSceneSetting()), "");
            detailsSceneField.setReadOnly(!this.isLast);
            detailsSceneField.setWidthFull();
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
            detailsPresentField.addValueChangeListener(e -> {
                if (e.isFromClient() && this.isLast) {
                    this.message.setPresentCharacters(e.getValue());
                    saveMessage();
                }
            });

            detailsInstructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS), StringUtils.defaultString(message.getInstructions()), "");
            detailsInstructionsField.setReadOnly(!this.isLast);
            detailsInstructionsField.setWidthFull();
            detailsInstructionsField.addValueChangeListener(e -> {
                if (e.isFromClient() && this.isLast) {
                    this.message.setInstructions(e.getValue());
                    saveMessage();
                }
            });

            detailsForm.add(detailsSceneField, detailsPovField, detailsPresentField, detailsInstructionsField);
            readOnlyPopover.add(detailsForm);

            metaLayout.add(metaHeader, idSpan, modelSpan, tokensSpan, wordsSpan, turnDetailsBtn);

            add(contentLayout, metaLayout);
            setFlexGrow(1, contentLayout);
            setFlexGrow(0, metaLayout);
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
            ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                try {
                    if (message != null && message.getSummary() != null) {
                        Summary summaryToDelete = summaryService.find(message.getSummary());
                        message.setSummary(null);
                        message = chatMessageService.save(message);
                        if (summaryToDelete != null) {
                            summaryService.delete(summaryToDelete, true);
                        }
                        refreshSummaryMenuItems();
                    }
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        }

        private void openSummaryDialog() {
            refreshSummaryMenuItems();
            boolean hasSummary = (message != null && message.getSummary() != null);
            SummaryDialog summaryDialog = new SummaryDialog(message, !hasSummary);
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
                String newResponse = responseTextArea.getValue();
                message.setResponse(newResponse);
                message.setEdited(true);
                try {
                    message = chatMessageService.save(message);
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
                responseMarkdown.setContent(StringUtils.defaultString(newResponse));
                responseTextArea.setVisible(false);
                responseMarkdown.setVisible(true);
                editing = false;
                editItem.setText(loc.getValue(L.LABEL_EDIT));
                updateMetrics(message);
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
            tokensSpan.setText(loc.getValue(L.LABEL_TOKENS) + ": " + msg.getTokenCount() + " (" + msg.getPromptTokens() + ")");
            wordsSpan.setText(loc.getValue(L.LABEL_WORDS) + ": " + msg.getWordCount());
        }

        private void showPrompt() {
            PromptDialog dialog = new PromptDialog(message.getBuiltPrompt());
            dialog.create();
            dialog.open();
        }
    }

    private class SummaryDialog extends Dialog {
        private final ChatMessage dialogMessage;

        private final Button actionButton;
        private final Details reasoningDetails;
        private final Markdown reasoningMarkdown;
        private final TextArea summaryArea;

        private CancellationToken cancellationToken;
        private boolean isGeneratingState;

        public SummaryDialog(ChatMessage message, boolean isGenerating) {
            this.dialogMessage = message;

            setHeaderTitle(isGenerating ? loc.getValue(L.LABEL_GENERATING_SUMMARY) : loc.getValue(L.LABEL_SUMMARY));
            setWidth("700px");
            setHeight("500px");
            setCloseOnEsc(false);
            setCloseOnOutsideClick(false);
            setModality(ModalityMode.STRICT);

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            layout.setPadding(false);
            layout.setSpacing(true);

            reasoningMarkdown = new Markdown();
            reasoningMarkdown.addClassName(SharedStyles.CHAT_MESSAGE_MARKDOWN);
            applyMarkdownStyles(reasoningMarkdown);

            reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
            reasoningDetails.setWidthFull();
            reasoningDetails.setVisible(false);

            summaryArea = new TextArea();
            summaryArea.setSizeFull();
            summaryArea.setReadOnly(true);

            ScrollPanel scrollPanel = new ScrollPanel();
            scrollPanel.setSizeFull();
            VerticalLayout scrollContent = new VerticalLayout(reasoningDetails, summaryArea);
            scrollContent.setPadding(false);
            scrollContent.setSpacing(true);
            scrollContent.setWidthFull();
            scrollPanel.add(scrollContent);

            layout.add(scrollPanel);
            layout.setFlexGrow(1, scrollPanel);
            add(layout);

            HorizontalLayout footer = new HorizontalLayout();
            footer.setWidthFull();
            footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            actionButton = new Button();
            updateActionButtonState(isGenerating);

            actionButton.addClickListener(_ -> {
                if (isGeneratingState) {
                    if (cancellationToken != null && !cancellationToken.isCancelled()) {
                        cancellationToken.cancel();
                    }
                    updateActionButtonState(false);
                } else {
                    close();
                }
            });

            footer.add(actionButton);
            getFooter().add(footer);

            if (!isGenerating) {
                loadExistingSummary();
            }
        }

        private void updateActionButtonState(boolean generating) {
            this.isGeneratingState = generating;
            if (generating) {
                actionButton.setText(loc.getValue(L.LABEL_STOP));
                actionButton.setThemeName("error primary");
            } else {
                actionButton.setText(loc.getValue(L.LABEL_EXIT));
                actionButton.setThemeName("primary");
            }
        }

        private void loadExistingSummary() {
            try {
                if (dialogMessage.getSummary() != null) {
                    Summary summary = summaryService.find(dialogMessage.getSummary());
                    if (summary != null) {
                        if (StringUtils.isNotBlank(summary.getReasoning())) {
                            reasoningMarkdown.setContent(summary.getReasoning());
                            reasoningDetails.setVisible(true);
                        } else {
                            reasoningDetails.setVisible(false);
                        }
                        summaryArea.setValue(StringUtils.defaultString(summary.getSummary()));
                    }
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        public void startGeneration() {
            UI ui = UI.getCurrent();
            try {
                cancellationToken = summaryService.createSummary(currentManuscript, dialogMessage, new SummaryService.AsyncCallback() {
                    @Override
                    public void onSummaryProgress(String reasoning, String summaryText) {
                        ui.access(() -> {
                            if (StringUtils.isNotBlank(reasoning)) {
                                reasoningMarkdown.setContent(reasoning);
                                reasoningDetails.setVisible(true);
                            }
                            if (StringUtils.isNotBlank(summaryText)) {
                                summaryArea.setValue(summaryText);
                            }
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onSummaryFinished(Summary summary) {
                        ui.access(() -> {
                            try {
                                dialogMessage.setSummary(summary);
                                chatMessageService.save(dialogMessage);
                                setHeaderTitle(loc.getValue(L.LABEL_SUMMARY));
                                updateActionButtonState(false);
                            } catch (Exception e) {
                                UIUtils.internalServerError(loc, e);
                            }
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onSummaryTerminated() {
                        ui.access(() -> {
                            setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_STOPPED));
                            updateActionButtonState(false);
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        ui.access(() -> {
                            setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_ERROR));
                            updateActionButtonState(false);
                            UIUtils.internalServerError(loc, throwable);
                            UIPushGuard.push(ui);
                        });
                    }
                });
            } catch (Exception e) {
                setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_ERROR));
                updateActionButtonState(false);
                UIUtils.internalServerError(loc, e);
            }
        }
    }
}