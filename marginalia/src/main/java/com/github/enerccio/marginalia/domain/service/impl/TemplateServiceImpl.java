package com.github.enerccio.marginalia.domain.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.macros.MacroHelpers;
import com.github.enerccio.marginalia.domain.templates.macros.MacroTranslator;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;

public class TemplateServiceImpl implements TemplateService, InitializingBean {

    private Handlebars handlebars;

    private final Cache<String, CompiledTemplate> templateCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofHours(24))
            .build();

    private record CompiledTemplate(Template template, MacroTranslator.Result translated, String key) {
    }

    @Override
    public ValidationResult isValidTemplate(String templateContent, String templateName) throws Exception {
        if (templateContent == null || templateContent.isBlank()) {
            return new ValidationResult(false, "Template cannot be empty.");
        }

        try {
            compile(templateContent);
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
        if (template == null) {
            return "";
        }

        CompiledTemplate compiled;
        try {
            compiled = templateCache.get(template, key -> {
                try {
                    return compile(key);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        Context context = MacroHelpers.createContext(values, compiled.translated(), compiled.key());
        try {
            return MacroHelpers.postProcess(compiled.template().apply(context));
        } finally {
            context.destroy();
        }
    }

    private CompiledTemplate compile(String template) throws IOException {
        MacroTranslator.Result translated = MacroTranslator.translate(template);
        Template compiled = handlebars.compileInline(translated.source());
        return new CompiledTemplate(compiled, translated, Integer.toHexString(template.hashCode()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        TemplateLoader templateLoader = new ClassPathTemplateLoader();
        // templates produce LLM prompts, not HTML - never escape quotes, ampersands etc.
        handlebars = new Handlebars(templateLoader).with(EscapingStrategy.NOOP);
        MacroHelpers.register(handlebars);
    }
}
