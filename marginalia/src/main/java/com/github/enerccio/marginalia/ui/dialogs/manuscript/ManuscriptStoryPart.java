package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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

import java.util.List;

@Configurable
public class ManuscriptStoryPart implements ManuscriptDialogPart {

    private static final String NOT_AVAILABLE = "N/A";

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final ManuscriptDialog parent;

    // Layout Containers
    private HorizontalLayout mainLayout;
    private VerticalLayout leftMarginLayout;
    private HorizontalLayout storyContainerLayout;
    private VerticalLayout centerLayout;
    private ScrollPanel centerContentPanel;
    private HorizontalLayout bottomControlsLayout;
    private VerticalLayout rightMetaLayout;

    // Menu Controls
    private MenuBar menuBar;
    private Button actionButton;

    // Popover Component
    private Popover newTurnPopover;
    private TextArea sceneSettingField;
    private TextField povCharacterField;
    private TextArea presentCharactersField;
    private TextArea instructionsField;

    // In-memory form state for new turn instructions
    private String pendingSceneSetting = "";
    private String pendingPovCharacter = "";
    private String pendingPresentCharacters = "";
    private String pendingInstructions = "";
    private boolean stateInitialized = false;

    // Active State Trackers
    private Manuscript currentManuscript;
    private ChatMessage selectedMessageNode;
    private boolean isFrozen = false;

    public ManuscriptStoryPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(false);

        // 1. LEFT MARGIN (380px, no padding or spacing, empty for extensions)
        leftMarginLayout = new VerticalLayout();
        leftMarginLayout.setWidth("380px");
        leftMarginLayout.setHeightFull();
        leftMarginLayout.setPadding(false);
        leftMarginLayout.setSpacing(false);
        leftMarginLayout.getStyle().set("border-right", "1px solid var(--lumo-contrast-10pct)");
        leftMarginLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        // 2. MAIN STORY REGION
        storyContainerLayout = new HorizontalLayout();
        storyContainerLayout.setSizeFull();
        storyContainerLayout.setSpacing(true);
        storyContainerLayout.setPadding(true);

        // 2a. CENTER REGION (Content Stream + Bottom Controls)
        centerLayout = new VerticalLayout();
        centerLayout.setSizeFull();
        centerLayout.setPadding(false);
        centerLayout.setSpacing(true);

        centerContentPanel = new ScrollPanel();
        centerContentPanel.setSizeFull();

        bottomControlsLayout = buildBottomControls();

        centerLayout.add(centerContentPanel, bottomControlsLayout);
        centerLayout.setFlexGrow(1, centerContentPanel);
        centerLayout.setFlexGrow(0, bottomControlsLayout);

        // 2b. RIGHT MARGIN (Meta Information)
        rightMetaLayout = buildRightMargin();
        rightMetaLayout.setWidth("280px");
        rightMetaLayout.setHeightFull();

        storyContainerLayout.add(centerLayout, rightMetaLayout);
        storyContainerLayout.setFlexGrow(1, centerLayout);
        storyContainerLayout.setFlexGrow(0, rightMetaLayout);

        mainLayout.add(leftMarginLayout, storyContainerLayout);
        mainLayout.setFlexGrow(0, leftMarginLayout);
        mainLayout.setFlexGrow(1, storyContainerLayout);

