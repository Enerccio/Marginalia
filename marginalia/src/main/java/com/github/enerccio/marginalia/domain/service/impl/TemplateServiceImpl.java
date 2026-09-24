package com.github.enerccio.marginalia.domain.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;

public class TemplateServiceImpl implements TemplateService {

    private final MustacheFactory mf = new DefaultMustacheFactory() {
        @Override
        public void encode(String value, Writer writer) {
            try {
                writer.write(value);
            } catch (IOException e) {
                throw new MustacheException("Failed to write unescaped template value", e);
            }
        }
    };

    private final Cache<String, Mustache> templateCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofHours(24))
            .build();

    @Override
    public ValidationResult isValidTemplate(String templateContent, String templateName) throws Exception {
        if (templateContent == null || templateContent.isBlank()) {
            return new ValidationResult(false, "Template cannot be empty.");
        }

        try {
            mf.compile(new StringReader(templateContent), templateName);
            return new ValidationResult(true, null);
        } catch (MustacheException e) {
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return new ValidationResult(false, errorMsg);
        } catch (Exception e) {
            return new ValidationResult(false, "Invalid template structure: " + e.getMessage());
        }
    }

    @Override
    public String processTemplate(String template, String templateName, TemplateData values) throws Exception {
        Mustache mustache = templateCache.get(template, key ->
            mf.compile(new StringReader(key), templateName)
        );

        StringWriter writer = new StringWriter();
        mustache.execute(writer, values).flush();
        return writer.toString();
    }

}
