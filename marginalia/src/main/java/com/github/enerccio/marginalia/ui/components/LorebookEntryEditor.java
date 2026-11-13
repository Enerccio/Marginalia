package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.LorebookActivationPreviewDialog;
import com.github.enerccio.marginalia.ui.widgets.MarkdownEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TagMultiComboBox;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One entry of a lorebook in a tab of the lorebook editor ({@code LorebookEditorDialog}): a row with the name, whether it
 * is used and its order; the settings of the activation in a panel that can be folded; the content (a Markdown editor) and
 * the note side by side, the note only when it is asked for; and a footer with the token estimate of the content and what
 * is wrong with its template, and the entries whose content names this one. Ctrl+click on the name or a keyword of another
 * entry in the content opens it ({@link #setLinks}). Every change is saved at once, as in the rows of the lorebook view.
 */
@Configurable
@Extendable
public class LorebookEntryEditor extends VerticalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ResourceService resourceService;

    private LorebookEntry entry;
    private final Lorebook lorebook;
    private final LorebookTokens tokens;
    private final Runnable onChanged;

    private TextField nameField;
    private Checkbox enabledBox;
    private IntegerField orderField;
    private Checkbox showNoteBox;
    private Details settings;
    private MarkdownEditor contentEditor;
    private TextArea noteField;
    private Span tokensSpan;
    private Span problemSpan;
    private Span warningSpan;
    private HorizontalLayout headerRow;
    // the fields of the settings, updated in place when the entry changes elsewhere
    private ComboBox<FilteringMode> filteringModeCombo;
    private TextField filteringField;
    private ComboBox<InsertionMode> insertionModeCombo;
    private ComboBox<ActivationContext> activationContextCombo;
    private IntegerField activationPartsField;
    private TagMultiComboBox tagCombo;
    private TagMultiComboBox negativeTagCombo;
    private IntegerField fromMessageField;
    private IntegerField toMessageField;
    private Checkbox activationSummariesCheckbox;
    private Checkbox preventRecursionCheckbox;
    private Checkbox recursionOnlyCheckbox;
    private TextArea conditionField;
    private LorebookEntry conditionHolder;
    private Button previewButton;
    private HorizontalLayout conflictBar;
    private Runnable keepMine;
    private boolean readOnly;
    private Consumer<String> onLocked;
    private Runnable onGone;
    private boolean noteShown;
    // what the Markdown editor shows (text, text and preview, preview), kept over a rebuild
    private MarkdownEditor.Mode editorMode = MarkdownEditor.Mode.LIVE;
    private Consumer<MarkdownEditor.Mode> onModeChange;
    private boolean settingsOpened = true;
    // the entries a link can open, and what opens one (the entry, in a new tab)
    private Supplier<List<LorebookEntry>> linkTargets;
    private BiConsumer<LorebookEntry, Boolean> onOpenLink;
    private HorizontalLayout mentionsBar;

    /**
     * @param entry     the entry, saved with every change
     * @param tokens    the counts of the editor, the tab updates the count of its entry
     * @param onChanged told after a change that the list or the totals show (the name, enabled, the order, the content)
     */
    public LorebookEntryEditor(LorebookEntry entry, LorebookTokens tokens, Runnable onChanged) {
        this(entry, null, tokens, onChanged);
    }

    /**
     * @param lorebook the lorebook of the entry as the editor has it (the entry's own reference may be a lazy one), may be null
     */
    public LorebookEntryEditor(LorebookEntry entry, Lorebook lorebook, LorebookTokens tokens, Runnable onChanged) {
        this.entry = entry;
        this.lorebook = lorebook;
        this.tokens = tokens;
        this.onChanged = onChanged;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = LorebookEntryEditor.class))
    public LorebookEntryEditor create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getThemeList().add("spacing-xs");
        build();
        return this;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = LorebookEntry.class))
    public LorebookEntry getEntry() {
        return entry;
    }

    /**
     * The lorebook of the entry: the one given to the editor, else the entry's reference.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Lorebook.class))
    public Lorebook getLorebook() {
        return lorebook != null ? lorebook : entry.getLorebook();
    }

    /**
     * Shows the entry as it is now in the database (another place changed it): its state goes into the object of the tab (the
     * list, the tags and extensions hold it) and the fields show it, in place, nothing is built again (the note, the settings
     * panel and the mode of the editor stay as the user left them).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "fresh", type = LorebookEntry.class)})
    public void reload(LorebookEntry fresh) {
        if (fresh == null) {
            return;
        }
        if (fresh != entry) {
            copyFields(fresh, entry);
        }
        if (nameField == null) {
            return;
        }
        showFields();
    }

    /**
     * Puts the values of the entry into the fields that are there (a value that is the same changes nothing, so the field the
     * user is in is not disturbed by its own save).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void showFields() {
        nameField.setValue(StringUtils.defaultString(entry.getName()));
        nameField.setInvalid(false);
        orderField.setValue(entry.getOrder());
        enabledBox.setValue(entry.isEnabled());
        contentEditor.setValue(StringUtils.defaultString(entry.getPayload()));
        noteField.setValue(StringUtils.defaultString(entry.getComment()));
        filteringModeCombo.setValue(entry.getFilteringMode());
        filteringField.setValue(StringUtils.defaultString(entry.getFiltering()));
        insertionModeCombo.setValue(entry.getInsertionMode());
        activationContextCombo.setValue(entry.getActivationContext());
        activationPartsField.setValue(entry.getActivationParts());
        activationPartsField.setVisible(entry.getActivationContext() == ActivationContext.INCLUDE_PARTS);
        tagCombo.setForEntity(entry);
        negativeTagCombo.setForEntity(entry, true);
        fromMessageField.setValue(entry.getFromMessage());
        toMessageField.setValue(entry.getToMessage());
        activationSummariesCheckbox.setValue(entry.isActivationSummaries());
        preventRecursionCheckbox.setValue(entry.isPreventRecursion());
        recursionOnlyCheckbox.setValue(entry.isRecursionOnly());
        conditionHolder.setActivationCondition(entry.getActivationCondition());
        conditionField.setValue(StringUtils.defaultString(entry.getActivationCondition()));
        hideConflict();
        validate();
        countTokens(false);
        showMentions();
    }

    /**
     * Shows the entry as the database has it now and tells the editor (an extension changed it: a generator, a revision).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "fresh", type = LorebookEntry.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void reloadFromDatabase() {
        try {
            LorebookEntry fresh = lorebookEntryService.find(entry);
            if (fresh == null || fresh.isDeleted()) {
                if (onGone != null) {
                    onGone.run();
                }
                return;
            }
            reload(fresh);
            countTokens(true);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        onChanged.run();
    }

    /**
     * The entry was written elsewhere (another window, an agent): the tab shows it as the database has it now, unless the user's
     * text waits for an answer (the footer asks already) or nothing the tab shows changed (the data of extensions is taken).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "fresh", type = LorebookEntry.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void changedElsewhere() {
        if (keepMine != null) {
            return;
        }
        LorebookEntry fresh;
        try {
            fresh = lorebookEntryService.find(entry);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        if (fresh == null || fresh.isDeleted()) {
            if (onGone != null) {
                onGone.run();
            }
            return;
        }
        boolean same = sameFields(fresh, entry);
        copyFields(fresh, entry);
        if (same) {
            return;
        }
        reload(entry);
        countTokens(true);
        onChanged.run();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "header", type = HorizontalLayout.class),
                    @APILocals(name = "body", type = HorizontalLayout.class),
                    @APILocals(name = "conflictText", type = Span.class),
                    @APILocals(name = "keep", type = Button.class),
                    @APILocals(name = "loadSaved", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class)})
    private void build() {
        nameField = new TextField(loc.getValue(L.LABEL_ENTRY_NAME));
        nameField.setValue(StringUtils.defaultString(entry.getName()));
        nameField.setValueChangeMode(ValueChangeMode.LAZY);
        nameField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                if (StringUtils.isBlank(e.getValue())) {
                    nameField.setInvalid(true);
                    nameField.setErrorMessage(loc.getValue(L.ERROR_NAME_CANT_BE_EMPTY));
                    return;
                }
                nameField.setInvalid(false);
                saveField(LorebookEntry::getName, LorebookEntry::setName, e.getValue(), false, null);
            }
        });

        enabledBox = new Checkbox(loc.getValue(L.LABEL_ENABLED), entry.isEnabled());
        enabledBox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::isEnabled, LorebookEntry::setEnabled, Boolean.TRUE.equals(e.getValue()), false, null);
            }
        });

        orderField = new IntegerField(loc.getValue(L.LABEL_ORDER));
        orderField.setWidth("120px");
        orderField.setStepButtonsVisible(true);
        orderField.setValue(entry.getOrder());
        orderField.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                saveField(LorebookEntry::getOrder, LorebookEntry::setOrder, e.getValue(), false, null);
            }
        });

        showNoteBox = new Checkbox(loc.getValue(L.LABEL_SHOW_NOTE), noteShown);
        showNoteBox.addValueChangeListener(e -> setNoteShown(Boolean.TRUE.equals(e.getValue())));

        previewButton = new Button(Solid.EYE.create(), event -> openActivationPreview());
        previewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previewButton.setTooltipText(loc.getValue(L.LABEL_PREVIEW_ACTIVATION));
        previewButton.setAriaLabel(loc.getValue(L.LABEL_PREVIEW_ACTIVATION));

        HorizontalLayout header = new HorizontalLayout(nameField, orderField, enabledBox, showNoteBox, previewButton);
        headerRow = header;
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.BASELINE);
        header.setFlexGrow(1, nameField);

        settings = new Details(loc.getValue(L.LABEL_SETTINGS), createSettings());
        settings.setWidthFull();
        settings.setOpened(settingsOpened);

        contentEditor = new MarkdownEditor();
        contentEditor.setSizeFull();
        contentEditor.setValue(StringUtils.defaultString(entry.getPayload()));
        contentEditor.setMode(editorMode);
        contentEditor.localize(loc);
        contentEditor.addModeChangeListener(mode -> {
            editorMode = mode;
            if (onModeChange != null) {
                onModeChange.accept(mode);
            }
        });
        // invalid templates are still saved (the user may be in the middle of editing), generation uses them as plain text
        contentEditor.addValueChangeListener(value -> saveField(LorebookEntry::getPayload, LorebookEntry::setPayload, value, true,
                () -> {
                    validate();
                    countTokens(true);
                }));

        contentEditor.addLinkClickListener(click -> openLinkAt(click.text(), click.position(), click.newTab()));
        // images: stored as resources of the entry, shown in the preview from the user's images
        contentEditor.setImageUploader(this::storeImage);
        contentEditor.setOnUploadFailed(name -> Notification.error(loc.getValue(L.ERROR_INVALID_IMAGE)));
        contentEditor.setImageSource(this::imageContent);

        noteField = new TextArea(loc.getValue(L.LABEL_NOTE));
        noteField.setSizeFull();
        noteField.setValue(StringUtils.defaultString(entry.getComment()));
        noteField.setValueChangeMode(ValueChangeMode.LAZY);
        noteField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::getComment, LorebookEntry::setComment, e.getValue(), true, null);
            }
        });
        noteField.setVisible(noteShown);

        HorizontalLayout body = new HorizontalLayout(contentEditor, noteField);
        body.setSizeFull();
        body.addClassName(CssStyles.MIN_HEIGHT_0);
        body.setFlexGrow(2, contentEditor);
        body.setFlexGrow(1, noteField);

        tokensSpan = new Span();
        tokensSpan.addClassName(CssStyles.HINT_TEXT);
        problemSpan = new Span();
        problemSpan.addClassName(CssStyles.TEXT_ERROR);
        warningSpan = new Span();
        warningSpan.addClassName(CssStyles.HINT_TEXT);
        Span conflictText = new Span(loc.getValue(L.MSG_ENTRY_CHANGED_ELSEWHERE));
        conflictText.addClassName(CssStyles.TEXT_ERROR);
        Button keep = new Button(loc.getValue(L.LABEL_KEEP_MY_TEXT), event -> {
            Runnable mine = keepMine;
            if (mine != null) {
                // the save that follows is not one in the middle of a conflict
                hideConflict();
                mine.run();
            }
        });
        keep.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        Button loadSaved = new Button(loc.getValue(L.LABEL_LOAD_SAVED_TEXT), event -> reloadFromDatabase());
        loadSaved.addThemeVariants(ButtonVariant.LUMO_SMALL);
        conflictBar = new HorizontalLayout(conflictText, keep, loadSaved);
        conflictBar.setAlignItems(FlexComponent.Alignment.BASELINE);
        conflictBar.setVisible(false);
        keepMine = null;

        mentionsBar = new HorizontalLayout();
        mentionsBar.setSpacing(false);
        mentionsBar.getThemeList().add("spacing-xs");
        mentionsBar.setAlignItems(FlexComponent.Alignment.BASELINE);
        mentionsBar.addClassName(CssStyles.FLEX_WRAP);

        HorizontalLayout footer = new HorizontalLayout(tokensSpan, problemSpan, warningSpan, conflictBar, mentionsBar);
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.BASELINE);
        footer.addClassName(CssStyles.FLEX_WRAP);

        add(header, settings, body, footer);
        setFlexGrow(1, body);

        validate();
        countTokens(false);
        showMentions();
        applyReadOnly();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = FlexLayout.class),
            locals = {@APILocals(name = "form", type = FlexLayout.class),
                    @APILocals(name = "filteringModeCombo", type = ComboBox.class),
                    @APILocals(name = "filteringField", type = TextField.class),
                    @APILocals(name = "insertionModeCombo", type = ComboBox.class),
                    @APILocals(name = "tagCombo", type = TagMultiComboBox.class),
                    @APILocals(name = "negativeTagCombo", type = TagMultiComboBox.class),
                    @APILocals(name = "activationPartsField", type = IntegerField.class),
                    @APILocals(name = "activationContextCombo", type = ComboBox.class),
                    @APILocals(name = "fromMessageField", type = IntegerField.class),
                    @APILocals(name = "toMessageField", type = IntegerField.class),
                    @APILocals(name = "activationSummariesCheckbox", type = Checkbox.class),
                    @APILocals(name = "preventRecursionCheckbox", type = Checkbox.class),
                    @APILocals(name = "recursionOnlyCheckbox", type = Checkbox.class),
                    @APILocals(name = "condition", type = LorebookEntry.class),
                    @APILocals(name = "conditionField", type = TextArea.class),
                    @APILocals(name = "rowBreak", type = Div.class)})
    private FlexLayout createSettings() {
        // dense: the fields have the width their values need and wrap, the texts grow into the rest of the line
        FlexLayout form = new FlexLayout();
        form.setWidthFull();
        form.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        form.setAlignItems(FlexComponent.Alignment.END);
        form.addClassName(CssStyles.GAP_S);

        ComboBox<FilteringMode> filteringModeCombo = new ComboBox<>(loc.getValue(L.LABEL_FILTERING_MODE));
        filteringModeCombo.setItems(FilteringMode.values());
        filteringModeCombo.setItemLabelGenerator(mode -> loc.getValue(loc.getFilteringMode(mode)));
        filteringModeCombo.setAllowCustomValue(false);
        filteringModeCombo.setValue(entry.getFilteringMode());
        filteringModeCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                saveField(LorebookEntry::getFilteringMode, LorebookEntry::setFilteringMode, e.getValue(), false, null);
            }
        });

        TextField filteringField = new TextField(loc.getValue(L.LABEL_FILTERING));
        filteringField.setValue(StringUtils.defaultString(entry.getFiltering()));
        filteringField.setValueChangeMode(ValueChangeMode.LAZY);
        filteringField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::getFiltering, LorebookEntry::setFiltering, e.getValue(), false, null);
            }
        });

        ComboBox<InsertionMode> insertionModeCombo = new ComboBox<>(loc.getValue(L.LABEL_INSERTION_MODE));
        insertionModeCombo.setItems(InsertionMode.values());
        insertionModeCombo.setItemLabelGenerator(mode -> loc.getValue(loc.getInsertionMode(mode)));
        insertionModeCombo.setAllowCustomValue(false);
        insertionModeCombo.setValue(entry.getInsertionMode());
        insertionModeCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                saveField(LorebookEntry::getInsertionMode, LorebookEntry::setInsertionMode, e.getValue(), false, null);
            }
        });

        TagMultiComboBox tagCombo = new TagMultiComboBox(loc.getValue(L.LABEL_TAGS));
        tagCombo.setForEntity(entry);
        tagCombo.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                onChanged.run();
            }
        });

        TagMultiComboBox negativeTagCombo = new TagMultiComboBox(loc.getValue(L.LABEL_NEGATIVE_TAGS), true);
        negativeTagCombo.setForEntity(entry, true);

        IntegerField activationPartsField = new IntegerField(loc.getValue(L.LABEL_PARTS));
        activationPartsField.setMin(1);
        activationPartsField.setStepButtonsVisible(true);
        activationPartsField.setValue(entry.getActivationParts());
        activationPartsField.setVisible(entry.getActivationContext() == ActivationContext.INCLUDE_PARTS);
        UIUtils.addTooltip(activationPartsField, loc.getValue(L.HELP_ACTIVATION_PARTS));
        activationPartsField.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                saveField(LorebookEntry::getActivationParts, LorebookEntry::setActivationParts, e.getValue(), false, null);
            }
        });

        ComboBox<ActivationContext> activationContextCombo = new ComboBox<>(loc.getValue(L.LABEL_ACTIVATION_CONTEXT));
        activationContextCombo.setItems(ActivationContext.values());
        activationContextCombo.setItemLabelGenerator(context -> loc.getValue(loc.getActivationContext(context)));
        activationContextCombo.setAllowCustomValue(false);
        activationContextCombo.setValue(entry.getActivationContext());
        UIUtils.addTooltip(activationContextCombo, loc.getValue(L.HELP_ACTIVATION_CONTEXT));
        activationContextCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                saveField(LorebookEntry::getActivationContext, LorebookEntry::setActivationContext, e.getValue(), false, null);
                activationPartsField.setVisible(e.getValue() == ActivationContext.INCLUDE_PARTS);
            }
        });

        IntegerField fromMessageField = new IntegerField(loc.getValue(L.LABEL_STATISTICS_FROM));
        fromMessageField.setMin(1);
        fromMessageField.setStepButtonsVisible(true);
        fromMessageField.setValue(entry.getFromMessage());
        UIUtils.addTooltip(fromMessageField, loc.getValue(L.HELP_ACTIVATION_RANGE));
        fromMessageField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::getFromMessage, LorebookEntry::setFromMessage, e.getValue(), false, null);
            }
        });

        IntegerField toMessageField = new IntegerField(loc.getValue(L.LABEL_STATISTICS_TO));
        toMessageField.setMin(1);
        toMessageField.setStepButtonsVisible(true);
        toMessageField.setValue(entry.getToMessage());
        UIUtils.addTooltip(toMessageField, loc.getValue(L.HELP_ACTIVATION_RANGE));
        toMessageField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::getToMessage, LorebookEntry::setToMessage, e.getValue(), false, null);
            }
        });

        Checkbox activationSummariesCheckbox = new Checkbox(loc.getValue(L.LABEL_ACTIVATION_SUMMARIES), entry.isActivationSummaries());
        UIUtils.addTooltip(activationSummariesCheckbox, loc.getValue(L.HELP_ACTIVATION_SUMMARIES));
        activationSummariesCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::isActivationSummaries, LorebookEntry::setActivationSummaries, Boolean.TRUE.equals(e.getValue()), false, null);
            }
        });

        Checkbox preventRecursionCheckbox = new Checkbox(loc.getValue(L.LABEL_DONT_SCAN_ITS_CONTENT), entry.isPreventRecursion());
        UIUtils.addTooltip(preventRecursionCheckbox, loc.getValue(L.HELP_PREVENT_RECURSION));
        preventRecursionCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::isPreventRecursion, LorebookEntry::setPreventRecursion, Boolean.TRUE.equals(e.getValue()), false, null);
            }
        });

        Checkbox recursionOnlyCheckbox = new Checkbox(loc.getValue(L.LABEL_RECURSION_ONLY), entry.isRecursionOnly());
        UIUtils.addTooltip(recursionOnlyCheckbox, loc.getValue(L.HELP_RECURSION_ONLY));
        recursionOnlyCheckbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                saveField(LorebookEntry::isRecursionOnly, LorebookEntry::setRecursionOnly, Boolean.TRUE.equals(e.getValue()), false, null);
            }
        });

        // the field writes the condition into the object it gets: a copy, the save puts it into the entry as stored
        LorebookEntry condition = new LorebookEntry();
        conditionHolder = condition;
        condition.setActivationCondition(entry.getActivationCondition());
        TextArea conditionField = LoreConditionField.create(loc, templateService, condition, () -> saveField(
                LorebookEntry::getActivationCondition, LorebookEntry::setActivationCondition, condition.getActivationCondition(),
                false, null));

        filteringModeCombo.setWidth("10em");
        insertionModeCombo.setWidth("11.5em");
        activationContextCombo.setWidth("12em");
        activationPartsField.setWidth("7em");
        fromMessageField.setWidth("7em");
        toMessageField.setWidth("7em");
        // the messages of the range are counted from 1, an empty end is the last one (the label is short, the tooltip says it)
        toMessageField.setPlaceholder("∞");
        // the first row is how the entry is found and where it goes, the filter takes the rest of it; the range, the tags,
        // the check boxes and the condition follow
        Div rowBreak = new Div();
        rowBreak.addClassName(CssStyles.FLEX_BREAK);
        form.add(filteringModeCombo, filteringField, insertionModeCombo, activationContextCombo, activationPartsField, rowBreak,
                fromMessageField, toMessageField, tagCombo, negativeTagCombo, activationSummariesCheckbox, preventRecursionCheckbox,
                recursionOnlyCheckbox, conditionField);
        // a check box keeps its label on one line
        activationSummariesCheckbox.addClassName(CssStyles.FLEX_SHRINK_0);
        preventRecursionCheckbox.addClassName(CssStyles.FLEX_SHRINK_0);
        recursionOnlyCheckbox.addClassName(CssStyles.FLEX_SHRINK_0);
        form.setFlexBasis("10em", filteringField);
        form.setFlexBasis("11em", tagCombo, negativeTagCombo);
        form.setFlexBasis("24em", conditionField);
        form.setFlexGrow(1, filteringField, tagCombo, negativeTagCombo, conditionField);
        this.filteringModeCombo = filteringModeCombo;
        this.filteringField = filteringField;
        this.insertionModeCombo = insertionModeCombo;
        this.activationContextCombo = activationContextCombo;
        this.activationPartsField = activationPartsField;
        this.tagCombo = tagCombo;
        this.negativeTagCombo = negativeTagCombo;
        this.fromMessageField = fromMessageField;
        this.toMessageField = toMessageField;
        this.activationSummariesCheckbox = activationSummariesCheckbox;
        this.preventRecursionCheckbox = preventRecursionCheckbox;
        this.recursionOnlyCheckbox = recursionOnlyCheckbox;
        this.conditionField = conditionField;
        return form;
    }

    /**
     * Which entries a turn would use, from the lorebook of the entry, with this entry's row selected and its verdict spelled out.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookActivationPreviewDialog.class)})
    private void openActivationPreview() {
        LorebookActivationPreviewDialog dialog = new LorebookActivationPreviewDialog(getLorebook()).focus(entry);
        dialog.create();
        dialog.open();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "shown", type = boolean.class)})
    private void setNoteShown(boolean shown) {
        noteShown = shown;
        noteField.setVisible(shown);
    }

    /**
     * Told when a save is refused because an agent works on the lorebook (with the name of the agent).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onLocked", type = Consumer.class)})
    public void setOnLocked(Consumer<String> onLocked) {
        this.onLocked = onLocked;
    }

    /**
     * Told when a save finds the entry deleted (somewhere else).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onGone", type = Runnable.class)})
    public void setOnGone(Runnable onGone) {
        this.onGone = onGone;
    }

    /**
     * Read only while an agent works on the lorebook: nothing can be changed, the note can still be shown.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "readOnly", type = boolean.class)})
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        applyReadOnly();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = boolean.class))
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * What the Markdown editor of the content shows: the text, the text with the preview, or the preview.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "mode", type = MarkdownEditor.Mode.class)})
    public void setEditorMode(MarkdownEditor.Mode mode) {
        editorMode = mode == null ? MarkdownEditor.Mode.LIVE : mode;
        if (contentEditor != null) {
            contentEditor.setMode(editorMode);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = MarkdownEditor.Mode.class))
    public MarkdownEditor.Mode getEditorMode() {
        return editorMode;
    }

    /**
     * Told when the user picks another mode of the Markdown editor (the dialog keeps it for the tab).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onModeChange", type = Consumer.class)})
    public void setOnModeChange(Consumer<MarkdownEditor.Mode> onModeChange) {
        this.onModeChange = onModeChange;
    }

    /**
     * Stores an image the user put into the content: a resource linked to the entry, the Markdown that shows it.
     *
     * @return the Markdown put in at the cursor
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "fileName", type = String.class),
                    @APIParameter(name = "content", type = byte[].class)},
            locals = {@APILocals(name = "image", type = Resource.class)})
    public String storeImage(String fileName, byte[] content) throws Exception {
        if (readOnly) {
            throw new IllegalStateException("The entry is read only");
        }
        Resource image = resourceService.uploadImage(fileName, content, LorebookEntry.class, entry.getId());
        return LoreImages.markup(FilenameUtils.getBaseName(fileName), image.getUuid());
    }

    /**
     * The content of an image of the user for the preview, null when the user has no such image.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = AbstractStreamResource.class),
            parameters = {@APIParameter(name = "uuid", type = String.class)},
            locals = {@APILocals(name = "image", type = Resource.class),
                    @APILocals(name = "stream", type = StreamResource.class),
                    @APILocals(name = "e", type = Exception.class)})
    private AbstractStreamResource imageContent(String uuid) {
        try {
            Resource image = resourceService.findImage(uuid);
            if (image == null) {
                return null;
            }
            StreamResource stream = new StreamResource(StringUtils.defaultIfBlank(image.getOriginalName(), uuid), () -> {
                try {
                    return new ByteArrayInputStream(resourceService.getResourceData(image));
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            stream.setContentType(image.getMimeType());
            return stream;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }

    /**
     * The links between entries: Ctrl+click on the name or a keyword of an entry in the content opens it, and the footer lists the
     * entries whose content names this one ({@link LoreMentions}).
     *
     * @param targets the entries a link can open (the list of the editor), asked at every click
     * @param open    opens an entry, in a new tab when the flag is set
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "targets", type = Supplier.class),
                    @APIParameter(name = "open", type = BiConsumer.class)})
    public void setLinks(Supplier<List<LorebookEntry>> targets, BiConsumer<LorebookEntry, Boolean> open) {
        this.linkTargets = targets;
        this.onOpenLink = open;
        showMentions();
    }

    /**
     * Opens the entry the text names at the position (a Ctrl+click in the content or its preview); nothing when it names none.
     *
     * @return whether an entry was opened
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "text", type = String.class),
                    @APIParameter(name = "position", type = int.class),
                    @APIParameter(name = "newTab", type = boolean.class)},
            locals = {@APILocals(name = "target", type = LorebookEntry.class)})
    public boolean openLinkAt(String text, int position, boolean newTab) {
        if (linkTargets == null || onOpenLink == null) {
            return false;
        }
        LorebookEntry target = LoreMentions.entryAt(text, position, linkTargets.get(), entry);
        if (target == null) {
            return false;
        }
        onOpenLink.accept(target, newTab);
        return true;
    }

    /**
     * Lists in the footer the entries whose content names this one (again after a change of the entries of the editor).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "mentioning", type = List.class),
                    @APILocals(name = "label", type = Span.class),
                    @APILocals(name = "other", type = LorebookEntry.class),
                    @APILocals(name = "link", type = Button.class),
                    @APILocals(name = "none", type = Span.class)})
    public void showMentions() {
        if (mentionsBar == null) {
            return;
        }
        mentionsBar.removeAll();
        if (linkTargets == null || onOpenLink == null) {
            mentionsBar.setVisible(false);
            return;
        }
        List<LorebookEntry> mentioning = LoreMentions.mentionedIn(entry, linkTargets.get());
        Span label = new Span(loc.getValue(L.LABEL_MENTIONED_IN));
        label.addClassName(CssStyles.HINT_TEXT);
        UIUtils.addTooltip(label, loc.getValue(L.HELP_ENTRY_LINKS));
        mentionsBar.add(label);
        for (LorebookEntry other : mentioning) {
            Button link = new Button(StringUtils.defaultIfBlank(other.getName(), "?"),
                    event -> onOpenLink.accept(other, event.isShiftKey() || event.isCtrlKey() || event.isMetaKey()));
            link.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
            mentionsBar.add(link);
        }
        if (mentioning.isEmpty()) {
            Span none = new Span("—");
            none.addClassName(CssStyles.HINT_TEXT);
            mentionsBar.add(none);
        }
        mentionsBar.setVisible(true);
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void applyReadOnly() {
        if (headerRow == null) {
            return;
        }
        // also what an extension put into the header (the wand)
        // the note and the preview change nothing
        headerRow.getChildren().filter(component -> component != showNoteBox && component != previewButton)
                .forEach(component -> component.getElement().setEnabled(!readOnly));
        settings.setEnabled(!readOnly);
        contentEditor.setReadOnly(readOnly);
        noteField.setReadOnly(readOnly);
        if (readOnly) {
            hideConflict();
        }
    }

    /**
     * Saves one field: into the entry as the database has it now, not the copy of the tab, so what was changed elsewhere
     * in the other fields stays (and the tab shows it). A {@code guarded} field (the content, the note) is not saved over
     * a text that was changed elsewhere since the tab had it: the footer asks to keep the text typed here or to load the
     * stored one.
     *
     * @param after run after a save, before the dialog is told
     * @return whether it was saved
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "getter", type = Function.class),
                    @APIParameter(name = "setter", type = BiConsumer.class),
                    @APIParameter(name = "value", type = Object.class),
                    @APIParameter(name = "guarded", type = boolean.class),
                    @APIParameter(name = "after", type = Runnable.class)},
            locals = {@APILocals(name = "fresh", type = LorebookEntry.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "tabPayload", type = String.class),
                    @APILocals(name = "tabComment", type = String.class),
                    @APILocals(name = "saved", type = LorebookEntry.class),
                    @APILocals(name = "locked", type = ManuscriptLockedException.class)})
    private <T> boolean saveField(Function<LorebookEntry, T> getter, BiConsumer<LorebookEntry, T> setter, T value,
                                  boolean guarded, Runnable after) {
        if (readOnly) {
            return false;
        }
        LorebookEntry fresh;
        try {
            fresh = lorebookEntryService.find(entry);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return false;
        }
        if (fresh == null || fresh.isDeleted()) {
            if (onGone != null) {
                onGone.run();
            }
            return false;
        }
        if (guarded && !same(getter.apply(fresh), getter.apply(entry)) && !same(getter.apply(fresh), value)) {
            keepMine = () -> saveField(getter, setter, value, false, after);
            conflictBar.setVisible(true);
            return false;
        }
        // a text that waits for the user's answer stays as the tab had it: the tab is not built again under the typed text
        boolean pending = keepMine != null;
        String tabPayload = entry.getPayload();
        String tabComment = entry.getComment();
        boolean elsewhere = !sameFields(fresh, entry);
        setter.accept(fresh, value);
        try {
            LorebookEntry saved = lorebookEntryService.save(fresh);
            // the list, the tags and extensions (a revision bar) hold this object: it takes the stored state
            copyFields(saved, entry);
            if (pending) {
                entry.setPayload(tabPayload);
                entry.setComment(tabComment);
            }
        } catch (Exception e) {
            ManuscriptLockedException locked = lockOf(e);
            if (locked != null && onLocked != null) {
                onLocked.accept(locked.getAgentName());
                return false;
            }
            UIUtils.internalServerError(loc, e);
            return false;
        }
        if (!pending) {
            hideConflict();
            if (elsewhere) {
                reload(entry);
            }
        }
        if (after != null) {
            after.run();
        }
        onChanged.run();
        return true;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void hideConflict() {
        keepMine = null;
        if (conflictBar != null) {
            conflictBar.setVisible(false);
        }
    }

    private static ManuscriptLockedException lockOf(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof ManuscriptLockedException locked) {
                return locked;
            }
        }
        return null;
    }

    private static boolean same(Object a, Object b) {
        if (a instanceof String || b instanceof String) {
            return StringUtils.defaultString((String) a).equals(StringUtils.defaultString((String) b));
        }
        return Objects.equals(a, b);
    }

    /**
     * Whether the fields the tab edits are the same in both.
     */
    private static boolean sameFields(LorebookEntry a, LorebookEntry b) {
        return same(a.getName(), b.getName()) && a.isEnabled() == b.isEnabled() && a.getOrder() == b.getOrder()
                && same(a.getPayload(), b.getPayload()) && same(a.getComment(), b.getComment())
                && same(a.getFilteringMode(), b.getFilteringMode()) && same(a.getFiltering(), b.getFiltering())
                && same(a.getInsertionMode(), b.getInsertionMode()) && same(a.getActivationContext(), b.getActivationContext())
                && a.getActivationParts() == b.getActivationParts() && same(a.getFromMessage(), b.getFromMessage())
                && same(a.getToMessage(), b.getToMessage()) && a.isActivationSummaries() == b.isActivationSummaries()
                && a.isPreventRecursion() == b.isPreventRecursion() && a.isRecursionOnly() == b.isRecursionOnly()
                && same(a.getActivationCondition(), b.getActivationCondition());
    }

    private static void copyFields(LorebookEntry from, LorebookEntry to) {
        to.setName(from.getName());
        to.setEnabled(from.isEnabled());
        to.setOrder(from.getOrder());
        to.setPayload(from.getPayload());
        to.setComment(from.getComment());
        to.setFilteringMode(from.getFilteringMode());
        to.setFiltering(from.getFiltering());
        to.setInsertionMode(from.getInsertionMode());
        to.setActivationContext(from.getActivationContext());
        to.setActivationParts(from.getActivationParts());
        to.setFromMessage(from.getFromMessage());
        to.setToMessage(from.getToMessage());
        to.setActivationSummaries(from.isActivationSummaries());
        to.setPreventRecursion(from.isPreventRecursion());
        to.setRecursionOnly(from.isRecursionOnly());
        to.setActivationCondition(from.getActivationCondition());
        // the data of extensions (the options of the Lore Generator), a save of this object elsewhere keeps them
        to.setAttributes(from.getAttributes());
        to.setModification(from.getModification());
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "changed", type = boolean.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void countTokens(boolean changed) {
        try {
            tokensSpan.setText(tokens.format(changed ? tokens.recount(entry) : tokens.of(entry)));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * What is wrong with the template of the content (it is saved anyway), and the names it uses that are not known.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "payload", type = String.class),
                    @APILocals(name = "result", type = TemplateService.ValidationResult.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void validate() {
        String payload = entry.getPayload();
        problemSpan.setText("");
        warningSpan.setText("");
        if (StringUtils.isBlank(payload)) {
            return;
        }
        try {
            TemplateService.ValidationResult result = templateService.isValidTemplate(payload, "lorebookEntry", LorebookTemplateData.class);
            if (!result.isValid()) {
                problemSpan.setText(loc.getValue(L.MSG_INVALID_TEMPLATE) + result.errorMessage());
            }
            if (result.hasWarnings()) {
                warningSpan.setText(loc.getValue(L.MSG_TEMPLATE_UNKNOWN_NAMES) + String.join(", ", result.unknownNames()));
            }
        } catch (Exception e) {
            problemSpan.setText(loc.getValue(L.MSG_INVALID_TEMPLATE) + e.getMessage());
        }
    }

    // for the tests

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = TextField.class))
    TextField getNameField() {
        return nameField;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getEnabledBox() {
        return enabledBox;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getOrderField() {
        return orderField;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = MarkdownEditor.class))
    MarkdownEditor getContentEditor() {
        return contentEditor;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = TextArea.class))
    TextArea getNoteField() {
        return noteField;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getShowNoteBox() {
        return showNoteBox;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Span.class))
    Span getTokensSpan() {
        return tokensSpan;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Span.class))
    Span getProblemSpan() {
        return problemSpan;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = HorizontalLayout.class))
    HorizontalLayout getConflictBar() {
        return conflictBar;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = HorizontalLayout.class))
    HorizontalLayout getMentionsBar() {
        return mentionsBar;
    }
}
