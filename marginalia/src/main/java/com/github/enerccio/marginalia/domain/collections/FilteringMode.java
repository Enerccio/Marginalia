package com.github.enerccio.marginalia.domain.collections;

public enum FilteringMode {

    /**
     * The filter is a list of keywords separated by commas; the entry is used when any of them is found.
     */
    KEYWORDS_OR,
    /**
     * The filter is a list of keywords separated by commas; the entry is used when all of them are found.
     */
    KEYWORDS_AND,
    /**
     * The whole filter is one text, found in the prompt (case-insensitive).
     */
    TEXT,
    /**
     * The filter is a regular expression.
     */
    REGEX

}
