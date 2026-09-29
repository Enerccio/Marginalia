package com.github.enerccio.marginalia.extensions.sidequery.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.sidequery.model.*;
import com.github.enerccio.marginalia.extensions.sidequery.service.SideQueryService;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.TreeMap;

@Configurable(preConstruction = true)
public class SideQueryTabContent extends VerticalLayout {

    @Autowired
    private InferenceServices inferenceServices;

    private final SideQueryService sideQueryService;
    private final Manuscript manuscript;
    private final SideQuerySession session;
    private final Runnable onSaveCallback;

    private ScrollPanel messagesScrollPanel;
    private VerticalLayout messagesListLayout;
    private ComboBox<String> savedQueriesCombo;
    private TextArea userInputArea;
    private Button sendBtn;
    private Button undoBtn;
    private Button generateAgainBtn;
    private Span tokenCountSpan;

    private Checkbox lorebookBox;
    private Checkbox messagesBox;
    private IntegerField messagesFromField;
    private IntegerField messagesToField;

    private CancellationToken activeToken;

    public SideQueryTabContent(SideQueryService sideQueryService,
                               Manuscript manuscript,
                               SideQuerySession session,
                               Runnable onSaveCallback) {
        this.sideQueryService = sideQueryService;
        this.manuscript = manuscript;
        this.session = session;
        this.onSaveCallback = onSaveCallback;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        buildUI();
        displayMessages();
    }

