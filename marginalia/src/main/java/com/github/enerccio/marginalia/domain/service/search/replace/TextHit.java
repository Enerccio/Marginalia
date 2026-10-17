package com.github.enerccio.marginalia.domain.service.search.replace;

/**
 * One match of a {@link TextSearch} in a text: the text around it and what the replacement turns it into.
 *
 * @param field       name of the field of the entity the text is from
 * @param before      a few words before the match, on one line
 * @param match       the matched text, as it is in the text
 * @param after       a few words after the match, on one line
 * @param replacement what the match is replaced with (a regex replacement has its groups filled in)
 */
public record TextHit(String field, String before, String match, String after, String replacement) {

    public TextHit withField(String field) {
        return new TextHit(field, before, match, after, replacement);
    }

}
