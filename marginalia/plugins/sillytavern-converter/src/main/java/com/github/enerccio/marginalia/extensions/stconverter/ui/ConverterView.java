package com.github.enerccio.marginalia.extensions.stconverter.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.structured.PartialJson;
import com.github.enerccio.marginalia.extensions.stconverter.STConverterLog;
import com.github.enerccio.marginalia.extensions.stconverter.model.ChatLine;
import com.github.enerccio.marginalia.extensions.stconverter.model.ConverterData;
import com.github.enerccio.marginalia.extensions.stconverter.model.TurnItem;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService.ModelSelection;
import com.github.enerccio.marginalia.extensions.stconverter.service.Defaults;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The tab of the converter next to the story: the SillyTavern chat the author loaded and the turn details the model
 * prepared from it, both kept with the book. Above the two lists the model for the wand, the range of the chat the wand works
 * on (clicked in the chat, or typed) with the number of tokens the request takes, and the wand itself.
 * <p>
 * The chat is a list of messages (who said what) with a checkbox for the ones the author has gone through; the turn details
 * are a list of what the new turn form of the story takes, also with a checkbox, and a click on one puts it into the form.
 */
@Configurable(preConstruction = true)
public class ConverterView extends VerticalLayout {

    /**
     * Puts the turn details into the new turn form of the story and opens it.
     */
    @FunctionalInterface
    public interface TurnOpener {
        /**
         * @return false when the form is not there
         */
        boolean open(TurnItem turn);
    }

    /** A message with its number (from 1). */
    private record Row(int number, ChatLine line) {
    }

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    private final ConverterService service;
    private final Manuscript manuscript;
    private final TurnOpener opener;
    private final UI ui = UI.getCurrent();
    private final NumberFormat numbers = NumberFormat.getIntegerInstance();

    private ConverterData data = new ConverterData();

    private ModelProtocolPicker picker;
    private Details convertDetails;
    private HorizontalLayout rangeBar;
    private IntegerField fromField;
    private IntegerField toField;
    private Span tokenInfo;
    private Span hint;
    private Button wandButton;
    private Button promptButton;
    private Span status;
    private TabSheet tabs;
    private Tab chatTab;
    private Tab turnsTab;
    private VerticalLayout chatPanel;
    private Span chatSummary;
    private Grid<Row> chatGrid;
    private Span turnSummary;
    private Grid<TurnItem> turnGrid;

    /** The range the wand works on and the message the last click started it at, from 1. */
    private int from = 1;
    private int to = 1;
    private int anchor = 1;
    private boolean adjusting;
    /** The number (from 1) of the message the author was at last (checked, clicked or jumped to), 0 for none; the same for the turns. */
    private int chatCursor;
    private int turnCursor;

    /** Tokens of each message, null until counted (and again after the model changes). */
    private long[] lineTokens;
    /** Tokens of the prompt without the chat (the template and the lore), null until counted. */
    private Long baseTokens;

    private boolean generating;
    private CancellationToken running;
    private final StringBuilder output = new StringBuilder();

