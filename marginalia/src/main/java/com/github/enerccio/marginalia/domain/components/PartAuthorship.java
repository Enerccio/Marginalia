package com.github.enerccio.marginalia.domain.components;

import java.util.List;

/**
 * Who wrote the words of the text of a part, worked out from the versions of its history, see
 * {@code MessageRevisionService#authorship(ChatMessage)}.
 *
 * @param generatedRanges the runs of the text a model wrote, [start, end) offsets in the text, in order
 * @param generatedWords  how many words a model wrote
 * @param writtenWords    how many words a person wrote
 */
public record PartAuthorship(String text, List<int[]> generatedRanges, int generatedWords, int writtenWords) {

    public int totalWords() {
        return generatedWords + writtenWords;
    }
}
