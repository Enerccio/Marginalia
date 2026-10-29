package com.github.enerccio.marginalia.extensions.stconverter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * What the converter keeps of a book or a backup it redrafts from: the parts of the branch the book was on, each with its
 * instructions and its text and nothing else (summaries, lorebooks, other branches, reasoning, images and settings are
 * left out). It is the content of the resource the converter stores, so a long book does not end up in the attributes of
 * the new one.
 */
public class BookSource {

    /** The value of {@link #format}, so a file that is not one is told from it. */
    public static final String FORMAT = "marginalia-redraft-source";

    /** The version of the file, raised when its shape changes. */
    public static final int VERSION = 1;

    /** Where the parts were read from. */
    public static final String ORIGIN_BOOK = "book";
    public static final String ORIGIN_BACKUP = "backup";

    private String format = FORMAT;
    private int version = VERSION;
    /** The name of the book. */
    private String name;
    /** {@link #ORIGIN_BOOK} or {@link #ORIGIN_BACKUP}. */
    private String origin;
    /** When the file was made, ISO-8601. */
    private String created;
    /** The parts in the order of the story, from the first part to the active leaf. */
    private List<Part> parts = new ArrayList<>();

    /** One part of the story: what the author typed into the new turn form and what came of it. */
    public static class Part {

        private String instructions;
        private String sceneSetting;
        private String povCharacter;
        private String presentCharacters;
        /** The text of the part. */
        private String text;

        public Part() {
        }

        public Part(String instructions, String sceneSetting, String povCharacter, String presentCharacters, String text) {
            this.instructions = instructions;
            this.sceneSetting = sceneSetting;
            this.povCharacter = povCharacter;
            this.presentCharacters = presentCharacters;
            this.text = text;
        }

        public String getInstructions() {
            return instructions;
        }

        public String getSceneSetting() {
            return sceneSetting;
        }

        public String getPovCharacter() {
            return povCharacter;
        }

        public String getPresentCharacters() {
            return presentCharacters;
        }

        public String getText() {
            return text;
        }
    }

    public String getFormat() {
        return format;
    }

    public int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public List<Part> getParts() {
        if (parts == null) {
            parts = new ArrayList<>();
        }
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }
}
