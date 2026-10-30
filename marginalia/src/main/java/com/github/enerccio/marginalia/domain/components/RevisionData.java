package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * What a new version of a part is made of, for the writer that has more to say than the part does: a redraft has a
 * reasoning of its own while the part keeps the old one, a text that is not stored in the part (yet) has no counts there.
 * <p>
 * The reasoning is stored as given, whatever the source: the caller knows whether the text came with one. {@code null}
 * counts are worked out by the service (the words are counted, the tokens are the ones of the part when the text is the one
 * of the part, else 0 - unknown).
 *
 * @param source    who wrote the text
 * @param detail    a short word about the source, see {@code MessageRevision#getSourceDetail()}, may be null
 * @param agent     whether an agent did it
 * @param text      the text of the version
 * @param reasoning the reasoning that came with the text, null for none
 * @param label     the name of a version saved by hand, null for none
 * @param wordCount null to count the words of the text
 * @param tokenCount null to take the tokens of the part (or 0 when its text is another one)
 */
public record RevisionData(RevisionSource source, String detail, boolean agent, String text, String reasoning, String label,
                           Integer wordCount, Long tokenCount) {

    /**
     * The text of the part as it is now, with its counts; the reasoning of the part only for a {@link RevisionSource#isModel()
     * model} source (a text a person or a program wrote does not use the reasoning the part has from an earlier generation).
     */
    public static RevisionData of(ChatMessage part, RevisionSource source, String detail, boolean agent) {
        RevisionSource kind = source == null ? RevisionSource.HUMAN : source;
        return new RevisionData(kind, detail, agent, part.getResponse(),
                kind.isModel() ? StringUtils.defaultIfEmpty(part.getResponseReasoning(), null) : null, null,
                part.getWordCount(), part.getTokenCount());
    }

    /**
     * The same data with another reasoning.
     */
    public RevisionData withReasoning(String reasoning) {
        return new RevisionData(source, detail, agent, text, reasoning, label, wordCount, tokenCount);
    }

    /**
     * The same data with another text and counts worked out again.
     */
    public RevisionData withText(String text) {
        return new RevisionData(source, detail, agent, text, reasoning, label, null, null);
    }
}
