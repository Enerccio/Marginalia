package com.github.enerccio.marginalia.extensions.betareaders.model;

import java.util.UUID;

/**
 * A beta reader the user talks to: a name, an icon and a description of how the persona reads and what it cares about. The
 * personas live in the library of the user ({@link BetaReadersSettings#getPersonas}) and in the book ({@link BookData#getPersonas});
 * a chat keeps a copy of the ones it uses.
 */
public class Persona {

    private String uuid = UUID.randomUUID().toString();
    private String name = "";
    /**
     * The icon as base64 of a PNG, at most {@code PersonaIcons.MAX_ICON_SIZE} pixels wide and high; null for none (a circle with the
     * first letters of the name is shown then).
     */
    private String icon;
    private String description = "";

    public Persona() {
    }

    public Persona(String name, String description) {
        this.name = name;
        this.description = description;
    }

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * An independent copy with the same uuid (the copy a chat keeps of a persona).
     */
    public Persona copy() {
        Persona copy = new Persona(name, description);
        copy.uuid = uuid;
        copy.icon = icon;
        return copy;
    }

    /**
     * The same persona with another uuid (a persona copied in the library).
     */
    public Persona duplicate() {
        Persona duplicate = copy();
        duplicate.uuid = UUID.randomUUID().toString();
        return duplicate;
    }
}
