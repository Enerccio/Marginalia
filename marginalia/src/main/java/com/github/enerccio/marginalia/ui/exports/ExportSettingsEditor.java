package com.github.enerccio.marginalia.ui.exports;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.ExportSettingsService;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.styles.PartStyleApplier;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The export settings of the user: for every format ({@link Exporter}) the style it is exported in, which the export dialog preselects,
 * and the templates of the format that the user can change (the title page). A format with neither is not shown.
 * <p>
 * Like {@code PartStyleEditor} it is a part of the settings page, which saves and discards for it: edits only fill the fields and tell the
 * page ({@link #setOnChange}); {@link #validate} and {@link #save} are called by the save of the page, {@link #reload} by its refresh (the
 * discard), which also shows the formats and styles as they are now.
 */
public class ExportSettingsEditor extends VerticalLayout {

    private final Localization loc;
    private final ExporterService exporterService;
    private final ExportSettingsService settingsService;
    private final PartStyleService partStyleService;
    private final TemplateService templateService;

    /**
     * What is shown for one format.
     */
    private static class Section {
        Exporter exporter;
        Select<String> style;
        final Map<ExportTemplate, TextAreaPopoverComponent> templates = new LinkedHashMap<>();
    }

    private final List<Section> sections = new ArrayList<>();
    private Runnable onChange = () -> {
    };
    // true while the fields are filled: their listeners must not count that as a change
    private boolean loading;

    public ExportSettingsEditor(Localization loc, ExporterService exporterService, ExportSettingsService settingsService,
                                PartStyleService partStyleService, TemplateService templateService) {
        this.loc = loc;
        this.exporterService = exporterService;
        this.settingsService = settingsService;
        this.partStyleService = partStyleService;
        this.templateService = templateService;
        setPadding(false);
        setWidthFull();
    }

    /**
     * @param onChange called when the user changes something (not when the editor fills its fields)
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {
        } : onChange;
    }

    /**
     * Shows the settings as they are stored now, dropping what was edited.
     */
    public void reload() throws Exception {
        loading = true;
        try {
            removeAll();
            sections.clear();
            List<PartStyle> styles = PartStyleApplier.sorted(partStyleService.listForExport(), loc);
            for (Exporter exporter : exporterService.getExporters()) {
                if (!exporter.supportsStyles() && exporter.getTemplates().isEmpty()) {
                    continue;
                }
                Section section = new Section();
                section.exporter = exporter;
                FormLayout form = new FormLayout();
                form.setWidthFull();
                form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
                form.addClassName(CssStyles.FORM_PADDING_BOTTOM);

                if (exporter.supportsStyles()) {
                    section.style = createStyleSelect(exporter, styles);
                    form.add(section.style);
                }
                for (ExportTemplate template : exporter.getTemplates()) {
                    TextAreaPopoverComponent field = createTemplateField(exporter, template);
                    section.templates.put(template, field);
                    form.add(field);
                }
                sections.add(section);

                Details details = new Details(exporter.getName() + " (." + exporter.getFileExtension() + ")", form);
                details.setWidthFull();
                add(details);
            }
        } finally {
            loading = false;
        }
    }

    private Select<String> createStyleSelect(Exporter exporter, List<PartStyle> styles) throws Exception {
        Select<String> select = new Select<>();
        select.setLabel(loc.getValue(L.LABEL_EXPORT_STYLE));
        select.setWidthFull();
        Map<String, String> names = new LinkedHashMap<>();
        styles.forEach(style -> names.put(style.getId(), style.getDisplayName(loc)));
        select.setItems(names.keySet());
        select.setItemLabelGenerator(names::get);
        // a format that has no look of its own can also be left without a style
        select.setEmptySelectionAllowed(exporter.getDefaultStyleId() == null);
        select.setEmptySelectionCaption(loc.getValue(L.LABEL_EXPORT_STYLE_OF_FORMAT));
        select.setValue(settingsService.getStyleId(exporter));
        select.addValueChangeListener(event -> changed());
        return select;
    }

    private TextAreaPopoverComponent createTemplateField(Exporter exporter, ExportTemplate template) throws Exception {
        TextAreaPopoverComponent field = new TextAreaPopoverComponent(template.label());
        field.setWidthFull();
        ResizableTextArea.install(loc, field, "160px");
        field.setPlaceholder(template.defaultText());
        field.setPopoverContent(TemplateHints.create(loc, field, field.getPopover(), template.dataClass(), template.defaultText()));
        field.setValue(StringUtils.defaultString(settingsService.getTemplate(exporter.getId(), template.key())));
        field.addValueChangeListener(event -> changed());
        return field;
    }

    private void changed() {
        if (!loading) {
            onChange.run();
        }
    }

    /**
     * @return false (and says why) when a template is not valid and the settings must not be saved; names the template uses that it is not
     * given are shown as a warning only
     */
    public boolean validate() {
        for (Section section : sections) {
            for (Map.Entry<ExportTemplate, TextAreaPopoverComponent> entry : section.templates.entrySet()) {
                TextAreaPopoverComponent field = entry.getValue();
                if (StringUtils.isBlank(field.getValue())) {
                    TemplateHints.showWarnings(loc, field, null);
                    continue;
                }
                try {
                    TemplateService.ValidationResult result = templateService.isValidTemplate(field.getValue(),
                            "export." + section.exporter.getId() + "." + entry.getKey().key(), entry.getKey().dataClass());
                    if (!result.isValid()) {
                        Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                        return false;
                    }
                    TemplateHints.showWarnings(loc, field, result);
                } catch (Exception e) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Stores what differs from what is stored now. The styles of the user have to be saved before (a style that is gone counts as none).
     */
    public void save() throws Exception {
        List<String> existing = partStyleService.listForExport().stream().map(PartStyle::getId).toList();
        for (Section section : sections) {
            String id = section.exporter.getId();
            if (section.style != null) {
                String value = section.style.getValue();
                if (value != null && (!existing.contains(value) || value.equals(section.exporter.getDefaultStyleId()))) {
                    // the look of the format itself is not stored, so the format keeps following the application
                    value = null;
                }
                if (!Objects.equals(value, settingsService.getChosenStyleId(id))) {
                    settingsService.setStyleId(id, value);
                }
            }
            for (Map.Entry<ExportTemplate, TextAreaPopoverComponent> entry : section.templates.entrySet()) {
                String text = StringUtils.isBlank(entry.getValue().getValue()) ? null : entry.getValue().getValue();
                if (entry.getKey().defaultText().equals(text)) {
                    // the default itself is not stored, so it keeps following the application
                    text = null;
                }
                if (!Objects.equals(text, settingsService.getTemplate(id, entry.getKey().key()))) {
                    settingsService.setTemplate(id, entry.getKey(), text);
                }
            }
        }
    }

    // for the tests and the page
    public Select<String> getStyleField(String exporterId) {
        return sections.stream().filter(s -> s.exporter.getId().equals(exporterId)).findFirst().map(s -> s.style).orElse(null);
    }

    public TextAreaPopoverComponent getTemplateField(String exporterId, String templateKey) {
        return sections.stream().filter(s -> s.exporter.getId().equals(exporterId))
                .flatMap(s -> s.templates.entrySet().stream()).filter(e -> e.getKey().key().equals(templateKey))
                .map(Map.Entry::getValue).findFirst().orElse(null);
    }

    public List<Exporter> getShownExporters() {
        return sections.stream().map(s -> s.exporter).toList();
    }
}
