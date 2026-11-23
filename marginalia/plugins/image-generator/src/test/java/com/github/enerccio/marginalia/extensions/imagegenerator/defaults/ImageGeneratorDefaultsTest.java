package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageGeneratorDefaultsTest {

    private static final List<ImageGeneratorDefaults> TRANSLATIONS = List.of(new ImageGeneratorDefaultsJA(), new ImageGeneratorDefaultsZH(),
            new ImageGeneratorDefaultsFR(), new ImageGeneratorDefaultsES(), new ImageGeneratorDefaultsDE(), new ImageGeneratorDefaultsIT(),
            new ImageGeneratorDefaultsKO());

    private static List<Method> texts() {
        return java.util.Arrays.stream(ImageGeneratorDefaults.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && m.getParameterCount() == 0 && m.getReturnType() == String.class)
                .toList();
    }

    @Test
    void everyTranslationHasEveryText() throws Exception {
        // a word may be the same as in English (German "Text"): what counts is that the language overrides every text
        assertFalse(texts().isEmpty());
        for (ImageGeneratorDefaults translation : TRANSLATIONS) {
            for (Method text : texts()) {
                String name = translation.getClass().getSimpleName() + "." + text.getName();
                assertDoesNotThrow(() -> translation.getClass().getDeclaredMethod(text.getName()), name + " is not translated");
                String value = (String) text.invoke(translation);
                assertFalse(value == null || value.isBlank(), name + " is empty");
            }
        }
    }

    @Test
    void theInstructionsAskForAnEnglishPrompt() {
        // image models read English best: every language asks for it (checked on the English text, which names it)
        ImageGeneratorDefaults english = new ImageGeneratorDefaults();
        assertTrue(english.tagsInstruction().contains("in English"));
        assertTrue(english.sentencesInstruction().contains("in English"));
    }
}
