package com.github.enerccio.marginalia.extensions.audiobook.studio;

/** Where the script of a part is kept (the application keeps it in the attributes of the part; the tests use a map). */
public interface StudioStore {

    /** @return what is stored for the part, null for nothing (or something that cannot be read) */
    StudioScript find(String partId);

    void put(String partId, StudioScript script);
}
