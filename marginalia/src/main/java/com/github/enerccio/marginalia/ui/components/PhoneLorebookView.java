package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.Restorable;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.dialogs.search.LorebookSearchReplaceDialog;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reduced {@link LorebookView} for phones. Lorebooks can only be selected (no creating, deleting, importing or
 * exporting) and entries cannot be added. The only things that can be changed are the enabled flag of the lorebook
 * and of its entries and the content of an entry; every other setting is shown as plain text.
 * <p>
 * It is also the view of a lorebook that is locked by an agent (an agent works on a book that uses it): it then says so
 * and changes nothing, and the desktop {@link LorebookView} shows this view for such a lorebook.
 */
@Configurable
@Extendable
public class PhoneLorebookView extends VerticalLayout implements Restorable, LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private AgentRunService agentRunService;

    private Lorebook currentLorebook;
    private boolean pinnedLorebook = false;

    private ComboBox<Lorebook> lorebookCombo;
    private Button refreshButton;
    private Button searchReplaceButton;
    private Checkbox lorebookEnabledCheckbox;
    private Span lorebookInfoSpan;
    private Span lorebookTokensSpan;
    private Grid<LorebookEntry> grid;

    private List<LorebookEntry> currentEntries = new ArrayList<>();
    private final Map<Long, Long> entryTokens = new HashMap<>();

    // uuids of the entries whose details are opened, kept over reloading the entries
    private final Set<String> openEntries = new LinkedHashSet<>();
    private Runnable onStateChange;
    // name of the agent that locks the lorebook (null when it is free), the view then changes nothing
    private String lockedBy;
    private Span lockedSpan;

    public PhoneLorebookView() {
        this(null);
    }

    public PhoneLorebookView(Lorebook lorebook) {
        this.currentLorebook = lorebook;
    }

    /**
     * @param pinnedLorebook the lorebook is chosen by the owner of the view ({@link #setLorebook}), so the lorebook
     *                       selection is hidden, like in {@link LorebookView}
     */
    public PhoneLorebookView(boolean pinnedLorebook) {
        this(null);
        this.pinnedLorebook = pinnedLorebook;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "controlsLayout", type = HorizontalLayout.class),
                    @APILocals(name = "enabledLayout", type = HorizontalLayout.class)})
    public Component create() throws Exception {
        setSizeFull();
        setPadding(false);
        setSpacing(true);

        lorebookCombo = new ComboBox<>(loc.getValue(L.LABEL_LOREBOOK));
        lorebookCombo.setItemLabelGenerator(Lorebook::getName);
        lorebookCombo.setWidthFull();
        lorebookCombo.setVisible(!pinnedLorebook);
        lorebookCombo.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                currentLorebook = event.getValue();
                openEntries.clear();
                updateSelectedLorebook();
                fireStateChanged();
            }
        });

        refreshButton = new Button(Solid.REFRESH.create(), _ -> {
            try {
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        HorizontalLayout controlsLayout = new HorizontalLayout(lorebookCombo, refreshButton);
        controlsLayout.setVisible(!pinnedLorebook);
        controlsLayout.setWidthFull();
        controlsLayout.setAlignItems(FlexComponent.Alignment.END);
        controlsLayout.setFlexGrow(1, lorebookCombo);

        lorebookEnabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLED));
        lorebookEnabledCheckbox.addValueChangeListener(e -> {
            if (currentLorebook != null && e.isFromClient()) {
                try {
                    currentLorebook.setEnabled(Boolean.TRUE.equals(e.getValue()));
                    lorebookService.save(currentLorebook);
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        lorebookTokensSpan = createTokensSpan();

        searchReplaceButton = new Button(Solid.EXCHANGE_ALT.create(), _ -> openSearchReplaceDialog());
        searchReplaceButton.setTooltipText(loc.getValue(L.LABEL_SEARCH_AND_REPLACE));

        HorizontalLayout enabledLayout = new HorizontalLayout(lorebookEnabledCheckbox, lorebookTokensSpan, searchReplaceButton);
        enabledLayout.setWidthFull();
        enabledLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        lorebookInfoSpan = new Span();
        lorebookInfoSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);

        setupGrid();

        lockedSpan = new Span();
        lockedSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        lockedSpan.setVisible(false);
        add(lockedSpan, controlsLayout, enabledLayout, lorebookInfoSpan, grid);
        setFlexGrow(1, grid);

        refresh();

        return this;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void setupGrid() {
        grid = new Grid<>(LorebookEntry.class, false);
        grid.setSizeFull();
        // the buttons of the rows are the only way to open details, so that openEntries knows about all of them
        grid.setDetailsVisibleOnClick(false);
        // rows grow with their content, so a long name breaks into lines instead of running under the button
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addComponentColumn(this::createEntryRow).setFlexGrow(1);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEntryDetailLayout));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "enabled", type = Checkbox.class),
                    @APILocals(name = "nameSpan", type = Span.class),
                    @APILocals(name = "toggleDetails", type = Button.class),
                    @APILocals(name = "row", type = HorizontalLayout.class)})
    private Component createEntryRow(LorebookEntry entry) {
        Checkbox enabled = new Checkbox(entry.isEnabled());
        enabled.setReadOnly(lockedBy != null);
        enabled.addClassName(CssStyles.FLEX_SHRINK_0);
        enabled.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                entry.setEnabled(Boolean.TRUE.equals(e.getValue()));
                saveEntry(entry);
                updateLorebookTokens();
            }
        });

        Span nameSpan = new Span(StringUtils.defaultString(entry.getName()));
        nameSpan.addClassNames(CssStyles.TEXT_BOLD, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE, CssStyles.MIN_WIDTH_0);

        Button toggleDetails = new Button((grid.isDetailsVisible(entry) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create(), event -> {
            boolean isVisible = grid.isDetailsVisible(entry);
            grid.setDetailsVisible(entry, !isVisible);
            event.getSource().setIcon(!isVisible ? Solid.ANGLE_UP.create() : Solid.ANGLE_DOWN.create());
            if (isVisible) {
                openEntries.remove(entry.getUuid());
            } else {
                openEntries.add(entry.getUuid());
            }
            fireStateChanged();
        });
        toggleDetails.setThemeName("tertiary");
        toggleDetails.addClassName(CssStyles.FLEX_SHRINK_0);

        HorizontalLayout row = new HorizontalLayout(enabled, nameSpan, toggleDetails);
        row.setWidthFull();
        row.setPadding(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setFlexGrow(1, nameSpan);
        return row;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "detailsLayout", type = VerticalLayout.class),
                    @APILocals(name = "payloadField", type = TextAreaPopoverComponent.class),
                    @APILocals(name = "payloadTokensSpan", type = Span.class)})
    public Component createEntryDetailLayout(LorebookEntry entry) {
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);
        detailsLayout.getThemeList().add("spacing-xs");
        detailsLayout.addClassName(CssStyles.SUBTLE_BOX);

        detailsLayout.add(infoSpan(L.LABEL_ENTRY_NAME, entry.getName()));
        detailsLayout.add(infoSpan(L.LABEL_ORDER, String.valueOf(entry.getOrder())));
        detailsLayout.add(infoSpan(L.LABEL_TAGS, tagsOf(entry, false)));
        detailsLayout.add(infoSpan(L.LABEL_FILTERING_MODE,
                entry.getFilteringMode() != null ? loc.getValue(loc.getFilteringMode(entry.getFilteringMode())) : null));
        detailsLayout.add(infoSpan(L.LABEL_FILTERING, entry.getFiltering()));
        detailsLayout.add(infoSpan(L.LABEL_INSERTION_MODE,
                entry.getInsertionMode() != null ? loc.getValue(loc.getInsertionMode(entry.getInsertionMode())) : null));
        detailsLayout.add(infoSpan(L.LABEL_NEGATIVE_TAGS, tagsOf(entry, true)));
        detailsLayout.add(infoSpan(L.LABEL_NOTE, entry.getComment()));

        TextAreaPopoverComponent payloadField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_CONTENT));
        payloadField.setWidthFull();
        ResizableTextArea.install(loc, payloadField, "160px");
        payloadField.setValue(StringUtils.defaultString(entry.getPayload()));
        payloadField.setReadOnly(lockedBy != null);
        payloadField.setPopoverContent(TemplateHints.create(loc, payloadField, payloadField.getPopover(), LorebookTemplateData.class, null));
        validatePayload(payloadField);

        Span payloadTokensSpan = createTokensSpan();
        payloadTokensSpan.setText(formatTokens(tokensOf(entry)));

        payloadField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                // invalid templates are still saved (user may be in the middle of editing), generation uses them as plain text
                validatePayload(payloadField);
                entry.setPayload(e.getValue());
                saveEntry(entry);
                payloadTokensSpan.setText(formatTokens(recountEntry(entry)));
                updateLorebookTokens();
            }
        });

        detailsLayout.add(payloadField, payloadTokensSpan);
        return detailsLayout;
    }

    /**
     * A "label: value" line, empty when there is no value.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "span", type = Span.class),
                    @APILocals(name = "labelSpan", type = Span.class),
                    @APILocals(name = "valueSpan", type = Span.class)})
    private Span infoSpan(L label, String value) {
        Span span = new Span();
        span.addClassNames(CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        if (StringUtils.isBlank(value)) {
            span.setVisible(false);
            return span;
        }
        Span labelSpan = new Span(loc.getValue(label) + ": ");
        labelSpan.addClassName(CssStyles.TEXT_SEMIBOLD);
        Span valueSpan = new Span(value);
        valueSpan.addClassName(CssStyles.PRE_WRAP);
        span.add(labelSpan, valueSpan);
        return span;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class),
                    @APIParameter(name = "negative", type = boolean.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private String tagsOf(LorebookEntry entry, boolean negative) {
        try {
            return joinTags(tagRelationService.getTagsForObject(entry, negative));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return "";
        }
    }

    private static String joinTags(List<Tag> tags) {
        return tags.stream().map(Tag::getValue).sorted().reduce((a, b) -> a + ", " + b).orElse("");
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Span.class),
            locals = {@APILocals(name = "span", type = Span.class)})
    private Span createTokensSpan() {
        Span span = new Span();
        span.addClassName(CssStyles.HINT_TEXT);
        return span;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "tokens", type = long.class)})
    private String formatTokens(long tokens) {
        return "~" + tokens + " " + loc.getValue(L.LABEL_TOKENS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = long.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "cached", type = Long.class)})
    private long tokensOf(LorebookEntry entry) {
        Long cached = entry.getId() != null ? entryTokens.get(entry.getId()) : null;
        return cached != null ? cached : recountEntry(entry);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = long.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "ex", type = Exception.class)})
    private long recountEntry(LorebookEntry entry) {
        long tokens = 0;
        try {
            tokens = tokenizerService.countTokensApprox(StringUtils.defaultString(entry.getPayload()));
        } catch (Exception ex) {
            UIUtils.internalServerError(loc, ex);
        }
        if (entry.getId() != null) {
            entryTokens.put(entry.getId(), tokens);
        }
        return tokens;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entry", type = LorebookEntry.class)})
    private void updateLorebookTokens() {
        if (currentLorebook == null) {
            lorebookTokensSpan.setText("");
            return;
        }
        long total = 0;
        for (LorebookEntry entry : currentEntries) {
            if (entry.isEnabled()) {
                total += tokensOf(entry);
            }
        }
        lorebookTokensSpan.setText(formatTokens(total));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "payloadField", type = TextArea.class)},
            locals = {@APILocals(name = "payload", type = String.class),
                    @APILocals(name = "result", type = TemplateService.ValidationResult.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void validatePayload(TextArea payloadField) {
        String payload = payloadField.getValue();
        if (StringUtils.isBlank(payload)) {
            payloadField.setInvalid(false);
            TemplateHints.showWarnings(loc, payloadField, null);
            return;
        }
        try {
            TemplateService.ValidationResult result = templateService.isValidTemplate(payload, "lorebookEntry", LorebookTemplateData.class);
            payloadField.setErrorMessage(result.isValid() ? null : loc.getValue(L.MSG_INVALID_TEMPLATE) + result.errorMessage());
            payloadField.setInvalid(!result.isValid());
            TemplateHints.showWarnings(loc, payloadField, result);
        } catch (Exception e) {
            payloadField.setErrorMessage(loc.getValue(L.MSG_INVALID_TEMPLATE) + e.getMessage());
            payloadField.setInvalid(true);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    public void refresh() throws Exception {
        loadLorebooks();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "lorebooks", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void loadLorebooks() {
        if (pinnedLorebook) {
            updateSelectedLorebook();
            return;
        }
        try {
            List<Lorebook> lorebooks = lorebookService.findAllRegularForUser();
            lorebooks.sort(Comparator.comparing(Lorebook::getName));
            lorebookCombo.setItems(lorebooks);
            if (currentLorebook == null && !lorebooks.isEmpty()) {
                currentLorebook = lorebooks.getFirst();
            }
            if (currentLorebook != null) {
                lorebookCombo.setValue(currentLorebook);
            }
            updateSelectedLorebook();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Opens the search and replace in the entries of the lorebook, the entries are shown again when something was
     * replaced.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookSearchReplaceDialog.class)})
    private void openSearchReplaceDialog() {
        if (currentLorebook == null) {
            return;
        }
        LorebookSearchReplaceDialog dialog = new LorebookSearchReplaceDialog(currentLorebook, true);
        dialog.onReplaced(() -> {
            try {
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        dialog.open();
    }

    /**
     * Reads whether an agent that works on a book that uses the lorebook locks it, and shows it.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "locking", type = AgentRun.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void updateLock() {
        try {
            AgentRun locking = currentLorebook == null ? null : agentRunService.findLockingRun(currentLorebook);
            lockedBy = locking == null ? null : locking.getAgent().getName();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        lockedSpan.setVisible(lockedBy != null);
        lockedSpan.setText(lockedBy == null ? "" : String.format(loc.getValue(L.MSG_AGENT_LOREBOOK_LOCKED), lockedBy));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void updateSelectedLorebook() {
        boolean hasLorebook = currentLorebook != null;
        updateLock();
        lorebookEnabledCheckbox.setEnabled(hasLorebook);
        lorebookEnabledCheckbox.setReadOnly(lockedBy != null);
        refreshButton.setEnabled(hasLorebook);
        searchReplaceButton.setEnabled(hasLorebook);
        searchReplaceButton.setVisible(lockedBy == null);

        if (hasLorebook) {
            lorebookEnabledCheckbox.setValue(currentLorebook.isEnabled());
            updateLorebookInfo();
            refreshEntries();
        } else {
            lorebookEnabledCheckbox.setValue(false);
            lorebookInfoSpan.setText("");
            // the grid keeps its own record of the opened details over setItems: close them, openEntries decides what opens again
            currentEntries.forEach(old -> grid.setDetailsVisible(old, false));
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "info", type = StringBuilder.class),
                    @APILocals(name = "tags", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void updateLorebookInfo() {
        StringBuilder info = new StringBuilder();
        try {
            if (currentLorebook.getSubbooks() != null && !currentLorebook.getSubbooks().isEmpty()) {
                info.append(loc.getValue(L.LABEL_SUB_LOREBOOKS)).append(": ")
                        .append(currentLorebook.getSubbooks().stream().map(Lorebook::getName).sorted()
                                .reduce((a, b) -> a + ", " + b).orElse(""));
            }
            String tags = joinTags(tagRelationService.getTagsForObject(currentLorebook));
            if (!tags.isEmpty()) {
                if (!info.isEmpty()) {
                    info.append("\n");
                }
                info.append(loc.getValue(L.LABEL_TAGS)).append(": ").append(tags);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        lorebookInfoSpan.setText(info.toString());
        lorebookInfoSpan.setVisible(!info.isEmpty());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entries", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void refreshEntries() {
        if (currentLorebook == null || currentLorebook.getId() == null) {
            currentEntries.forEach(old -> grid.setDetailsVisible(old, false));
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
            return;
        }
        try {
            List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(currentLorebook.getId());
            currentEntries.forEach(old -> grid.setDetailsVisible(old, false));
            grid.setItems(entries);
            showOpenEntries(entries);
            currentEntries = entries;
            entryTokens.clear();
            updateLorebookTokens();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void saveEntry(LorebookEntry entry) {
        try {
            lorebookEntryService.save(entry);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Lorebook.class))
    public Lorebook getCurrentLorebook() {
        return currentLorebook;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lorebook", type = Lorebook.class)})
    public void setLorebook(Lorebook lorebook) {
        if (!Objects.equals(currentLorebook, lorebook)) {
            openEntries.clear();
        }
        this.currentLorebook = lorebook;
        updateSelectedLorebook();
    }

    /**
     * Shows the details of the entries that were opened and still exist and hides those of the others, forgets the
     * entries that are gone.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entries", type = List.class)},
            locals = {@APILocals(name = "entry", type = LorebookEntry.class)})
    private void showOpenEntries(List<LorebookEntry> entries) {
        openEntries.retainAll(entries.stream().map(LorebookEntry::getUuid).collect(Collectors.toSet()));
        for (LorebookEntry entry : entries) {
            grid.setDetailsVisible(entry, openEntries.contains(entry.getUuid()));
        }
    }

    /**
     * Called after the user changed what {@link #storeState()} stores (the lorebook, an opened entry), the owner
     * passes it on to the workspace autosave.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onStateChange", type = Runnable.class)})
    public void setOnStateChange(Runnable onStateChange) {
        this.onStateChange = onStateChange;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    private void fireStateChanged() {
        if (onStateChange != null) {
            onStateChange.run();
        }
    }

    /**
     * The lorebook (unless it is pinned: its owner chooses it) and the entries whose details are opened.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        if (!pinnedLorebook && currentLorebook != null && currentLorebook.getUuid() != null) {
            state.addProperty("lorebook", currentLorebook.getUuid());
        }
        if (!openEntries.isEmpty()) {
            state.add("entries", RestorableState.array(openEntries));
        }
        return state;
    }

    /**
     * Selects the stored lorebook when it still exists (otherwise the lorebook stays as it is) and opens the stored
     * entries that are still in it.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "lorebookUuid", type = String.class),
                    @APILocals(name = "stored", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void restoreState(JsonObject self) {
        if (grid == null) {
            return;
        }
        try {
            String lorebookUuid = RestorableState.string(self, "lorebook");
            if (!pinnedLorebook && lorebookUuid != null) {
                Lorebook stored = lorebookService.findForUser(lorebookUuid);
                if (stored != null) {
                    currentLorebook = stored;
                }
            }
            openEntries.clear();
            openEntries.addAll(RestorableState.strings(self, "entries"));
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

}
