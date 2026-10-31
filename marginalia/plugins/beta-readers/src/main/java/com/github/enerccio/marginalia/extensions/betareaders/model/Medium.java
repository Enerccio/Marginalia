package com.github.enerccio.marginalia.extensions.betareaders.model;

import java.util.UUID;

/**
 * What the text stands for: a book, a movie, a manga... The {@link #prompt} is a fragment of the prompts of the chats
 * ({@code {{medium}}}) that tells the personas to read the text as the source of that medium and to react as its audience.
 */
public class Medium {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String prompt = "";

    public Medium() {
    }

    public Medium(String id, String name, String prompt) {
        this.id = id;
        this.name = name;
        this.prompt = prompt;
    }

    /**
     * What the medium is chosen by (the value of the inherited setting): a fixed text for the built-in ones, a uuid for the ones
     * the user made.
     */
    public String getId() {
        return id;
    }

    /**
     * An independent copy with the same id (what an editor works on until it is saved).
     */
    public Medium copy() {
        return new Medium(id, name, prompt);
    }

    /**
     * The same medium with an id of its own (a medium copied in the library).
     */
    public Medium duplicate() {
        return new Medium(UUID.randomUUID().toString(), name, prompt);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
