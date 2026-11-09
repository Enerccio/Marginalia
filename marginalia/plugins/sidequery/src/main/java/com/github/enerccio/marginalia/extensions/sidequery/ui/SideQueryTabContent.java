package com.github.enerccio.marginalia.extensions.sidequery.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.sidequery.SideQueryExtension;
import com.github.enerccio.marginalia.extensions.sidequery.SideQueryLog;
import com.github.enerccio.marginalia.extensions.sidequery.model.*;
import com.github.enerccio.marginalia.extensions.sidequery.service.SideQueryService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.sidequery.SideQueryLocalization.*;

@Configurable(preConstruction = true)
public class SideQueryTabContent extends VerticalLayout {
    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private Localization loc;

    private final SideQueryService sideQueryService;
    private final Manuscript manuscript;
    private final SideQuerySession session;
    private final Runnable onSaveCallback;
    private final Consumer<Boolean> generationStateListener;
    private final Runnable onRenamed;

    private HorizontalLayout optionsBar;
    private ScrollPanel messagesScrollPanel;
    private VerticalLayout messagesListLayout;
    private HorizontalLayout storagePanel;
    private ComboBox<String> savedQueriesCombo;
    private TextArea userInputArea;
    private Button sendBtn;
    private Button undoBtn;
    private Button generateAgainBtn;
    private Span tokenCountSpan;

    private Checkbox lorebookBox;
    private Button sourcesBtn;
    private Checkbox messagesBox;
    private IntegerField messagesFromField;
    private IntegerField messagesToField;

    private CancellationToken activeToken;
    private boolean namingTab;

    public SideQueryTabContent(SideQueryService sideQueryService,
                               Manuscript manuscript,
                               SideQuerySession session,
                               Runnable onSaveCallback) {
        this(sideQueryService, manuscript, session, onSaveCallback, null, null);
    }

    public SideQueryTabContent(SideQueryService sideQueryService,
                               Manuscript manuscript,
                               SideQuerySession session,
                               Runnable onSaveCallback,
                               Consumer<Boolean> generationStateListener,
                               Runnable onRenamed) {
        this.sideQueryService = sideQueryService;
        this.manuscript = manuscript;
        this.session = session;
        this.onSaveCallback = onSaveCallback;
        this.generationStateListener = generationStateListener;
        this.onRenamed = onRenamed;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);

