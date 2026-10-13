package com.github.enerccio.marginalia.extensions.charactertracker.model;

public enum RelationDirection {
    /**
     * From the source to the target character only (e.g. "employs").
     */
    SOURCE_TARGET,
    /**
     * Both ways, the characters are equal in it (e.g. "friends").
     */
    UNIDIRECTIONAL
}
