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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TemplateServiceImpl implements TemplateService, InitializingBean {

    private static final Pattern ROOT_NAME = Pattern.compile("[./\\[]");

    private Handlebars handlebars;

    private final Cache<String, CompiledTemplate> templateCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofHours(24))
            .build();

    private record CompiledTemplate(Template template, MacroTranslator.Result translated, String key) {
    }

    @Override
    public ValidationResult isValidTemplate(String templateContent, String templateName) throws Exception {
        return isValidTemplate(templateContent, templateName, null);
    }

    @Override
    public ValidationResult isValidTemplate(String templateContent, String templateName, Class<? extends TemplateData> dataClass) throws Exception {
        if (templateContent == null || templateContent.isBlank()) {
            return new ValidationResult(false, "Template cannot be empty.");
        }

        try {
            CompiledTemplate compiled = compile(templateContent);
            List<String> unknownNames = dataClass == null ? List.of() : findUnknownNames(compiled.template(), dataClass);
            return new ValidationResult(true, null, unknownNames);
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

    /**
     * Names of variables and sections that would end in the missing helper (rendered as "Error"). Checked statically,
     * so names in branches that are not rendered are found too. Paths are checked by their first segment, names relative
     * to the current context ({{this}}, {{.}}, {{../x}}, {{@index}}) are skipped since they depend on the data being
     * iterated.
     */
    private List<String> findUnknownNames(Template template, Class<? extends TemplateData> dataClass) throws Exception {
        TemplateData sample = dataClass.getDeclaredConstructor().newInstance();
        Set<String> unknown = new LinkedHashSet<>();
        for (String name : template.collect(TagType.VAR, TagType.TRIPLE_VAR, TagType.SECTION)) {
            String root = rootName(name);
            if (root == null || handlebars.helper(root) != null || MacroHelpers.resolves(sample, root)) {
                continue;
            }
            unknown.add(name);
        }
        return new ArrayList<>(unknown);
    }

    private static String rootName(String name) {
        if (name == null) {
            return null;
        }
        String n = name.strip();
        if (n.isEmpty() || n.startsWith(".") || n.startsWith("@") || n.equals("this") || n.startsWith("this.") || n.startsWith("this/")) {
            return null;
        }
        String root = ROOT_NAME.split(n, 2)[0];
        return root.isEmpty() ? null : root;
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
