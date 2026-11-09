package com.github.enerccio.marginalia.extensions.audiobook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * How the text of a book is spelled for the ear, kept with the book ({@code audiobook.spoken} in its attributes): whether the English rules (numbers,
 * abbreviations, roman numeral headings) are on, the word before the number of a chapter, and the pronunciation dictionary.
 */
public class SpokenSettings {

    public enum Rules {
        /** Numbers, abbreviations and numbered headings are read as English words. */
        ENGLISH,
        /** Only the markdown marks and the dictionary: for a book in another language. */
        NONE
    }

    /** One word of the book and what is said for it (also a reading in kana, or a respelling of a made-up word). */
    public static class Entry {
        private String word = "";
        private String spoken = "";

        public Entry() {
        }

        public Entry(String word, String spoken) {
            this.word = word;
            this.spoken = spoken;
        }

        public String getWord() {
            return word == null ? "" : word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getSpoken() {
            return spoken == null ? "" : spoken;
        }

        public void setSpoken(String spoken) {
            this.spoken = spoken;
        }
    }

    public static final String DEFAULT_CHAPTER_WORD = "Chapter";

    private Rules rules = Rules.ENGLISH;
    private String chapterWord = DEFAULT_CHAPTER_WORD;
    private List<Entry> dictionary = new ArrayList<>();

    public Rules getRules() {
        return rules == null ? Rules.ENGLISH : rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    /** What is said before the number of a heading that is only a number ("Chapter" for "IV. The Arch"); blank for nothing. */
    public String getChapterWord() {
        return chapterWord == null ? "" : chapterWord;
    }

    public void setChapterWord(String chapterWord) {
        this.chapterWord = chapterWord;
    }

    public List<Entry> getDictionary() {
        if (dictionary == null) {
            dictionary = new ArrayList<>();
        }
        return dictionary;
    }

    public void setDictionary(List<Entry> dictionary) {
        this.dictionary = dictionary == null ? new ArrayList<>() : new ArrayList<>(dictionary);
    }
}
