package com.github.enerccio.marginalia.ui.styles;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.domain.styles.PartStyleTransfer;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The editor of the text styles in the user settings: the list of the styles (built in ones and the user's own, by name), and for the
 * selected one its name, the options of {@link PartStyleOption} (fonts, colors, headings...) and the CSS of its own, with the CSS the
 * style makes and a preview of a sample text with the style applied, which follows every change.
 * <p>
 * The editor is a part of the settings page, which saves and discards for it: every edit, duplicate, delete and change of the default
 * changes a working copy in memory and tells the page ({@link #setOnChange}, which locks the navigation like any field of the page);
 * {@link #validate} and {@link #save} are called by the save of the page, {@link #reload} by its refresh (the discard). Built in styles
 * are shown read only; duplicate one to change it.
 */
public class PartStyleEditor extends HorizontalLayout {

    private static final String PREVIEW_ID = "preview";

    private final Localization loc;
    private final PartStyleService service;

    private final Grid<PartStyle> grid = new Grid<>();
    private final Button duplicateButton;
    private final Button deleteButton;
    private final Button defaultButton;
    private final Button exportButton;

    private final TextField nameField = new TextField();
    private final Checkbox forExportBox = new Checkbox();
    private final Map<PartStyleOption, HasValue<?, ?>> optionFields = new EnumMap<>(PartStyleOption.class);
    private final TextArea customCssField = new TextArea();
    private final TextArea generatedCssField = new TextArea();
    private final Div preview = new Div();

    // the working copy: the built in styles and the styles of the user as they are edited; nothing is stored before save()
    private final List<PartStyle> styles = new ArrayList<>();
    // the ids of the styles of the user that are stored now, to find the ones that were deleted
    private final Set<String> persisted = new HashSet<>();
    private Runnable onChange = () -> {
    };

    private PartStyle selected;
    private String defaultId;
    // true while the fields are filled from a style: their listeners must not count that as a change
    private boolean loading;

    public PartStyleEditor(Localization loc, PartStyleService service) {
        this.loc = loc;
        this.service = service;
        setSizeFull();
        addClassName(CssStyles.MIN_HEIGHT_0);

        duplicateButton = new Button(loc.getValue(L.LABEL_DUPLICATE), event -> duplicate());
        deleteButton = new Button(loc.getValue(L.LABEL_DELETE), event -> delete());
        defaultButton = new Button(loc.getValue(L.LABEL_STYLE_SET_DEFAULT), event -> makeDefault());
        exportButton = new Button(loc.getValue(L.LABEL_EXPORT_BACKUP));

        add(createListColumn(), createEditColumn());
        setFlexGrow(1, getComponentAt(1));
        try {
            reload();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Called after every change the user makes, with the settings page that marks itself as changed. Not called by {@link #reload}.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {
        } : onChange;
    }

    /**
     * The field of the option (the content of the tabs is not in the component tree until the browser asks for it).
     */
    public HasValue<?, ?> getOptionField(PartStyleOption option) {
        return optionFields.get(option);
    }

    /**
     * The field of the custom CSS.
     */
    public TextArea getCustomCssField() {
        return customCssField;
    }

    /**
     * The read only field that shows the stylesheet the style makes.
     */
    public TextArea getGeneratedCssField() {
        return generatedCssField;
    }

    // --- layout ---

    private Component createListColumn() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        grid.addComponentColumn(this::listCell).setFlexGrow(1);
        grid.addSelectionListener(event -> {
            if (loading) {
                return;
            }
            PartStyle chosen = event.getFirstSelectedItem().orElse(null);
            if (chosen == null || (selected != null && chosen.getId().equals(selected.getId()))) {
                // a click on the selected row deselects it: keep it
                if (chosen == null && selected != null) {
                    select(selected);
                }
                return;
            }
            show(chosen);
        });

        HorizontalLayout buttons = new HorizontalLayout(duplicateButton, deleteButton);
        buttons.setPadding(false);
        defaultButton.setWidthFull();

        VerticalLayout column = new VerticalLayout(grid, buttons, createTransferButtons(), defaultButton);
        column.addClassNames(CssStyles.STYLE_EDITOR_LIST, CssStyles.MIN_HEIGHT_0);
        // a VerticalLayout is as wide as its parent unless told otherwise, which the stylesheet can't override
        column.setWidth("18rem");
        column.setPadding(false);
        column.setHeightFull();
        column.setFlexGrow(1, grid);
        return column;
    }

    // the selected style as a JSON file, and a JSON file of styles into the working copy
    private Component createTransferButtons() {
        Anchor download = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) event -> {
            byte[] data = exportJson().getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(data), exportFileName(), "application/json", data.length);
        }), "");
        download.getElement().setAttribute("download", true);
        download.add(exportButton);

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> importJson(new String(data, StandardCharsets.UTF_8)));
        Upload upload = new Upload(handler);
        upload.setAcceptedFileTypes(".json", "application/json");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(PartStyleTransfer.MAX_BYTES);
        upload.setDropAllowed(false);
        Button importButton = new Button(loc.getValue(L.LABEL_IMPORT));
        upload.setUploadButton(importButton);
        upload.addSucceededListener(event -> upload.clearFileList());
        upload.addFileRejectedListener(event -> Notification.show(loc.getValue(L.ERROR_STYLE_IMPORT_NOT_STYLES)));

        HorizontalLayout row = new HorizontalLayout(download, upload);
        row.setPadding(false);
        return row;
    }

    /**
     * The JSON file of the selected style, as it is on the screen (saved or not).
     */
    public String exportJson() {
        return PartStyleTransfer.toJson(selected == null ? List.of() : List.of(selected));
    }

    private String exportFileName() {
        String name = selected == null ? "style" : StringUtils.defaultIfBlank(selected.getDisplayName(loc), "style");
        return name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}\\s]+", "-") + ".style.json";
    }

    /**
     * Adds the styles of a JSON file to the working copy as styles of the user (the first one is selected); the page saves them. The
     * user is told when the file can't be read.
     *
     * @return whether the file was taken
     */
    public boolean importJson(String json) {
        PartStyleTransfer.Imported imported;
        try {
            imported = PartStyleTransfer.fromJson(json);
        } catch (PartStyleTransfer.ImportException e) {
            Notification.show(switch (e.getReason()) {
                case NEWER_VERSION -> loc.getValue(L.ERROR_STYLE_IMPORT_NEWER);
                case UNBALANCED_CSS -> String.format(loc.getValue(L.ERROR_STYLE_IMPORT_CSS), e.getStyleName());
                default -> loc.getValue(L.ERROR_STYLE_IMPORT_NOT_STYLES);
            });
            return false;
        }
        styles.addAll(imported.styles());
        refreshList();
        show(imported.styles().getFirst());
        onChange.run();
        String message = String.format(loc.getValue(L.MSG_STYLES_IMPORTED), imported.styles().size());
        if (imported.ignoredOptions() > 0) {
            message += " " + String.format(loc.getValue(L.MSG_STYLE_IMPORT_IGNORED), imported.ignoredOptions());
        }
        Notification.show(message);
        return true;
    }

    private Component listCell(PartStyle style) {
        Span name = new Span(style.getDisplayName(loc));
        Span marks = new Span();
        List<String> notes = new ArrayList<>();
        if (style.isBuiltin()) {
            notes.add(loc.getValue(L.LABEL_STYLE_BUILTIN_MARK));
        }
        if (style.isForExport()) {
            notes.add(loc.getValue(L.LABEL_STYLE_EXPORT_MARK));
        }
        if (style.getId().equals(defaultId)) {
            notes.add(loc.getValue(L.LABEL_STYLE_DEFAULT_MARK));
        }
        if (!notes.isEmpty()) {
            marks.setText("(" + String.join(", ", notes) + ")");
            marks.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);
        }
        HorizontalLayout cell = new HorizontalLayout(name, marks);
        cell.setPadding(false);
        cell.addClassName(CssStyles.GAP_S);
        return cell;
    }

    private Component createEditColumn() {
        nameField.setLabel(loc.getValue(L.LABEL_NAME));
        nameField.setWidthFull();
        nameField.setValueChangeMode(ValueChangeMode.LAZY);
        nameField.addValueChangeListener(event -> {
            changed();
            if (!loading) {
                // the name is what the list sorts by
                refreshList();
            }
        });

        forExportBox.setLabel(loc.getValue(L.LABEL_STYLE_FOR_EXPORT));
        forExportBox.addValueChangeListener(event -> {
            changed();
            if (!loading) {
                // a style for exports is not one for books: it can't be the default of the books
                if (selected != null && selected.isForExport() && selected.getId().equals(defaultId)) {
                    defaultId = null;
                }
                refreshList();
            }
        });

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addClassName(CssStyles.MIN_HEIGHT_0);
        tabs.add(loc.getValue(L.LABEL_STYLE_OPTIONS), createOptionsTab());
        tabs.add(loc.getValue(L.LABEL_STYLE_CUSTOM_CSS), createCssTab());

        preview.addClassName(CssStyles.STYLE_EDITOR_PREVIEW);
        Markdown sample = new Markdown(loc.getValue(L.MSG_STYLE_PREVIEW_SAMPLE));
        sample.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
        preview.add(sample);
        Span previewLabel = new Span(loc.getValue(L.LABEL_STYLE_PREVIEW));
        previewLabel.addClassName(CssStyles.TEXT_SEMIBOLD);

        VerticalLayout column = new VerticalLayout(nameField, forExportBox, tabs, previewLabel, preview);
        // takes what the list leaves (the flex grow of the row), and may shrink below its content
        column.setWidth(null);
        column.addClassName(CssStyles.MIN_WIDTH_0);
        column.setPadding(false);
        column.setHeightFull();
        column.addClassName(CssStyles.MIN_HEIGHT_0);
        column.setFlexGrow(1, tabs);
        return column;
    }

    private Component createOptionsTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setPadding(false);
        Span help = new Span(loc.getValue(L.HELP_STYLE_VALUES));
        help.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);
        tab.add(help);

        for (PartStyleOption.Group group : PartStyleOption.Group.values()) {
            Span title = new Span(loc.getValue(group.getLabel()));
            title.addClassName(CssStyles.TEXT_SEMIBOLD);
            FormLayout form = new FormLayout();
            form.setWidthFull();
            for (PartStyleOption option : PartStyleOption.values()) {
                if (option.getGroup() == group) {
                    form.add(createField(option));
                }
            }
            tab.add(title, form);
        }
        return tab;
    }

    private Component createField(PartStyleOption option) {
        String label = loc.getValue(option.getLabel());
        switch (option.getKind()) {
            case BOOLEAN -> {
                Checkbox box = new Checkbox(label);
                box.addValueChangeListener(event -> changed());
                optionFields.put(option, box);
                return box;
            }
            case CHOICE -> {
                ComboBox<String> combo = new ComboBox<>(label);
                combo.setItems(option.getChoices());
                combo.setClearButtonVisible(true);
                combo.addValueChangeListener(event -> changed());
                optionFields.put(option, combo);
                return combo;
            }
            case FONT -> {
                ComboBox<String> combo = new ComboBox<>(label);
                combo.setItems(PartStyleOption.FONT_SUGGESTIONS);
                combo.setAllowCustomValue(true);
                combo.setClearButtonVisible(true);
                combo.addCustomValueSetListener(event -> combo.setValue(event.getDetail()));
                combo.addValueChangeListener(event -> changed());
                UIUtils.setupComboBoxOverlayWidth(combo, PartStyleOption.FONT_SUGGESTIONS);
                optionFields.put(option, combo);
                return combo;
            }
            default -> {
                TextField field = new TextField(label);
                field.setClearButtonVisible(true);
                field.setValueChangeMode(ValueChangeMode.LAZY);
                field.addValueChangeListener(event -> changed());
                optionFields.put(option, field);
                return field;
            }
        }
    }

    private Component createCssTab() {
        VerticalLayout tab = new VerticalLayout();
        tab.setPadding(false);
        Span help = new Span(loc.getValue(L.HELP_STYLE_CUSTOM_CSS));
        help.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);

        customCssField.setLabel(loc.getValue(L.LABEL_STYLE_CUSTOM_CSS));
        customCssField.setWidthFull();
        customCssField.setMinHeight("10rem");
        customCssField.addClassName(CssStyles.STYLE_EDITOR_CSS);
        customCssField.setValueChangeMode(ValueChangeMode.LAZY);
        customCssField.addValueChangeListener(event -> changed());

        generatedCssField.setLabel(loc.getValue(L.LABEL_STYLE_GENERATED_CSS));
        generatedCssField.setWidthFull();
        generatedCssField.setMinHeight("10rem");
        generatedCssField.setReadOnly(true);
        generatedCssField.addClassName(CssStyles.STYLE_EDITOR_CSS);

        tab.add(help, customCssField, generatedCssField);
        return tab;
    }

    // --- state ---

    /**
     * Reads the styles again from the service (what is stored now), which drops every change that was not saved; the style that was
     * selected stays selected when it still exists.
     */
    public void reload() throws Exception {
        String keep = selected == null ? null : selected.getId();
        styles.clear();
        styles.addAll(service.list());
        persisted.clear();
        styles.stream().filter(style -> !style.isBuiltin()).forEach(style -> persisted.add(style.getId()));
        defaultId = service.getDefaultId();
        selected = null;
        refreshList();
        show(find(keep));
    }

    private PartStyle find(String id) {
        // the first of the list as the user sees it when there is no such style
        return styles.stream().filter(style -> style.getId().equals(id)).findFirst()
                .orElse(styles.isEmpty() ? null : PartStyleApplier.sorted(styles, loc).getFirst());
    }

    /**
     * Whether the styles can be saved: every style of the user has a name and balanced braces. When one has not, it is selected and
     * the user is told.
     */
    public boolean validate() {
        for (PartStyle style : styles) {
            if (style.isBuiltin()) {
                continue;
            }
            if (StringUtils.isBlank(style.getName())) {
                show(style);
                nameField.setInvalid(true);
                Notification.show(loc.getValue(L.ERROR_STYLE_NAME_REQUIRED));
                return false;
            }
            if (style.getCustomCss() != null && !PartStyle.isBalanced(style.getCustomCss())) {
                show(style);
                Notification.show(loc.getValue(L.ERROR_STYLE_CSS_UNBALANCED));
                return false;
            }
        }
        return true;
    }

    /**
     * Stores the styles as they are now: deletes the ones that were deleted, saves the others and sets the default. Call
     * {@link #validate} first.
     */
    public void save() throws Exception {
        Set<String> kept = new HashSet<>();
        styles.stream().filter(style -> !style.isBuiltin()).forEach(style -> kept.add(style.getId()));
        for (String id : persisted) {
            if (!kept.contains(id)) {
                service.delete(id);
            }
        }
        for (PartStyle style : styles) {
            if (!style.isBuiltin()) {
                service.save(style);
            }
        }
        service.setDefault(defaultId);
        reload();
    }

    private void refreshList() {
        List<PartStyle> sorted = PartStyleApplier.sorted(styles, loc);
        boolean wasLoading = loading;
        loading = true;
        try {
            grid.setItems(sorted);
            if (selected != null) {
                grid.select(selected);
            }
        } finally {
            loading = wasLoading;
        }
    }

    private void select(PartStyle style) {
        boolean wasLoading = loading;
        loading = true;
        try {
            grid.select(style);
        } finally {
            loading = wasLoading;
        }
    }

    /**
     * Fills the fields from the style, which becomes the selected one.
     */
    private void show(PartStyle style) {
        selected = style;
        select(style);
        loading = true;
        try {
            boolean readOnly = style == null || style.isBuiltin();
            nameField.setInvalid(false);
            nameField.setValue(style == null ? "" : style.getDisplayName(loc));
            nameField.setReadOnly(readOnly);
            forExportBox.setValue(style != null && style.isForExport());
            forExportBox.setReadOnly(readOnly);
            for (Map.Entry<PartStyleOption, HasValue<?, ?>> entry : optionFields.entrySet()) {
                setField(entry.getValue(), style == null ? null : style.get(entry.getKey()));
                entry.getValue().setReadOnly(readOnly);
            }
            customCssField.setValue(style == null ? "" : StringUtils.defaultString(style.getCustomCss()));
            customCssField.setReadOnly(readOnly);
        } finally {
            loading = false;
        }
        refresh();
    }

    @SuppressWarnings("unchecked")
    private static void setField(HasValue<?, ?> field, String value) {
        if (field instanceof Checkbox box) {
            box.setValue("true".equalsIgnoreCase(value));
        } else if (value == null) {
            // a text field has no null, its empty value is ""
            field.clear();
        } else {
            ((HasValue<?, String>) field).setValue(value);
        }
    }

    private static String valueOf(HasValue<?, ?> field) {
        Object value = field.getValue();
        if (value instanceof Boolean flag) {
            return flag ? "true" : null;
        }
        return value == null ? null : value.toString();
    }

    /**
     * A field was edited: the selected style takes what the fields show.
     */
    private void changed() {
        if (loading || selected == null || selected.isBuiltin()) {
            return;
        }
        selected.setName(nameField.getValue());
        selected.setForExport(forExportBox.getValue());
        optionFields.forEach((option, field) -> selected.set(option, valueOf(field)));
        selected.setCustomCss(customCssField.getValue());
        refresh();
        onChange.run();
    }

    /**
     * Brings the buttons, the generated CSS and the preview in line with the selected style.
     */
    private void refresh() {
        boolean builtin = selected == null || selected.isBuiltin();
        duplicateButton.setEnabled(selected != null);
        exportButton.setEnabled(selected != null);
        deleteButton.setEnabled(!builtin);
        // the styles for exports are not offered for books
        defaultButton.setEnabled(selected != null && !selected.isForExport() && !selected.getId().equals(defaultId));
        grid.getDataProvider().refreshAll();

        PartStyle now = selected == null ? new PartStyle(PREVIEW_ID, "") : selected;
        generatedCssField.setValue(now.toCss());
        // the sheet of the preview has an id of its own, so the stylesheet of the style that is edited is not touched until it is saved
        PartStyleApplier.apply(preview, now.copy(PREVIEW_ID, now.getName()));
    }

    // --- actions ---

    private void duplicate() {
        if (selected == null) {
            return;
        }
        // what is on the screen, saved or not
        PartStyle copy = selected.copy(UUID.randomUUID().toString(),
                String.format(loc.getValue(L.LABEL_STYLE_COPY_NAME), selected.getDisplayName(loc)));
        styles.add(copy);
        refreshList();
        show(copy);
        onChange.run();
    }

    private void delete() {
        if (selected == null || selected.isBuiltin()) {
            return;
        }
        int index = Math.max(0, styles.indexOf(selected) - 1);
        styles.remove(selected);
        if (selected.getId().equals(defaultId)) {
            defaultId = null;
        }
        selected = null;
        refreshList();
        show(styles.get(Math.min(index, styles.size() - 1)));
        onChange.run();
    }

    private void makeDefault() {
        if (selected == null) {
            return;
        }
        defaultId = selected.getId();
        refresh();
        refreshList();
        onChange.run();
    }
}
