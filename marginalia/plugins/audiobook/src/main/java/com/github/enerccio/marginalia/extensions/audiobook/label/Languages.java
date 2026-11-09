package com.github.enerccio.marginalia.extensions.audiobook.label;

/** Which language a piece is in when it is not the language of the book: by its script (a piece in kana is Japanese, in Cyrillic Russian). */
public final class Languages {

    private Languages() {
    }

    /**
     * @return the code of the language of the script the text is mostly in when it is not Latin, null for Latin script (the language of the book) or
     * no letters. Han characters alone are taken for Chinese, with kana for Japanese.
     */
    public static String ofScript(String text) {
        int latin = 0, han = 0, kana = 0, hangul = 0, cyrillic = 0, greek = 0, hebrew = 0, arabic = 0, devanagari = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (!Character.isLetter(cp)) {
                continue;
            }
            switch (Character.UnicodeScript.of(cp)) {
                case LATIN -> latin++;
                case HAN -> han++;
                case HIRAGANA, KATAKANA -> kana++;
                case HANGUL -> hangul++;
                case CYRILLIC -> cyrillic++;
                case GREEK -> greek++;
                case HEBREW -> hebrew++;
                case ARABIC -> arabic++;
                case DEVANAGARI -> devanagari++;
                default -> {
                }
            }
        }
        int other = han + kana + hangul + cyrillic + greek + hebrew + arabic + devanagari;
        if (other == 0 || other < latin) {
            return null;
        }
        if (kana > 0) {
            return "ja";
        }
        int most = Math.max(Math.max(Math.max(han, hangul), Math.max(cyrillic, greek)), Math.max(Math.max(hebrew, arabic), devanagari));
        if (most == han) return "zh";
        if (most == hangul) return "ko";
        if (most == cyrillic) return "ru";
        if (most == greek) return "el";
        if (most == hebrew) return "he";
        if (most == arabic) return "ar";
        return "hi";
    }
}
