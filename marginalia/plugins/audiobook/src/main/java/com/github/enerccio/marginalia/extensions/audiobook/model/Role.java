package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Who reads: a voice, and the voices to use instead when the piece is read in a mood (a whisper, a shout) or is in another language (a Japanese
 * clip). A voice is the text form of a {@code VoiceRef}: {@code predefined:Henry.wav} or {@code clip:<resource uuid>}.
 */
public class Role {

    private static final String MOOD = "mood:";
    private static final String LANGUAGE = "lang:";

    private String voice;
    /** {@code lang:ja} and {@code mood:WHISPER} to a voice. */
    private Map<String, String> variants = new LinkedHashMap<>();

    public String getVoice() {
        return voice == null ? "" : voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public Map<String, String> getVariants() {
        if (variants == null) {
            variants = new LinkedHashMap<>();
        }
        return variants;
    }

    public void setVariants(Map<String, String> variants) {
        this.variants = variants == null ? new LinkedHashMap<>() : new LinkedHashMap<>(variants);
    }

    public static String moodKey(String mood) {
        return MOOD + mood.toUpperCase(java.util.Locale.ROOT);
    }

    public static String languageKey(String language) {
        return LANGUAGE + language.toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isMoodKey(String key) {
        return key.startsWith(MOOD);
    }

    public static boolean isLanguageKey(String key) {
        return key.startsWith(LANGUAGE);
    }

    /**
     * The voice for a piece: the one of its language when there is one (a clip that speaks Japanese), else the one of its mood, else the voice
     * of the role.
     *
     * @param mood     the name of the delivery, null or {@code NEUTRAL} for none
     * @param language the code of the language when it is not the one of the book, else null
     * @return the voice, blank when the role has none for this piece
     */
    public String pick(String mood, String language) {
        if (language != null && !language.isBlank()) {
            String voice = variant(languageKey(language));
            if (voice != null) {
                return voice;
            }
        }
        if (mood != null && !mood.isBlank() && !"NEUTRAL".equalsIgnoreCase(mood)) {
            String voice = variant(moodKey(mood));
            if (voice != null) {
                return voice;
            }
        }
        return getVoice().strip();
    }

    private String variant(String key) {
        String voice = getVariants().get(key);
        return voice == null || voice.isBlank() ? null : voice.strip();
    }
}
