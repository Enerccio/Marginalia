package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardOptions.Mode;
import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardOptions.OutlineDetail;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.ModelSelection;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.WizardPrompt;
import com.github.enerccio.marginalia.extensions.arcoutline.service.Defaults;
import com.github.enerccio.marginalia.extensions.arcoutline.service.WizardContext;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization.*;

/**
 * A conversation with the model about the plot of the book. The model sees what the author chose to send (the
 * lorebook, the outline, the summaries and the latest parts of the story, see {@link WizardOptions}) and talks about it;
 * nothing is changed in the book by the conversation. What was agreed on goes to the arc generator
 * ({@link ArcGeneratorDialog}) with one click on a message.
 * <p>
 * A book has several chats. A chat can be branched (copied whole, or up to a message) and summarized: the summary is a
 * message of the chat, and from then on the model gets it in place of the messages before it, which stay visible.
 */
@Configurable(preConstruction = true)
public class PlotWizardDialog extends Dialog {

    private static final int NAME_LENGTH = 40;

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    private final ArcOutlineService service;
    private final Manuscript manuscript;
    private final Supplier<ArcOutline> outline;
    private final Consumer<Arc> onArc;

    private final WizardOptions options;
    private WizardData data;
    private WizardChat chat;

    private ModelProtocolPicker modelPicker;
    private Select<Mode> modeSelect;
    private Checkbox lorebookBox;
    private Select<OutlineDetail> outlineSelect;
    private Checkbox summariesBox;
    private Checkbox storyBox;
    private IntegerField storyTokensField;
    private TextAreaPopoverComponent promptArea;
    private Span tokenInfo;

    private ComboBox<WizardChat> chatSelect;
    private Button newButton;
    private Button branchButton;
    private Button renameButton;
    private Button deleteButton;
    private Button summarizeButton;
    private ScrollPanel messagesPanel;
    private TextArea input;
    private Button sendButton;
    private Button exitButton;

    /** The answer that is being written, null while the model is quiet. */
    private Card streaming;
    private boolean running;
    private boolean updating;
    private CancellationToken token;

    /** A message of the chat on the screen. */
    private record Card(Div root, Markdown text, Markdown reasoning, Details reasoningDetails) {
    }

