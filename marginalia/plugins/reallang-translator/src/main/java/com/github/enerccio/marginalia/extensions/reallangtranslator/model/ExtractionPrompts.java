package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The prompts that come with the plugin, always offered next to the ones of the user's library. They are code, not data of the user: the user copies one
 * into the library to change it.
 */
public final class ExtractionPrompts {

    /** The id of the built-in prompt for Japanese. */
    public static final String JAPANESE = "builtin:japanese-names-and-terms";

    private static final String JAPANESE_TEXT = """
            You are a professional literary Japanese translator and localizer. Go through the MANUSCRIPT and extract every proper noun and coined term that needs one fixed Japanese form, so that the whole book writes it the same way.

            Categorize every term and give its Japanese form by these definitions:
            • Real-world non-Japanese proper nouns (USA, UN, Mississippi River): use the established Japanese form (アメリカ, 国連, ミシシッピ川). Do not spell out letters phonetically in katakana.
            • Fictional non-Japanese proper nouns (Belmont): transliterate phonetically into katakana (ベルモント).
            • Proper nouns of Japanese origin (Akane): kanji, with the hiragana reading as the reading (written 茜, reading あかね). Leave English titles like "Kingdom of" out of the kanji unless they are part of the proper name itself.
            • Fictional coined terms with an existing concept (Familiar): when there is a direct, natural Japanese translation, use kanji (使い魔).
            • Fictional coined terms and gikun (the specific magic systems and other concepts of the world): a unique concept of the world is written in kanji with the katakana reading as the reading (written 魔力結合, reading ファミリア・ボンド). Even when such a term contains a proper noun, prefer gikun.

            When a term falls into a gray area, weigh it silently and give only the result; no reasoning, no greetings.""";

    private ExtractionPrompts() {
    }

    /**
     * The prompts that come with the plugin.
     */
    public static List<ExtractionPrompt> builtIn() {
        List<ExtractionPrompt> prompts = new ArrayList<>();
        prompts.add(new ExtractionPrompt(JAPANESE, "Japanese: names and special terms (kanji, katakana, gikun)", JAPANESE_TEXT));
        return prompts;
    }

    /**
     * Whether the id is one of the built-in prompts.
     */
    public static boolean isBuiltIn(String id) {
        return builtIn().stream().anyMatch(prompt -> prompt.getId().equals(id));
    }
}