    public ConverterView(ConverterService service, Manuscript manuscript, TurnOpener opener) {
        this.service = service;
        this.manuscript = manuscript;
        this.opener = opener;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        // small text, tight spacing, and the whole tab scrolls
        addClassName(STConverterStyles.ROOT);

        try {
            data = service.load(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        build();
        resetRange();
        refreshAll();
    }

    // --- construction ---

    private void build() {
        picker = new ModelProtocolPicker();
        picker.setWrap(true);
        picker.setWidthFull();
        picker.setSpacing(false);
        picker.addClassName(STConverterStyles.PICKER);
        try {
            ModelSelection initial = service.resolveModel(manuscript, data);
            if (initial != null) {
                picker.setSelection(initial.ai(), initial.protocol());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        // after the initial choice: it was not made by the user
        picker.addSelectionListener(this::modelChanged);

        fromField = rangeField("From");
        toField = rangeField("To");
        fromField.addValueChangeListener(e -> rangeTyped());
        toField.addValueChangeListener(e -> rangeTyped());
        Button allButton = new Button("All", e -> {
            resetRange();
            rangeChanged();
        });
        allButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        tokenInfo = new Span();
        tokenInfo.addClassName(CssStyles.TEXT_SECONDARY);
        tokenInfo.setWidthFull();

        wandButton = new Button("Convert range", Solid.WAND_MAGIC_SPARKLES.create(), e -> {
            if (generating) {
                stop();
            } else {
                generate();
            }
        });
        wandButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        wandButton.setTooltipText("Ask the model for the turn details of the messages in the range");
        promptButton = new Button(Solid.PEN.create(), e -> openPromptDialog());
        promptButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        promptButton.setTooltipText("Edit the prompt of the wand");

        HorizontalLayout fields = new HorizontalLayout(fromField, toField, allButton);
        fields.setAlignItems(FlexComponent.Alignment.BASELINE);
        HorizontalLayout actions = new HorizontalLayout(wandButton, promptButton);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        rangeBar = new HorizontalLayout(fields, actions, tokenInfo);
        rangeBar.setWidthFull();
        rangeBar.setWrap(true);
        rangeBar.setSpacing(false);
        rangeBar.setAlignItems(FlexComponent.Alignment.CENTER);
        rangeBar.setPadding(false);

        hint = new Span("Click a message to start a range, Shift+click to extend it.");
        hint.addClassName(STConverterStyles.HINT);
        status = new Span();
        status.addClassName(CssStyles.NOTICE_WARNING);
        status.setVisible(false);

        // closed until the author needs the wand: the lists get the room
        VerticalLayout convertContent = new VerticalLayout(picker, rangeBar, hint);
        convertContent.setWidthFull();
        convertContent.setSpacing(false);
        convertContent.setPadding(false);
        convertContent.getThemeList().add("spacing-xs");
        convertDetails = new Details("Convert to turn details", convertContent);
        convertDetails.setOpened(false);
        convertDetails.setWidthFull();

        VerticalLayout top = new VerticalLayout(convertDetails, status);
        top.setWidthFull();
        top.setSpacing(false);
        top.setPadding(false);
        top.addClassName(STConverterStyles.BAR);
        top.getThemeList().add("spacing-xs");

        chatPanel = new VerticalLayout();
        chatPanel.setSizeFull();
        chatPanel.setPadding(false);
        chatPanel.setSpacing(false);
        chatPanel.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN, STConverterStyles.LISTS);

        VerticalLayout turnsPanel = new VerticalLayout();
        turnsPanel.setSizeFull();
        turnsPanel.setPadding(false);
        turnsPanel.setSpacing(false);
        turnsPanel.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN, STConverterStyles.LISTS);
        turnSummary = new Span();
        turnSummary.addClassNames(CssStyles.TEXT_SECONDARY, STConverterStyles.ELLIPSIS);
        Button nextTurn = nextUncheckedButton(this::nextUncheckedTurn);
        HorizontalLayout turnHeader = new HorizontalLayout(turnSummary, nextTurn);
        turnHeader.setWidthFull();
        turnHeader.setSpacing(false);
        turnHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        turnHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        turnHeader.addClassName(STConverterStyles.PAD);
        buildTurnGrid();
        turnsPanel.add(turnHeader, turnGrid);
        turnsPanel.setFlexGrow(1, turnGrid);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addClassName(STConverterStyles.TABS);
        chatTab = new Tab("Chat");
        turnsTab = new Tab("Turn details");
        tabs.add(chatTab, chatPanel);
        tabs.add(turnsTab, turnsPanel);

        add(top, tabs);
        setFlexGrow(1, tabs);
    }

    private IntegerField rangeField(String label) {
        IntegerField field = new IntegerField(label);
        field.setMin(1);
        field.addThemeName("small");
        field.setWidth("75px");
        field.setStepButtonsVisible(false);
        return field;
    }

    private void buildChatGrid() {
        chatGrid = new Grid<>();
        chatGrid.setSizeFull();
        chatGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        chatGrid.addComponentColumn(row -> reviewedBox(row.line().isReviewed(), checked -> {
            row.line().setReviewed(checked);
            chatCursor = row.number();
            persist();
            chatGrid.getDataProvider().refreshItem(row);
            refreshChatSummary();
        })).setHeader("✓").setWidth("45px").setFlexGrow(0);
        chatGrid.addColumn(Row::number).setHeader("#").setWidth("50px").setFlexGrow(0);
        chatGrid.addComponentColumn(row -> {
            // who said it in bold above the text, the messages of the user marked with »
            Div name = new Div(new Span((row.line().isUser() ? "» " : "") + row.line().getName()));
            name.addClassName(CssStyles.TEXT_BOLD);
            Div text = new Div(new Span(row.line().getText()));
            text.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE);
            return new Div(name, text);
        }).setHeader("Message").setFlexGrow(1);
        chatGrid.setPartNameGenerator(row -> {
            StringBuilder parts = new StringBuilder(STConverterStyles.PART_CLICKABLE);
            if (row.number() >= from && row.number() <= to) {
                parts.append(' ').append(STConverterStyles.PART_IN_RANGE);
            }
            if (row.line().isReviewed()) {
                parts.append(' ').append(STConverterStyles.PART_REVIEWED);
            }
            if (row.number() == chatCursor) {
                parts.append(' ').append(STConverterStyles.PART_CURRENT);
            }
            return parts.toString();
        });
        chatGrid.addItemClickListener(e -> {
            if (!generating) {
                clickMessage(e.getItem().number(), e.isShiftKey());
            }
        });
    }

    private void buildTurnGrid() {
        turnGrid = new Grid<>();
        turnGrid.setSizeFull();
        turnGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        turnGrid.addComponentColumn(turn -> reviewedBox(turn.isReviewed(), checked -> {
            turn.setReviewed(checked);
            turnCursor = data.getTurns().indexOf(turn) + 1;
            persist();
            turnGrid.getDataProvider().refreshItem(turn);
            refreshTurnSummary();
        })).setHeader("✓").setWidth("45px").setFlexGrow(0);
        turnGrid.addColumn(TurnItem::messages).setHeader("Msgs").setWidth("65px").setFlexGrow(0);
        turnGrid.addComponentColumn(ConverterView::turnContent).setHeader("Turn").setFlexGrow(1);
        turnGrid.addComponentColumn(turn -> {
            Button delete = new Button(Solid.TRASH.create(), e -> {
                data.getTurns().remove(turn);
                persist();
                refreshTurns();
            });
            delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            delete.setTooltipText("Delete the turn details");
            return delete;
        }).setWidth("50px").setFlexGrow(0);
        turnGrid.setPartNameGenerator(turn -> STConverterStyles.PART_CLICKABLE
                + (turn.isReviewed() ? " " + STConverterStyles.PART_REVIEWED : "")
                + (data.getTurns().indexOf(turn) + 1 == turnCursor ? " " + STConverterStyles.PART_CURRENT : ""));
        turnGrid.addItemClickListener(e -> {
            if (!opener.open(e.getItem())) {
                Notification.warning("The new turn form of the story is not available");
            }
        });
        turnGrid.setTooltipGenerator(turn -> "Click to put the turn details into the new turn form");
    }

    /** The four fields of a turn in one element, each under a bold line with its name (the fields without text are left out). */
    private static Div turnContent(TurnItem turn) {
        Div content = new Div();
        addField(content, "Scene setting", turn.getSceneSetting());
        addField(content, "POV character", turn.getPovCharacter());
        addField(content, "Present characters", turn.getPresentCharacters());
        addField(content, "Instructions", turn.getInstructions());
        return content;
    }

    private static void addField(Div content, String label, String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        Div name = new Div(new Span(label));
        name.addClassName(CssStyles.TEXT_BOLD);
        Div value = new Div(new Span(text));
        value.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE);
        content.add(name, value);
    }

