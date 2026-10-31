package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaPart;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaSpoken;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.model.PartType;
import com.github.enerccio.marginalia.extensions.betareaders.model.SentPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.vaadin.flow.component.Component;
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
 * or left, or a turn of the model (its reasoning, collapsed, and the messages of the personas and the narrator).
 */
public final class BetaMessageCard {

    private BetaMessageCard() {
    }

    /**
     * The card of a line of the history.
     */
    public static Component create(BetaChat chat, BetaMessage message) {
        return switch (message.getKind()) {
            case USER -> user(message);
            case JOIN -> note(T.localized("%s joined the conversation.").formatted(name(message)), message.getText());
            case LEAVE -> note(T.localized("%s left the conversation.").formatted(name(message)), message.getText());
            case ASSISTANT -> {
                Turn turn = new Turn(chat);
                turn.show(message.getReasoning(), message.getSpoken(), message.getRawText());
                yield turn;
            }
        };
    }

    private static String name(BetaMessage message) {
        return message.getPersonaName() == null ? "" : message.getPersonaName();
    }

    private static Component user(BetaMessage message) {
        Div card = new Div();
        card.addClassName(BetaReadersStyles.USER);
        Span who = new Span(T.localized("You"));
        who.addClassName(CssStyles.TEXT_BOLD);
        card.add(who);
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

    private static Component note(String text, String user) {
        Div note = new Div(new Span(text));
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
         * Shows the turn: the reasoning in a closed section above the messages, then the messages; a reply that could not be read as
         * messages is shown as it was written.
         */
        public void show(String reasoning, List<BetaSpoken> messages, String rawText) {
            removeAll();
            if (reasoning != null && !reasoning.isBlank()) {
                Div text = new Div(reasoning.strip());
                text.addClassNames(BetaReadersStyles.TEXT, BetaReadersStyles.HINT);
                add(new Details(T.localized("Reasoning"), text));
            }
            for (BetaSpoken spoken : messages) {
                add(spoken(spoken));
            }
            if (messages.isEmpty() && rawText != null && !rawText.isBlank()) {
                Div text = new Div(rawText.strip());
                text.addClassName(BetaReadersStyles.TEXT);
                add(text);
            }
        }

        private Component spoken(BetaSpoken spoken) {
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
            return row;
        }
    }
}
