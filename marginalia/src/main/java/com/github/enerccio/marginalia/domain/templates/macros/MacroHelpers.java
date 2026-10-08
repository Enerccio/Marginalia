package com.github.enerccio.marginalia.domain.templates.macros;

import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import com.github.enerccio.marginalia.domain.templates.macros.Macros.MacroCall;
import com.github.enerccio.marginalia.domain.templates.macros.Macros.MacroDefinition;
import com.github.jknack.handlebars.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Handlebars helpers that evaluate macros produced by {@link MacroTranslator}. The actual macro logic lives in
 * {@link TemplateData}; helpers only unpack arguments.
 */
public final class MacroHelpers {

    public static final String DATA_TEMPLATE_DATA = "marginalia.templateData";
    public static final String DATA_LITERALS = "marginalia.literals";
    public static final String DATA_TEMPLATE_KEY = "marginalia.templateKey";

    private static final String MISSING_VALUE = "Error";
    private static final Pattern TRIM = Pattern.compile("(?:\\r?\\n)*" + Pattern.quote(TemplateData.TRIM_MARKER) + "(?:\\r?\\n)*");

    private MacroHelpers() {
    }

    /**
     * Prepares handlebars context for rendering of translated template.
     */
    public static Context createContext(TemplateData data, MacroTranslator.Result translated, String templateKey) {
        List<ValueResolver> resolvers = new ArrayList<>();
        resolvers.add(TemplateDataValueResolver.INSTANCE);
        resolvers.addAll(ValueResolver.defaultValueResolvers());
        Context context = Context.newBuilder(data).resolver(resolvers.toArray(ValueResolver[]::new)).build();
        context.data(DATA_TEMPLATE_DATA, data);
        context.data(DATA_LITERALS, translated.literals());
        context.data(DATA_TEMPLATE_KEY, templateKey);
        return context;
    }

    /**
     * Applies output post-processing ({{trim}}).
     */
    public static String postProcess(String output) {
        if (output == null || !output.contains(TemplateData.TRIM_MARKER)) {
            return output;
        }
        return TRIM.matcher(output).replaceAll("");
    }

    public static void register(Handlebars handlebars) {
        for (MacroDefinition definition : Macros.all()) {
            if (Macros.IF.equals(definition.name())) {
                continue;
            }
            handlebars.registerHelper(Macros.helperName(definition.name()), (Object site, Options options) -> {
                List<String> args = new ArrayList<>();
                for (Object param : options.params) {
                    args.add(str(param));
                }
                if (options.tagType == TagType.SECTION) {
                    args.add(str(options.fn()));
                }
                MacroCall call = new MacroCall(site(site), Objects.toString(options.data(DATA_TEMPLATE_KEY), ""), args);
                return nullToEmpty(definition.function().apply(data(options), call));
            });
        }

        handlebars.registerHelper(MacroTranslator.LIT, (Object index, Options options) -> {
            List<String> literals = options.data(DATA_LITERALS);
            int i = site(index);
            if (literals == null || i < 0 || i >= literals.size()) {
                return "";
            }
            return literals.get(i);
        });

        handlebars.registerHelper(MacroTranslator.CONCAT, (Object site, Options options) -> {
            StringBuilder sb = new StringBuilder();
            for (Object param : options.params) {
                sb.append(str(param));
            }
            return sb.toString();
        });

        handlebars.registerHelper(MacroTranslator.PROP, (Object site, Options options) ->
                str(data(options).resolveProperty(str(options.param(0, "")))));

        handlebars.registerHelper(MacroTranslator.COND, (Object site, Options options) -> {
            String name = str(options.param(0, ""));
            TemplateData data = data(options);
            MacroDefinition definition = Macros.find(name);
            if (definition != null && definition.minArgs() == 0 && !Macros.IF.equals(definition.name())) {
                return nullToEmpty(definition.function().apply(data, new MacroCall(site(site),
                        Objects.toString(options.data(DATA_TEMPLATE_KEY), ""), List.of())));
            }
            if (data.hasProperty(name)) {
                return data.resolveProperty(name);
            }
            // plain text is truthy
            return name;
        });

        handlebars.registerHelper(MacroTranslator.IF, (Object site, Options options) -> {
            boolean negate = TemplateData.isTruthy(options.param(1, false));
            boolean truthy = TemplateData.isTruthy(options.param(0, null)) ^ negate;
            if (options.tagType == TagType.SECTION) {
                return truthy ? options.fn() : options.inverse();
            }
            return truthy ? str(options.param(2, "")) : "";
        });

        handlebars.registerHelper(MacroTranslator.VAR, (Object site, Options options) -> {
            Scope scope = "global".equals(str(options.param(0, ""))) ? Scope.GLOBAL : Scope.LOCAL;
            String name = str(options.param(1, ""));
            String operator = str(options.param(2, ""));
            return nullToEmpty(data(options).variableShorthand(scope, name, operator, () -> {
                try {
                    if (options.tagType == TagType.SECTION) {
                        return str(options.fn());
                    }
                    return str(options.param(3, ""));
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }));
        });

        // unknown variables are reported in the output, so typos in templates are visible
        handlebars.registerHelperMissing((Object context, Options options) -> MISSING_VALUE);
    }

    /**
     * Resolves properties of {@link TemplateData}: declared properties with null value resolve to empty string (instead
     * of being reported as missing) and properties of the shared {@link com.github.enerccio.marginalia.domain.templates.TemplateContext}
     * are available in every template ({{povCharacter}}, {{manuscriptName}}...).
     */
    private enum TemplateDataValueResolver implements ValueResolver {
        INSTANCE;

        @Override
        public Object resolve(Object context, String name) {
            if (context instanceof TemplateData data) {
                if (data.hasProperty(name)) {
                    Object value = data.resolveProperty(name);
                    return value == null ? "" : value;
                }
                // argument-less macros as values, so {{#if user}} works like {{if user}}
                MacroDefinition definition = Macros.find(name);
                if (definition != null && definition.minArgs() == 0 && !Macros.IF.equals(definition.name())) {
                    return nullToEmpty(definition.function().apply(data, new MacroCall(-1, "", List.of())));
                }
                // don't let the default resolvers reflect into TemplateData methods ({{templateContext}})
                return null;
            }
            return UNRESOLVED;
        }

        @Override
        public Object resolve(Object context) {
            return UNRESOLVED;
        }

        @Override
        public Set<Map.Entry<String, Object>> propertySet(Object context) {
            return Collections.emptySet();
        }
    }

    private static TemplateData data(Options options) {
        TemplateData data = options.data(DATA_TEMPLATE_DATA);
        if (data == null) {
            throw new IllegalStateException("Template rendered without template data");
        }
        return data;
    }

    private static int site(Object site) {
        if (site instanceof Number number) {
            return number.intValue();
        }
        return -1;
    }

    private static String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Object nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
