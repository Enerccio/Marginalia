package com.github.enerccio.marginalia.extensions.statetracker.extract;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The translations of the built-in prompts: each translates every text and keeps the Handlebars tags and the format placeholders
 * of the English one.
 */
class StateTrackerDefaultsTranslationTest {

    private static final Pattern TAG = Pattern.compile("\\{\\{[^}]*}}");
    private static final Pattern JSON = Pattern.compile("\\{\"(clock|characters)\":\\[.*?]}(?=[。.]|$)");
    // the words the plugin parses or writes into the request: they stay English in every language
    // (the list markers are compared with the colon normalized: "single :" in French, "single：" in Japanese and Chinese)
    private static final List<String> KEPT = List.of("- single:", "- list:", "- description:", "\"set\"", "\"clear\"", "\"add\"", "\"remove\"",
            "strict", "(stays)", "- none:", "- advance:", "- day:", "- date:", "- flashback:", "- drop:", "- update:", "- merge:",
            "\"approximate\": true", "dawn, morning, noon, afternoon, evening, night");

    private static String markers(String text) {
        return text.replace("：", ":").replaceAll("(?m)^(\\s*- \\w+) :", "$1:");
    }

    private static final Pattern FORMAT = Pattern.compile("%(\\d+\\$)?[-#+ 0,(]*\\d*(\\.\\d+)?[sdf]");

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

    // the texts: the methods that return a String, without arguments or with one enum argument (each of its constants)
    private static List<Object[]> calls() {
        List<Object[]> calls = new ArrayList<>();
        for (Method method : StateTrackerDefaults.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers()) || method.getReturnType() != String.class) {
                continue;
            }
            if (method.getParameterCount() == 0) {
                calls.add(new Object[]{method, null});
            } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isEnum()) {
                for (Object constant : method.getParameterTypes()[0].getEnumConstants()) {
                    calls.add(new Object[]{method, constant});
                }
            }
        }
        return calls;
    }

    private static String call(Object target, Object[] call) throws Exception {
        Method method = (Method) call[0];
        return call[1] == null ? (String) method.invoke(target) : (String) method.invoke(target, call[1]);
    }

    @Test
    void translationsOverrideEveryTextAndKeepTheTags() throws Exception {
        StateTrackerDefaults english = new StateTrackerDefaults();
        assertFalse(calls().isEmpty());
        for (StateTrackerDefaults translation : List.of(new StateTrackerDefaultsJA(), new StateTrackerDefaultsZH(), new StateTrackerDefaultsFR(),
                new StateTrackerDefaultsES(), new StateTrackerDefaultsDE(), new StateTrackerDefaultsIT(), new StateTrackerDefaultsKO())) {
            String language = translation.getClass().getSimpleName();
            for (Object[] call : calls()) {
                Method method = (Method) call[0];
                assertNotNull(translation.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()),
                        language + " translates " + method.getName());
                String original = call(english, call);
                String translated = call(translation, call);
                assertNotEquals(original, translated, language + " " + method.getName());
                // the sections open and close in the same order, the values may move with the word order of the language
                assertEquals(blocks(all(TAG, original)), blocks(all(TAG, translated)), "sections of " + language + " " + method.getName());
                assertEquals(all(TAG, original).stream().sorted().toList(), all(TAG, translated).stream().sorted().toList(),
                        "tags of " + language + " " + method.getName());
                assertEquals(all(FORMAT, original).stream().sorted().toList(), all(FORMAT, translated).stream().sorted().toList(),
                        "placeholders of " + language + " " + method.getName());
                assertEquals(all(JSON, original), all(JSON, translated), "answer format of " + language + " " + method.getName());
                for (String kept : KEPT) {
                    assertEquals(markers(original).contains(kept), markers(translated).contains(kept), "'" + kept + "' in " + language + " " + method.getName());
                }
            }
        }
    }
}
