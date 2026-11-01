package com.github.enerccio.marginalia.extensions.betareaders.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompts of the chats.
 */
public class BetaReadersTemplateData extends TemplateData {

    /** The prompt fragment of the medium of the chat; empty for none. */
    private String medium = "";

    /** The scene of the chat; empty for none. */
    private String scene = "";

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene == null ? "" : scene;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium == null ? "" : medium;
    }
}