    private static Checkbox reviewedBox(boolean value, java.util.function.Consumer<Boolean> onChange) {
        Checkbox box = new Checkbox(value);
        box.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                onChange.accept(e.getValue());
            }
        });
        return box;
    }

    // --- the chat ---

    /** Shows the upload while there is no chat, the messages when there are. */
    private void refreshChat() {
        chatPanel.removeAll();
        if (data.getChat().isEmpty()) {
            chatGrid = null;
            chatSummary = null;
            InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, bytes) -> ui.access(() -> upload(bytes, metadata.fileName())));
            Upload upload = new Upload(handler);
            upload.setAcceptedFileTypes(".jsonl", ".json", "application/json", "application/x-ndjson");
            upload.setMaxFiles(1);
            upload.setMaxFileSize(64 * 1024 * 1024);
            Span explanation = new Span("Upload a SillyTavern chat (.jsonl, without branches). It is kept with the book.");
            explanation.addClassName(STConverterStyles.HINT);
            VerticalLayout empty = new VerticalLayout(explanation, upload);
            empty.setPadding(true);
            chatPanel.add(empty);
            return;
        }

        chatSummary = new Span();
        chatSummary.addClassNames(CssStyles.TEXT_SECONDARY, STConverterStyles.ELLIPSIS);
        Button next = nextUncheckedButton(this::nextUncheckedMessage);
        Button remove = new Button(Solid.XMARK.create(), e -> ConfirmDialog.show(
                "Remove the chat and the turn details made of it from the book?", this::removeChat));
        remove.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        remove.setTooltipText("Remove the chat");
        HorizontalLayout buttons = new HorizontalLayout(next, remove);
        buttons.setSpacing(false);
        HorizontalLayout header = new HorizontalLayout(chatSummary, buttons);
        header.setWidthFull();
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassName(STConverterStyles.PAD);

        buildChatGrid();
        List<Row> rows = new ArrayList<>();
        List<ChatLine> chat = data.getChat();
        for (int i = 0; i < chat.size(); i++) {
            rows.add(new Row(i + 1, chat.get(i)));
        }
        chatGrid.setItems(rows);
        chatPanel.add(header, chatGrid);
        chatPanel.setFlexGrow(1, chatGrid);
        refreshChatSummary();
    }

    private void refreshChatSummary() {
        if (chatSummary != null) {
            long reviewed = data.getChat().stream().filter(ChatLine::isReviewed).count();
            chatSummary.setText(data.getChat().size() + " messages, " + reviewed + " checked");
            // the name of the file can be long
            chatSummary.getElement().setProperty("title", StringUtils.defaultIfBlank(data.getFileName(), "chat"));
        }
    }

    private void upload(byte[] bytes, String fileName) {
        try {
            int count = service.importChat(data, bytes, fileName);
            service.save(manuscript, data);
            lineTokens = null;
            resetRange();
            refreshAll();
            Notification.success(count + " messages loaded");
        } catch (IllegalArgumentException e) {
            Notification.warning(StringUtils.defaultIfBlank(e.getMessage(), "Not a SillyTavern chat") + " (" + fileName + ")");
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void removeChat() {
        chatCursor = 0;
        turnCursor = 0;
        data.setChat(new ArrayList<>());
        data.setTurns(new ArrayList<>());
        data.setFileName(null);
        persist();
        lineTokens = null;
        resetRange();
        refreshAll();
    }

    /** The button that goes to the next row that is not checked. */
    private static Button nextUncheckedButton(Runnable action) {
        Button button = new Button(Solid.ANGLES_DOWN.create(), e -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        button.setTooltipText("Go to the next unchecked row");
        return button;
    }

    private void nextUncheckedMessage() {
        if (chatGrid == null) {
            return;
        }
        List<ChatLine> chat = data.getChat();
        int index = ConverterService.nextUnchecked(chat.size(), chatCursor, i -> chat.get(i).isReviewed());
        if (index < 0) {
            Notification.success("All messages are checked");
            return;
        }
        chatCursor = index + 1;
        chatGrid.getDataProvider().refreshAll();
        chatGrid.scrollToIndex(index);
    }

    private void nextUncheckedTurn() {
        List<TurnItem> turns = data.getTurns();
        int index = ConverterService.nextUnchecked(turns.size(), turnCursor, i -> turns.get(i).isReviewed());
        if (index < 0) {
            Notification.success(turns.isEmpty() ? "There are no turn details" : "All turns are checked");
            return;
        }
        turnCursor = index + 1;
        turnGrid.getDataProvider().refreshAll();
        turnGrid.scrollToIndex(index);
    }

    // --- the turn details ---

    private void refreshTurns() {
        turnCursor = Math.min(turnCursor, data.getTurns().size());
        turnGrid.setItems(new ArrayList<>(data.getTurns()));
        refreshTurnSummary();
    }

    private void refreshTurnSummary() {
        long reviewed = data.getTurns().stream().filter(TurnItem::isReviewed).count();
        turnSummary.setText(data.getTurns().isEmpty()
                ? "No turn details yet: choose a range and use the wand."
                : data.getTurns().size() + " turns, " + reviewed + " checked. Click one to open it in the new turn form.");
        turnsTab.setLabel("Turn details (" + data.getTurns().size() + ")");
    }

    // --- the range ---

    private void resetRange() {
        from = 1;
        to = Math.max(1, data.getChat().size());
        anchor = from;
    }

    private void clickMessage(int number, boolean extend) {
        chatCursor = number;
        if (extend) {
            from = Math.min(anchor, number);
            to = Math.max(anchor, number);
        } else {
            anchor = number;
            from = number;
            to = number;
        }
        rangeChanged();
    }

    private void rangeTyped() {
        if (adjusting) {
            return;
        }
        int size = data.getChat().size();
        int typedFrom = fromField.getValue() != null ? fromField.getValue() : 1;
        int typedTo = toField.getValue() != null ? toField.getValue() : size;
        from = Math.max(1, Math.min(typedFrom, Math.max(1, size)));
        to = Math.max(from, Math.min(typedTo, Math.max(1, size)));
        anchor = from;
        rangeChanged();
    }

    private void rangeChanged() {
        adjusting = true;
        try {
            fromField.setValue(from);
            toField.setValue(to);
        } finally {
            adjusting = false;
        }
        if (chatGrid != null) {
            chatGrid.getDataProvider().refreshAll();
        }
        updateTokenInfo();
    }

    // --- tokens ---

    private void modelChanged() {
        data.setAiId(picker.getAi() != null ? picker.getAi().getId() : null);
        data.setProtocolId(picker.getProtocol() != null ? picker.getProtocol().getId() : null);
        persist();
        lineTokens = null;
        baseTokens = null;
        updateTokenInfo();
    }

    /**
     * Shows what the wand would send for the range: the tokens of its messages and of the rest of the prompt (the template
     * and the lore, counted once).
     */
    private void updateTokenInfo() {
        List<ChatLine> chat = data.getChat();
        if (chat.isEmpty()) {
            tokenInfo.setText("");
            return;
        }
        AI ai = picker.getAi();
        if (lineTokens == null) {
            lineTokens = new long[chat.size()];
            for (int i = 0; i < chat.size(); i++) {
                lineTokens[i] = service.countTokens(ai, chat.get(i).getText()) + 12;
            }
        }
        long chatTokens = 0;
        for (int i = Math.max(1, from); i <= Math.min(to, chat.size()); i++) {
            chatTokens += lineTokens[i - 1];
        }
        if (baseTokens == null) {
            try {
                String empty = service.buildPrompt(manuscript, data, service.promptTemplate(data), 1, 0);
                baseTokens = service.countTokens(ai, empty);
            } catch (Exception e) {
                // an invalid template of the user: the counter does without the rest of the prompt
                STConverterLog.get().warn("Can't count the prompt: {}", e.getMessage());
                baseTokens = 0L;
            }
        }
        tokenInfo.setText((to - from + 1) + " messages: about " + numbers.format(chatTokens + baseTokens)
                + " tokens (chat " + numbers.format(chatTokens) + " + prompt " + numbers.format(baseTokens) + ")");
    }

    // --- the wand ---

    private void generate() {
        if (data.getChat().isEmpty()) {
            Notification.warning("Load a chat first");
            return;
        }
        AI ai = picker.getAi();
        if (ai == null) {
            Notification.warning("Choose a model");
            return;
        }
        try {
            String prompt = service.buildPrompt(manuscript, data, service.promptTemplate(data), from, to);
            JsonNode schema = service.schema();
            int runFrom = from;
            int runTo = to;
            output.setLength(0);
            showStatus(null);
            InferenceService inference = inferenceServices.forAIOrThrow(ai);
            generating = true;
            updateControls();
            showStatus("Waiting for the model...");
            try {
                running = inference.stream(service.toPayload(prompt), picker.getProtocol(), schema, null, callback(runFrom, runTo));
            } catch (Exception e) {
                // nothing runs, do not leave the controls frozen
                generating = false;
                running = null;
                updateControls();
                throw e;
            }
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            // a provider that can't take a response schema
            Notification.warning(e.getMessage());
            showStatus(null);
        } catch (Exception e) {
            // most likely a prompt that is not a valid template
            Notification.error("Can't convert: " + StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
            showStatus(null);
        }
    }

    private InferenceService.InferenceAsyncCallback callback(int runFrom, int runTo) {
        return new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                ui.access(() -> {
                    if (chunkType == InferenceService.ChunkType.RESPONSE) {
                        output.append(text);
                        JsonNode partial = PartialJson.parse(output.toString());
                        int turns = partial != null && partial.path("turns").isArray() ? partial.path("turns").size() : 0;
                        showStatus("Writing the turn details... " + turns + " so far");
                    } else {
                        showStatus("The model is thinking...");
                    }
                    UIPushGuard.push(ui);
                    controller.continueInference();
                });
            }

            @Override
            public void onCompletion() {
                finish(runFrom, runTo, null);
            }

            @Override
            public void onCancel() {
                finish(runFrom, runTo, null);
            }

            @Override
            public void onError(Throwable exception) {
                finish(runFrom, runTo, exception);
            }

            @Override
            public boolean isDead() {
                return running != null && running.isCancelled();
            }
        };
    }

    private void finish(int runFrom, int runTo, Throwable error) {
        ui.access(() -> {
            boolean stopped = running != null && running.isCancelled();
            generating = false;
            running = null;
            showStatus(null);
            if (error != null) {
                UIUtils.inferenceError(loc, error);
            } else if (stopped) {
                Notification.warning("Stopped, nothing was changed");
            } else if (output.isEmpty()) {
                showStatus("The model did not answer.");
            } else {
                accept(runFrom, runTo);
            }
            updateControls();
            UIPushGuard.push(ui);
        });
    }

    /** Checks the answer and puts its turns into the turn details of the book. */
    private void accept(int runFrom, int runTo) {
        try {
            List<TurnItem> added = service.readAnswer(output.toString(), runFrom, runTo);
            int replaced = ConverterService.mergeTurns(data.getTurns(), added, runFrom, runTo);
            persist();
            refreshTurns();
            baseTokens = null;
            tabs.setSelectedTab(turnsTab);
            Notification.success(added.size() + " turns for the messages " + runFrom + "-" + runTo
                    + (replaced > 0 ? " (" + replaced + " earlier ones replaced)" : ""));
        } catch (Exception e) {
            STConverterLog.get().warn("The answer of the model is not usable: {}", e.getMessage());
            showStatus(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
        }
    }

    private void stop() {
        CancellationToken token = running;
        if (token != null) {
            token.cancel();
        }
    }

    // --- the prompt ---

    private void openPromptDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Prompt of the wand");
        dialog.setWidth("800px");
        TextArea area = new TextArea();
        area.setWidthFull();
        area.setMinHeight("360px");
        area.setMaxHeight("70vh");
        area.setValue(service.promptTemplate(data));
        area.setHelperText("Variables: {{backgroundLore}}, {{chat}}, {{firstMessage}}, {{lastMessage}}");
        dialog.add(area);
        Button defaults = new Button("Default", e -> area.setValue(Defaults.PROMPT));
        Button cancel = new Button("Cancel", e -> dialog.close());
        Button save = new Button("Save", e -> {
            String value = area.getValue();
            data.setPrompt(StringUtils.isBlank(value) || value.strip().equals(Defaults.PROMPT.strip()) ? null : value);
            persist();
            baseTokens = null;
            updateTokenInfo();
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(defaults, cancel, save);
        dialog.open();
    }

    // --- display ---

    private void refreshAll() {
        refreshChat();
        refreshTurns();
        boolean loaded = !data.getChat().isEmpty();
        // the model and the range only matter when there is a chat
        convertDetails.setVisible(loaded);
        if (loaded) {
            rangeChanged();
        }
        updateControls();
    }

    private void showStatus(String message) {
        status.setText(message == null ? "" : message);
        status.setVisible(message != null);
    }

    /** Freezes the controls while the model runs; the wand then stops it. */
    private void updateControls() {
        picker.setEnabled(!generating);
        fromField.setEnabled(!generating);
        toField.setEnabled(!generating);
        promptButton.setEnabled(!generating);
        wandButton.setText(generating ? "Stop" : "Convert range");
        wandButton.setIcon((generating ? Solid.STOP : Solid.WAND_MAGIC_SPARKLES).create());
        wandButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        if (generating) {
            wandButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        }
    }

    /** Saves the data of the book; a failure is shown. */
    private void persist() {
        try {
            service.save(manuscript, data);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
