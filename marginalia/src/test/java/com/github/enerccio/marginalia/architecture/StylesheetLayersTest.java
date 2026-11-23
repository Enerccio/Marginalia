package com.github.enerccio.marginalia.architecture;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.MarginaliaCssStyles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The look is split like the classes: the platform's stylesheet ({@code platform-styles.css} in the platform JAR's
 * {@code META-INF/frontend/styles}) has the rules of the classes of {@link CssStyles}, Marginalia's
 * ({@code shared-styles.css}) the rules of {@link MarginaliaCssStyles} and imports the platform's first. A class of
 * one layer styled in the stylesheet of the other would be lost by another application on the platform (or would
 * style it with Marginalia's look), so every name has its rule in its own file and the platform's file names none of
 * Marginalia's. A name is a class ({@code .name}), a part of a component ({@code ::part(name)}), or a prefix that
 * ends with {@code -} ({@code .name-1}).
 */
class StylesheetLayersTest {

    static final Path PLATFORM_CSS = Path.of("platform/src/main/resources/META-INF/frontend/styles/platform-styles.css");
    static final Path APP_CSS = Path.of("src/main/frontend/styles/shared-styles.css");

    /** Names with no rule on purpose, and why. */
    static final Map<String, String> WITHOUT_RULE = Map.of(
            "chat-message-markdown", "the hook the custom CSS of a part style targets (PartStyle), the user writes its rules");

    @Test
    void platformClassesAreStyledByThePlatform() throws IOException {
        String css = rules(PLATFORM_CSS);
        assertThat(unstyled(CssStyles.class, css)).as("CssStyles names without a rule in " + PLATFORM_CSS).isEmpty();
    }

    @Test
    void marginaliaClassesAreStyledByMarginalia() throws IOException {
        String css = rules(APP_CSS);
        assertThat(unstyled(MarginaliaCssStyles.class, css)).as("MarginaliaCssStyles names without a rule in " + APP_CSS)
                .isEmpty();
    }

    @Test
    void thePlatformStylesheetNamesNoMarginaliaClass() throws IOException {
        String css = rules(PLATFORM_CSS);
        Map<String, String> found = new TreeMap<>();
        names(MarginaliaCssStyles.class).forEach((field, name) -> {
            if (styled(css, name)) {
                found.put(field, name);
            }
        });
        assertThat(found).as("Marginalia's classes styled in " + PLATFORM_CSS + " (move the rules to " + APP_CSS + ")")
                .isEmpty();
    }

    @Test
    void marginaliaImportsThePlatformStylesFirst() throws IOException {
        String first = rules(APP_CSS).strip().lines().findFirst().orElse("");
        assertThat(first).isEqualTo("@import \"../generated/jar-resources/styles/platform-styles.css\";");
    }

    private static List<String> unstyled(Class<?> type, String css) {
        List<String> missing = new ArrayList<>();
        names(type).forEach((field, name) -> {
            if (!WITHOUT_RULE.containsKey(name) && !styled(css, name)) {
                missing.add(field + " (" + name + ")");
            }
        });
        return missing;
    }

    private static boolean styled(String css, String name) {
        String quoted = Pattern.quote(name);
        String pattern = name.endsWith("-") ? "\\." + quoted + "[\\w-]" : "(\\.|part\\()" + quoted + "(?![\\w-])";
        return Pattern.compile(pattern).matcher(css).find();
    }

    /** The constant name to the CSS name, of every public static final String of the class. */
    private static Map<String, String> names(Class<?> type) {
        Map<String, String> names = new TreeMap<>();
        for (Field field : type.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType() == String.class) {
                try {
                    names.put(field.getName(), (String) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return names;
    }

    /** The stylesheet without its comments. */
    private static String rules(Path file) throws IOException {
        return Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(Files.readString(file)).replaceAll("");
    }
}