        buildUI();
        displayMessages();
    }

    private void buildUI() {
        optionsBar = new HorizontalLayout();
        optionsBar.setWidthFull();
        optionsBar.setWrap(true);
        optionsBar.setAlignItems(Alignment.CENTER);
        optionsBar.addClassNames(CssStyles.PADDING_COMPACT, SideQueryStyles.BAR);

        SideQueryOptions opts = session.getOptions();

        lorebookBox = new Checkbox(T.localized(LOREBOOK), opts.isIncludeLorebook());
        lorebookBox.addValueChangeListener(e -> {
            opts.setIncludeLorebook(e.getValue());
            triggerSave();
            updateTokenCount();
        });

        messagesBox = new Checkbox(T.localized(CHAT_LOGS), opts.isIncludeMessages());
        messagesBox.setTooltipText(T.localized(STORY_PARTS_FROM_TO_NUMBERED));
        messagesFromField = new IntegerField();
        messagesFromField.setWidth("60px");
        messagesFromField.setMin(1);
        messagesFromField.setValue(opts.getPartsFrom());
        messagesFromField.setTooltipText(T.localized(FIRST_STORY_PART_FROM_1));

        messagesToField = new IntegerField();
        messagesToField.setWidth("60px");
        messagesToField.setMin(1);
        messagesToField.setValue(opts.getPartsTo());
        messagesToField.setTooltipText(T.localized(LAST_STORY_PART));

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
                opts.setPartsFrom(e.getValue());
                triggerSave();
                updateTokenCount();
            }
        });

        messagesToField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                opts.setPartsTo(e.getValue());
                triggerSave();
                updateTokenCount();
            }
        });

        tokenCountSpan = new Span(T.localized(TOKENS_0));
        tokenCountSpan.addClassNames(CssStyles.MARGIN_LEFT_AUTO, CssStyles.TEXT_S);

        sourcesBtn = createSourcesButton(opts);

        optionsBar.add(lorebookBox, messagesBox, messagesFromField, new Span(T.localized(TO)), messagesToField, sourcesBtn, tokenCountSpan);

        messagesListLayout = new VerticalLayout();
        messagesListLayout.setWidthFull();
        messagesListLayout.setPadding(true);
        messagesListLayout.setSpacing(true);

        messagesScrollPanel = new ScrollPanel();
        messagesScrollPanel.setWidthFull();
        messagesScrollPanel.addClassName(CssStyles.MIN_HEIGHT_0);
        messagesScrollPanel.add(messagesListLayout);

        VerticalLayout controlsLayout = new VerticalLayout();
        controlsLayout.setWidthFull();
        controlsLayout.setPadding(true);
        controlsLayout.setSpacing(true);
        controlsLayout.addClassName(CssStyles.BORDER_TOP_SUBTLE);

        // Query Storage Selector Row
        storagePanel = new HorizontalLayout();
        storagePanel.setWidthFull();
        storagePanel.setAlignItems(Alignment.CENTER);
        // the tab is narrow in the drawer of a phone
        storagePanel.setWrap(true);

        savedQueriesCombo = new ComboBox<>();
        savedQueriesCombo.setPlaceholder(T.localized(SELECT_SAVED_QUERY));
        savedQueriesCombo.setWidthFull();
        savedQueriesCombo.setMinWidth("10em");
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
        saveQueryBtn.setTooltipText(T.localized(SAVE_PROMPT));

        Button saveAsQueryBtn = new Button(Solid.FOLDER_PLUS.create(), e -> handleSaveAsQuery());
        saveAsQueryBtn.setThemeName("tertiary icon small");
        saveAsQueryBtn.setTooltipText(T.localized(SAVE_PROMPT_AS));

        Button deleteQueryBtn = new Button(Solid.TRASH.create(), e -> handleDeleteQuery());
        deleteQueryBtn.setThemeName("tertiary icon small");
        deleteQueryBtn.setTooltipText(T.localized(DELETE_SAVED_PROMPT));

        storagePanel.add(savedQueriesCombo, saveQueryBtn, saveAsQueryBtn, deleteQueryBtn);
        storagePanel.setFlexGrow(1, savedQueriesCombo);

        userInputArea = new TextArea();
        userInputArea.setWidthFull();
        userInputArea.setPlaceholder(T.localized(ENTER_QUERY));
        ResizableTextArea.install(loc, userInputArea, "100px");

        HorizontalLayout btnRow = new HorizontalLayout();
        btnRow.setWidthFull();
        btnRow.setWrap(true);

        sendBtn = new Button("SEND", e -> handleSend());
        sendBtn.setThemeName("primary");
        DialogShortcuts.submitWith(sendBtn, userInputArea);

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
            Notification.show(T.localized(CANNOT_SAVE_AN_EMPTY_QUERY));
            return;
        }

        if (StringUtils.isNotBlank(selected)) {
            try {
                sideQueryService.saveQueryTemplate(selected, currentText);
                Notification.show(T.localized(SAVED_QUERY).formatted(selected));
                refreshSavedQueriesCombo();
                savedQueriesCombo.setValue(selected);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        } else {
            handleSaveAsQuery();
        }
    }

    private void handleSaveAsQuery() {
        String currentText = userInputArea.getValue();
        if (StringUtils.isBlank(currentText)) {
            Notification.show(T.localized(CANNOT_SAVE_AN_EMPTY_QUERY));
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(SAVE_QUERY_AS));

        TextField nameField = new TextField(T.localized(QUERY_NAME));
        nameField.setWidthFull();
        DialogFocus.onOpen(dialog, nameField);

        Button saveBtn = new Button(T.localized(SAVE), e -> {
            String name = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (name.isEmpty()) {
                Notification.show(T.localized(NAME_CANNOT_BE_EMPTY));
                return;
            }
            try {
                sideQueryService.saveQueryTemplate(name, currentText);
                refreshSavedQueriesCombo();
                savedQueriesCombo.setValue(name);
                Notification.show(T.localized(SAVED_QUERY).formatted(name));
                dialog.close();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
        saveBtn.setThemeName("primary");

        Button cancelBtn = new Button(T.localized(CANCEL), e -> dialog.close());

        dialog.add(new VerticalLayout(nameField));
        dialog.getFooter().add(cancelBtn, saveBtn);
        DialogShortcuts.submitWith(saveBtn, nameField);
        dialog.open();
    }

    private void handleDeleteQuery() {
        String selected = savedQueriesCombo.getValue();
        if (StringUtils.isBlank(selected)) {
            Notification.show(T.localized(SELECT_A_SAVED_QUERY));
            return;
        }

        try {
            sideQueryService.deleteQueryTemplate(selected);
            refreshSavedQueriesCombo();
            savedQueriesCombo.setValue(null);
            Notification.show(T.localized(DELETED_QUERY).formatted(selected));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void displayMessages() {
        messagesListLayout.removeAll();
        List<SideQueryMessage> msgs = session.getMessages();
        int total = msgs.size();
        for (int i = 0; i < total; i++) {
            messagesListLayout.add(createMessageCard(msgs.get(i), i, total));
        }
        updateButtonStates();
        updateTokenCount();
    }

    private SideQueryMessageCard createMessageCard(SideQueryMessage msg, int index, int totalMessages) {
        return new SideQueryMessageCard(msg, index, totalMessages, new SideQueryMessageCard.MessageCardListener() {
            @Override
            public void onMoveUp(SideQueryMessage message, int currentIndex) {
                moveMessage(currentIndex, currentIndex - 1);
            }

            @Override
            public void onMoveDown(SideQueryMessage message, int currentIndex) {
                moveMessage(currentIndex, currentIndex + 1);
            }

            @Override
            public void onToggleInclude(SideQueryMessage message) {
                message.setIncluded(!message.isIncluded());
                triggerSave();
                displayMessages();
            }

            @Override
            public void onMessageEdited(SideQueryMessage message) {
                triggerSave();
                displayMessages();
            }
        });
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
            setGeneratingState(false);
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
            Protocol targetProtocol = sideQueryService.resolveProtocol(profileSetting, manuscript);
            InferenceService service = inferenceServices.forAI(targetAi);

            List<LLMChatMessage> payload = sideQueryService.buildPromptPayload(manuscript, profileSetting, session);

            SideQueryMessage aiMsg = new SideQueryMessage();
            aiMsg.setFromUser(false);
            aiMsg.setContents(T.localized(GENERATING_RESPONSE));
            session.getMessages().add(aiMsg);

            // Display messages once to mount the card in the UI
            displayMessages();

            // Lock input controls and notify parent view to lock tabs
            setGeneratingState(true);

            // Obtain reference to the newly added AI card for in-place updates during streaming
            SideQueryMessageCard aiCard = (SideQueryMessageCard) messagesListLayout.getComponentAt(messagesListLayout.getComponentCount() - 1);

            InferenceService.InferenceAsyncCallback queryCallback = new InferenceService.InferenceAsyncCallback() {
                private final StringBuilder responseBuf = new StringBuilder();
                private final StringBuilder reasoningBuf = new StringBuilder();

                @Override
                public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                    ui.access(() -> {
                        if (chunkType == InferenceService.ChunkType.REASONING) {
                            reasoningBuf.append(text);
                            aiCard.updateReasoning(reasoningBuf.toString());
                        } else {
                            responseBuf.append(text);
                            aiCard.updateContent(responseBuf.toString());
                        }
                        UIPushGuard.push(ui);
                        controller.continueInference();
                    });
                }

                @Override
                public void onCompletion() {
                    ui.access(() -> {
                        activeToken = null;
                        triggerSave();
                        setGeneratingState(false);
                        updateTokenCount();
                        new DesktopNotifier(ui).finished(SideQueryExtension.NOTIFY_QUERY, false, manuscript.getName());
                        UIPushGuard.push(ui);
                        nameTab(ui, profileSetting);
                    });
                }

                @Override
                public void onCancel() {
                    ui.access(() -> {
                        activeToken = null;
                        triggerSave();
                        setGeneratingState(false);
                        updateTokenCount();
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public void onError(Throwable exception) {
                    ui.access(() -> {
                        activeToken = null;
                        aiCard.updateContent(T.localized(ERROR).formatted(InferenceErrors.messageOf(loc, exception)));
                        triggerSave();
                        setGeneratingState(false);
                        updateTokenCount();
                        new DesktopNotifier(ui).failed(SideQueryExtension.NOTIFY_QUERY, false, manuscript.getName());
                        UIPushGuard.push(ui);
                    });
                }

                @Override
                public boolean isDead() {
                    return activeToken == null || activeToken.isCancelled();
                }
            };
            try (InferenceSession _ = InferenceSession.open(SideQueryService.SESSION_KIND); UsageContext _ = UsageContext.open(manuscript)) {
                activeToken = service.stream(payload, targetProtocol, queryCallback);
            }

        } catch (Exception e) {
            SideQueryMessage aiMsg = new SideQueryMessage();
            aiMsg.setFromUser(false);
            aiMsg.setContents(T.localized(FAILED_TO_START_INFERENCE).formatted(e.getMessage()));
            session.getMessages().add(aiMsg);
            triggerSave();
            displayMessages();
            setGeneratingState(false);
        }
    }

    /**
     * Names the tab by the model after the first answer when AI tab naming is enabled in the profile.
     */
    private void nameTab(UI ui, SideQuerySetting profileSetting) {
        if (namingTab || !sideQueryService.shouldNameTab(profileSetting, session)) {
            return;
        }
        namingTab = true;
        try {
            CancellationToken token = sideQueryService.generateTabName(manuscript, profileSetting, session, name -> ui.access(() -> {
                namingTab = false;
                if (session.isManuallyRenamed()) {
                    return;
                }
                session.setName(name);
                session.setAutoNamed(true);
                triggerSave();
                if (onRenamed != null) {
                    onRenamed.run();
                }
                UIPushGuard.push(ui);
            }));
            if (token == null) {
                namingTab = false;
            }
        } catch (Exception e) {
            namingTab = false;
            SideQueryLog.get().warn("Failed to name side query tab: {}", e.getMessage());
        }
    }

    /**
     * The button that opens the list of the data other extensions offer (see {@code IPCService}), with a checkbox for
     * each. The list is read every time the popover opens: extensions come and go.
     */
    private Button createSourcesButton(SideQueryOptions opts) {
        Button button = new Button(Solid.PLUG.create());
        button.setThemeName("tertiary icon small");
        button.setAriaLabel(T.localized(ADDITIONAL_SOURCES));
        button.setTooltipText(T.localized(ADDITIONAL_SOURCES_DATA));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);
        content.getThemeList().add("spacing-xs");

        Popover popover = new Popover(content);
        popover.setTarget(button);
        popover.setPosition(PopoverPosition.BOTTOM_START);
        popover.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                fillSources(content, opts);
            }
        });
        return button;
    }

    private void fillSources(VerticalLayout content, SideQueryOptions opts) {
        content.removeAll();
        List<SideQueryService.Source> manuscriptSources = sideQueryService.listManuscriptSources();
        List<SideQueryService.Source> messageSources = sideQueryService.listMessageSources();

        if (manuscriptSources.isEmpty() && messageSources.isEmpty()) {
            content.add(new Span(T.localized(NO_OTHER_EXTENSION_OFFERS_DATA)));
            return;
        }
        if (!manuscriptSources.isEmpty()) {
            content.add(sourcesHeading(T.localized(ABOUT_THE_BOOK)));
            manuscriptSources.forEach(source -> content.add(sourceCheckbox(source, opts.getManuscriptSources())));
        }
        if (!messageSources.isEmpty()) {
            content.add(sourcesHeading(T.localized(ABOUT_STORY_PARTS_ADDED)));
            messageSources.forEach(source -> content.add(sourceCheckbox(source, opts.getMessageSources())));
        }
    }

    private Span sourcesHeading(String text) {
        Span heading = new Span(text);
        heading.addClassNames(CssStyles.TEXT_BOLD, CssStyles.TEXT_S);
        return heading;
    }

    private Checkbox sourceCheckbox(SideQueryService.Source source, List<String> checked) {
        Checkbox box = new Checkbox(source.name(), checked.contains(source.id()));
        box.setTooltipText(source.bundle());
        box.addValueChangeListener(e -> {
            if (e.getValue()) {
                if (!checked.contains(source.id())) {
                    checked.add(source.id());
                }
            } else {
                checked.remove(source.id());
            }
            triggerSave();
            updateTokenCount();
        });
        return box;
    }

    private void setGeneratingState(boolean generating) {
        if (generationStateListener != null) {
            generationStateListener.accept(generating);
        }

        optionsBar.setEnabled(!generating);
        if (!generating && session != null) {
            SideQueryOptions opts = session.getOptions();
            boolean incMsgs = opts.isIncludeMessages();
            messagesFromField.setEnabled(incMsgs);
            messagesToField.setEnabled(incMsgs);
        }

        storagePanel.setEnabled(!generating);
        userInputArea.setEnabled(!generating);
        messagesListLayout.setEnabled(!generating);

        if (generating) {
            sendBtn.setText(T.localized(STOP));
            sendBtn.setThemeName("error primary");
            sendBtn.setEnabled(true);
            undoBtn.setEnabled(false);
            generateAgainBtn.setEnabled(false);
        } else {
            sendBtn.setText(T.localized(SEND));
            sendBtn.setThemeName("primary");
            sendBtn.setEnabled(true);
            undoBtn.setEnabled(!session.getMessages().isEmpty());
            generateAgainBtn.setEnabled(!session.getMessages().isEmpty() && !session.getMessages().getLast().isFromUser());
        }
    }

    private void updateButtonStates() {
        setGeneratingState(activeToken != null);
    }

    private void updateTokenCount() {
        try {
            SideQuerySettings settings = sideQueryService.getSettings();
            SideQuerySetting profileSetting = settings.getSettings().get(settings.getDefaultSetting());
            AI targetAi = sideQueryService.resolveAI(profileSetting, manuscript);
            Protocol targetProtocol = sideQueryService.resolveProtocol(profileSetting, manuscript);
            InferenceService service = inferenceServices.forAI(targetAi);

            List<LLMChatMessage> payload = sideQueryService.buildPromptPayload(manuscript, profileSetting, session);
            long count = 0;
            for (LLMChatMessage msg : payload) {
                count += service.countTokensApprox(msg.getContent());
            }
            tokenCountSpan.setText(T.localized(TOKENS).formatted(count));
        } catch (Exception ignored) {}
    }

    private void triggerSave() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }
}