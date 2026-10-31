package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.betareaders.model.*;

import java.util.List;

/**
 * What the user does to the history of a chat: edit a message, delete a message (or one message of a turn), take the last reply
 * back, bring a persona into a group and send it away. It changes the chat in memory; the caller stores it. What was sent of the
 * story is worked out from the messages that are left ({@link PartsTracker}), so nothing is kept in step here.
 */
public final class ChatEdits {

    private ChatEdits() {
    }

    /**
     * The message of the chat with the id; null when there is none.
     */
    public static BetaMessage find(BetaChat chat, String id) {
        return chat.getMessages().stream().filter(message -> message.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Changes what the user wrote. The parts the message sent stay as they were sent.
     *
     * @throws IllegalArgumentException when there is no such message of the user, or the text is empty
     */
    public static void edit(BetaChat chat, String id, String text) {
        BetaMessage message = find(chat, id);
        if (message == null || message.getKind() != MessageKind.USER) {
            throw new IllegalArgumentException("There is no message of the user " + id);
        }
        if (text == null || (text.isBlank() && message.getContext().isBlank())) {
            throw new IllegalArgumentException("The message is empty");
        }
        message.setText(text.strip());
    }

    /**
     * Deletes a line of the history, whatever it is.
     *
     * @return whether there was such a message
     */
    public static boolean delete(BetaChat chat, String id) {
        return chat.getMessages().removeIf(message -> message.getId().equals(id));
    }

    /**
     * Deletes one message of a turn; the turn keeps the rest (and is sent back to the model without it). A turn left with nothing is
     * deleted.
     *
     * @param index the place of the message in the turn, from 0
     * @return whether there was such a message
     */
    public static boolean deleteSpoken(BetaChat chat, String id, int index) {
        BetaMessage message = find(chat, id);
        if (message == null || message.getKind() != MessageKind.ASSISTANT || index < 0 || index >= message.getSpoken().size()) {
            return false;
        }
        message.getSpoken().remove(index);
        if (message.getSpoken().isEmpty() && message.getRawText().isBlank()) {
            chat.getMessages().remove(message);
        }
        return true;
    }

    /**
     * Takes the last reply of the model back, to ask again.
     *
     * @return the reply that was dropped; null when the history does not end with a reply
     */
    public static BetaMessage dropLastReply(BetaChat chat) {
        List<BetaMessage> messages = chat.getMessages();
        if (messages.isEmpty() || messages.getLast().getKind() != MessageKind.ASSISTANT) {
            return null;
        }
        return messages.removeLast();
    }

    /**
     * Brings a persona into a group chat: a copy of it joins and a line of the history tells the model. A persona that left comes back.
     *
     * @param note what the user says about it (may be empty)
     * @throws IllegalStateException when the chat is a chat with one persona
     */
    public static void join(BetaChat chat, Persona persona, String note) {
        requireGroup(chat);
        chat.addPersona(persona);
        chat.getMessages().add(BetaMessage.joined(persona, note));
    }

    /**
     * Sends a persona out of a group chat; its copy stays for the messages it said.
     *
     * @throws IllegalStateException when the chat is a chat with one persona, or the persona is not in the conversation, or it is
     *                               the last one
     */
    public static void leave(BetaChat chat, String uuid, String note) {
        requireGroup(chat);
        List<Persona> present = chat.present();
        Persona leaving = present.stream().filter(persona -> persona.getUuid().equals(uuid)).findFirst()
                .orElseThrow(() -> new IllegalStateException("The persona is not in the conversation"));
        if (present.size() == 1) {
            throw new IllegalStateException("The last persona can't leave");
        }
        chat.removePersona(uuid);
        chat.getMessages().add(BetaMessage.left(leaving, note));
    }

    /**
     * Changes the medium of the chat; the history gets a line that says so, because what was said before was said about the old one.
     * Nothing happens when the medium stays the same.
     *
     * @param id           the id of the medium the chat chooses; null or blank for the inherited one
     * @param effectiveName the name of the medium the chat reads the text as after the change (the inherited one when {@code id} is empty)
     * @return whether the medium was changed
     */
    public static boolean changeMedium(BetaChat chat, String id, String effectiveName) {
        String now = id == null || id.isBlank() ? null : id;
        if (java.util.Objects.equals(now, chat.getMedium())) {
            return false;
        }
        chat.setMedium(now);
        if (!chat.getMessages().isEmpty()) {
            BetaMessage line = BetaMessage.user(effectiveName == null ? "" : effectiveName);
            line.setKind(MessageKind.MEDIUM);
            chat.getMessages().add(line);
        }
        return true;
    }

    /**
     * Changes the scene of the chat; the history gets a line that says so, as it does for the medium. Nothing happens when the scene
     * stays the same.
     *
     * @param scene          the scene the chat chooses; null or blank for the inherited one
     * @param effectiveScene the scene the chat has after the change (the inherited one when {@code scene} is empty)
     * @return whether the scene was changed
     */
    public static boolean changeScene(BetaChat chat, String scene, String effectiveScene) {
        String now = scene == null || scene.isBlank() ? null : scene.strip();
        if (java.util.Objects.equals(now, chat.getScene())) {
            return false;
        }
        chat.setScene(now);
        if (!chat.getMessages().isEmpty()) {
            BetaMessage line = BetaMessage.user(effectiveScene == null ? "" : effectiveScene);
            line.setKind(MessageKind.SCENE);
            chat.getMessages().add(line);
        }
        return true;
    }

    /**
     * Takes the current definition of a persona (its description and icon) into the copy the chat keeps. The name stays: the messages
     * the persona said are in the history under it.
     *
     * @return whether the chat has a copy of the persona
     */
    public static boolean refresh(BetaChat chat, Persona source) {
        for (var member : chat.getPersonas()) {
            if (member.getPersona().getUuid().equals(source.getUuid())) {
                member.getPersona().setDescription(source.getDescription());
                member.getPersona().setIcon(source.getIcon());
                return true;
            }
        }
        return false;
    }

    private static void requireGroup(BetaChat chat) {
        if (chat.getKind() != ChatKind.GROUP) {
            throw new IllegalStateException("Personas join and leave a group chat only");
        }
    }
}
