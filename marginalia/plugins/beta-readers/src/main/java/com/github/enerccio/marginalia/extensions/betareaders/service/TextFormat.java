package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaPart;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaSpoken;
import com.github.enerccio.marginalia.extensions.betareaders.model.PartType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The free text form of messages: a paragraph per message, {@code **Name:**} and then the parts in order, an action between
 * asterisks and speech as it is. It is what the model is told to write when it does not answer in JSON, and how the replies it
 * wrote are sent back to it.
 */
public final class TextFormat {

    private TextFormat() {
    }

    /**
     * The messages of a turn as text, a blank line between the messages.
     */
    public static String render(List<BetaSpoken> messages) {
        return messages.stream().map(TextFormat::render).collect(Collectors.joining("\n\n"));
    }

    /**
     * One message as a paragraph.
     */
    public static String render(BetaSpoken message) {
        return "**" + message.getSpeaker() + ":** " + message.getParts().stream()
                .map(TextFormat::render).collect(Collectors.joining(" "));
    }

    private static String render(BetaPart part) {
        return part.getType() == PartType.ACTION ? "*" + part.getText().strip() + "*" : part.getText().strip();
    }
}
