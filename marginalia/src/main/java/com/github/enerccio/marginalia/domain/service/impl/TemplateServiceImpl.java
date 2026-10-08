package com.github.enerccio.marginalia.domain.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;

public class TemplateServiceImpl implements TemplateService, InitializingBean {

    private Handlebars handlebars;

    private final Cache<String, Template> templateCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofHours(24))
            .build();

    @Override
    public ValidationResult isValidTemplate(String templateContent, String templateName) throws Exception {
        if (templateContent == null || templateContent.isBlank()) {
            return new ValidationResult(false, "Template cannot be empty.");
        }

        try {
            handlebars.compileInline(templateContent);
            return new ValidationResult(true, null);
        } catch (HandlebarsException e) {
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return new ValidationResult(false, errorMsg);
        } catch (Exception e) {
            return new ValidationResult(false, "Invalid template structure: " + e.getMessage());
        }
    }

    @Override
    public String processTemplate(String template, String templateName, TemplateData values) throws Exception {
        Template t = templateCache.get(template, key ->
                {
                    try {
                        return handlebars.compileInline(key);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        StringWriter writer = new StringWriter();
        t.apply(values, writer);
        return writer.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        TemplateLoader templateLoader = new ClassPathTemplateLoader();
        handlebars = new Handlebars(templateLoader);
        handlebars.registerHelperMissing((_, _) -> "Error");
    }
}
