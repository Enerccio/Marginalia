package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
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

/**
 * Reduced {@link LorebookView} for phones. Lorebooks can only be selected (no creating, deleting, importing or
 * exporting) and entries cannot be added. The only things that can be changed are the enabled flag of the lorebook
 * and of its entries and the content of an entry; every other setting is shown as plain text.
 */
@Configurable
@Extendable
public class PhoneLorebookView extends VerticalLayout {

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

    private Lorebook currentLorebook;
    private boolean pinnedLorebook = false;

    private ComboBox<Lorebook> lorebookCombo;
    private Button refreshButton;
    private Checkbox lorebookEnabledCheckbox;
    private Span lorebookInfoSpan;
    private Span lorebookTokensSpan;
    private Grid<LorebookEntry> grid;

    private List<LorebookEntry> currentEntries = new ArrayList<>();
    private final Map<Long, Long> entryTokens = new HashMap<>();

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
                updateSelectedLorebook();
            }
        });

        refreshButton = new Button(Solid.REFRESH.create(), _ -> {
            try {
                refresh();
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
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
                    UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
                }
            }
        });

        lorebookTokensSpan = createTokensSpan();

        HorizontalLayout enabledLayout = new HorizontalLayout(lorebookEnabledCheckbox, lorebookTokensSpan);
        enabledLayout.setWidthFull();
        enabledLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        lorebookInfoSpan = new Span();
        lorebookInfoSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);

        setupGrid();

        add(controlsLayout, enabledLayout, lorebookInfoSpan, grid);
        setFlexGrow(1, grid);

        refresh();

        return this;
    }

    private void setupGrid() {
        grid = new Grid<>(LorebookEntry.class, false);
        grid.setSizeFull();
        // rows grow with their content, so a long name breaks into lines instead of running under the button
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addComponentColumn(this::createEntryRow).setFlexGrow(1);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEntryDetailLayout));
    }

    private Component createEntryRow(LorebookEntry entry) {
        Checkbox enabled = new Checkbox(entry.isEnabled());
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

        Button toggleDetails = new Button(Solid.ANGLE_DOWN.create(), event -> {
            boolean isVisible = grid.isDetailsVisible(entry);
            grid.setDetailsVisible(entry, !isVisible);
            event.getSource().setIcon(!isVisible ? Solid.ANGLE_UP.create() : Solid.ANGLE_DOWN.create());
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

    private String tagsOf(LorebookEntry entry, boolean negative) {
        try {
            return joinTags(tagRelationService.getTagsForObject(entry, negative));
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
            return "";
        }
    }

    private static String joinTags(List<Tag> tags) {
        return tags.stream().map(Tag::getValue).sorted().reduce((a, b) -> a + ", " + b).orElse("");
    }

    private Span createTokensSpan() {
        Span span = new Span();
        span.addClassName(CssStyles.HINT_TEXT);
        return span;
    }

    private String formatTokens(long tokens) {
        return "~" + tokens + " " + loc.getValue(L.LABEL_TOKENS);
    }

    private long tokensOf(LorebookEntry entry) {
        Long cached = entry.getId() != null ? entryTokens.get(entry.getId()) : null;
        return cached != null ? cached : recountEntry(entry);
    }

    private long recountEntry(LorebookEntry entry) {
        long tokens = 0;
        try {
            tokens = tokenizerService.countTokensApprox(StringUtils.defaultString(entry.getPayload()));
        } catch (Exception ex) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), ex);
        }
        if (entry.getId() != null) {
            entryTokens.put(entry.getId(), tokens);
        }
        return tokens;
    }

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

    public void refresh() throws Exception {
        loadLorebooks();
    }

    private void loadLorebooks() {
        if (pinnedLorebook) {
            updateSelectedLorebook();
            return;
        }
        try {
            List<Lorebook> lorebooks = lorebookService.findAllForUser();
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
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    private void updateSelectedLorebook() {
        boolean hasLorebook = currentLorebook != null;
        lorebookEnabledCheckbox.setEnabled(hasLorebook);
        refreshButton.setEnabled(hasLorebook);

        if (hasLorebook) {
            lorebookEnabledCheckbox.setValue(currentLorebook.isEnabled());
            updateLorebookInfo();
            refreshEntries();
        } else {
            lorebookEnabledCheckbox.setValue(false);
            lorebookInfoSpan.setText("");
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
        }
    }

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
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
        lorebookInfoSpan.setText(info.toString());
        lorebookInfoSpan.setVisible(!info.isEmpty());
    }

    public void refreshEntries() {
        if (currentLorebook == null || currentLorebook.getId() == null) {
            grid.setItems(new ArrayList<>());
            currentEntries = new ArrayList<>();
            entryTokens.clear();
            updateLorebookTokens();
            return;
        }
        try {
            List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(currentLorebook.getId());
            grid.setItems(entries);
            currentEntries = entries;
            entryTokens.clear();
            updateLorebookTokens();
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    private void saveEntry(LorebookEntry entry) {
        try {
            lorebookEntryService.save(entry);
        } catch (Exception e) {
            UIUtils.showError(loc.getValue(L.ERROR_INTERNAL_SERVER_ERROR), e);
        }
    }

    public Lorebook getCurrentLorebook() {
        return currentLorebook;
    }

    public void setLorebook(Lorebook lorebook) {
        this.currentLorebook = lorebook;
        updateSelectedLorebook();
    }
}
