package com.github.enerccio.marginalia.extensions.chineselocalization;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.loc.Language;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Chinese built-in prompts translate every text of {@link Defaults} and keep its Handlebars tags and, in the notices
 * of the agents, its {@code String.format} placeholders (the same checks as the application's
 * {@code DefaultsTranslationTest} does for its own languages).
 */
class DefaultsZHTest {

    private static final Pattern TAG = Pattern.compile("\\{\\{[^}]*}}");
    private static final Pattern FORMAT = Pattern.compile("%(\\d+\\$)?[-#+ 0,(]*\\d*(\\.\\d+)?[a-zA-Z%]");
    private static final Set<String> NOT_TEXTS = Set.of("getLanguage", "getDisplayName");

    private static List<Method> texts() {
        List<Method> texts = new ArrayList<>();
        for (Method method : Defaults.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())
                    && method.getParameterCount() == 0 && method.getReturnType() == String.class
                    && !NOT_TEXTS.contains(method.getName())) {
                texts.add(method);
            }
        }
        return texts;
    }

    private static List<String> sorted(Pattern pattern, String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        found.sort(String::compareTo);
        return found;
    }

    @Test
    void translatesEveryText() {
        assertTrue(texts().size() > 40);
        for (Method text : texts()) {
            assertDoesNotThrow(() -> DefaultsZH.class.getDeclaredMethod(text.getName()), text.getName());
        }
    }

    @Test
    void keepsTagsAndPlaceholders() throws Exception {
        Defaults english = new Defaults();
        Defaults chinese = new DefaultsZH();
        for (Method text : texts()) {
            String original = (String) text.invoke(english);
            String translated = (String) text.invoke(chinese);
            assertFalse(translated.isBlank(), text.getName());
            assertEquals(sorted(TAG, original), sorted(TAG, translated), "tags of " + text.getName());
            if (text.getName().startsWith("agent")) {
                assertEquals(sorted(FORMAT, original), sorted(FORMAT, translated), "placeholders of " + text.getName());
            }
        }
    }

    @Test
    void isOfferedByTheExtension() {
        List<Defaults> prompts = new ChineseLocalizationExtension().getPromptDefaults();
        assertEquals(1, prompts.size());
        assertEquals(Language.of("zh"), prompts.getFirst().getLanguage());
    }
}
