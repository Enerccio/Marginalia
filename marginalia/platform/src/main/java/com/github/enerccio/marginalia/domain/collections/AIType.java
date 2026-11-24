package com.github.enerccio.marginalia.domain.collections;

public enum AIType {

    OPEN_AI_COMPATIBLE,

    /** An AI whose provider comes from an extension, see {@code ExtensionAI}. Stored as an ordinal: keep it last. */
    EXTENSION,

}
