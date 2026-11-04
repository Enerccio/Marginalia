package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Conlang Translator in the languages besides English; the English text is the key.
 */
public class ConlangTranslatorLocalization extends PluginLocalizationBundle {

    public static final ConlangTranslatorLocalization T = new ConlangTranslatorLocalization();

    public static final String TRANSLATE_THE_SPEECH = "Translate the speech";
    public static final String TRANSLATING_THE_SPEECH = "Translating the speech of the part...";
    public static final String TRANSLATED_SPEECHES = "Translated %d speech(es), %d new word(s) in the lexicon";
    public static final String SOME_WORDS_NOT_IN_THE_LEXICON = "%d translation(s) use words that are not in the lexicon and were not reported";
    public static final String NO_SPEECH_IN_THE_LANGUAGE = "No speech of the part is spoken in the language";
    public static final String THE_BOOK_HAS_NO_LANGUAGE = "The book has no language to translate into";
    public static final String THE_BOOK_HAS_NO_MODEL = "There is no model for the translation: the book has none and neither have your defaults";
    public static final String THE_PART_HAS_NO_TEXT = "The part has no text";
    public static final String THE_MODEL_ANSWERED_WRONG = "The model did not answer in the expected form";
    public static final String THE_TRANSLATION_FAILED = "The translation failed: %s";
    public static final String THE_PART_IS_BEING_TRANSLATED = "The part is being translated already";

    @Override
    protected void loadKeys() {
        ja(TRANSLATE_THE_SPEECH, "台詞を翻訳");
        zh(TRANSLATE_THE_SPEECH, "翻译对白");

        ja(TRANSLATING_THE_SPEECH, "パートの台詞を翻訳中...");
        zh(TRANSLATING_THE_SPEECH, "正在翻译本段对白...");

        ja(TRANSLATED_SPEECHES, "台詞を%d件翻訳し、語彙に新しい単語を%d個追加しました");
        zh(TRANSLATED_SPEECHES, "已翻译 %d 句对白,词汇表新增 %d 个词");

        ja(SOME_WORDS_NOT_IN_THE_LEXICON, "%d件の翻訳に、語彙になく報告もされていない単語があります");
        zh(SOME_WORDS_NOT_IN_THE_LEXICON, "%d 句翻译使用了词汇表中没有且未报告的词");

        ja(NO_SPEECH_IN_THE_LANGUAGE, "このパートにこの言語の台詞はありません");
        zh(NO_SPEECH_IN_THE_LANGUAGE, "本段没有使用该语言的对白");

        ja(THE_BOOK_HAS_NO_LANGUAGE, "この本には翻訳先の言語がありません");
        zh(THE_BOOK_HAS_NO_LANGUAGE, "这本书没有可翻译成的语言");

        ja(THE_BOOK_HAS_NO_MODEL, "翻訳用のモデルがありません。本にもデフォルトにも設定されていません");
        zh(THE_BOOK_HAS_NO_MODEL, "没有可用于翻译的模型:书和默认设置都没有指定");

        ja(THE_PART_HAS_NO_TEXT, "パートにテキストがありません");
        zh(THE_PART_HAS_NO_TEXT, "本段没有文本");

        ja(THE_MODEL_ANSWERED_WRONG, "モデルが想定外の形式で回答しました");
        zh(THE_MODEL_ANSWERED_WRONG, "模型没有按预期格式回答");

        ja(THE_TRANSLATION_FAILED, "翻訳に失敗しました: %s");
        zh(THE_TRANSLATION_FAILED, "翻译失败:%s");

        ja(THE_PART_IS_BEING_TRANSLATED, "このパートはすでに翻訳中です");
        zh(THE_PART_IS_BEING_TRANSLATED, "本段正在翻译");
    }
}
