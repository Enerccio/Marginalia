package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The translations of the built-in prompts: each language translates every text, and keeps the Handlebars tags and the
 * {@code String.format} placeholders of the English one (a lost {@code {{/selection}}} or {@code %d} breaks the prompt).
 */
class DefaultsTranslationTest {

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

    private static List<String> all(Pattern pattern, String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }

    private static List<String> blocks(List<String> tags) {
        return tags.stream().filter(tag -> tag.matches("\\{\\{\\s*([#/^]|else).*")).toList();
    }

    @Test
    void everyLanguageTranslatesEveryText() throws Exception {
        assertThat(texts()).hasSizeGreaterThan(40);
        for (Class<? extends Defaults> translation : List.of(DefaultsJA.class, DefaultsFR.class,
                DefaultsES.class, DefaultsDE.class, DefaultsIT.class, DefaultsKO.class)) {
            for (Method text : texts()) {
                assertThat(translation.getDeclaredMethod(text.getName()))
                        .as("%s translates %s", translation.getSimpleName(), text.getName()).isNotNull();
            }
        }
    }

    @Test
    void translationsKeepTagsAndPlaceholders() throws Exception {
        Defaults english = new Defaults();
        for (Defaults translation : Defaults.builtIn()) {
            for (Method text : texts()) {
                String original = (String) text.invoke(english);
                String translated = (String) text.invoke(translation);
                assertThat(translated).as("%s %s", translation.getLanguage(), text.getName()).isNotBlank();
                // the sections open and close in the same order, the values may move with the word order of the language
                assertThat(blocks(all(TAG, translated))).as("sections of %s %s", translation.getLanguage(), text.getName())
                        .isEqualTo(blocks(all(TAG, original)));
                assertThat(all(TAG, translated)).as("tags of %s %s", translation.getLanguage(), text.getName())
                        .containsExactlyInAnyOrderElementsOf(all(TAG, original));
                if (!text.getName().startsWith("agent")) {
                    // the prompts are templates, not format strings ("by 50% by" is no placeholder)
                    continue;
                }
                assertThat(all(FORMAT, translated)).as("placeholders of %s %s", translation.getLanguage(), text.getName())
                        .containsExactlyInAnyOrderElementsOf(all(FORMAT, original));
            }
        }
    }

    @Test
    void noticesStartWithTheirPrefix() {
        for (Defaults defaults : Defaults.builtIn()) {
            for (String notice : List.of(defaults.agentContextWarning(), defaults.agentContextCritical(),
                    defaults.agentNoStartCheckpoint(), defaults.agentNoAutomaticCheckpoint(), defaults.agentRestored(),
                    defaults.agentRewound(), defaults.agentSubagentsStillWorking(), defaults.agentTooManyCalls(),
                    defaults.agentRepeatingNotice(), defaults.agentRepeatingNoTools(), defaults.agentOneCallNotice(),
                    defaults.agentSingleCallNotice())) {
                assertThat(notice).as(defaults.getLanguage().code()).startsWith(defaults.agentNoticePrefix());
            }
        }
    }

    @Test
    void languagesAreDistinct() {
        assertThat(Defaults.builtIn().stream().map(Defaults::getLanguage).distinct()).hasSize(Defaults.builtIn().size());
        assertThat(Defaults.builtIn().getFirst().getLanguage()).isEqualTo(Language.EN);
    }
}
