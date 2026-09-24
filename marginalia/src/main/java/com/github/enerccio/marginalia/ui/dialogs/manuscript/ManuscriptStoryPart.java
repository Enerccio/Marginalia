package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.SharedStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.HashMap;
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

    private final ManuscriptDialog parent;
    private CancellationToken activeGenerationToken;

    private HorizontalLayout mainLayout;
    private VerticalLayout leftMarginLayout;
    private VerticalLayout centerLayout;
    private ScrollPanel centerContentPanel;
    private HorizontalLayout bottomControlsLayout;

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
    private boolean stateInitialized = false;

    private Manuscript currentManuscript;
    private boolean isFrozen = false;

    private final Map<Long, ChatMessageCard> activeCardMap = new HashMap<>();
    private ChatMessageCard streamingCard;

    public ManuscriptStoryPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(false);

        leftMarginLayout = new VerticalLayout();
        leftMarginLayout.setWidth("320px");
        leftMarginLayout.setHeightFull();
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

        mainLayout.add(leftMarginLayout, centerLayout);
        mainLayout.setFlexGrow(0, leftMarginLayout);
        mainLayout.setFlexGrow(1, centerLayout);

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
                ChatMessage leaf = currentManuscript.getActiveLeaf();
                leaf.setScrollPosition((int) pos);
                try {
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
        content.setWidth("340px");

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
        if (!stateInitialized) {
            ChatMessage lastMsg = currentManuscript != null ? currentManuscript.getActiveLeaf() : null;
            if (lastMsg != null) {
                pendingSceneSetting = StringUtils.defaultString(lastMsg.getSceneSetting());
                pendingPovCharacter = StringUtils.defaultString(lastMsg.getPovCharacter());
                pendingPresentCharacters = StringUtils.defaultString(lastMsg.getPresentCharacters());
            }
            pendingInstructions = "";
            stateInitialized = true;
        }

        sceneSettingField.setValue(pendingSceneSetting);
        povCharacterField.setValue(pendingPovCharacter);
        presentCharactersField.setValue(pendingPresentCharacters);
        instructionsField.setValue(pendingInstructions);
    }

    private void renderStoryContent() {
        centerContentPanel.removeAll();
        activeCardMap.clear();

        if (currentManuscript == null || currentManuscript.getActiveLeaf() == null) {
            Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
            centerContentPanel.add(UIUtils.centerComponent(emptyLabel));
            return;
        }

        try {
            List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(currentManuscript.getActiveLeaf());
            for (ChatMessage msg : branch) {
                ChatMessageCard card = new ChatMessageCard(msg);
                card.setFrozen(isFrozen);
                if (msg.getId() != null) {
                    activeCardMap.put(msg.getId(), card);
                }
                centerContentPanel.add(card);
            }
            restoreScrollPosition();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
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
        autosaveAndSwapAllToMarkdown();
        UI ui = UI.getCurrent();

        TurnInput input = new TurnInput(
                pendingSceneSetting,
                pendingPovCharacter,
                pendingPresentCharacters,
                pendingInstructions
        );

        parent.freeze();

        this.activeGenerationToken = storyGenerationService.generateNextTurn(
                currentManuscript,
                input,
                new GenerationListener() {

                    @Override
                    public void onNodeCreated(ChatMessage message) {
                        ui.access(() -> {
                            ChatMessageCard card = new ChatMessageCard(message);
                            card.setFrozen(true);
                            if (message.getId() != null) {
                                activeCardMap.put(message.getId(), card);
                            }
                            streamingCard = card;
                            centerContentPanel.add(card);
                            scrollToBottom();
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onReasoningChunk(String chunk, ChatMessage message) {
                        ui.access(() -> {
                            ChatMessageCard card = getOrCreateCard(message);
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
                            ChatMessageCard card = getOrCreateCard(message);
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
                            ChatMessageCard card = getOrCreateCard(message);
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
                            activeGenerationToken = null;
                            streamingCard = null;
                            parent.unfreeze();
                            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), cause);
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onSimpleError(String error) {
                        ui.access(() -> {
                            activeGenerationToken = null;
                            streamingCard = null;
                            parent.unfreeze();
                            Notification.error(error);
                            UIPushGuard.push(ui);
                        });
                    }
                }
        );
    }

    private ChatMessageCard getOrCreateCard(ChatMessage message) {
        if (message == null) return streamingCard;
        if (message.getId() != null && activeCardMap.containsKey(message.getId())) {
            return activeCardMap.get(message.getId());
        }
        return streamingCard;
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
        this.stateInitialized = false;
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

        private final VerticalLayout contentLayout;
        private Button menuBtn;
        private ContextMenu hamburgerMenu;
        private MenuItem editItem;
        private MenuItem deleteItem;
        private Details reasoningDetails;
        private Markdown reasoningMarkdown;
        private Markdown responseMarkdown;
        private TextArea responseTextArea;
        private boolean editing = false;

        private final VerticalLayout metaLayout;
        private Span modelSpan;
        private Span tokensSpan;
        private Span wordsSpan;
        private Button turnDetailsBtn;

        public ChatMessageCard(ChatMessage message) {
            this.message = message;

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
            detailsForm.setWidth("320px");
            detailsForm.getStyle().set("padding", "12px");

            TextArea roScene = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING), StringUtils.defaultString(message.getSceneSetting()), "");
            roScene.setReadOnly(true);
            roScene.setWidthFull();

            TextField roPov = new TextField(loc.getValue(L.LABEL_POV_CHARACTER), StringUtils.defaultString(message.getPovCharacter()), "");
            roPov.setReadOnly(true);
            roPov.setWidthFull();

            TextArea roPresent = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS), StringUtils.defaultString(message.getPresentCharacters()), "");
            roPresent.setReadOnly(true);
            roPresent.setWidthFull();

            TextArea roInst = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS), StringUtils.defaultString(message.getInstructions()), "");
            roInst.setReadOnly(true);
            roInst.setWidthFull();

            detailsForm.add(roScene, roPov, roPresent, roInst);
            readOnlyPopover.add(detailsForm);

            metaLayout.add(metaHeader, modelSpan, tokensSpan, wordsSpan, turnDetailsBtn);

            add(contentLayout, metaLayout);
            setFlexGrow(1, contentLayout);
            setFlexGrow(0, metaLayout);
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
            message.setResponseReasoning(reasoningText);
            reasoningDetails.setOpened(true);
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
            modelSpan.setText(loc.getValue(L.LABEL_MODEL) + ": " + modelName);
            tokensSpan.setText(loc.getValue(L.LABEL_TOKENS) + ": " + msg.getTokenCount() + " (" + msg.getPromptTokens() + ")");
            wordsSpan.setText(loc.getValue(L.LABEL_WORDS) + ": " + msg.getWordCount());
        }
    }
}