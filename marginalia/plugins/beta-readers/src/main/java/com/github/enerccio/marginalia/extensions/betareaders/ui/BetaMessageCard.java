package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * A line of the history of a chat as the user sees it: what they wrote (and which parts of the story it sent), a persona that joined
 * or left, a change of the medium, or a turn of the model (its reasoning, collapsed, and the messages of the personas and the
 * narrator). With {@link Actions} the lines have the buttons to edit and delete them, and the last turn the button to ask again.
 */
public final class BetaMessageCard {

    private BetaMessageCard() {
    }

    /**
     * What the buttons of the cards do.
     */
    public interface Actions {

        /** Edits what the user wrote. */
        void edit(BetaMessage message);

        /** Deletes a line of the history. */
        void delete(BetaMessage message);

        /** Deletes one message of a turn. */
        void deleteSpoken(BetaMessage turn, int index);

        /** Takes the last turn back and asks again. */
        void regenerate();
    }

    /**
     * The card of a line of the history, without buttons.
     */
    public static Component create(BetaChat chat, BetaMessage message) {
        return create(chat, message, null, false);
    }

    /**
     * @param actions what the buttons do; null for a card without buttons
     * @param last    whether this is the last line of the history (the last turn can be asked for again)
     */
    public static Component create(BetaChat chat, BetaMessage message, Actions actions, boolean last) {
        return switch (message.getKind()) {
            case USER -> user(message, actions);
            case JOIN -> note(T.localized("%s joined the conversation.").formatted(name(message)), message, actions);
            case LEAVE -> note(T.localized("%s left the conversation.").formatted(name(message)), message, actions);
            case MEDIUM -> note(T.localized("From now on the text is read as: %s").formatted(message.getText()), null, actions, message);
            case SCENE -> note(T.localized("The scene is now: %s").formatted(message.getText()), null, actions, message);
            case ASSISTANT -> {
                Turn turn = new Turn(chat);
                turn.show(message.getReasoning(), message.getSpoken(), message.getRawText(), message, actions, last);
                yield turn;
            }
        };
    }

    private static String name(BetaMessage message) {
        return message.getPersonaName() == null ? "" : message.getPersonaName();
    }

    private static Button button(com.vaadin.flow.component.icon.Icon icon, String tooltip, Runnable action) {
        Button button = new Button(icon, _ -> action.run());
        button.addThemeName("tertiary");
        button.addThemeName("small");
        button.setTooltipText(tooltip);
        button.setAriaLabel(tooltip);
        return button;
    }

    private static Div actionsRow(Component... buttons) {
        Div row = new Div(buttons);
        row.addClassName(BetaReadersStyles.ACTIONS);
        return row;
    }

    private static Component user(BetaMessage message, Actions actions) {
        Div card = new Div();
        card.addClassName(BetaReadersStyles.USER);
        Span who = new Span(T.localized("You"));
        who.addClassName(CssStyles.TEXT_BOLD);
        Div headline = new Div(who);
        headline.addClassName(BetaReadersStyles.HEADLINE);
        if (actions != null) {
            headline.add(actionsRow(
                    button(Solid.PEN.create(), T.localized("Edit message"), () -> actions.edit(message)),
                    button(Solid.TRASH.create(), T.localized("Delete message"), () -> actions.delete(message))));
        }
        card.add(headline);
        String sent = sentParts(message.getSentParts());
        if (!sent.isEmpty()) {
            Div hint = new Div(T.localized("Parts %s sent").formatted(sent));
            hint.addClassName(BetaReadersStyles.HINT);
            card.add(hint);
        }
        if (!message.getText().isBlank()) {
            Div text = new Div(message.getText());
            text.addClassName(BetaReadersStyles.TEXT);
            card.add(text);
        }
        return card;
    }

    /**
     * The numbers of the parts a message sent as ranges ("41-43, 50"); empty when it sent none (or the numbers are not known).
     */
    static String sentParts(List<SentPart> parts) {
        return PartsTracker.ranges(parts.stream().map(SentPart::getNumber).filter(number -> number > 0).toList());
    }

    private static Component note(String text, BetaMessage message, Actions actions) {
        return note(text, message.getText(), actions, message);
    }

