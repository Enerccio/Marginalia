package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

public interface TemplateService {

    ValidationResult isValidTemplate(String template, String templateName) throws Exception;
    String processTemplate(String template, String templateName, TemplateData values) throws Exception;

    record ValidationResult(boolean isValid, String errorMessage) {

    }
}
