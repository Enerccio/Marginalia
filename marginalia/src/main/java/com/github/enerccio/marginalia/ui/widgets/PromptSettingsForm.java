package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The editors of the text settings that a book has and a folder can set for its books ({@link PromptField}: the templates,
 * the point of view, the tense, the style and the prompts), the same for a book and for a folder, on the desktop and on the
 * phone. An empty field is not set: it is inherited, which the field shows by the inherited value as its placeholder and by
 * a line saying where that value comes from (the folder above, the user settings or the built-in default).
 * <p>
 * The form knows nothing about what it edits: {@link #load} takes the values and the folder the empty ones are inherited
 * from, {@link #values()} gives the values back and the owner saves them. A change by the user is reported to the
 * {@code onChange} callback once the templates are all valid (an invalid one is told to the user and is not saved).
 */
@Configurable
public class PromptSettingsForm {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TemplateService templateService;

    private final Map<PromptField, HasValue<?, String>> editors = new EnumMap<>(PromptField.class);
    private final Map<PromptField, EffectiveSetting<String>> inherited = new EnumMap<>(PromptField.class);
    private final Map<PromptField, String> warnings = new EnumMap<>(PromptField.class);
    private final String areaHeight;
    private final Runnable onChange;

    private boolean loading = false;

    /**
     * @param areaHeight the height of the text areas of the form, e.g. "240px" (the style is a little lower)
     * @param onChange   told after the user changed a field and every template is valid, may be null
     */
    public PromptSettingsForm(String areaHeight, Runnable onChange) {
        this.areaHeight = areaHeight;
        this.onChange = onChange;
    }

    /**
     * Makes the editors, which the form is laid out from; it is done after the construction because the services and
     * texts are injected after it.
     *
     * @return this form
     */
    public PromptSettingsForm create() {
        for (PromptField field : PromptField.values()) {
            editors.put(field, createEditor(field, areaHeight));
        }
        return this;
    }

    private HasValue<?, String> createEditor(PromptField field, String areaHeight) {
        String label = loc.getValue(label(field));
        if (field == PromptField.POV || field == PromptField.TENSE) {
            TextFieldPopOverComponent editor = new TextFieldPopOverComponent(label);
            editor.setWidthFull();
            editor.addValueChangeListener(this::changed);
            return editor;
        }
        TextAreaPopoverComponent editor = new TextAreaPopoverComponent(label);
        editor.setWidthFull();
        ResizableTextArea.install(loc, editor, field == PromptField.STYLE ? "160px" : areaHeight);
        editor.addValueChangeListener(this::changed);
        return editor;
    }

    private static L label(PromptField field) {
        return switch (field) {
            case MASTER_TEMPLATE -> L.LABEL_MASTER_TEMPLATE;
            case POV -> L.LABEL_POV;
            case TENSE -> L.LABEL_TENSE;
            case STYLE -> L.LABEL_STYLE;
            case USER_PROMPT -> L.LABEL_USER_PROMPT;
            case SUMMARY_PROMPT -> L.LABEL_SUMMARY_PROMPT;
            case META_SUMMARY_PROMPT -> L.LABEL_META_SUMMARY_PROMPT;
            case REDRAFT_SYSTEM_PROMPT -> L.LABEL_REDRAFT_SYSTEM_PROMPT;
            case REDRAFT_PROMPT -> L.LABEL_REDRAFT_PROMPT;
            case ANNOTATE_SYSTEM_PROMPT -> L.LABEL_ANNOTATE_SYSTEM_PROMPT;
            case ANNOTATE_PROMPT -> L.LABEL_ANNOTATE_PROMPT;
            case IMPERSONATE_PROMPT -> L.LABEL_IMPERSONATE_PROMPT;
        };
    }

    /**
     * The help under the field that is about the field and not about where its value comes from.
     */
    private static L help(PromptField field) {
        return switch (field) {
            case REDRAFT_SYSTEM_PROMPT, ANNOTATE_SYSTEM_PROMPT -> L.HELP_PART_SYSTEM_PROMPT;
            case REDRAFT_PROMPT, ANNOTATE_PROMPT -> L.HELP_PART_LAST_PROMPT;
            default -> null;
        };
    }

    /**
     * The data of the template the field is validated against and shown hints for; null for a plain text.
     */
    private static Class<? extends TemplateData> dataClass(PromptField field) {
        return switch (field) {
            case MASTER_TEMPLATE -> MasterTemplateData.class;
            case USER_PROMPT -> UserPromptData.class;
            case SUMMARY_PROMPT -> SummaryTemplateData.class;
            case META_SUMMARY_PROMPT -> MetaSummaryTemplateData.class;
            case REDRAFT_SYSTEM_PROMPT, REDRAFT_PROMPT -> RedraftTemplateData.class;
            case ANNOTATE_SYSTEM_PROMPT, ANNOTATE_PROMPT -> AnnotateTemplateData.class;
            case IMPERSONATE_PROMPT -> ImpersonateTemplateData.class;
            default -> null;
        };
    }

    private static String templateName(PromptField field) {
        return switch (field) {
            case MASTER_TEMPLATE -> "masterTemplate";
            case USER_PROMPT -> "userPrompt";
            case SUMMARY_PROMPT -> "summaryPrompt";
            case META_SUMMARY_PROMPT -> "metaSummaryPrompt";
            case REDRAFT_SYSTEM_PROMPT -> "redraftSystemPrompt";
            case REDRAFT_PROMPT -> "redraftPrompt";
            case ANNOTATE_SYSTEM_PROMPT -> "annotateSystemPrompt";
            case ANNOTATE_PROMPT -> "annotatePrompt";
            case IMPERSONATE_PROMPT -> "impersonatePrompt";
            default -> null;
        };
    }

    /**
     * The editor of the field, for the owner to lay out.
     */
    public Component component(PromptField field) {
        return (Component) editors.get(field);
    }

    /**
     * The editors in the order of the fields, one under another (the phone).
     */
    public List<Component> components() {
        List<Component> components = new ArrayList<>();
        for (PromptField field : PromptField.values()) {
            components.add(component(field));
        }
        return components;
    }

    /**
     * The editors in two columns, the templates and prompts across both (the desktop).
     */
    public FormLayout formLayout() {
        FormLayout layout = new FormLayout();
        layout.setWidthFull();
        layout.addClassName(CssStyles.FORM_PADDING_BOTTOM);
        for (PromptField field : PromptField.values()) {
            Component editor = component(field);
            layout.add(editor);
            if (field != PromptField.POV && field != PromptField.TENSE) {
                layout.setColspan(editor, 2);
            }
        }
        return layout;
    }

    /**
     * Puts the values into the editors and works out what an empty one inherits.
     *
     * @param own           what is set where the form is for, a blank text is not set
     * @param inheritedFrom the folder the book or folder is in: the empty fields are inherited from it, the folders above
     *                      it and then the user settings (null: the top level)
     */
    public void load(Function<PromptField, String> own, Folder inheritedFrom) {
        loading = true;
        try {
            for (PromptField field : PromptField.values()) {
                EffectiveSetting<String> fallback = manuscriptService.resolveInherited(inheritedFrom, field);
                inherited.put(field, fallback);
                warnings.remove(field);

                HasValue<?, String> editor = editors.get(field);
                setPlaceholder(editor, fallback.value());
                editor.setValue(StringUtils.defaultString(own.apply(field)));

                Class<? extends TemplateData> dataClass = dataClass(field);
                if (editor instanceof TextAreaPopoverComponent area) {
                    area.setPopoverContent(TemplateHints.create(loc, area, area.getPopover(), dataClass, fallback.value()));
                } else if (editor instanceof TextFieldPopOverComponent line) {
                    line.setPopoverContent(TemplateHints.create(loc, line, line.getPopover(), null, fallback.value()));
                }
                if (dataClass != null) {
                    validate(field);
                }
                refreshHelper(field);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    /**
     * What the editors hold now: every field with its text, blank when it is not set.
     */
    public Map<PromptField, String> values() {
        Map<PromptField, String> values = new EnumMap<>(PromptField.class);
        editors.forEach((field, editor) -> values.put(field, StringUtils.defaultString(editor.getValue())));
        return values;
    }

    /**
     * Checks all the templates, tells the user about the first invalid one and shows the unknown names of the others.
     *
     * @return false if a template is invalid and must not be saved
     */
    public boolean validate() {
        boolean valid = true;
        for (PromptField field : PromptField.values()) {
            if (dataClass(field) != null && !validate(field)) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    private boolean validate(PromptField field) {
        HasValue<?, String> editor = editors.get(field);
        String template = editor.getValue();
        warnings.remove(field);
        try {
            if (StringUtils.isBlank(template)) {
                return true;
            }
            TemplateService.ValidationResult result = templateService.isValidTemplate(template, templateName(field), dataClass(field));
            if (!result.isValid()) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                return false;
            }
            if (result.hasWarnings()) {
                warnings.put(field, loc.getValue(L.MSG_TEMPLATE_UNKNOWN_NAMES) + String.join(", ", result.unknownNames()));
            }
            return true;
        } catch (Exception e) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
            return false;
        } finally {
            refreshHelper(field);
        }
    }

    private void changed(HasValue.ValueChangeEvent<String> event) {
        if (loading) {
            return;
        }
        PromptField field = fieldOf(event.getHasValue());
        if (field != null) {
            refreshHelper(field);
        }
        if (event.isFromClient() && validate() && onChange != null) {
            onChange.run();
        }
    }

    private PromptField fieldOf(HasValue<?, String> editor) {
        for (Map.Entry<PromptField, HasValue<?, String>> entry : editors.entrySet()) {
            if (entry.getValue() == editor) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The line under the field: what it is about, the unknown names of the template and, while the field is empty, where
     * its inherited value comes from.
     */
    private void refreshHelper(PromptField field) {
        List<String> lines = new ArrayList<>();
        if (help(field) != null) {
            lines.add(loc.getValue(help(field)));
        }
        if (warnings.containsKey(field)) {
            lines.add(warnings.get(field));
        }
        EffectiveSetting<String> fallback = inherited.get(field);
        if (fallback != null && StringUtils.isBlank(editors.get(field).getValue())) {
            lines.add(origin(loc, fallback));
        }
        setHelper(editors.get(field), lines.isEmpty() ? null : String.join(" ", lines));
    }

    /**
     * Where an inherited value comes from, as the text under an empty field.
     */
    public static String origin(Localization loc, EffectiveSetting<?> setting) {
        return switch (setting.origin()) {
            case FOLDER -> String.format(loc.getValue(L.LABEL_INHERITED_FROM_FOLDER), setting.folder().getName());
            case USER -> loc.getValue(L.LABEL_INHERITED_FROM_USER);
            default -> loc.getValue(L.LABEL_INHERITED_DEFAULT);
        };
    }

    private static void setPlaceholder(HasValue<?, String> editor, String text) {
        if (editor instanceof TextArea area) {
            area.setPlaceholder(text);
        } else if (editor instanceof TextField line) {
            line.setPlaceholder(text);
        }
    }

    private static void setHelper(HasValue<?, String> editor, String text) {
        if (editor instanceof TextArea area) {
            area.setHelperText(text);
        } else if (editor instanceof TextField line) {
            line.setHelperText(text);
        }
    }
}
