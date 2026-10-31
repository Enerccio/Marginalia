package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * A persona of a chat: the copy the chat made when the persona was added (so editing or deleting the persona in the library or the
 * book changes nothing here) and whether it is in the conversation now. A persona that left stays, because its messages are still
 * in the history.
 */
public class ChatPersona {

    private Persona persona = new Persona();
    private boolean present = true;

    public ChatPersona() {
    }

    public ChatPersona(Persona persona, boolean present) {
        this.persona = persona;
        this.present = present;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
