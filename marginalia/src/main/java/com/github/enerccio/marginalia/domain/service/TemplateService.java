package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

import java.util.List;

public interface TemplateService {

    ValidationResult isValidTemplate(String template, String templateName) throws Exception;

    /**
     * Validates template like {@link #isValidTemplate(String, String)} and also reports names the template uses that
     * are not defined for given template data (template variables, shared context properties, argument-less macros
     * and helpers). Such names render as "Error", so they are most likely typos. They are reported in
     * {@link ValidationResult#unknownNames()} and don't make the template invalid.
     *
     * @param dataClass template data the template is rendered with, null skips the unknown names check
     */
    ValidationResult isValidTemplate(String template, String templateName, Class<? extends TemplateData> dataClass) throws Exception;

    String processTemplate(String template, String templateName, TemplateData values) throws Exception;

    /**
     * @param unknownNames names that are not defined for the template data and render as "Error" (warnings)
     */
    record ValidationResult(boolean isValid, String errorMessage, List<String> unknownNames) {

        public ValidationResult(boolean isValid, String errorMessage) {
            this(isValid, errorMessage, List.of());
        }

        public ValidationResult {
            unknownNames = unknownNames == null ? List.of() : List.copyOf(unknownNames);
        }

        public boolean hasWarnings() {
            return !unknownNames.isEmpty();
        }
    }
}