        container.add(loc.getValue(L.LABEL_STORY_PART), mainLayout);
        return mainLayout;
    }

    /**
     * Builds bottom control bar with MenuBar and Action button (+ / Stop).
     */
    private HorizontalLayout buildBottomControls() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle().set("padding-top", "8px");
        layout.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        // Menubar with action items
        menuBar = new MenuBar();
        menuBar.addItem(loc.getValue(L.LABEL_EDIT));
        menuBar.addItem(loc.getValue(L.LABEL_REFRESH));

        // Dual-purpose button: '+' for new turn when active, 'Stop' to unfreeze/cancel when frozen
        actionButton = new Button(VaadinIcon.PLUS.create());
        actionButton.setThemeName("primary icon");

        // Construct Popover for New Turn Instructions
        newTurnPopover = buildNewTurnPopover(actionButton);

        actionButton.addClickListener(event -> {
            if (this.isFrozen && parent != null) {
                parent.unfreeze();
            }
        });

        layout.add(menuBar);
        layout.setFlexGrow(1, menuBar);
        layout.add(actionButton);

        return layout;
    }

    /**
     * Constructs Popover for entering new turn instructions and scene parameters.
     */
    private Popover buildNewTurnPopover(Button targetButton) {
        Popover popover = new Popover();
        popover.setTarget(targetButton);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidth("380px");

        Span title = new Span(loc.getValue(L.LABEL_NEW_TURN_INSTRUCTIONS));
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("font-size", "var(--lumo-font-size-m)");

        sceneSettingField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING));
        sceneSettingField.setWidthFull();
        sceneSettingField.setMinHeight("80px");
        sceneSettingField.addValueChangeListener(e -> pendingSceneSetting = e.getValue());

        povCharacterField = new TextField(loc.getValue(L.LABEL_POV_CHARACTER));
        povCharacterField.setWidthFull();
        povCharacterField.addValueChangeListener(e -> pendingPovCharacter = e.getValue());

        presentCharactersField = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS));
        presentCharactersField.setWidthFull();
        presentCharactersField.setMinHeight("60px");
        presentCharactersField.addValueChangeListener(e -> pendingPresentCharacters = e.getValue());

        instructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS));
        instructionsField.setWidthFull();
        instructionsField.setMinHeight("100px");
        instructionsField.addValueChangeListener(e -> pendingInstructions = e.getValue());

        Button generateBtn = new Button(loc.getValue(L.LABEL_GENERATE), VaadinIcon.PLAY.create(), event -> {
            popover.close();
            if (parent != null) {
                parent.freeze();
            }
        });
        generateBtn.setThemeName("primary");
        generateBtn.setWidthFull();

        content.add(title, sceneSettingField, povCharacterField, presentCharactersField, instructionsField, generateBtn);
        popover.add(content);

        // Sync values to form state whenever popover opens
        popover.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                syncFieldsFromState();
            }
        });

        return popover;
    }

    /**
     * Pre-populates state variables from the last message if not initialized yet,
     * then loads values into fields.
     */
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

    private VerticalLayout buildRightMargin() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle().set("border-left", "1px solid var(--lumo-contrast-10pct)");
        layout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        Span metaHeader = new Span(loc.getValue(L.LABEL_NODE_METADATA));
        metaHeader.getStyle().set("font-weight", "bold");

        layout.add(metaHeader);
        return layout;
    }

    private void renderStoryContent() {
        centerContentPanel.removeAll();

        if (currentManuscript == null || currentManuscript.getActiveLeaf() == null) {
            Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
            centerContentPanel.add(UIUtils.centerComponent(emptyLabel));
            return;
        }

        try {
            List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(currentManuscript.getActiveLeaf());
            for (ChatMessage msg : branch) {
                Component messageBlock = createMessageComponent(msg);
                centerContentPanel.add(messageBlock);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private Component createMessageComponent(ChatMessage msg) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");

        TextArea contentArea = new TextArea();
        contentArea.setWidthFull();
        contentArea.setValue(StringUtils.defaultString(msg.getResponse()));
        contentArea.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                msg.setResponse(e.getValue());
                try {
                    chatMessageService.save(msg);
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        card.add(contentArea);
        card.addClickListener(e -> updateRightMetaPanel(msg));
        return card;
    }

    /**
     * Updates the Right Margin and reuses Popover structure (read-only without Generate button)
     * to show turn details for selected ChatMessage.
     */
    private void updateRightMetaPanel(ChatMessage msg) {
        this.selectedMessageNode = msg;
        rightMetaLayout.removeAll();

        Span header = new Span(loc.getValue(L.LABEL_NODE_METADATA));
        header.getStyle().set("font-weight", "bold");

        if (msg == null) {
            rightMetaLayout.add(header, new Span(loc.getValue(L.MSG_SELECT_MESSAGE_METADATA)));
            return;
        }

        String modelName = StringUtils.defaultIfBlank(msg.getModelUsed(), NOT_AVAILABLE);
        Span modelSpan = new Span(loc.getValue(L.LABEL_MODEL) + ": " + modelName);
        Span tokensSpan = new Span(loc.getValue(L.LABEL_TOKENS) + ": " + msg.getTokenCount() + " (" + msg.getPromptTokens() + ")");
        Span wordsSpan = new Span(loc.getValue(L.LABEL_WORDS) + ": " + msg.getWordCount());

        rightMetaLayout.add(header, modelSpan, tokensSpan, wordsSpan);

        // Read-only Turn Details Trigger Button
        Button detailsBtn = new Button(loc.getValue(L.LABEL_TURN_DETAILS), VaadinIcon.INFO_CIRCLE.create());
        detailsBtn.setWidthFull();

        // Popover layout structure in read-only mode for inspecting parent/selected turn data
        Popover readOnlyPopover = new Popover();
        readOnlyPopover.setTarget(detailsBtn);

        FormLayout detailsForm = new FormLayout();
        detailsForm.setWidth("360px");
        detailsForm.getStyle().set("padding", "16px");

        TextArea roScene = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING), StringUtils.defaultString(msg.getSceneSetting()), "");
        roScene.setReadOnly(true);
        roScene.setWidthFull();

        TextField roPov = new TextField(loc.getValue(L.LABEL_POV_CHARACTER), StringUtils.defaultString(msg.getPovCharacter()), "");
        roPov.setReadOnly(true);
        roPov.setWidthFull();

        TextArea roPresent = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS), StringUtils.defaultString(msg.getPresentCharacters()), "");
        roPresent.setReadOnly(true);
        roPresent.setWidthFull();

        TextArea roInst = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS), StringUtils.defaultString(msg.getInstructions()), "");
        roInst.setReadOnly(true);
        roInst.setWidthFull();

        detailsForm.add(roScene, roPov, roPresent, roInst);
        readOnlyPopover.add(detailsForm);

        rightMetaLayout.add(detailsBtn);
    }

    @Override
    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;

        leftMarginLayout.setEnabled(!frozen);
        centerContentPanel.setEnabled(!frozen);
        rightMetaLayout.setEnabled(!frozen);
        menuBar.setEnabled(!frozen);

        if (frozen) {
            newTurnPopover.close();
            newTurnPopover.setTarget(null);
            actionButton.setIcon(VaadinIcon.STOP.create());
            actionButton.setThemeName("error primary icon");
        } else {
            newTurnPopover.setTarget(actionButton);
            actionButton.setIcon(VaadinIcon.PLUS.create());
            actionButton.setThemeName("primary icon");
        }
    }

    @Override
    public void load(Manuscript manuscript) {
        this.currentManuscript = manuscript;
        this.stateInitialized = false; // Reset state so next popover load fetches from leaf
        renderStoryContent();
        updateRightMetaPanel(manuscript != null ? manuscript.getActiveLeaf() : null);
    }

    @Override
    public void onTabLeave() throws Exception { }

    @Override
    public void onTabEnter() throws Exception {
        if (parent != null) {
            load(parent.getManuscript());
        }
    }
}