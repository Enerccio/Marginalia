package com.github.enerccio.marginalia.extensions.reallangtranslator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Real Language Translator in the languages besides English; the English text is the key.
 */
public class ReallangTranslatorLocalization extends PluginLocalizationBundle {

    public static final ReallangTranslatorLocalization T = new ReallangTranslatorLocalization();

    public static final String REAL_LANGUAGE_TRANSLATOR = "Real Language Translator";

    @Override
    protected void loadKeys() {
        ja(REAL_LANGUAGE_TRANSLATOR, "実在言語トランスレーター");
        zh(REAL_LANGUAGE_TRANSLATOR, "现实语言翻译器");
    }
}
