package com.github.enerccio.marginalia.domain.components;

import java.util.List;

/**
 * Who wrote the words of a branch of a book: the sum of the {@link PartAuthorship} of its parts.
 *
 * @param parts                  how many parts have a text
 * @param partsWithGeneratedText how many of them have words a model wrote
 * @param generatedWords         how many words a model wrote
 * @param writtenWords           how many words a person wrote
 * @param models                 the models that generated the text of the parts that have some, sorted, without repeats
 */
public record BookAuthorship(int parts, int partsWithGeneratedText, long generatedWords, long writtenWords, List<String> models) {

    public BookAuthorship {
        models = models == null ? List.of() : List.copyOf(models);
    }

    public long totalWords() {
        return generatedWords + writtenWords;
    }

    /**
     * @return the share of the words a model wrote, 0 to 1; 0 for a text without words
     */
    public double generatedShare() {
        return totalWords() == 0 ? 0 : (double) generatedWords / totalWords();
    }

    /**
     * @return the share of the words a person wrote, 0 to 1; 0 for a text without words
     */
    public double writtenShare() {
        return totalWords() == 0 ? 0 : (double) writtenWords / totalWords();
    }
}
