package com.github.enerccio.marginalia.extensions.sidequery.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.sidequery.model.SideQueryMessage;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

public class SideQueryMessageCard extends VerticalLayout {

    public interface MessageCardListener {
        void onMoveUp(SideQueryMessage message, int currentIndex);
        void onMoveDown(SideQueryMessage message, int currentIndex);
        void onToggleInclude(SideQueryMessage message);
        void onMessageEdited(SideQueryMessage message);
    }

    private final SideQueryMessage message;
    private final MessageCardListener listener;

    private int index;
    private int totalMessages;

    // View components
    private HorizontalLayout headerLayout;
    private Span senderSpan;
    private Button moveUpBtn;
    private Button moveDownBtn;
    private Button editBtn;
    private Button copyBtn;
    private Button toggleBtn;

    private Details reasoningDetails;
    private Markdown reasoningMarkdown;
    private Markdown contentMarkdown;
    private Span infoSpan;

    // Edit components
    private HorizontalLayout editHeaderLayout;
    private TextArea editArea;
    private HorizontalLayout editBtnRow;

    private boolean editing = false;

    public SideQueryMessageCard(SideQueryMessage message, int index, int totalMessages, MessageCardListener listener) {
        this.message = message;
        this.index = index;
        this.totalMessages = totalMessages;
        this.listener = listener;

        setWidthFull();
        setPadding(true);
        setSpacing(false);

        buildCardUI();
        updateCardStyle();
        updateCardData();
        updateButtonStates();
    }

    private void buildCardUI() {
        // --- View Mode Header ---
        headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);

        senderSpan = new Span();
        senderSpan.getStyle().set("font-weight", "bold");

        moveUpBtn = new Button(Solid.ARROW_UP.create(), e -> {
            if (listener != null) listener.onMoveUp(message, index);
        });
        moveUpBtn.setThemeName("tertiary icon small");
        moveUpBtn.setTooltipText("Move message up");

        moveDownBtn = new Button(Solid.ARROW_DOWN.create(), e -> {
            if (listener != null) listener.onMoveDown(message, index);
        });
        moveDownBtn.setThemeName("tertiary icon small");
        moveDownBtn.setTooltipText("Move message down");

        editBtn = new Button(Solid.PEN.create(), e -> setEditing(true));
        editBtn.setThemeName("tertiary icon small");
        editBtn.setTooltipText("Edit message");

        copyBtn = new Button(Solid.COPY.create(), e -> {
            UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", message.getContents());
            Notification.show("Copied to clipboard");
        });
        copyBtn.setThemeName("tertiary icon small");
        copyBtn.setTooltipText("Copy message");

        toggleBtn = new Button();
        toggleBtn.setThemeName("tertiary icon small");
        toggleBtn.addClickListener(e -> {
            if (listener != null) listener.onToggleInclude(message);
        });

        headerLayout.add(senderSpan, UIUtils.voidComponent(), moveUpBtn, moveDownBtn, editBtn, copyBtn, toggleBtn);

        // --- View Mode Body ---
        reasoningMarkdown = new Markdown();
        reasoningDetails = new Details("Thinking Process", reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.addOpenedChangeListener(e -> message.setReasoningOpened(e.isOpened()));

        contentMarkdown = new Markdown();
        contentMarkdown.setWidthFull();

        infoSpan = new Span();
        infoSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        infoSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // --- Edit Mode Components ---
        editHeaderLayout = new HorizontalLayout();
        editHeaderLayout.setWidthFull();
        editHeaderLayout.setAlignItems(Alignment.CENTER);

        Span editSenderSpan = new Span((message.isFromUser() ? "User" : "AI") + " (Editing)");
        editSenderSpan.getStyle().set("font-weight", "bold");
        editHeaderLayout.add(editSenderSpan);

        editArea = new TextArea();
        editArea.setWidthFull();
        editArea.setMinHeight("100px");

        Button saveEditBtn = new Button("Save", e -> {
            message.setContents(editArea.getValue());
            setEditing(false);
            if (listener != null) listener.onMessageEdited(message);
        });
        saveEditBtn.setThemeName("primary small");

        Button cancelEditBtn = new Button("Cancel", e -> setEditing(false));
        cancelEditBtn.setThemeName("tertiary small");

        editBtnRow = new HorizontalLayout(saveEditBtn, cancelEditBtn);

        // Standard layout content
        add(headerLayout, reasoningDetails, contentMarkdown, infoSpan);
    }

    private void updateCardStyle() {
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("background-color", message.isFromUser() ? "var(--lumo-contrast-5pct)" : "transparent");

        if (!message.isIncluded()) {
            getStyle().set("opacity", "0.5");
            getStyle().set("border-style", "dashed");
        } else {
            getStyle().set("opacity", "1.0");
            getStyle().set("border-style", "solid");
        }
    }

    private void updateCardData() {
        senderSpan.setText(message.isFromUser() ? "User" : "AI");

        toggleBtn.setIcon(message.isIncluded() ? Solid.EYE.create() : Solid.EYE_SLASH.create());
        toggleBtn.setTooltipText(message.isIncluded() ? "Exclude from context" : "Include in context");

        if (StringUtils.isNotBlank(message.getReasoning())) {
            reasoningMarkdown.setContent(message.getReasoning());
            reasoningDetails.setVisible(true);
            reasoningDetails.setOpened(message.isReasoningOpened());
        } else {
            reasoningDetails.setVisible(false);
        }

        contentMarkdown.setContent(message.getContents() != null ? message.getContents() : "");

        if (StringUtils.isNotBlank(message.getGenInfoText())) {
            infoSpan.setText(message.getGenInfoText());
            infoSpan.setVisible(true);
        } else {
            infoSpan.setVisible(false);
        }

        updateCardStyle();
    }

    public void updatePosition(int index, int totalMessages) {
        this.index = index;
        this.totalMessages = totalMessages;
        updateButtonStates();
    }

    private void updateButtonStates() {
        moveUpBtn.setEnabled(index > 0);
        moveDownBtn.setEnabled(index < totalMessages - 1);
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        removeAll();

        if (editing) {
            editArea.setValue(message.getContents() != null ? message.getContents() : "");
            add(editHeaderLayout, editArea, editBtnRow);
        } else {
            updateCardData();
            add(headerLayout, reasoningDetails, contentMarkdown, infoSpan);
        }
    }

    public boolean isEditing() {
        return editing;
    }

    /**
     * Directly updates content for streaming responses without full UI recreation.
     */
    public void updateContent(String contents) {
        message.setContents(contents);
        contentMarkdown.setContent(contents != null ? contents : "");
    }

    /**
     * Directly updates reasoning text for streaming responses without full UI recreation.
     */
    public void updateReasoning(String reasoning) {
        message.setReasoning(reasoning);
        if (StringUtils.isNotBlank(reasoning)) {
            reasoningMarkdown.setContent(reasoning);
            if (!reasoningDetails.isVisible()) {
                reasoningDetails.setVisible(true);
                reasoningDetails.setOpened(message.isReasoningOpened());
            }
        }
    }

    /**
     * Updates optional info text (e.g. token counts / timing info).
     */
    public void updateGenInfo(String genInfoText) {
        message.setGenInfoText(genInfoText);
        if (StringUtils.isNotBlank(genInfoText)) {
            infoSpan.setText(genInfoText);
            infoSpan.setVisible(true);
        } else {
            infoSpan.setVisible(false);
        }
    }

    /**
     * Refreshes the view from current message state.
     */
    public void refresh() {
        if (!editing) {
            updateCardData();
            updateButtonStates();
        }
    }

    public SideQueryMessage getMessage() {
        return message;
    }
}