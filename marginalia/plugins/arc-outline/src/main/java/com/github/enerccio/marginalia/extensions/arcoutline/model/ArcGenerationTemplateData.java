package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompt for generating an arc.
 */
public class ArcGenerationTemplateData extends TemplateData {

    /** The entries of the book's lorebook (and its sub lorebooks). */
    private String backgroundLore;
    /** The arcs the book has, with their chapters. */
    private String existingArcs;
    /** What the author wants from the new arc. */
    private String specification;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getExistingArcs() {
        return existingArcs;
    }

    public void setExistingArcs(String existingArcs) {
        this.existingArcs = existingArcs;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }
}
