package com.github.enerccio.marginalia.extensions.betareaders.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompts of the chats.
 */
public class BetaReadersTemplateData extends TemplateData {

    /** The prompt fragment of the medium of the chat; empty for none. */
    private String medium = "";

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium == null ? "" : medium;
    }
}