    /**
     * @param outline gives the outline the book has now (the one the arcs tab shows)
     * @param onArc   gets an arc the author generated from a message of the chat and saved
     */
    public PlotWizardDialog(ArcOutlineService service, Manuscript manuscript, Supplier<ArcOutline> outline, Consumer<Arc> onArc) {
        this.service = service;
        this.manuscript = manuscript;
        this.outline = outline;
        this.onArc = onArc;
        this.options = loadOptions();
        this.data = loadData();
        this.chat = data.activeChat();
        if (chat == null) {
            chat = newChat();
        }

        setHeaderTitle(T.localized(DEVELOP_THE_PLOT));
        setWidth("90vw");
        setHeight("90vh");
        // closing while the model runs would leave it running without anyone to read it
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        addThemeName("no-padding");
        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                stop();
            }
        });

        SplitLayout panels = new SplitLayout(buildOptions(), buildChat());
        panels.setSizeFull();
        panels.setSplitterPosition(30);
        panels.addClassName(CssStyles.MIN_HEIGHT_0);
        add(panels);

        exitButton = new Button(T.localized(EXIT), _ -> close());
        getFooter().add(exitButton);

        refreshChats();
        renderMessages();
        updateControls();
        updateTokenInfo();
    }

    // --- stored data ---

    private WizardOptions loadOptions() {
        try {
            WizardOptions stored = service.getSettings().getWizard();
            return stored != null ? stored : new WizardOptions();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return new WizardOptions();
        }
    }

    private WizardData loadData() {
        try {
            return service.loadWizard(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return new WizardData();
        }
    }

    /** Stores the options as the ones the dialog opens with next time. */
    private void rememberOptions() {
        try {
            // read again, the settings may have been changed in the meantime
            var settings = service.getSettings();
            settings.setWizard(options);
            service.saveSettings(settings);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /** Stores the chats with the book. */
    private void persist() {
        try {
            data.setActive(chat.getUuid());
            service.saveWizard(manuscript, data);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- the options (left) ---

    private Component buildOptions() {
        modelPicker = new ModelProtocolPicker();
        try {
            // starts with the model of the book, can be changed for this conversation
            ModelSelection initial = service.resolveModel(manuscript);
            if (initial != null) {
                modelPicker.setSelection(initial.ai(), initial.protocol());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        modelPicker.addSelectionListener(this::optionsChanged);

        modeSelect = new Select<>();
        modeSelect.setLabel(T.localized(HOW_THE_MODEL_HELPS));
        modeSelect.setItems(Mode.values());
        modeSelect.setItemLabelGenerator(PlotWizardDialog::label);
        modeSelect.setWidthFull();
        modeSelect.setValue(options.getMode());
        modeSelect.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                options.setMode(e.getValue());
                optionsChanged();
            }
        });

        lorebookBox = new Checkbox(T.localized(SEND_THE_LOREBOOK), options.isLorebook());
        lorebookBox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                options.setLorebook(e.getValue());
                optionsChanged();
            }
        });

        outlineSelect = new Select<>();
        outlineSelect.setLabel(T.localized(SEND_THE_OUTLINE));
        outlineSelect.setItems(OutlineDetail.values());
        outlineSelect.setItemLabelGenerator(PlotWizardDialog::label);
        outlineSelect.setWidthFull();
        outlineSelect.setValue(options.getOutline());
        outlineSelect.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                options.setOutline(e.getValue());
                optionsChanged();
            }
        });

        summariesBox = new Checkbox(T.localized(SEND_THE_SUMMARIES), options.isSummaries());
        summariesBox.setTooltipText(T.localized(THE_SUMMARIES_STAND_IN));
        summariesBox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                options.setSummaries(e.getValue());
                optionsChanged();
            }
        });

        storyBox = new Checkbox(T.localized(SEND_THE_LATEST_PARTS), options.isStory());
        storyBox.setTooltipText(T.localized(THE_NEWEST_PARTS_THAT_FIT));
        storyBox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                options.setStory(e.getValue());
                optionsChanged();
            }
        });

        storyTokensField = new IntegerField(T.localized(TOKENS_OF_THE_STORY));
        storyTokensField.setMin(0);
        storyTokensField.setStepButtonsVisible(true);
        storyTokensField.setStep(1000);
        storyTokensField.setWidthFull();
        storyTokensField.setHelperText(T.localized(EMPTY_A_THIRD_OF_WHAT_THE_MODEL_TAKES));
        storyTokensField.setValue(options.getStoryTokens());
        storyTokensField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                options.setStoryTokens(e.getValue());
                optionsChanged();
            }
        });

        promptArea = new TextAreaPopoverComponent(T.localized(PROMPT));
        promptArea.setWidthFull();
        promptArea.setValue(service.wizardPrompt(options));
        promptArea.setMinHeight("160px");
        promptArea.setMaxHeight("30vh");
        promptArea.setHelperText(T.localized(VARIABLES).formatted("{{backgroundLore}}, {{outline}}, {{summaries}}, {{story}}, {{mode}}"));
        promptArea.setPopoverContent(TemplateHints.create(loc, promptArea, promptArea.getPopover(), null, Defaults.WIZARD_PROMPT));
        promptArea.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                // the default is not stored, so that it can change with a new version
                options.setPrompt(e.getValue().strip().equals(Defaults.WIZARD_PROMPT.strip()) ? null : e.getValue());
                optionsChanged();
            }
        });

        tokenInfo = new Span();
        tokenInfo.setWidthFull();

        VerticalLayout left = new VerticalLayout(modelPicker, modeSelect, lorebookBox, outlineSelect, summariesBox, storyBox,
                storyTokensField, tokenInfo, promptArea);
        left.setPadding(true);
        left.getThemeList().add("spacing-xs");
        left.setSizeFull();
        left.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);
        return left;
    }

    private void optionsChanged() {
        rememberOptions();
        updateControls();
        updateTokenInfo();
    }

    private static String label(Mode mode) {
        return switch (mode) {
            case FREE -> T.localized(MODE_FREE_TALK);
            case EXPLORE -> T.localized(MODE_EXPLORE_DIRECTIONS);
            case CHALLENGE -> T.localized(MODE_FIND_THE_PROBLEMS);
            case EXPAND -> T.localized(MODE_EXPAND_AN_IDEA);
        };
    }

    private static String label(OutlineDetail detail) {
        return switch (detail) {
            case NONE -> T.localized(NOT_SENT);
            case TERSE -> T.localized(DESCRIPTIONS_ONLY);
            case FULL -> T.localized(WITH_THE_NOTES);
        };
    }

    /**
     * Shows how big what the model gets is and how much it takes: the system prompt with what was chosen, the summary of the
     * chat, and the chat itself.
     */
    private void updateTokenInfo() {
        if (tokenInfo == null) {
            return;
        }
        tokenInfo.removeClassName(CssStyles.NOTICE_WARNING);
        AI ai = modelPicker.getAi();
        NumberFormat format = NumberFormat.getIntegerInstance();
        try {
            int capacity = ai == null ? 0 : TokenLimits.promptTokens(ai, modelPicker.getProtocol());
            WizardPrompt prompt = service.buildWizardPrompt(manuscript, outline.get(), options, promptArea.getValue(), capacity);
            String system = WizardContext.withSummary(prompt.system(), WizardContext.lastSummary(chat.getMessages()));
            long tokens = service.countTokens(system) + WizardContext.sinceSummary(chat.getMessages()).stream()
                    .mapToLong(m -> service.countTokens(m.getText())).sum();
            StringBuilder text = new StringBuilder(T.localized(THE_MODEL_GETS).formatted(prompt.summaries(), prompt.storyParts()));
            if (capacity <= 0) {
                text.append(' ').append(T.localized(PROMPT_SENT_TO_THE_MODEL).formatted(format.format(tokens)));
            } else if (tokens > capacity) {
                text.append(' ').append(T.localized(PROMPT_SENT_TO_THE_MODEL_DOES_NOT_FIT)
                        .formatted(format.format(tokens), format.format(capacity)));
                tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
            } else {
                text.append(' ').append(T.localized(PROMPT_SENT_TO_THE_MODEL_ABOUT).formatted(format.format(tokens), format.format(capacity)));
            }
            tokenInfo.setText(text.toString());
        } catch (Exception e) {
            // most likely a prompt that is not a valid template
            tokenInfo.setText(T.localized(THE_PROMPT_CANT_BE_BUILT).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
            tokenInfo.addClassName(CssStyles.NOTICE_WARNING);
        }
    }

    // --- the chat (right) ---

    private Component buildChat() {
        chatSelect = new ComboBox<>();
        chatSelect.setAllowCustomValue(false);
        chatSelect.setItemLabelGenerator(this::chatName);
        chatSelect.setWidthFull();
        chatSelect.setAriaLabel(T.localized(CHAT));
        chatSelect.addValueChangeListener(e -> {
            if (!updating && e.getValue() != null && e.getValue() != chat) {
                chat = e.getValue();
                persist();
                renderMessages();
                updateControls();
                updateTokenInfo();
            }
        });

        newButton = iconButton(Solid.PLUS, T.localized(NEW_CHAT), _ -> {
            chat = newChat();
            persist();
            refreshChats();
            renderMessages();
            updateControls();
            updateTokenInfo();
            input.focus();
        });
        branchButton = iconButton(Solid.CODE_BRANCH, T.localized(BRANCH_THIS_CHAT), _ -> branch(chat.getMessages().size()));
        renameButton = iconButton(Solid.PEN, T.localized(RENAME_THE_CHAT), _ -> rename());
        summarizeButton = iconButton(Solid.COMPRESS, T.localized(SUMMARIZE_THE_CHAT), _ -> summarize());
        deleteButton = iconButton(Solid.TRASH, T.localized(DELETE_THE_CHAT_BUTTON), _ -> deleteChat());

        HorizontalLayout toolbar = new HorizontalLayout(chatSelect, newButton, branchButton, renameButton, summarizeButton, deleteButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.expand(chatSelect);

        messagesPanel = new ScrollPanel();
        messagesPanel.addClassName(ArcOutlineStyles.WIZARD_MESSAGES);
        messagesPanel.setWidthFull();

        input = new TextArea();
        input.setWidthFull();
        input.setMinHeight("90px");
        input.setMaxHeight("30vh");
        input.setPlaceholder(T.localized(TALK_ABOUT_THE_PLOT));
        DialogFocus.onOpen(this, input);

        sendButton = new Button(T.localized(SEND), Solid.PAPER_PLANE.create(), _ -> {
            if (running) {
                stop();
            } else {
                send();
            }
        });
        sendButton.setThemeName("primary");

        HorizontalLayout inputRow = new HorizontalLayout(input, sendButton);
        inputRow.setWidthFull();
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        inputRow.expand(input);

        VerticalLayout right = new VerticalLayout(toolbar, messagesPanel, inputRow);
        right.setPadding(true);
        right.getThemeList().add("spacing-xs");
        right.setSizeFull();
        right.setFlexGrow(1, messagesPanel);
        right.addClassName(CssStyles.MIN_HEIGHT_0);
        return right;
    }

    private Button iconButton(Solid icon, String tooltip, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(icon.create(), listener);
        button.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        button.setAriaLabel(tooltip);
        button.setTooltipText(tooltip);
        return button;
    }

    private WizardChat newChat() {
        WizardChat created = new WizardChat();
        data.getChats().add(created);
        return created;
    }

    private String chatName(WizardChat c) {
        if (StringUtils.isNotBlank(c.getName())) {
            return c.getName();
        }
        return c.getMessages().stream().filter(m -> m.isFromUser() && StringUtils.isNotBlank(m.getText())).findFirst()
                .map(m -> StringUtils.abbreviate(m.getText().strip().replaceAll("\\s+", " "), NAME_LENGTH))
                .orElse(T.localized(NEW_CHAT));
    }

    private void refreshChats() {
        updating = true;
        try {
            chatSelect.setItems(data.getChats());
            chatSelect.setValue(chat);
        } finally {
            updating = false;
        }
    }

    /** Copies the first {@code count} messages of the chat into a new chat and goes there. */
    private void branch(int count) {
        if (running) {
            return;
        }
        WizardChat copy = chat.branch(T.localized(BRANCH_OF).formatted(chatName(chat)), count);
        data.getChats().add(data.getChats().indexOf(chat) + 1, copy);
        chat = copy;
        persist();
        refreshChats();
        renderMessages();
        updateControls();
        updateTokenInfo();
    }

    private void rename() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(RENAME_THE_CHAT));
        TextField name = new TextField(T.localized(NAME));
        name.setWidth("400px");
        name.setValue(chatName(chat));
        DialogFocus.onOpen(dialog, name);
        Button ok = new Button(T.localized(SAVE), _ -> {
            chat.setName(StringUtils.trimToNull(name.getValue()));
            persist();
            refreshChats();
            dialog.close();
        });
        ok.setThemeName("primary");
        dialog.add(name);
        dialog.getFooter().add(new Button(T.localized(CANCEL), _ -> dialog.close()), ok);
        dialog.open();
    }

    private void deleteChat() {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(T.localized(DELETE_THE_CHAT_BUTTON));
        confirm.setText(T.localized(DELETE_THE_CHAT).formatted(chatName(chat)));
        confirm.setCancelable(true);
        confirm.setConfirmText(T.localized(DELETE_THE_CHAT_BUTTON));
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(_ -> {
            data.getChats().remove(chat);
            chat = data.getChats().isEmpty() ? newChat() : data.getChats().getFirst();
            persist();
            refreshChats();
            renderMessages();
            updateControls();
            updateTokenInfo();
        });
        confirm.open();
    }

    // --- the messages ---

    private void renderMessages() {
        messagesPanel.removeAll();
        List<WizardMessage> messages = chat.getMessages();
        int lastSummary = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).isSummary()) {
                lastSummary = i;
            }
        }
        for (int i = 0; i < messages.size(); i++) {
            Card card = card(messages.get(i), i, i < lastSummary);
            messagesPanel.add(card.root());
        }
        scrollToEnd();
    }

    /**
     * The card of a message.
     *
     * @param index the place of the message in the chat, -1 for the answer that is being written
     * @param old   the message is before the newest summary: the model does not get it any more
     */
    private Card card(WizardMessage message, int index, boolean old) {
        Div root = new Div();
        root.addClassName(ArcOutlineStyles.WIZARD_MESSAGE);
        root.addClassName(message.isSummary() ? ArcOutlineStyles.WIZARD_SUMMARY
                : message.isFromUser() ? ArcOutlineStyles.WIZARD_USER : ArcOutlineStyles.WIZARD_MODEL);
        if (old) {
            root.addClassName(ArcOutlineStyles.WIZARD_OLD);
        }

        Span who = new Span(message.isSummary() ? T.localized(SUMMARY_OF_THE_CHAT) : message.isFromUser() ? T.localized(YOU) : T.localized(MODEL));
        who.addClassName(CssStyles.TEXT_BOLD);
        HorizontalLayout head = new HorizontalLayout(who);
        head.setWidthFull();
        head.setAlignItems(FlexComponent.Alignment.CENTER);
        head.setPadding(false);
        head.setSpacing(false);
        Div spacer = new Div();
        head.add(spacer);
        head.expand(spacer);
        if (index >= 0) {
            if (!message.isFromUser() && !message.isSummary()) {
                head.add(small(Solid.WAND_MAGIC_SPARKLES, T.localized(GENERATE_AN_ARC_FROM_THIS), _ -> generateArc(message.getText())));
            }
            head.add(small(Solid.COPY, T.localized(COPY), _ -> {
                UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", message.getText());
                Notification.success(T.localized(COPIED_TO_THE_CLIPBOARD));
            }));
            head.add(small(Solid.CODE_BRANCH, T.localized(BRANCH_FROM_HERE), _ -> branch(index + 1)));
            head.add(small(Solid.XMARK, T.localized(DELETE_THE_MESSAGE), _ -> deleteMessage(message)));
        }
        root.add(head);

        Markdown reasoning = new Markdown();
        Details details = new Details(T.localized(REASONING), reasoning);
        details.setWidthFull();
        boolean hasReasoning = StringUtils.isNotBlank(message.getReasoning());
        reasoning.setContent(hasReasoning ? message.getReasoning() : "");
        details.setVisible(hasReasoning);
        root.add(details);

        Markdown text = new Markdown();
        text.setWidthFull();
        text.setContent(StringUtils.defaultString(message.getText()));
        root.add(text);
        if (message.isSummary()) {
            Span note = new Span(T.localized(THE_MODEL_GETS_THIS_SUMMARY));
            note.addClassName(CssStyles.HINT_TEXT);
            root.add(note);
        }
        return new Card(root, text, reasoning, details);
    }

    private Button small(Solid icon, String tooltip, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = iconButton(icon, tooltip, listener);
        button.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);
        // nothing is changed while the model writes
        button.setEnabled(!running);
        return button;
    }

    private void deleteMessage(WizardMessage message) {
        if (running) {
            return;
        }
        chat.getMessages().remove(message);
        persist();
        renderMessages();
        refreshChats();
        updateTokenInfo();
    }

    private void generateArc(String text) {
        new ArcGeneratorDialog(service, manuscript, outline.get(), text, onArc).open();
    }

    private void scrollToEnd() {
        messagesPanel.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    // --- talking to the model ---

    private void send() {
        String text = input.getValue();
        if (StringUtils.isBlank(text)) {
            return;
        }
        if (modelPicker.getAi() == null) {
            Notification.warning(T.localized(CHOOSE_A_MODEL));
            return;
        }
        List<LLMChatMessage> payload;
        try {
            chat.getMessages().add(new WizardMessage(true, text.strip(), null));
            payload = payload();
        } catch (Exception e) {
            chat.getMessages().removeLast();
            Notification.error(T.localized(CANT_GENERATE).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
            return;
        }
        input.clear();
        persist();
        refreshChats();
        renderMessages();
        updateTokenInfo();
        start(payload, false);
    }

    /** The request for the chat as it is: the system prompt with what the author chose, and the messages that fit. */
    private List<LLMChatMessage> payload() throws Exception {
        AI ai = modelPicker.getAi();
        int capacity = TokenLimits.promptTokens(ai, modelPicker.getProtocol());
        WizardPrompt prompt = service.buildWizardPrompt(manuscript, outline.get(), options, promptArea.getValue(), capacity);
        String system = WizardContext.withSummary(prompt.system(), WizardContext.lastSummary(chat.getMessages()));
        List<WizardMessage> history = WizardContext.sinceSummary(chat.getMessages());
        if (capacity > 0) {
            history = WizardContext.historyThatFits(history, capacity - service.countTokens(system), service::countTokens);
        }
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, system));
        for (WizardMessage message : history) {
            payload.add(LLMChatMessage.of(message.isFromUser() ? LLMRole.USER : LLMRole.ASSISTANT, message.getText()));
        }
        return payload;
    }

    /** Asks what the summary should keep, then asks the model to summarize the chat; the summary becomes the newest message of the chat. */
    private void summarize() {
        if (modelPicker.getAi() == null) {
            Notification.warning(T.localized(CHOOSE_A_MODEL));
            return;
        }
        if (WizardContext.sinceSummary(chat.getMessages()).stream().noneMatch(m -> !m.isFromUser())) {
            Notification.warning(T.localized(NOTHING_TO_SUMMARIZE));
            return;
        }
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(SUMMARIZE_THE_CHAT_TITLE));
        dialog.setWidth("600px");
        TextArea note = new TextArea(T.localized(WHAT_THE_SUMMARY_SHOULD_KEEP));
        note.setWidthFull();
        note.setMinHeight("120px");
        note.setMaxHeight("40vh");
        note.setPlaceholder(T.localized(THE_SUMMARY_KEEPS_PLACEHOLDER));
        note.setHelperText(T.localized(EMPTY_THE_MODEL_DECIDES));
        DialogFocus.onOpen(dialog, note);
        Button ok = new Button(T.localized(SUMMARIZE), Solid.COMPRESS.create(), _ -> {
            dialog.close();
            runSummary(note.getValue());
        });
        ok.setThemeName("primary");
        dialog.add(note);
        dialog.getFooter().add(new Button(T.localized(CANCEL), _ -> dialog.close()), ok);
        dialog.open();
    }

    /** @param note what the author asks the summary to keep or stress, may be blank */
    private void runSummary(String note) {
        List<WizardMessage> messages = WizardContext.sinceSummary(chat.getMessages());
        StringBuilder transcript = new StringBuilder();
        String earlier = WizardContext.lastSummary(chat.getMessages());
        if (StringUtils.isNotBlank(earlier)) {
            transcript.append("Summary of the conversation before:\n").append(earlier.strip()).append("\n\n");
        }
        for (WizardMessage message : messages) {
            transcript.append(message.isFromUser() ? "Author: " : "Consultant: ").append(message.getText().strip()).append("\n\n");
        }
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, WizardContext.summaryPrompt(Defaults.WIZARD_SUMMARY_PROMPT, note)));
        payload.add(LLMChatMessage.of(LLMRole.USER, transcript.toString().stripTrailing()));
        start(payload, true);
    }

    /**
     * Runs the request and shows the answer as it arrives.
     *
     * @param summary the answer is a summary of the chat
     */
    private void start(List<LLMChatMessage> payload, boolean summary) {
        WizardMessage answer = new WizardMessage(false, "", null);
        answer.setSummary(summary);
        streaming = card(answer, -1, false);
        messagesPanel.add(streaming.root());
        scrollToEnd();
        StringBuilder text = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        UI ui = UI.getCurrent();
        running = true;
        updateControls();
        try (InferenceSession _ = InferenceSession.open("arc-outline"); UsageContext _ = UsageContext.open(manuscript)) {
            token = inferenceServices.forAIOrThrow(modelPicker.getAi()).stream(payload, modelPicker.getProtocol(),
                    callback(ui, answer, text, reasoning));
        } catch (Exception e) {
            // nothing runs, do not leave the controls frozen
            running = false;
            token = null;
            discardStreaming();
            updateControls();
            Notification.error(T.localized(CANT_GENERATE).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
        }
    }

    private InferenceService.InferenceAsyncCallback callback(UI ui, WizardMessage answer, StringBuilder text, StringBuilder reasoning) {
        return new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType type, String chunk) {
                ui.access(() -> {
                    Card card = streaming;
                    if (card != null) {
                        if (type == InferenceService.ChunkType.REASONING) {
                            reasoning.append(chunk);
                            card.reasoning().setContent(reasoning.toString());
                            card.reasoningDetails().setVisible(true);
                        } else {
                            text.append(chunk);
                            card.text().setContent(text.toString());
                        }
                        scrollToEnd();
                    }
                    UIPushGuard.push(ui);
                    controller.continueInference();
                });
            }

            @Override
            public void onCompletion() {
                finish(ui, answer, text, reasoning, null);
            }

            @Override
            public void onCancel() {
                finish(ui, answer, text, reasoning, null);
            }

            @Override
            public void onError(Throwable exception) {
                finish(ui, answer, text, reasoning, exception);
            }

            @Override
            public boolean isDead() {
                return token != null && token.isCancelled();
            }
        };
    }

    private void finish(UI ui, WizardMessage answer, StringBuilder text, StringBuilder reasoning, Throwable error) {
        ui.access(() -> {
            running = false;
            token = null;
            if (StringUtils.isNotBlank(text)) {
                // what arrived is kept, also when the model was stopped or failed on the way
                answer.setText(text.toString().strip());
                answer.setReasoning(StringUtils.defaultIfBlank(reasoning.toString(), null));
                chat.getMessages().add(answer);
                persist();
            }
            streaming = null;
            if (error != null) {
                UIUtils.inferenceError(loc, error);
            } else if (StringUtils.isBlank(text)) {
                Notification.warning(T.localized(THE_MODEL_DID_NOT_ANSWER));
            }
            refreshChats();
            renderMessages();
            updateControls();
            updateTokenInfo();
            UIPushGuard.push(ui);
        });
    }

    private void discardStreaming() {
        if (streaming != null) {
            messagesPanel.remove(streaming.root());
            streaming = null;
        }
    }

    private void stop() {
        CancellationToken current = token;
        if (current != null) {
            current.cancel();
        }
    }

    /** Freezes what must not change while the model writes; the button that sends then stops. */
    private void updateControls() {
        boolean idle = !running;
        modelPicker.setEnabled(idle);
        modeSelect.setEnabled(idle);
        lorebookBox.setEnabled(idle);
        outlineSelect.setEnabled(idle);
        summariesBox.setEnabled(idle);
        storyBox.setEnabled(idle);
        storyTokensField.setEnabled(idle && storyBox.getValue());
        promptArea.setEnabled(idle);
        chatSelect.setEnabled(idle);
        newButton.setEnabled(idle);
        branchButton.setEnabled(idle && !chat.getMessages().isEmpty());
        renameButton.setEnabled(idle);
        summarizeButton.setEnabled(idle && chat.getMessages().stream().anyMatch(m -> !m.isFromUser()));
        deleteButton.setEnabled(idle);
        input.setEnabled(idle);
        exitButton.setEnabled(idle);
        sendButton.setText(running ? T.localized(STOP) : T.localized(SEND));
        sendButton.setIcon((running ? Solid.STOP : Solid.PAPER_PLANE).create());
        sendButton.setThemeName(running ? "error primary" : "primary");
    }
}