    private void buildUI() {
        HorizontalLayout optionsBar = new HorizontalLayout();
        optionsBar.setWidthFull();
        optionsBar.setAlignItems(Alignment.CENTER);
        optionsBar.getStyle().set("padding", "6px 12px");
        optionsBar.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        SideQueryOptions opts = session.getOptions();

        lorebookBox = new Checkbox("Lorebook", opts.isIncludeLorebook());
        lorebookBox.addValueChangeListener(e -> {
            opts.setIncludeLorebook(e.getValue());
            triggerSave();
            updateTokenCount();
        });

        messagesBox = new Checkbox("Chat Logs", opts.isIncludeMessages());
        messagesFromField = new IntegerField();
        messagesFromField.setWidth("60px");
        messagesFromField.setValue(opts.getMessagesFrom());

        messagesToField = new IntegerField();
        messagesToField.setWidth("60px");
        messagesToField.setValue(opts.getMessagesTo());

        messagesFromField.setEnabled(opts.isIncludeMessages());
        messagesToField.setEnabled(opts.isIncludeMessages());

        messagesBox.addValueChangeListener(e -> {
            opts.setIncludeMessages(e.getValue());
            messagesFromField.setEnabled(e.getValue());
            messagesToField.setEnabled(e.getValue());
            triggerSave();
            updateTokenCount();
        });

        messagesFromField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                opts.setMessagesFrom(e.getValue());
                triggerSave();
                updateTokenCount();
            }
        });

        messagesToField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                opts.setMessagesTo(e.getValue());
                triggerSave();
                updateTokenCount();
            }
        });

        tokenCountSpan = new Span("Tokens: 0");
        tokenCountSpan.getStyle().set("margin-left", "auto");
        tokenCountSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");

        optionsBar.add(lorebookBox, messagesBox, messagesFromField, new Span("to"), messagesToField, tokenCountSpan);

        messagesListLayout = new VerticalLayout();
        messagesListLayout.setWidthFull();
        messagesListLayout.setPadding(true);
        messagesListLayout.setSpacing(true);

        messagesScrollPanel = new ScrollPanel();
        messagesScrollPanel.setSizeFull();
        messagesScrollPanel.add(messagesListLayout);

        VerticalLayout controlsLayout = new VerticalLayout();
        controlsLayout.setWidthFull();
        controlsLayout.setPadding(true);
        controlsLayout.setSpacing(true);
        controlsLayout.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        // Query Storage Selector Row
        HorizontalLayout storagePanel = new HorizontalLayout();
        storagePanel.setWidthFull();
        storagePanel.setAlignItems(Alignment.CENTER);

        savedQueriesCombo = new ComboBox<>();
        savedQueriesCombo.setPlaceholder("-- Select Saved Query --");
        savedQueriesCombo.setWidthFull();
        refreshSavedQueriesCombo();

        savedQueriesCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                String val = sideQueryService.getSavedQueries().get(e.getValue());
                if (val != null) {
                    userInputArea.setValue(val);
                }
            }
        });

        Button saveQueryBtn = new Button(Solid.SAVE.create(), e -> handleSaveQuery());
        saveQueryBtn.setThemeName("tertiary icon small");
        saveQueryBtn.setTooltipText("Save prompt");

        Button saveAsQueryBtn = new Button(Solid.FOLDER_PLUS.create(), e -> handleSaveAsQuery());
        saveAsQueryBtn.setThemeName("tertiary icon small");
        saveAsQueryBtn.setTooltipText("Save prompt as...");

        Button deleteQueryBtn = new Button(Solid.TRASH.create(), e -> handleDeleteQuery());
        deleteQueryBtn.setThemeName("tertiary icon small");
        deleteQueryBtn.setTooltipText("Delete saved prompt");

        storagePanel.add(savedQueriesCombo, saveQueryBtn, saveAsQueryBtn, deleteQueryBtn);
        storagePanel.setFlexGrow(1, savedQueriesCombo);

        userInputArea = new TextArea();
        userInputArea.setWidthFull();
        userInputArea.setPlaceholder("Enter query...");
        userInputArea.setMinHeight("60px");

        HorizontalLayout btnRow = new HorizontalLayout();
        btnRow.setWidthFull();

        sendBtn = new Button("SEND", e -> handleSend());
        sendBtn.setThemeName("primary");

        undoBtn = new Button("UNDO", e -> handleUndo());
        generateAgainBtn = new Button("REGENERATE", e -> handleGenerateAgain());

        btnRow.add(undoBtn, sendBtn, generateAgainBtn);
        btnRow.setFlexGrow(1, sendBtn);

        controlsLayout.add(storagePanel, userInputArea, btnRow);

        add(optionsBar, messagesScrollPanel, controlsLayout);
        setFlexGrow(1, messagesScrollPanel);
    }

    private void refreshSavedQueriesCombo() {
        TreeMap<String, String> queries = sideQueryService.getSavedQueries();
        savedQueriesCombo.setItems(queries.keySet());
    }

    private void handleSaveQuery() {
        String selected = savedQueriesCombo.getValue();
        String currentText = userInputArea.getValue();

        if (StringUtils.isBlank(currentText)) {
            Notification.show("Cannot save an empty query");
            return;
        }

        if (StringUtils.isNotBlank(selected)) {
            try {
                sideQueryService.saveQueryTemplate(selected, currentText);
                Notification.show("Saved query: " + selected);
                refreshSavedQueriesCombo();
                savedQueriesCombo.setValue(selected);
            } catch (Exception e) {
                UIUtils.internalServerError(null, e);
            }
        } else {
            handleSaveAsQuery();
        }
    }

    private void handleSaveAsQuery() {
        String currentText = userInputArea.getValue();
        if (StringUtils.isBlank(currentText)) {
            Notification.show("Cannot save an empty query");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Save Query As");

        TextField nameField = new TextField("Query Name");
        nameField.setWidthFull();

        Button saveBtn = new Button("Save", e -> {
            String name = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (name.isEmpty()) {
                Notification.show("Name cannot be empty");
                return;
            }
            try {
                sideQueryService.saveQueryTemplate(name, currentText);
                refreshSavedQueriesCombo();
                savedQueriesCombo.setValue(name);
                Notification.show("Saved query: " + name);
                dialog.close();
            } catch (Exception ex) {
                UIUtils.internalServerError(null, ex);
            }
        });
        saveBtn.setThemeName("primary");

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(nameField));
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void handleDeleteQuery() {
        String selected = savedQueriesCombo.getValue();
        if (StringUtils.isBlank(selected)) {
            Notification.show("Select a saved query to delete");
            return;
        }

        try {
            sideQueryService.deleteQueryTemplate(selected);
            refreshSavedQueriesCombo();
            savedQueriesCombo.setValue(null);
            Notification.show("Deleted query: " + selected);
        } catch (Exception e) {
            UIUtils.internalServerError(null, e);
        }
    }

    private void displayMessages() {
        messagesListLayout.removeAll();
        List<SideQueryMessage> msgs = session.getMessages();
        for (int i = 0; i < msgs.size(); i++) {
            messagesListLayout.add(createMessageCard(msgs.get(i), i, msgs.size()));
        }
        updateButtonStates();
        updateTokenCount();
    }

    private VerticalLayout createMessageCard(SideQueryMessage msg, int index, int totalMessages) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("background-color", msg.isFromUser() ? "var(--lumo-contrast-5pct)" : "transparent");

        if (!msg.isIncluded()) {
            card.getStyle().set("opacity", "0.5");
            card.getStyle().set("border-style", "dashed");
        } else {
            card.getStyle().set("opacity", "1.0");
            card.getStyle().set("border-style", "solid");
        }

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        Span senderSpan = new Span(msg.isFromUser() ? "User" : "AI");
        senderSpan.getStyle().set("font-weight", "bold");

        Button moveUpBtn = new Button(Solid.ARROW_UP.create(), e -> moveMessage(index, index - 1));
        moveUpBtn.setThemeName("tertiary icon small");
        moveUpBtn.setTooltipText("Move message up");
        moveUpBtn.setEnabled(index > 0);

        Button moveDownBtn = new Button(Solid.ARROW_DOWN.create(), e -> moveMessage(index, index + 1));
        moveDownBtn.setThemeName("tertiary icon small");
        moveDownBtn.setTooltipText("Move message down");
        moveDownBtn.setEnabled(index < totalMessages - 1);

        Button editBtn = new Button(Solid.PEN.create(), e -> startEditingMessage(card, msg));
        editBtn.setThemeName("tertiary icon small");
        editBtn.setTooltipText("Edit message");

        Button copyBtn = new Button(Solid.COPY.create(), e -> {
            UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", msg.getContents());
            Notification.show("Copied to clipboard");
        });
        copyBtn.setThemeName("tertiary icon small");
        copyBtn.setTooltipText("Copy message");

        Button toggleBtn = new Button(msg.isIncluded() ? Solid.EYE.create() : Solid.EYE_SLASH.create(), e -> {
            msg.setIncluded(!msg.isIncluded());
            triggerSave();
            displayMessages();
        });
        toggleBtn.setThemeName("tertiary icon small");
        toggleBtn.setTooltipText(msg.isIncluded() ? "Exclude from context" : "Include in context");

        header.add(senderSpan, UIUtils.voidComponent(), moveUpBtn, moveDownBtn, editBtn, copyBtn, toggleBtn);

        Markdown reasoningMarkdown = new Markdown();
        Details reasoningDetails = new Details("Thinking Process", reasoningMarkdown);
        reasoningDetails.setWidthFull();

        if (StringUtils.isNotBlank(msg.getReasoning())) {
            reasoningMarkdown.setContent(msg.getReasoning());
            reasoningDetails.setVisible(true);

            // Preserve open/closed state across streaming re-renders
            reasoningDetails.setOpened(msg.isReasoningOpened());
            reasoningDetails.addOpenedChangeListener(e -> msg.setReasoningOpened(e.isOpened()));
        } else {
            reasoningDetails.setVisible(false);
        }

        Markdown contentMarkdown = new Markdown();
        contentMarkdown.setWidthFull();
        contentMarkdown.setContent(msg.getContents());

        card.add(header, reasoningDetails, contentMarkdown);

        if (StringUtils.isNotBlank(msg.getGenInfoText())) {
            Span infoSpan = new Span(msg.getGenInfoText());
            infoSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            infoSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
            card.add(infoSpan);
        }

        return card;
    }

    private void startEditingMessage(VerticalLayout card, SideQueryMessage msg) {
        card.removeAll();

        HorizontalLayout editHeader = new HorizontalLayout();
        editHeader.setWidthFull();
        editHeader.setAlignItems(Alignment.CENTER);

        Span senderSpan = new Span((msg.isFromUser() ? "User" : "AI") + " (Editing)");
        senderSpan.getStyle().set("font-weight", "bold");
        editHeader.add(senderSpan);

        TextArea editArea = new TextArea();
        editArea.setWidthFull();
        editArea.setMinHeight("100px");
        editArea.setValue(msg.getContents() != null ? msg.getContents() : "");

        HorizontalLayout btnRow = new HorizontalLayout();
        Button saveBtn = new Button("Save", e -> {
            msg.setContents(editArea.getValue());
            triggerSave();
            displayMessages();
        });
        saveBtn.setThemeName("primary small");

        Button cancelBtn = new Button("Cancel", e -> displayMessages());
        cancelBtn.setThemeName("tertiary small");

        btnRow.add(saveBtn, cancelBtn);

        card.add(editHeader, editArea, btnRow);
    }

    private void moveMessage(int fromIndex, int toIndex) {
        List<SideQueryMessage> msgs = session.getMessages();
        if (fromIndex >= 0 && fromIndex < msgs.size() && toIndex >= 0 && toIndex < msgs.size()) {
            SideQueryMessage removed = msgs.remove(fromIndex);
            msgs.add(toIndex, removed);
            triggerSave();
            displayMessages();
        }
    }

    private void handleSend() {
        if (activeToken != null) {
            activeToken.cancel();
            activeToken = null;
            updateButtonStates();
            return;
        }

        String input = userInputArea.getValue();
        if (StringUtils.isBlank(input)) return;

        SideQueryMessage userMsg = new SideQueryMessage();
        userMsg.setFromUser(true);
        userMsg.setContents(input);

        session.getMessages().add(userMsg);
        userInputArea.clear();
        if (savedQueriesCombo != null) {
            savedQueriesCombo.setValue(null);
        }
        triggerSave();
        displayMessages();

        startAiGeneration();
    }

    private void handleUndo() {
        if (!session.getMessages().isEmpty()) {
            SideQueryMessage last = session.getMessages().removeLast();
            if (last.isFromUser()) {
                userInputArea.setValue(last.getContents());
            }
            triggerSave();
            displayMessages();
        }
    }

    private void handleGenerateAgain() {
        if (!session.getMessages().isEmpty() && !session.getMessages().getLast().isFromUser()) {
            session.getMessages().removeLast();
            triggerSave();
            displayMessages();
            startAiGeneration();
        }
    }

    private void startAiGeneration() {
        UI ui = UI.getCurrent();

        try {
            SideQuerySettings settings = sideQueryService.getSettings();
            SideQuerySetting profileSetting = settings.getSettings().get(settings.getDefaultSetting());

            AI targetAi = sideQueryService.resolveAI(profileSetting, manuscript);
            InferenceService service = inferenceServices.forAI(targetAi);

            List<LLMChatMessage> payload = sideQueryService.buildPromptPayload(manuscript, profileSetting, session);

            SideQueryMessage aiMsg = new SideQueryMessage();
            aiMsg.setFromUser(false);
            aiMsg.setContents("Generating response...");
            session.getMessages().add(aiMsg);
            displayMessages();

            activeToken = service.stream(payload, new InferenceService.InferenceAsyncCallback() {
                private final StringBuilder responseBuf = new StringBuilder();
                private final StringBuilder reasoningBuf = new StringBuilder();

                @Override
                public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                    ui.access(() -> {
                        if (chunkType == InferenceService.ChunkType.REASONING) {
                            reasoningBuf.append(text);
                            aiMsg.setReasoning(reasoningBuf.toString());
                        } else {
                            responseBuf.append(text);
                            aiMsg.setContents(responseBuf.toString());
                        }
                        displayMessages();
                        UIPushGuard.push(ui);
                        controller.continueInference();
                    });
                }

                @Override
                public void onCompletion() {
                    ui.access(() -> {
                        activeToken = null;
                        triggerSave();
                        displayMessages();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public void onCancel() {
                    ui.access(() -> {
                        activeToken = null;
                        triggerSave();
                        displayMessages();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public void onError(Throwable exception) {
                    ui.access(() -> {
                        activeToken = null;
                        aiMsg.setContents("Error: " + exception.getMessage());
                        triggerSave();
                        displayMessages();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public boolean isDead() {
                    return activeToken != null && activeToken.isCancelled();
                }
            });

        } catch (Exception e) {
            SideQueryMessage aiMsg = new SideQueryMessage();
            aiMsg.setFromUser(false);
            aiMsg.setContents("Failed to start inference: " + e.getMessage());
            session.getMessages().add(aiMsg);
            triggerSave();
            displayMessages();
        }
    }

    private void updateButtonStates() {
        boolean generating = activeToken != null;
        sendBtn.setText(generating ? "STOP" : "SEND");
        undoBtn.setEnabled(!generating && !session.getMessages().isEmpty());
        generateAgainBtn.setEnabled(!generating && !session.getMessages().isEmpty() && !session.getMessages().getLast().isFromUser());
    }

    private void updateTokenCount() {
        try {
            SideQuerySettings settings = sideQueryService.getSettings();
            SideQuerySetting profileSetting = settings.getSettings().get(settings.getDefaultSetting());
            AI targetAi = sideQueryService.resolveAI(profileSetting, manuscript);
            InferenceService service = inferenceServices.forAI(targetAi);

            List<LLMChatMessage> payload = sideQueryService.buildPromptPayload(manuscript, profileSetting, session);
            long count = 0;
            for (LLMChatMessage msg : payload) {
                count += service.countTokensApprox(msg.getContent());
            }
            tokenCountSpan.setText("Tokens: ~" + count);
        } catch (Exception ignored) {}
    }

    private void triggerSave() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }
}