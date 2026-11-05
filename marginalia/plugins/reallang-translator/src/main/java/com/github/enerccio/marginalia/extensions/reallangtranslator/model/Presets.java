package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import java.util.List;

/**
 * The starting points the language editor offers: a new language begins as one of them and the user changes what they like.
 */
public final class Presets {

    /** What a preset is called and how it fills a language in. */
    public record Preset(String id, String label, java.util.function.Consumer<RealLanguage> fill) {
    }

    static final String JAPANESE_POLITENESS = "Choose the level of speech from the relationship of the speaker and the listener (age, rank, closeness, the place): the plain "
            + "form between close equals, family and children; the polite desu/masu form with strangers, colleagues and equals; the honorific and humble forms (keigo) to "
            + "superiors, customers and elders. Keep the same level for the same pair of people throughout, and change it only when the relationship or the mood changes. "
            + "Let the pronouns, the sentence-final particles and the word choice carry the gender and the voice of the speaker.";

    static final String JAPANESE_NAMES = "Names of people and places keep their identity: a Japanese name is written the way a Japanese text would write it, a foreign name "
            + "in katakana (or in Latin letters when the script is romaji). Family name first for Japanese characters unless the story orders the names otherwise.";

    private Presets() {
    }

    public static List<Preset> all() {
        return List.of(
                new Preset("ja-kana-kanji", "Japanese (kana and kanji)", language -> japanese(language, "Japanese", Script.KANA_KANJI)),
                new Preset("ja-kana", "Japanese (kana only)", language -> japanese(language, "Japanese (kana only)", Script.KANA)),
                new Preset("ja-romaji", "Japanese (romaji)", language -> {
                    japanese(language, "Japanese (romaji)", Script.ROMAJI);
                    language.setRules("Write the Japanese in Hepburn romaji with long vowels marked by a macron (ō, ū), words separated as in a romaji text, no kana and no kanji.");
                }),
                new Preset("other", "Another language", language -> {
                    language.setName("");
                    language.setScript(Script.NATIVE);
                    language.setReading(false);
                    language.setPoliteness("");
                    language.setNames("");
                    language.setRules("");
                }));
    }

    private static void japanese(RealLanguage language, String name, Script script) {
        language.setName(name);
        language.setTongue("Japanese");
        language.setScript(script);
        language.setHonorifics(true);
        language.setReading(script != Script.ROMAJI);
        language.setPoliteness(JAPANESE_POLITENESS);
        language.setNames(JAPANESE_NAMES);
        language.setRules(script == Script.KANA ? "Write in hiragana and katakana only: no kanji." : "");
    }

    /**
     * A new language made from the preset with the id.
     */
    public static RealLanguage make(String presetId) {
        RealLanguage language = new RealLanguage();
        all().stream().filter(preset -> preset.id().equals(presetId)).findFirst().ifPresent(preset -> preset.fill().accept(language));
        return language;
    }
}
