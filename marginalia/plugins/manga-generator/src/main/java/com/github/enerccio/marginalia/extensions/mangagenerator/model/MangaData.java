package com.github.enerccio.marginalia.extensions.mangagenerator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the plugin keeps with a book (in its attributes, so it travels with backups and copies).
 */
public class MangaData {

    private List<Manga> mangas = new ArrayList<>();
    private int activeManga = -1;

    public List<Manga> getMangas() {
        if (mangas == null) {
            mangas = new ArrayList<>();
        }
        return mangas;
    }

    public int getActiveManga() {
        return activeManga;
    }

    public void setActiveManga(int activeManga) {
        this.activeManga = activeManga;
    }

    /**
     * The manga that is open, null when the book has none.
     */
    public Manga getCurrent() {
        return activeManga >= 0 && activeManga < getMangas().size() ? getMangas().get(activeManga) : null;
    }
}
