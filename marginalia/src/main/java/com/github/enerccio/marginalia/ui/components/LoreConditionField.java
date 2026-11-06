package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

/**
 * The field of the condition of a lorebook entry (the desktop and the phone view): a template that has to give a true
 * value for the entry to be used. A condition that is not a valid template, or uses a name that does not exist, is shown
 * as invalid; it is still saved (the author may be in the middle of editing), the entry then counts as not used.
 */
final class LoreConditionField {

    private LoreConditionField() {
    }

    /**
     * @param save saves the entry after the condition was changed by the user
     */
    static TextArea create(Localization loc, TemplateService templateService, LorebookEntry entry, Runnable save) {
        TextArea field = new TextArea(loc.getValue(L.LABEL_ACTIVATION_CONDITION));
        field.setWidthFull();
        ResizableTextArea.install(loc, field, "60px");
        field.setValue(StringUtils.defaultString(entry.getActivationCondition()));
        UIUtils.addTooltip(field, loc.getValue(L.HELP_ACTIVATION_CONDITION));
        validate(loc, templateService, field);
        field.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                validate(loc, templateService, field);
                entry.setActivationCondition(e.getValue());
                save.run();
            }
        });
        return field;
    }

    private static void validate(Localization loc, TemplateService templateService, TextArea field) {
        String condition = field.getValue();
        if (StringUtils.isBlank(condition)) {
            field.setInvalid(false);
            return;
        }
        try {
            TemplateService.ValidationResult result = templateService.isValidTemplate(condition, "lorebookCondition", LorebookTemplateData.class);
            if (!result.isValid()) {
                field.setErrorMessage(loc.getValue(L.MSG_INVALID_TEMPLATE) + result.errorMessage());
            } else if (result.hasWarnings()) {
                field.setErrorMessage(loc.getValue(L.MSG_TEMPLATE_UNKNOWN_NAMES) + String.join(", ", result.unknownNames()));
            }
            field.setInvalid(!result.isValid() || result.hasWarnings());
        } catch (Exception e) {
            field.setErrorMessage(loc.getValue(L.MSG_INVALID_TEMPLATE) + e.getMessage());
            field.setInvalid(true);
        }
    }
}
