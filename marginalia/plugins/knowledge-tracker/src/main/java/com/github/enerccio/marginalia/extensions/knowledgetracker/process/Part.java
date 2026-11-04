package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

/**
 * What the processor needs of a part of the story: who it is, whose point of view it is told from, who is present in it
 * and its text.
 *
 * @param uuid              the uuid of the part
 * @param povCharacter      the point of view field, may be {@code null}
 * @param presentCharacters the present characters field, may be {@code null}
 * @param text              the text of the part, may be {@code null}
 * @param id                the database id of the part, which the story needs to scroll to it; {@code null} when not known
 */
public record Part(String uuid, String povCharacter, String presentCharacters, String text, Long id) {

    public Part(String uuid, String povCharacter, String presentCharacters, String text) {
        this(uuid, povCharacter, presentCharacters, text, null);
    }

    public Part(String uuid, String presentCharacters, String text) {
        this(uuid, null, presentCharacters, text, null);
    }
}
