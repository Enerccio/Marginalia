package com.github.enerccio.marginalia.extensions.arcoutline.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One conversation of the plot wizard. A branch is a copy of a conversation (all of it, or up to a message) that has
 * nothing to do with the original afterwards.
 */
public class WizardChat {

    private String uuid = UUID.randomUUID().toString();
    private String name;
    private List<WizardMessage> messages = new ArrayList<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** The name the author gave, null for none (the first message names the chat then). */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WizardMessage> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(List<WizardMessage> messages) {
        this.messages = messages;
    }

    /**
     * A new chat with copies of the first {@code count} messages.
     *
     * @param count how many messages are copied, all of them when it is larger than the chat
     */
    public WizardChat branch(String name, int count) {
        WizardChat copy = new WizardChat();
        copy.name = name;
        for (WizardMessage message : getMessages().subList(0, Math.max(0, Math.min(count, getMessages().size())))) {
            copy.messages.add(message.copy());
        }
        return copy;
    }
}
