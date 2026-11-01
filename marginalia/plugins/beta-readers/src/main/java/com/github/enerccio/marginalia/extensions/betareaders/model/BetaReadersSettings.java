package com.github.enerccio.marginalia.extensions.betareaders.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings of the user, stored in the attributes of the user settings under {@link #KEY}: the user level of the settings that
 * books and folders inherit (the prompts, the medium, the AI and the protocol), the library of personas and the library of mediums.
 * A blank prompt, medium, AI or protocol is not set: the next level (the built-in default) applies.
 */
public class BetaReadersSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.betareaders";

    /** The prompt of a 1v1 chat, blank for the built-in one. */
    private String singlePrompt = "";
    /** The prompt of a group chat, blank for the built-in one. */
    private String groupPrompt = "";
    /** The scene of the chats, blank for the built-in one (an online chat). */
    private String scene = "";
    /** The id of the medium the chats use when nothing else chooses one, blank for the built-in default. */
    private String medium = "";
    /** The uuid of the AI of the chats, blank for the AI of the book. */
    private String ai = "";
    /** The uuid of the protocol of the chats, blank for the protocol of the book. */
    private String protocol = "";
    /** The most messages the model may write in one turn of a chat that sets no limit itself; 0 for the built-in one (it follows the kind and the size of the group). */
    private int maxMessages;
    private List<Persona> personas = new ArrayList<>();
    private List<Medium> mediums = new ArrayList<>();

    public String getSinglePrompt() {
        return singlePrompt;
    }

    public void setSinglePrompt(String singlePrompt) {
        this.singlePrompt = singlePrompt;
    }

    public String getGroupPrompt() {
        return groupPrompt;
    }

    public void setGroupPrompt(String groupPrompt) {
        this.groupPrompt = groupPrompt;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getAi() {
        return ai;
    }

    public void setAi(String ai) {
        this.ai = ai;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = Math.max(0, maxMessages);
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(List<Persona> personas) {
        this.personas = personas;
    }

    public List<Medium> getMediums() {
        return mediums;
    }

    public void setMediums(List<Medium> mediums) {
        this.mediums = mediums;
    }

    /**
     * Replaces what a stored object may lack (a list that was never written) so the rest of the code can rely on it.
     */
    public BetaReadersSettings normalize() {
        singlePrompt = singlePrompt == null ? "" : singlePrompt;
        groupPrompt = groupPrompt == null ? "" : groupPrompt;
        scene = scene == null ? "" : scene;
        medium = medium == null ? "" : medium;
        ai = ai == null ? "" : ai;
        protocol = protocol == null ? "" : protocol;
        maxMessages = Math.max(0, maxMessages);
        personas = personas == null ? new ArrayList<>() : personas;
        mediums = mediums == null ? new ArrayList<>() : mediums;
        return this;
    }
}
