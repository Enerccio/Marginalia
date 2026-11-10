package com.github.enerccio.marginalia.extensions.stconverter.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.structured.PartialJson;
import com.github.enerccio.marginalia.extensions.stconverter.STConverterExtension;
import com.github.enerccio.marginalia.extensions.stconverter.STConverterLog;
import com.github.enerccio.marginalia.extensions.stconverter.model.ChatLine;
import com.github.enerccio.marginalia.extensions.stconverter.model.ConverterData;
import com.github.enerccio.marginalia.extensions.stconverter.model.TurnItem;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService;
import com.github.enerccio.marginalia.extensions.stconverter.service.ConverterService.ModelSelection;
import com.github.enerccio.marginalia.extensions.stconverter.service.Defaults;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
import org.springframework.web.context.request.RequestAttributes;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.stconverter.STConverterLocalization.*;

/**
 * The tab of the converter next to the story: the SillyTavern chat the author loaded and the turn details the model
 * prepared from it, both kept with the book. Above the two lists the model for the wand, the range of the chat the wand works
 * on (clicked in the chat, or typed) with the number of tokens the request takes, and the wand itself.
 * <p>
 * The chat is a list of messages (who said what) with a checkbox for the ones the author has gone through; the turn details
 * are a list of what the new turn form of the story takes, also with a checkbox, and a click on one puts it into the form.
 * <p>
 * The source can also be the parts of another book (picked from the books of the author, or read from a backup file): the
 * list is then the text of the parts and the turn details are their instructions, which the book has already, so there is no
 * wand for it.
 * <p>
 * On a phone (the drawer of the story) the tab is the same, narrower: the range is chosen with two taps (a tap starts it, the
 * next one ends it) because there is no Shift key, and the turn details have no column of their own for the numbers of the
 * messages.
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

    @Autowired
    private ManuscriptService manuscriptService;

    private final ConverterService service;
    private final Manuscript manuscript;
    private final TurnOpener opener;
    private final boolean phone;
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
    private Checkbox alignBox;
    private Button autoButton;
    private HorizontalLayout autoBar;
    private IntegerField batchField;
    private Button autoStartButton;
    private Span autoInfo;
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
    /** On a phone: the next tap ends the range that the last one started. */
    private boolean endTap;
    private boolean adjusting;
    /** The number (from 1) of the message the author was at last (checked, clicked or jumped to), 0 for none; the same for the turns. */
    private int chatCursor;
    private int turnCursor;

    /** Tokens of each message, null until counted (and again after the model changes). */
    private long[] lineTokens;
    /** Tokens of the prompt without the chat (the template and the lore), null until counted. */
    private Long baseTokens;

    /**
     * The request context of the browser session. The model answers on other threads, which have none, and the services (the
     * prompt, the book, the tokens) need the session of the user; the work that follows an answer runs inside of it. Taken
     * again whenever the user starts something, on the thread of their request.
     */
    private RequestAttributes requestScope = ThreadCopyRequestAttributes.create();

    private boolean generating;
    private CancellationToken running;
    private final StringBuilder output = new StringBuilder();

    /** The auto mode: the blocks of messages sent one after another (null while it is not running), the one it is at, and the range to give back. */
    private List<int[]> autoBlocks;
    private int autoIndex;
    private int[] autoOriginal;
    private boolean autoStopped;
    private int autoAdded;
    private int autoReplaced;
    /** Which try of the block it is at (from 0) and why the last one did not give turn details. */
    private int autoAttempt;
    private String autoProblem;
    /** How often a block is sent when the answer of the model can't be used. */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * @param phone the tab is in the drawer of the story on a phone
     */
    public ConverterView(ConverterService service, Manuscript manuscript, TurnOpener opener, boolean phone) {
        this.service = service;
        this.manuscript = manuscript;
        this.opener = opener;
        this.phone = phone;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        // small text, tight spacing, and the whole tab scrolls
        addClassName(STConverterStyles.ROOT);
        if (phone) {
            addClassName(STConverterStyles.PHONE);
        }

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

        fromField = rangeField(T.localized(FROM));
        toField = rangeField(T.localized(TO));
        fromField.addValueChangeListener(e -> rangeTyped());
        toField.addValueChangeListener(e -> rangeTyped());
        Button allButton = new Button(T.localized(ALL), e -> {
            resetRange();
            rangeChanged();
        });
        allButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        tokenInfo = new Span();
        tokenInfo.addClassName(CssStyles.TEXT_SECONDARY);
        tokenInfo.setWidthFull();

        wandButton = new Button(T.localized(CONVERT_RANGE), Solid.WAND_MAGIC_SPARKLES.create(), e -> {
            if (generating) {
                stop();
            } else {
                generate();
            }
        });
        wandButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        wandButton.setTooltipText(T.localized(ASK_THE_MODEL));
        promptButton = new Button(Solid.PEN.create(), e -> openPromptDialog());
        promptButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        promptButton.setTooltipText(T.localized(EDIT_THE_PROMPT));

        HorizontalLayout fields = new HorizontalLayout(fromField, toField, allButton);
        // the buttons sit on the line of the bottom of the fields, not of their labels
        fields.setAlignItems(FlexComponent.Alignment.END);
        fields.setSpacing(false);
        fields.getThemeList().add("spacing-xs");
        HorizontalLayout actions = new HorizontalLayout(wandButton, promptButton);
        actions.setAlignItems(FlexComponent.Alignment.END);
        actions.setSpacing(false);
        actions.getThemeList().add("spacing-xs");
        rangeBar = new HorizontalLayout(fields, actions, tokenInfo);
        rangeBar.setWidthFull();
        rangeBar.setWrap(true);
        rangeBar.setSpacing(false);
        rangeBar.getThemeList().add("spacing-xs");
        rangeBar.setAlignItems(FlexComponent.Alignment.END);
        rangeBar.setPadding(false);

        alignBox = new Checkbox(T.localized(ALIGN_TO_TURNS), data.isAlign());
        alignBox.setTooltipText(T.localized(THE_RANGE_STARTS));
        alignBox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                data.setAlign(e.getValue());
                persist();
                applyAlign();
                rangeChanged();
            }
        });
        autoButton = new Button(T.localized(AUTO_MODE), Solid.GEARS.create(), e -> autoBar.setVisible(!autoBar.isVisible()));
        autoButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        autoButton.setTooltipText(T.localized(CONVERT_THE_WHOLE_RANGE));
        HorizontalLayout options = new HorizontalLayout(alignBox, autoButton);
        options.setAlignItems(FlexComponent.Alignment.CENTER);
        options.setSpacing(false);
        options.setPadding(false);

        batchField = new IntegerField(T.localized(MESSAGES_PER_BATCH));
        batchField.setMin(1);
        batchField.setMax(1000);
        batchField.addThemeName("small");
        batchField.setWidth("150px");
        batchField.setStepButtonsVisible(false);
        batchField.setValue(data.getBatchSize());
        batchField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                data.setBatchSize(e.getValue() != null ? Math.max(1, e.getValue()) : ConverterData.DEFAULT_BATCH_SIZE);
                persist();
                updateTokenInfo();
            }
        });
        autoStartButton = new Button(T.localized(CONVERT_IN_BATCHES), Solid.FORWARD.create(), e -> {
            if (generating) {
                stop();
            } else {
                startAuto();
            }
        });
        autoStartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        autoStartButton.setTooltipText(T.localized(ASK_THE_MODEL_FOR_THE_TURN));
        autoInfo = new Span();
        autoInfo.addClassName(CssStyles.TEXT_SECONDARY);
        autoInfo.setWidthFull();
        autoBar = new HorizontalLayout(batchField, autoStartButton, autoInfo);
        autoBar.setWidthFull();
        autoBar.setWrap(true);
        autoBar.setSpacing(false);
        autoBar.getThemeList().add("spacing-xs");
        autoBar.setPadding(false);
        autoBar.setAlignItems(FlexComponent.Alignment.END);
        autoBar.setVisible(false);

        hint = new Span(phone ? T.localized(TAP_A_MESSAGE_TO_START)
                : T.localized(CLICK_A_MESSAGE_TO_START));
        hint.addClassName(STConverterStyles.HINT);
        status = new Span();
        status.addClassName(CssStyles.NOTICE_WARNING);
        status.setVisible(false);

        // closed until the author needs the wand: the lists get the room
        VerticalLayout convertContent = new VerticalLayout(picker, rangeBar, options, autoBar, hint);
        convertContent.setWidthFull();
        convertContent.setSpacing(false);
        convertContent.setPadding(false);
        convertContent.getThemeList().add("spacing-xs");
        convertDetails = new Details(T.localized(CONVERT_TO_TURN_DETAILS), convertContent);
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
        tabs.setWidthFull();
        tabs.addClassName(STConverterStyles.TABS);
        chatTab = new Tab(T.localized(CHAT));
        turnsTab = new Tab(T.localized(TURN_DETAILS));
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
        })).setHeader("✓").setWidth(phone ? "40px" : "45px").setFlexGrow(0);
        chatGrid.addColumn(Row::number).setHeader("#").setWidth(phone ? "42px" : "50px").setFlexGrow(0);
        chatGrid.addComponentColumn(row -> {
            // who said it in bold above the text, the messages of the user marked with »
            Div text = new Div(new Span(row.line().getText()));
            text.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE);
            if (StringUtils.isBlank(row.line().getName())) {
                return new Div(text);
            }
            Div name = new Div(new Span((row.line().isUser() ? "» " : "") + row.line().getName()));
            name.addClassName(CssStyles.TEXT_BOLD);
            return new Div(name, text);
        }).setHeader(T.localized(MESSAGE)).setFlexGrow(1);
        chatGrid.setPartNameGenerator(row -> {
            StringBuilder parts = new StringBuilder(STConverterStyles.PART_CLICKABLE);
            if (!data.isBookSource() && row.number() >= from && row.number() <= to) {
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
            // the range is for the wand, and the parts of a book have no wand
            if (!generating && !data.isBookSource()) {
                clickMessage(e.getItem().number(), phone ? endTap : e.isShiftKey());
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
        })).setHeader("✓").setWidth(phone ? "40px" : "45px").setFlexGrow(0);
        if (!phone) {
            turnGrid.addColumn(TurnItem::messages).setHeader(T.localized(MSGS)).setWidth("65px").setFlexGrow(0);
        }
        // the narrow tab has no room for a column of the numbers, they are the first line of the turn
        turnGrid.addComponentColumn(turn -> turnContent(turn, phone)).setHeader(T.localized(TURN)).setFlexGrow(1);
        turnGrid.addComponentColumn(turn -> {
            Button delete = new Button(Solid.TRASH.create(), e -> {
                data.getTurns().remove(turn);
                persist();
                refreshTurns();
            });
            delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            delete.setTooltipText(T.localized(DELETE_THE_TURN_DETAILS));
            // the turn details of a book are its instructions, read from the stored parts each time
            delete.setVisible(!data.isBookSource());
            Button merge = new Button(Solid.CODE_MERGE.create(), e -> openMergeDialog(turn));
            merge.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            merge.setTooltipText(T.localized(MERGE_WITH_THE_NEXT_TURN));
            // the last turn has none after it
            merge.setVisible(data.getTurns().indexOf(turn) < data.getTurns().size() - 1);
            return new Div(merge, delete);
        }).setWidth(phone ? "44px" : "50px").setFlexGrow(0);
        turnGrid.setPartNameGenerator(turn -> STConverterStyles.PART_CLICKABLE
                + (turn.isReviewed() ? " " + STConverterStyles.PART_REVIEWED : "")
                + (data.getTurns().indexOf(turn) + 1 == turnCursor ? " " + STConverterStyles.PART_CURRENT : ""));
        turnGrid.addItemClickListener(e -> {
            if (!opener.open(e.getItem())) {
                Notification.warning(T.localized(THE_NEW_TURN_FORM));
            }
        });
        turnGrid.setTooltipGenerator(turn -> T.localized(CLICK_TO_PUT_THE_TURN));
    }

    /** The four fields of a turn in one element, each under a bold line with its name (the fields without text are left out). */
    private static Div turnContent(TurnItem turn, boolean withMessages) {
        Div content = new Div();
        if (withMessages) {
            addField(content, T.localized(MSGS), turn.messages());
        }
        addField(content, T.localized(SCENE_SETTING), turn.getSceneSetting());
        addField(content, T.localized(POV_CHARACTER), turn.getPovCharacter());
        addField(content, T.localized(PRESENT_CHARACTERS), turn.getPresentCharacters());
        addField(content, T.localized(INSTRUCTIONS), turn.getInstructions());
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
            Upload chatUpload = upload(Solid.UPLOAD, T.localized(UPLOAD_A_CHAT), (bytes, fileName) -> uploadChat(bytes, fileName),
                    ".jsonl", ".json", "application/json", "application/x-ndjson");
            Upload backupUpload = upload(Solid.BOX_ARCHIVE, T.localized(LOAD_A_BACKUP), (bytes, fileName) -> uploadBackup(bytes, fileName),
                    ".json", ".zip", "application/json", "application/zip");
            Button bookButton = new Button(T.localized(PICK_A_BOOK), Solid.BOOK.create(), e -> openBookPicker());
            Span chatExplanation = new Span(T.localized(A_SILLYTAVERN_CHAT_JSONL_OR_AN_LM_STUDIO));
            chatExplanation.addClassName(STConverterStyles.HINT);
            Span bookExplanation = new Span(T.localized(OR_THE_PARTS));
            bookExplanation.addClassName(STConverterStyles.HINT);
            Span keptExplanation = new Span(T.localized(ONLY_THE_INSTRUCTIONS));
            keptExplanation.addClassName(STConverterStyles.HINT);
            VerticalLayout empty = new VerticalLayout(chatExplanation, chatUpload, bookExplanation, backupUpload, bookButton, keptExplanation);
            empty.setPadding(true);
            chatPanel.add(empty);
            return;
        }

        chatSummary = new Span();
        chatSummary.addClassNames(CssStyles.TEXT_SECONDARY, STConverterStyles.ELLIPSIS);
        Button next = nextUncheckedButton(this::nextUncheckedMessage);
        Button remove = new Button(Solid.XMARK.create(), e -> ConfirmDialog.show(data.isBookSource()
                ? T.localized(REMOVE_THE_SOURCE_AND_WHAT)
                : T.localized(REMOVE_THE_CHAT_AND_THE_TURN), this::removeChat));
        remove.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        remove.setTooltipText(data.isBookSource() ? T.localized(REMOVE_THE_SOURCE) : T.localized(REMOVE_THE_CHAT));
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
            chatSummary.setText((data.isBookSource() ? T.localized(PARTS_CHECKED) : T.localized(MESSAGES_CHECKED))
                    .formatted(data.getChat().size(), reviewed));
            // the name of the file can be long
            chatSummary.getElement().setProperty("title", StringUtils.defaultIfBlank(data.getFileName(), data.isBookSource() ? T.localized(BOOK) : T.localized(CHAT_LOWERCASE)));
        }
    }

    /** The upload with the button of the given label; the file is handed to the action in the session of the browser. */
    private Upload upload(Solid icon, String label, java.util.function.BiConsumer<byte[], String> action, String... types) {
        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, bytes) -> ui.access(() -> action.accept(bytes, metadata.fileName())));
        Upload upload = new Upload(handler);
        upload.setUploadButton(new Button(label, icon.create()));
        upload.setAcceptedFileTypes(types);
        upload.setMaxFiles(1);
        upload.setMaxFileSize(64 * 1024 * 1024);
        return upload;
    }

    private void uploadChat(byte[] bytes, String fileName) {
        try {
            int count = service.importChat(manuscript, data, bytes, fileName);
            sourceLoaded();
            Notification.success(T.localized(MESSAGES_LOADED).formatted(count));
        } catch (IllegalArgumentException e) {
            Notification.warning(StringUtils.defaultIfBlank(e.getMessage(), T.localized(NOT_A_SILLYTAVERN_OR_LM_STUDIO_CHAT)) + " (" + fileName + ")");
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void uploadBackup(byte[] bytes, String fileName) {
        try {
            int count = service.importBackup(manuscript, data, bytes);
            sourceLoaded();
            Notification.success(T.localized(PARTS_LOADED).formatted(count));
        } catch (IllegalArgumentException e) {
            Notification.warning(StringUtils.defaultIfBlank(e.getMessage(), T.localized(NOT_A_BACKUP)) + " (" + fileName + ")");
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /** Takes the parts of the book the author picks; the books of the author, but not the one that is open. */
    private void openBookPicker() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(PICK_A_BOOK));
        dialog.setWidth("min(500px, 95vw)");
        ComboBox<Manuscript> books = new ComboBox<>(T.localized(BOOK));
        new BookLabels().apply(books);
        books.setWidthFull();
        try {
            books.setItems(manuscriptService.findAllRegularForUser().stream()
                    .filter(book -> manuscript.getId() == null || !manuscript.getId().equals(book.getId()))
                    .sorted(Comparator.comparing(book -> StringUtils.defaultString(book.getName()), String.CASE_INSENSITIVE_ORDER))
                    .toList());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Span hint = new Span(T.localized(THE_PARTS_OF_THE_BRANCH));
        hint.addClassName(STConverterStyles.HINT);
        dialog.add(new VerticalLayout(books, hint));
        Button cancel = new Button(T.localized(CANCEL), e -> dialog.close());
        Button load = new Button(T.localized(LOAD), e -> {
            Manuscript picked = books.getValue();
            if (picked == null) {
                return;
            }
            try {
                int count = service.importBook(manuscript, data, picked);
                dialog.close();
                sourceLoaded();
                Notification.success(T.localized(PARTS_LOADED).formatted(count));
            } catch (IllegalArgumentException ex) {
                Notification.warning(ex.getMessage());
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
        load.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        load.setEnabled(false);
        books.addValueChangeListener(e -> load.setEnabled(e.getValue() != null));
        dialog.getFooter().add(cancel, load);
        dialog.open();
    }

    /** Saves the source that was just loaded and shows it. */
    private void sourceLoaded() throws Exception {
        service.save(manuscript, data);
        chatCursor = 0;
        turnCursor = 0;
        lineTokens = null;
        resetRange();
        refreshAll();
    }

    private void removeChat() {
        chatCursor = 0;
        turnCursor = 0;
        try {
            service.removeChat(data);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        persist();
        lineTokens = null;
        resetRange();
        refreshAll();
    }

    /** The button that goes to the next row that is not checked. */
    private static Button nextUncheckedButton(Runnable action) {
        Button button = new Button(Solid.ANGLES_DOWN.create(), e -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        button.setTooltipText(T.localized(GO_TO_THE_NEXT_UNCHECKED));
        return button;
    }

    private void nextUncheckedMessage() {
        if (chatGrid == null) {
            return;
        }
        List<ChatLine> chat = data.getChat();
        int index = ConverterService.nextUnchecked(chat.size(), chatCursor, i -> chat.get(i).isReviewed());
        if (index < 0) {
            Notification.success(T.localized(ALL_MESSAGES_ARE_CHECKED));
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
            Notification.success(turns.isEmpty() ? T.localized(THERE_ARE_NO_TURN_DETAILS) : T.localized(ALL_TURNS_ARE_CHECKED));
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

    /**
     * Shows the turn and the one after it joined, for the author to edit, and makes one turn of them. The turn details of a
     * book can be merged too: the merged ones are kept with the book and replace those of its parts.
     */
    private void openMergeDialog(TurnItem turn) {
        int index = data.getTurns().indexOf(turn);
        if (index < 0 || index + 1 >= data.getTurns().size()) {
            return;
        }
        TurnItem next = data.getTurns().get(index + 1);
        new MergeTurnsDialog(loc, T.localized(MERGE_WITH_THE_NEXT_TURN), toMergeTurn(turn), toMergeTurn(next), 0, merged -> {
            // the list may have changed while the dialog was open (the wand, a delete)
            int at = data.getTurns().indexOf(turn);
            if (at < 0 || at + 1 >= data.getTurns().size() || data.getTurns().get(at + 1) != next) {
                Notification.warning(T.localized(THE_TURN_DETAILS_HAVE_CHANGED));
                return true;
            }
            ConverterService.joinTurns(data, at, merged.sceneSetting(), merged.povCharacter(), merged.presentCharacters(), merged.instructions());
            turnCursor = at + 1;
            persist();
            refreshTurns();
            return true;
        }).open();
    }

    private static MergeTurnsDialog.Turn toMergeTurn(TurnItem turn) {
        return new MergeTurnsDialog.Turn(null, turn.getSceneSetting(), turn.getPovCharacter(), turn.getPresentCharacters(), turn.getInstructions());
    }

    private void refreshTurnSummary() {
        long reviewed = data.getTurns().stream().filter(TurnItem::isReviewed).count();
        turnSummary.setText(data.getTurns().isEmpty()
                ? T.localized(NO_TURN_DETAILS_YET_CHOOSE)
                : T.localized(TURNS_CHECKED_CLICK_ONE).formatted(data.getTurns().size(), reviewed));
        turnsTab.setLabel(T.localized(TURN_DETAILS_D).formatted(data.getTurns().size()));
    }

    // --- the range ---

    private void resetRange() {
        from = 1;
        to = Math.max(1, data.getChat().size());
        anchor = from;
        endTap = false;
        applyAlign();
    }

    /** Moves the range to the boundaries of the turns when the author wants that. */
    private void applyAlign() {
        if (data.isAlign() && !data.getChat().isEmpty()) {
            int[] aligned = ConverterService.alignRange(data.getChat(), from, to);
            from = aligned[0];
            to = aligned[1];
        }
    }

    private void clickMessage(int number, boolean extend) {
        chatCursor = number;
        // on a phone the tap that starts a range is followed by the one that ends it
        endTap = phone && !extend;
        if (extend) {
            from = Math.min(anchor, number);
            to = Math.max(anchor, number);
        } else {
            anchor = number;
            from = number;
            to = number;
        }
        applyAlign();
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
        endTap = false;
        applyAlign();
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
            autoInfo.setText("");
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
        tokenInfo.setText(T.localized(MESSAGES_ABOUT_TOKENS_CHAT).formatted(to - from + 1,
                numbers.format(chatTokens + baseTokens), numbers.format(chatTokens), numbers.format(baseTokens)));

        // the auto mode sends the range in batches, each with the prompt
        int requests = ConverterService.splitBlocks(chat, from, to, data.getBatchSize(), data.isAlign()).size();
        autoInfo.setText((requests == 1 ? T.localized(REQUEST_ABOUT_TOKENS_IN_ALL) : T.localized(REQUESTS_ABOUT_TOKENS_IN_ALL))
                .formatted(requests, numbers.format(chatTokens + baseTokens * requests)));
    }

    // --- the wand ---

    private void generate() {
        startRun(from, to);
    }

    /**
     * Sends the messages {@code runFrom} to {@code runTo} to the model.
     *
     * @return false when nothing runs (the reason is shown)
     */
    private boolean startRun(int runFrom, int runTo) {
        RequestAttributes fresh = ThreadCopyRequestAttributes.create();
        if (fresh != null) {
            requestScope = fresh;
        }
        if (data.getChat().isEmpty()) {
            Notification.warning(T.localized(LOAD_A_CHAT_FIRST));
            return false;
        }
        AI ai = picker.getAi();
        if (ai == null) {
            Notification.warning(T.localized(CHOOSE_A_MODEL));
            return false;
        }
        try {
            String prompt = service.buildPrompt(manuscript, data, service.promptTemplate(data), runFrom, runTo);
            JsonNode schema = service.schema();
            output.setLength(0);
            showStatus(null);
            InferenceService inference = inferenceServices.forAIOrThrow(ai);
            generating = true;
            updateControls();
            showStatus(progress(T.localized(WAITING_FOR_THE_MODEL)));
            try (InferenceSession _ = InferenceSession.open("sillytavern-converter")) {
                running = inference.stream(service.toPayload(prompt), picker.getProtocol(), schema, null, callback(runFrom, runTo));
            } catch (Exception e) {
                // nothing runs, do not leave the controls frozen
                generating = false;
                running = null;
                updateControls();
                throw e;
            }
            return true;
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            // a provider that can't take a response schema
            autoProblem = StringUtils.defaultIfBlank(e.getMessage(), e.toString());
            Notification.warning(autoProblem, Notification.DURATION_LONGER);
            showStatus(autoProblem);
        } catch (Exception e) {
            // most likely a prompt that is not a valid template
            STConverterLog.get().warn("Can't convert", e);
            autoProblem = T.localized(CANT_CONVERT).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
            Notification.error(autoProblem, Notification.DURATION_LONGER);
            showStatus(autoProblem);
        }
        return false;
    }

    /** The text with the batch the auto mode is at in front of it. */
    private String progress(String text) {
        if (autoBlocks == null) {
            return text;
        }
        return autoAttempt > 0
                ? T.localized(BATCH_OF_TRY).formatted(autoIndex + 1, autoBlocks.size(), autoAttempt + 1, MAX_ATTEMPTS, text)
                : T.localized(BATCH).formatted(autoIndex + 1, autoBlocks.size(), text);
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
                        showStatus(progress(T.localized(WRITING_THE_TURN_DETAILS_SO).formatted(turns)));
                    } else {
                        showStatus(progress(T.localized(THE_MODEL_IS_THINKING)));
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
            try (InRequestScope _ = new InRequestScope(requestScope)) {
                afterRun(runFrom, runTo, error);
            } catch (RuntimeException e) {
                // nothing may leave the controls frozen or the auto mode half way without a word
                STConverterLog.get().error("The converter failed after a request", e);
                generating = false;
                running = null;
                if (autoBlocks != null) {
                    autoProblem = StringUtils.defaultIfBlank(e.getMessage(), e.toString());
                    endAuto();
                } else {
                    updateControls();
                }
                Notification.error(T.localized(CANT_GO).formatted(StringUtils.defaultIfBlank(e.getMessage(), e.toString())));
            }
            UIPushGuard.push(ui);
        });
    }

    private void afterRun(int runFrom, int runTo, Throwable error) {
        boolean stopped = running != null && running.isCancelled();
        generating = false;
        running = null;
        showStatus(null);
        boolean accepted = false;
        boolean usable = true;
        if (error != null) {
            UIUtils.inferenceError(loc, error);
            autoProblem = StringUtils.defaultIfBlank(error.getMessage(), error.toString());
            usable = false;
        } else if (stopped) {
            if (autoBlocks == null) {
                Notification.warning(T.localized(STOPPED_NOTHING_WAS_CHANGED));
            }
            usable = false;
        } else if (output.isEmpty()) {
            autoProblem = T.localized(THE_MODEL_DID_NOT_ANSWER);
            showStatus(autoProblem);
        } else {
            accepted = accept(runFrom, runTo);
        }
        if (autoBlocks == null) {
            if (error != null || (!stopped && !accepted)) {
                new DesktopNotifier(ui).failed(STConverterExtension.NOTIFY_CONVERT, false, manuscript.getName());
            } else if (!stopped) {
                new DesktopNotifier(ui).finished(STConverterExtension.NOTIFY_CONVERT, false, manuscript.getName());
            }
            updateControls();
        } else if (accepted && !autoStopped) {
            autoIndex++;
            autoAttempt = 0;
            autoProblem = null;
            runNextBlock();
        } else if (usable && !accepted && !autoStopped && autoAttempt + 1 < MAX_ATTEMPTS) {
            // an answer that can't be used happens, the same batch is asked for again
            STConverterLog.get().warn("Batch {} of {} gave no usable answer, trying again: {}", autoIndex + 1, autoBlocks.size(), autoProblem);
            autoAttempt++;
            runNextBlock();
        } else {
            endAuto();
        }
    }

    /**
     * Checks the answer and puts its turns into the turn details of the book.
     *
     * @return false when the answer is not usable (shown)
     */
    private boolean accept(int runFrom, int runTo) {
        try {
            List<TurnItem> added = service.readAnswer(output.toString(), runFrom, runTo);
            int replaced = ConverterService.mergeTurns(data.getTurns(), added, runFrom, runTo);
            persist();
            refreshTurns();
            if (autoBlocks != null) {
                autoAdded += added.size();
                autoReplaced += replaced;
                return true;
            }
            baseTokens = null;
            tabs.setSelectedTab(turnsTab);
            Notification.success(T.localized(TURNS_FOR_THE_MESSAGES).formatted(added.size(), runFrom, runTo)
                    + (replaced > 0 ? T.localized(EARLIER_ONES_REPLACED).formatted(replaced) : ""));
            return true;
        } catch (Exception e) {
            STConverterLog.get().warn("The answer of the model is not usable: {}", e.getMessage());
            autoProblem = StringUtils.defaultIfBlank(e.getMessage(), e.toString());
            showStatus(autoProblem);
            return false;
        }
    }

    private void stop() {
        autoStopped = autoBlocks != null;
        CancellationToken token = running;
        if (token != null) {
            token.cancel();
        }
    }

    // --- the auto mode ---

    /** Asks before it sends the range to the model in batches. */
    private void startAuto() {
        if (generating || data.getChat().isEmpty()) {
            return;
        }
        List<int[]> blocks = ConverterService.splitBlocks(data.getChat(), from, to, data.getBatchSize(), data.isAlign());
        if (blocks.isEmpty()) {
            return;
        }
        ConfirmDialog.show(T.localized(CONVERT_THE_MESSAGES_IN_REQUESTS)
                .formatted(from, to, blocks.size()), () -> {
            autoBlocks = blocks;
            autoIndex = 0;
            autoOriginal = new int[]{from, to, anchor};
            autoStopped = false;
            autoAdded = 0;
            autoReplaced = 0;
            autoAttempt = 0;
            autoProblem = null;
            runNextBlock();
        });
    }

    /** Sends the block the auto mode is at, or ends the auto mode when they are all done or a block can't be sent. */
    private void runNextBlock() {
        if (autoIndex >= autoBlocks.size()) {
            endAuto();
            return;
        }
        int[] block = autoBlocks.get(autoIndex);
        // show where it is
        from = block[0];
        to = block[1];
        chatCursor = from;
        rangeChanged();
        if (chatGrid != null) {
            chatGrid.scrollToIndex(from - 1);
        }
        if (!startRun(block[0], block[1])) {
            endAuto();
        }
    }

    /**
     * Ends the auto mode. When all blocks are done the range is given back and the turn details are shown, otherwise the
     * range is what is left, so that it can go on from there.
     */
    private void endAuto() {
        int done = autoIndex;
        int total = autoBlocks.size();
        boolean complete = done >= total;
        if (complete) {
            from = autoOriginal[0];
            to = autoOriginal[1];
            anchor = autoOriginal[2];
        } else {
            from = autoBlocks.get(done)[0];
            to = autoOriginal[1];
            anchor = from;
        }
        int added = autoAdded;
        int replaced = autoReplaced;
        if (complete) {
            new DesktopNotifier(ui).finished(STConverterExtension.NOTIFY_CONVERT, false, manuscript.getName());
        } else if (!autoStopped) {
            new DesktopNotifier(ui).failed(STConverterExtension.NOTIFY_CONVERT, false, manuscript.getName());
        }
        autoBlocks = null;
        autoOriginal = null;
        baseTokens = null;
        rangeChanged();
        updateControls();
        if (complete) {
            tabs.setSelectedTab(turnsTab);
            Notification.success(T.localized(TURNS_MADE_IN_REQUESTS).formatted(added, total)
                    + (replaced > 0 ? T.localized(EARLIER_ONES_REPLACED).formatted(replaced) : ""));
        } else {
            String message = T.localized(STOPPED_AFTER_OF_REQUESTS_TURNS).formatted(done, total, added)
                    + (autoProblem != null ? ": " + autoProblem : "") + T.localized(THE_RANGE_IS_WHAT);
            // the notification goes away, the status line stays until the next request
            Notification.warning(message, Notification.DURATION_LONGER);
            showStatus(message);
        }
    }

    // --- the prompt ---

    private void openPromptDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(PROMPT_OF_THE_WAND));
        dialog.setWidth("min(800px, 95vw)");
        TextArea area = new TextArea();
        area.setWidthFull();
        area.setMinHeight("360px");
        area.setMaxHeight("70vh");
        area.setValue(service.promptTemplate(data));
        area.setHelperText(T.localized(VARIABLES_BACKGROUNDLORE_CHAT));
        dialog.add(area);
        Button defaults = new Button(T.localized(DEFAULT), e -> area.setValue(Defaults.PROMPT));
        Button cancel = new Button(T.localized(CANCEL), e -> dialog.close());
        Button save = new Button(T.localized(SAVE), e -> {
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
        // the model and the range only matter when there is a chat, the parts of a book need no wand
        convertDetails.setVisible(loaded && !data.isBookSource());
        if (loaded && !data.isBookSource()) {
            rangeChanged();
        }
        chatTab.setLabel(data.isBookSource() ? T.localized(PARTS) : T.localized(CHAT));
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
        alignBox.setEnabled(!generating);
        batchField.setEnabled(!generating);
        autoButton.setEnabled(!generating);
        wandButton.setEnabled(autoBlocks == null);
        autoStartButton.setText(generating ? T.localized(STOP) : T.localized(CONVERT_IN_BATCHES));
        autoStartButton.setIcon((generating ? Solid.STOP : Solid.FORWARD).create());
        autoStartButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        if (generating) {
            autoStartButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        }
        wandButton.setText(generating ? T.localized(STOP) : T.localized(CONVERT_RANGE));
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
