package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
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
import com.vaadin.flow.component.icon.VaadinIcon;
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

        bottomControlsLayout = buildBottomControls();

        centerLayout.add(centerContentPanel, bottomControlsLayout);
        centerLayout.setFlexGrow(1, centerContentPanel);
        centerLayout.setFlexGrow(0, bottomControlsLayout);

        mainLayout.add(leftMarginLayout, centerLayout);
        mainLayout.setFlexGrow(0, leftMarginLayout);
        mainLayout.setFlexGrow(1, centerLayout);

        container.add(loc.getValue(L.LABEL_STORY_PART), mainLayout);
        return mainLayout;
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
        menuBar.addItem(loc.getValue(L.LABEL_EDIT));
        menuBar.addItem(loc.getValue(L.LABEL_REFRESH));

        actionButton = new Button(VaadinIcon.PLUS.create());
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

        Button generateBtn = new Button(loc.getValue(L.LABEL_GENERATE), VaadinIcon.PLAY.create(), event -> {
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
                if (msg.getId() != null) {
                    activeCardMap.put(msg.getId(), card);
                }
                centerContentPanel.add(card);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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
                            if (message.getId() != null) {
                                activeCardMap.put(message.getId(), card);
                            }
                            streamingCard = card;
                            centerContentPanel.add(card);
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onReasoningChunk(String chunk, ChatMessage message) {
                        ui.access(() -> {
                            ChatMessageCard card = getOrCreateCard(message);
                            if (card != null) {
                                card.updateReasoning(message.getResponseReasoning());
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
                            renderStoryContent();
                            UIPushGuard.push(ui);
                        });
                    }

                    @Override
                    public void onCancelled(ChatMessage partialMessage) {
                        ui.access(() -> {
                            activeGenerationToken = null;
                            streamingCard = null;
                            parent.unfreeze();
                            renderStoryContent();
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
        centerContentPanel.setEnabled(!frozen);
        menuBar.setEnabled(!frozen);

        if (frozen) {
            newTurnPopover.close();
            newTurnPopover.setTarget(null);
            actionButton.setIcon(VaadinIcon.STOP.create());
            actionButton.setThemeName("error primary icon small");
        } else {
            newTurnPopover.setTarget(actionButton);
            actionButton.setIcon(VaadinIcon.PLUS.create());
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
        private Details reasoningDetails;
        private Markdown reasoningMarkdown;
        private Markdown responseMarkdown;
        private TextArea responseTextArea;
        private boolean editing = false;

        private final VerticalLayout metaLayout;
        private Span modelSpan;
        private Span tokensSpan;
        private Span wordsSpan;

        public ChatMessageCard(ChatMessage message) {
            this.message = message;

            setWidthFull();
            setPadding(false);
            setSpacing(false);
            getStyle().set("margin-bottom", "8px");
            getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            getStyle().set("border-radius", "var(--lumo-border-radius-s)");

            contentLayout = new VerticalLayout();
            contentLayout.setWidthFull();
            contentLayout.setPadding(false);
            contentLayout.setSpacing(false);
            contentLayout.getStyle().set("padding", "8px 12px");

            HorizontalLayout headerBar = new HorizontalLayout();
            headerBar.setWidthFull();
            headerBar.setPadding(false);
            headerBar.setSpacing(false);
            headerBar.setAlignItems(FlexComponent.Alignment.CENTER);

            menuBtn = new Button(VaadinIcon.MENU.create());
            menuBtn.setThemeName("tertiary icon small");
            menuBtn.getStyle().set("margin-left", "auto");

            hamburgerMenu = new ContextMenu();
            hamburgerMenu.setTarget(menuBtn);
            hamburgerMenu.setOpenOnClick(true);
            editItem = hamburgerMenu.addItem(loc.getValue(L.LABEL_EDIT), event -> toggleEdit());

            headerBar.add(UIUtils.voidComponent(), menuBtn);

            reasoningMarkdown = new Markdown();
            reasoningMarkdown.setWidthFull();
            if (StringUtils.isNotBlank(message.getResponseReasoning())) {
                reasoningMarkdown.setContent(message.getResponseReasoning());
            }

            reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
            reasoningDetails.setWidthFull();

            responseMarkdown = new Markdown();
            responseMarkdown.setWidthFull();
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

            Button turnDetailsBtn = new Button(loc.getValue(L.LABEL_TURN_DETAILS), VaadinIcon.INFO_CIRCLE.create());
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