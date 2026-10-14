package com.github.enerccio.marginalia.extensions.arcoutline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * What is stored in the attributes of the book under {@link #KEY}.
 */
public class ArcOutlineData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.arcoutline";

    private List<Arc> arcs = new ArrayList<>();

    public List<Arc> getArcs() {
        return arcs;
    }

    public void setArcs(List<Arc> arcs) {
        this.arcs = arcs;
    }
}
