package com.github.enerccio.marginalia.extensions.arcoutline.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The conversations of the plot wizard, what is stored in the attributes of the book under {@link #KEY}.
 */
public class WizardData {

    public static final String KEY = ArcOutlineData.KEY + ".wizard";

    private List<WizardChat> chats = new ArrayList<>();
    /** The uuid of the chat the dialog opens with. */
    private String active;

    public List<WizardChat> getChats() {
        if (chats == null) {
            chats = new ArrayList<>();
        }
        return chats;
    }

    public void setChats(List<WizardChat> chats) {
        this.chats = chats;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    /** The chat with the uuid, null when there is none. */
    public WizardChat find(String uuid) {
        return getChats().stream().filter(chat -> chat.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /** The chat the dialog opens with: the active one, else the first; null when there are no chats. */
    public WizardChat activeChat() {
        WizardChat chat = StringUtils.isNotBlank(active) ? find(active) : null;
        return chat != null ? chat : getChats().isEmpty() ? null : getChats().getFirst();
    }
}
