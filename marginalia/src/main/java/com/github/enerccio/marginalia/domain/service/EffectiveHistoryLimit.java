package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;

/**
 * How many versions of a part the history of a book keeps, and the level that sets it: the strictest (smallest) of the limits
 * the book, its folders, the user settings and the {@code VERSION_HISTORY} permission set. See
 * {@link ManuscriptService#resolveHistoryLimit}.
 *
 * @param value  most versions of a part, zero keeps no history, null is no limit
 * @param origin the level of the value; when levels set the same, the nearest to the book
 * @param folder the folder that sets it when the origin is {@link Origin#FOLDER}, null otherwise
 */
public record EffectiveHistoryLimit(Integer value, Origin origin, Folder folder) {

    /**
     * What a user without the {@code VERSION_HISTORY} permission gets: the permission is theirs by default.
     */
    public static final int DEFAULT_MAX_VERSIONS = 50;

    public enum Origin {
        BOOK, FOLDER, USER, PERMISSION,
        /** no level sets a limit */
        NONE
    }

    public boolean isUnlimited() {
        return value == null;
    }

    /**
     * Whether the history keeps nothing at all.
     */
    public boolean isOff() {
        return value != null && value <= 0;
    }
}
