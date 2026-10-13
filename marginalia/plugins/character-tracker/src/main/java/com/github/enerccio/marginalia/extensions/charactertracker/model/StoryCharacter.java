package com.github.enerccio.marginalia.extensions.charactertracker.model;

import java.util.UUID;

/**
 * A character of the book. Named {@code StoryCharacter} so it doesn't hide {@link java.lang.Character} in this
 * package.
 */
public class StoryCharacter {

    /**
     * Color of a character that has none (or an invalid one).
     */
    public static final String DEFAULT_COLOR = "#e0e0e0";

    private String uuid = UUID.randomUUID().toString();
    private String name = "";
    private String tagline = "";
    private String note = "";
    /**
     * The icon as base64 of a PNG, at most {@code CharacterTrackerService.MAX_ICON_SIZE} pixels wide and high;
     * {@code null} when the character has none.
     */
    private String icon;
    /**
     * Background color of the character, {@code #rrggbb}.
     */
    private String color = DEFAULT_COLOR;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
