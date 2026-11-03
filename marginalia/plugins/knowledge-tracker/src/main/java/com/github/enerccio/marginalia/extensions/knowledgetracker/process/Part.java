package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

/**
 * What the processor needs of a part of the story: who it is, whose point of view it is told from, who is present in it
 * and its text.
 *
 * @param uuid              the uuid of the part
 * @param povCharacter      the point of view field, may be {@code null}
 * @param presentCharacters the present characters field, may be {@code null}
 * @param text              the text of the part, may be {@code null}
 */
public record Part(String uuid, String povCharacter, String presentCharacters, String text) {

    public Part(String uuid, String presentCharacters, String text) {
        this(uuid, null, presentCharacters, text);
    }
}