    private static Component note(String text, String user, Actions actions, BetaMessage message) {
        Div line = new Div(new Span(text));
        line.addClassName(BetaReadersStyles.HEADLINE);
        if (actions != null) {
            line.add(actionsRow(button(Solid.TRASH.create(), T.localized("Delete message"), () -> actions.delete(message))));
        }
        Div note = new Div(line);
        if (user != null && !user.isBlank()) {
            note.add(new Div(user.strip()));
        }
        note.addClassName(BetaReadersStyles.NOTE);
        return note;
    }

    /**
     * A turn of the model. It is rebuilt every time the reply grows while it streams, so the same component serves the live turn and
     * the turns of the history.
     */
    public static final class Turn extends Div {

        private final BetaChat chat;

        public Turn(BetaChat chat) {
            this.chat = chat;
            addClassName(BetaReadersStyles.TURN);
        }

        /**
         * Shows a turn that is being written: no buttons.
         */
        public void show(String reasoning, List<BetaSpoken> messages, String rawText) {
            show(reasoning, messages, rawText, null, null, false);
        }

        /**
         * Shows the turn: the reasoning in a closed section above the messages, then the messages; a reply that could not be read as
         * messages is shown as it was written.
         *
         * @param owner   the line of the history the turn is (for the buttons); may be null
         * @param actions what the buttons do; null for none
         * @param last    the turn can be asked for again
         */
        public void show(String reasoning, List<BetaSpoken> messages, String rawText, BetaMessage owner, Actions actions, boolean last) {
            removeAll();
            if (reasoning != null && !reasoning.isBlank()) {
                Div text = new Div(reasoning.strip());
                text.addClassNames(BetaReadersStyles.TEXT, BetaReadersStyles.HINT);
                add(new Details(T.localized("Reasoning"), text));
            }
            for (int i = 0; i < messages.size(); i++) {
                add(spoken(messages.get(i), i, owner, actions));
            }
            if (messages.isEmpty() && rawText != null && !rawText.isBlank()) {
                Div text = new Div(rawText.strip());
                text.addClassName(BetaReadersStyles.TEXT);
                add(text);
            }
            if (actions != null && owner != null) {
                Div row = actionsRow();
                if (messages.isEmpty()) {
                    row.add(button(Solid.TRASH.create(), T.localized("Delete message"), () -> actions.delete(owner)));
                }
                if (last) {
                    Button regenerate = new Button(T.localized("Regenerate"), Solid.ROTATE.create(), _ -> actions.regenerate());
                    regenerate.addThemeName("tertiary");
                    regenerate.addThemeName("small");
                    row.add(regenerate);
                }
                if (row.getChildren().findAny().isPresent()) {
                    add(row);
                }
            }
        }

        private Component spoken(BetaSpoken spoken, int index, BetaMessage owner, Actions actions) {
            Div row = new Div();
            row.addClassName(BetaReadersStyles.SPOKEN);
            if (spoken.isNarrator()) {
                Div space = new Div();
                space.addClassName(BetaReadersStyles.NO_ICON);
                row.add(space);
            } else {
                Persona persona = chat.findPersona(spoken.getSpeaker());
                if (persona == null) {
                    // a name the chat does not know: a circle with its letters, in a color that follows the name
                    persona = new Persona(spoken.getSpeaker(), "");
                    persona.setUuid(UUID.nameUUIDFromBytes(spoken.getSpeaker().getBytes(StandardCharsets.UTF_8)).toString());
                }
                Image icon = new Image(PersonaIcons.source(persona), persona.getName());
                icon.addClassName(BetaReadersStyles.ICON);
                row.add(icon);
            }

            Div body = new Div();
            Span name = new Span(spoken.isNarrator() ? T.localized("Narrator") : spoken.getSpeaker());
            name.addClassName(CssStyles.TEXT_BOLD);
            Div parts = new Div();
            parts.addClassName(BetaReadersStyles.TEXT);
            for (BetaPart part : spoken.getParts()) {
                Span span = new Span(part.getText());
                if (part.getType() == PartType.ACTION) {
                    span.addClassName(BetaReadersStyles.ACTION);
                }
                if (parts.getChildren().findAny().isPresent()) {
                    parts.add(new Span(" "));
                }
                parts.add(span);
            }
            body.add(name, parts);
            row.add(body);
            if (actions != null && owner != null) {
                row.add(actionsRow(button(Solid.TRASH.create(), T.localized("Delete message"), () -> actions.deleteSpoken(owner, index))));
            }
            return row;
        }
    }
}
