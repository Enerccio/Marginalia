package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;

/**
 * The value a setting of a book has, and where it comes from: the book itself, a folder above it, the user settings or
 * the built-in default. See {@link ManuscriptService#resolve}.
 *
 * @param value  the value, never blank for a text
 * @param origin where the value is set
 * @param folder the folder that sets it when the origin is {@link Origin#FOLDER}, null otherwise
 */
public record EffectiveSetting<T>(T value, Origin origin, Folder folder) {

    public enum Origin {
        BOOK, FOLDER, USER, DEFAULT
    }

    /**
     * Whether the book or a folder above it sets the value, not the user settings or the default.
     */
    public boolean isCustom() {
        return origin == Origin.BOOK || origin == Origin.FOLDER;
    }
}
